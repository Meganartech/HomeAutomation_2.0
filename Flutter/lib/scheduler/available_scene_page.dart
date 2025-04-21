import 'dart:convert';
import 'dart:io';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/add_devices_and_delete/no_device_page.dart';
import 'package:home_auto_sample/scheduler/Scene_deleted.dart';
import 'package:home_auto_sample/scheduler/scheduler_page.dart';
import 'package:home_auto_sample/scheduler/setup_Routine_page.dart';
import 'package:home_auto_sample/service/database_service.dart';
import 'package:home_auto_sample/settings/settings_1_page.dart';
import 'package:home_auto_sample/tabBar/custom_appbar.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:workmanager/workmanager.dart';


class AvailableScenesPage extends StatefulWidget {
  const AvailableScenesPage({super.key});

  @override
  _AvailableScenesPageState createState() => _AvailableScenesPageState();
}

class _AvailableScenesPageState extends State<AvailableScenesPage> {
  int currentIndex = 1;
  List<Device> _devices = [];
  List<String> scenes = []; 
  late DatabaseReference dbRef;
  bool isLoading = true;
  String? userId;

  @override
  void initState() {
    super.initState();
    _fetchDevices();
    _initializeUser();
  }

  Future<void> _initializeUser() async {
  User? user = FirebaseAuth.instance.currentUser;
  if (user != null) {
    try {
      userId = user.uid;
      dbRef = FirebaseDatabase.instance.ref("users/$userId/scenes");
      _listenToScenes();
    } catch (e) {
      debugPrint("Scheduler initialization error: $e");
    }
  }
}
  
Future<void> _listenToScenes() async {
  dbRef.onValue.listen((event) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final String? sharedPrefsData = prefs.getString('EspDevice');
      final Map<String, dynamic> currentPrefs = sharedPrefsData != null 
          ? jsonDecode(sharedPrefsData) as Map<String, dynamic>
          : {};

      final String userId = FirebaseAuth.instance.currentUser?.uid ?? '';

      if (userId.isEmpty) return;

      if (event.snapshot.exists && event.snapshot.value != null) {
        Map<dynamic, dynamic> firebaseScenes = event.snapshot.value as Map<dynamic, dynamic>;
        List<String> loadedScenes = firebaseScenes.keys.map((sceneName) => sceneName.toString()).toList();
        
        final autoOnOffRef = FirebaseDatabase.instance.ref("users/$userId/AutomaticOnOff");
        final autoOnOffSnapshot = await autoOnOffRef.get();
        final dynamic firebaseAutoOnOff = autoOnOffSnapshot.value;

        List<String> savedScenes = prefs.getStringList("savedScenes") ?? [];
        savedScenes = savedScenes.where((scene) => firebaseScenes.containsKey(scene)).toList();

        currentPrefs.putIfAbsent(userId, () => {});

        currentPrefs[userId]['scenes'] = firebaseScenes;
        currentPrefs[userId]['AutomaticOnOff'] = firebaseAutoOnOff ?? {};

        await prefs.setString('EspDevice', jsonEncode(currentPrefs));

        if (mounted) {
          setState(() {
            scenes = loadedScenes;
            isLoading = false;
          });
        }
      } else {
        // Firebase has no scenes - clear these specific fields in SharedPreferences
        if (currentPrefs.containsKey(userId)) {
          currentPrefs[userId].remove('scenes');
          currentPrefs[userId].remove('AutomaticOnOff');
          await prefs.setString('EspDevice', jsonEncode(currentPrefs));
        }

        if (mounted) {
          setState(() {
            scenes = [];
            isLoading = false;
          });
        }
      }
    } catch (e, stack) {
      debugPrint("Error syncing scenes: $e\n$stack");
      if (mounted) {
        setState(() => isLoading = false);
      }
    }
  });
}

  Future<void> _removeScene(String sceneName) async {
  if (userId == null) return;

  try {
    // 1. Get the scene data
    final sceneRef = FirebaseDatabase.instance.ref("users/$userId/scenes/$sceneName");
    final sceneSnapshot = await sceneRef.get();
    
    if (!sceneSnapshot.exists) return;

    final sceneData = sceneSnapshot.value as Map<dynamic, dynamic>;
    final devicePortPairs = <String>{};

    // 2. Collect device-port pairs from the nested structure
    sceneData.forEach((deviceId, deviceData) {
      if (deviceData is Map && deviceData['ports'] is Map) {
        final ports = deviceData['ports'] as Map<dynamic, dynamic>;
        ports.forEach((portKey, _) {
          devicePortPairs.add('$deviceId:$portKey');
          debugPrint("Collected device-port pair: $deviceId:$portKey");
        });
      }
    });

    // 3. COMPREHENSIVE SCHEDULER CANCELLATION
    try {
      // Workmanager doesn't provide direct access to pending work requests
      // So we'll cancel by known tag patterns
      
      // Cancel all device-port specific tasks
      for (final pair in devicePortPairs) {
        // Cancel ON tasks
        await Workmanager().cancelByTag('${pair}_ON');
        // Cancel OFF tasks
        await Workmanager().cancelByTag('${pair}_OFF');
        debugPrint("Cancelled tasks for $pair");
      }
      
      // Cancel scene-level tasks
      await Workmanager().cancelByTag('scene_$sceneName');
      debugPrint("Cancelled scene-level tasks");

      // As a last resort, cancel ALL work if needed
      // await Workmanager().cancelAll();
    } catch (e) {
      debugPrint("Error cancelling scheduled tasks: $e");
    }

    // 4. Remove from Firebase AutomaticOnOff
    final autoOnOffRef = FirebaseDatabase.instance.ref("users/$userId/AutomaticOnOff");
    final autoOnOffSnapshot = await autoOnOffRef.get();
    
    if (autoOnOffSnapshot.exists) {
      final autoOnOffData = autoOnOffSnapshot.value as Map<dynamic, dynamic>;
      final deletionFutures = <Future>[];
      
      autoOnOffData.forEach((deviceId, deviceData) {
        if (deviceData is Map) {
          deviceData.forEach((portKey, _) {
            if (devicePortPairs.contains('$deviceId:$portKey')) {
              deletionFutures.add(
                FirebaseDatabase.instance
                  .ref("users/$userId/AutomaticOnOff/$deviceId/$portKey")
                  .remove()
              );
            }
          });
        }
      });
      
      if (deletionFutures.isNotEmpty) {
        await Future.wait(deletionFutures);
      }
    }

    // 5. Remove from SharedPreferences
    final prefs = await SharedPreferences.getInstance();
    final String? sharedPrefsData = prefs.getString('EspDevice');
    
    if (sharedPrefsData != null) {
      final data = jsonDecode(sharedPrefsData) as Map<String, dynamic>;
      bool modified = false;
      
      if (data.containsKey(userId) && data[userId] is Map) {
        final userData = data[userId] as Map<String, dynamic>;
        
        // Remove from AutomaticOnOff
        if (userData['AutomaticOnOff'] is Map) {
          final autoOnOff = Map<String, dynamic>.from(userData['AutomaticOnOff'] as Map);
          autoOnOff.removeWhere((deviceId, deviceData) {
            if (deviceData is! Map) return false;
            deviceData.removeWhere((portKey, _) => 
              devicePortPairs.contains('$deviceId:$portKey'));
            return deviceData.isEmpty;
          });
          userData['AutomaticOnOff'] = autoOnOff;
          modified = true;
        }

        // Remove scene
        if (userData['scenes'] is Map) {
          (userData['scenes'] as Map).remove(sceneName);
          modified = true;
        }
        
        if (modified) {
          await prefs.setString('EspDevice', jsonEncode(data));
        }
      }
    }

    // 6. Remove from savedScenes list
    final savedScenes = prefs.getStringList("savedScenes") ?? [];
    if (savedScenes.remove(sceneName)) {
      await prefs.setStringList("savedScenes", savedScenes);
    }

    // 7. Finally delete the scene itself
    await sceneRef.remove();

    // 8. Update UI
    if (mounted) {
      setState(() => scenes.remove(sceneName));
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => SceneDeletePage()),
      );
    }
  } catch (error, stackTrace) {
    debugPrint("Error deleting scene: $error\n$stackTrace");
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Failed to delete scene: ${error.toString()}')),
      );
    }
  }
}
  
  void _confirmDelete(String sceneName) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Delete Scene"),
        content: Text("Are you sure you want to delete the scene '$sceneName'?"),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text("Cancel")),
          TextButton(
            onPressed: () async {
              Navigator.pop(context);
              await _removeScene(sceneName);
            },
            child: const Text("Delete", style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
  }
  
Future<void> _fetchDevices() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  String? espDeviceJson = prefs.getString('EspDevice');

  List<Device> devices = [];

  Map<String, dynamic> espDeviceMap = jsonDecode(espDeviceJson!);

  espDeviceMap.forEach((uid, userDeviceData) {
    if (userDeviceData is Map<String, dynamic>) {
      userDeviceData.forEach((deviceId, deviceData) {
        if (deviceData is Map<String, dynamic> &&
            deviceData.containsKey("name") &&
            deviceData.containsKey("editname")) {

          String deviceName = deviceData["name"] ?? "Unknown Device";
          String editedName = deviceData["editname"] ?? "";

          String finalDeviceName = editedName.isNotEmpty ? editedName : deviceName;

          devices.add(Device(
            deviceId: deviceId,
            deviceName: finalDeviceName,
          ));
        }
      });
    }
  });

  print('Fetched ESP Devices: $devices');

  setState(() {
    _devices = devices;
  });
}

  void _onTabtapped(int index) {
    if (index == currentIndex) return;
  setState(() {
    currentIndex = index;
  });
 
  switch (index) {
    case 0:
      bool hasDevices = _devices.isNotEmpty;
      print('Devices: $hasDevices');
      
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (context) => hasDevices ? const AvailableDevicesPage() : const NoDevicePage(),
        ),
      );
      break;
    case 1:
      Navigator.of(context).push(
        MaterialPageRoute(builder: (context) => const SchedulerPage()),
      );
      break;
    case 2:
      Navigator.of(context).push(
        MaterialPageRoute(builder: (context) => const Settings1Page()),
      );
      break;
  }
}


  @override
  Widget build(BuildContext context) {
     final theme = Theme.of(context);  
      return WillPopScope(
    onWillPop: () async {
      bool? exitApp = await showDialog(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Exit App'),
          content: const Text('Do you want to exit the app?'),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(false), 
              child: const Text('No'),
            ),
            TextButton(
              onPressed: () => exit(0), 
              child: const Text('Yes'),
            ),
          ],
        ),
      );

      return exitApp ?? false;
    },
      child: Scaffold(
        appBar: PreferredSize(
          preferredSize: const Size.fromHeight(50),
          child: AppBar(
            automaticallyImplyLeading: false,
            flexibleSpace: Stack(
              children: [
                Positioned(
                  child: Image.asset(
                    'assets/images/home_14.7.png',
                    fit: BoxFit.cover,
                  ),
                ),
                Positioned(
                  top: 0,
                  bottom: 0,
                  right: 0,
                  //left: 200,
                  child: Image.asset(
                    'assets/images/home_14.8.png',
                  ),
                ),
              ],
            ),
          ),
        ),
        body: Stack(
  children: [
    SafeArea(
      child: Padding(
        padding: const EdgeInsets.only(top: 10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Padding(
              padding: EdgeInsets.only(left: 10),
              child: Text(
                'Your Scenes',
                style: TextStyle(
                  fontSize: 25,
                ),
              ),
            ),
            Divider(height: 2, color: theme.dividerColor),
            const SizedBox(height: 10),
            // Wrap ListView.builder in Expanded
            Expanded(
              child: ListView.builder(
                itemCount: scenes.length,
                itemBuilder: (context, index) {
                  String sceneName = scenes[index];
                  return Dismissible(
                    key: Key(sceneName),
                    direction: DismissDirection.endToStart,
                    background: Container(
                      alignment: Alignment.centerRight,
                      padding: const EdgeInsets.symmetric(horizontal: 20),
                      color: Colors.red,
                      child: const Icon(Icons.delete, color: Colors.white),
                    ),
                    confirmDismiss: (direction) async {
                      return await showDialog(
                        context: context,
                        builder: (context) => AlertDialog(
                          title: const Text("Delete Scene"),
                          content: Text("Are you sure you want to delete the scene '$sceneName'?"),
                          actions: [
                            TextButton(
                                onPressed: () => Navigator.pop(context, false),
                                child: const Text("Cancel")),
                            TextButton(
                                onPressed: () => Navigator.pop(context, true),
                                child: const Text("Delete",
                                    style: TextStyle(color: Colors.red))),
                          ],
                        ),
                      );
                    },
                    onDismissed: (direction) {
                      _removeScene(sceneName);
                    },
                    child: Card(
                      margin: const EdgeInsets.all(10),
                      child: ListTile(
                        title: Text(sceneName,
                            style: const TextStyle(
                                fontSize: 18, fontWeight: FontWeight.bold)),
                        trailing: IconButton(
                            icon: const Icon(Icons.delete, color: Colors.red),
                            onPressed: () => _confirmDelete(sceneName)),
                      ),
                    ),
                  );
                },
              ),
            ),
            Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 20),
                      child: Padding(
                        padding: const EdgeInsets.only(bottom: 40,left: 285),
                        child: Builder(
                          builder: (context) {
                            return TextButton(
                              style: TextButton.styleFrom(
                                backgroundColor: const Color.fromARGB(255, 0, 188, 212),
                              ),
                              onPressed: () {
                                Navigator.of(context).push(
                                  MaterialPageRoute(
                                    builder: (context) => const SetupRoutinePage(), 
                                  ),
                                );
                              },
                              child: const Text(
                                'Add  +',
                                style: TextStyle(color: Colors.white),
                              ),
                            );
                          },
                        ),
                      ),
                    ),
          ],
        ),
      ),
    ),
  ],
),

        bottomNavigationBar:
            CustomTabBar(currentIndex: currentIndex, onTabTapped: _onTabtapped),
      ),
    );
  }

}
