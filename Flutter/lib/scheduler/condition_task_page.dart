import 'dart:async';
import 'dart:convert';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/add_devices_and_delete/no_device_page.dart';
import 'package:home_auto_sample/scheduler/background_sheduler.dart';
import 'package:home_auto_sample/scheduler/scenecreated_page.dart';
import 'package:home_auto_sample/scheduler/scheduler_page.dart';
import 'package:home_auto_sample/scheduler/setup_time.dart';
import 'package:home_auto_sample/service/database_service.dart';
import 'package:home_auto_sample/settings/settings_1_page.dart';
import 'package:home_auto_sample/tabBar/custom_appbar.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ConditionTaskPage extends StatefulWidget {
  final String selectedStartTime;
  final String selectedEndTime;

  const ConditionTaskPage({super.key,required this.selectedStartTime, required this.selectedEndTime});

  @override
  State<ConditionTaskPage> createState() => _ConditionTaskPageState();
}

class _ConditionTaskPageState extends State<ConditionTaskPage> {
  
  int currentIndex = 1;
  List<Device> _devices = [];
  bool isConditionExpanded = false;
  bool isTaskExpanded = false;
  final PortService _portService = PortService();

  final String userId = FirebaseAuth.instance.currentUser!.uid;

  final List<String> _timeConditions = [];
  final List<Device> _selectedTasks = [];

  @override
  void initState() {
    super.initState();
    _fetchDevices();
  if (widget.selectedStartTime.isNotEmpty) {
    _timeConditions.add(_formatDate(widget.selectedStartTime));
  }
  if (widget.selectedEndTime.isNotEmpty) {
    _timeConditions.add(_formatDate(widget.selectedEndTime));
  }
  }

void _addTask() {
  if (_devices.isEmpty) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('No devices available!')),
    );
    return;
  }

  List<Device> availableDevices = _devices.where((device) =>
      !_selectedTasks.any((selected) => selected.deviceId == device.deviceId)
  ).toList();

  if (availableDevices.isEmpty) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('All devices are already selected!')),
    );
    return;
  }

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(10)),
    ),
    builder: (context) {
      List<Device> tempSelectedDevices = []; 

      return StatefulBuilder(
        builder: (context, setModalState) {
          return Padding(
            padding: const EdgeInsets.only(top:10),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.only(top: 5,bottom:3, left:10 ),
                  child: const Text(
                    "Your Devices",
                    style: TextStyle(fontSize: 20),
                  ),
                ),
                const SizedBox(height: 10),
                const Divider(height: 2, color: Colors.black),
                Padding(
                  padding: const EdgeInsets.only(left: 5),
                  child: SizedBox(
                    height: 250, 
                    child: GridView.builder(
                      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: 3,
                        childAspectRatio: 1,
                        crossAxisSpacing: 10,
                        mainAxisSpacing: 10,
                      ),
                      itemCount: availableDevices.length,
                      itemBuilder: (context, index) {
                        Device device = availableDevices[index];
                        bool isSelected = tempSelectedDevices.contains(device);
                  
                        return GestureDetector(
                          onTap: () async {
                            List<int>? selectedPorts = await _showPortSelectionDialog(context, device);
                            
                            if (selectedPorts != null && selectedPorts.isNotEmpty) {
                              setModalState(() {
                                if (!tempSelectedDevices.contains(device)) { 
                                  tempSelectedDevices.add(device);
                                  _portService.updatePorts(device.deviceId, selectedPorts);
                                }
                              });
                            }
                          },
                          child: Padding(
                            padding: const EdgeInsets.only(top: 9, bottom: 0,left: 4),
                            child: Stack(
                              alignment: Alignment.center,
                              children: [
                                Image.asset(
                                  'assets/images/tickframe2.png',
                                  width: 100,
                                  height: 170,
                                  fit: BoxFit.cover,
                                ),
                                Positioned(
                                  bottom: 10,
                                  child: Text(
                                    device.deviceName,
                                    style: const TextStyle(
                                      fontSize: 14,
                                      fontWeight: FontWeight.bold,
                                      color: Colors.black,
                                    ),
                                    textAlign: TextAlign.center,
                                  ),
                                ),
                                if (isSelected)
                                  const Positioned(
                                    top: 28,
                                    left: 12,
                                    child: Icon(Icons.check_circle, color: Colors.blue, size: 17),
                                  ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                ),
                const SizedBox(height: 20),
                Align(
                  alignment: Alignment.center,
                  child: ElevatedButton(
                    onPressed: () {
                      setState(() {
                        _selectedTasks.addAll(
                          tempSelectedDevices.where((device) => 
                            !_selectedTasks.contains(device) 
                          ),
                        );
                      });
                      Navigator.pop(context);
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blue,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10),
                      ),
                    ),
                    child: const Text("Done", style: TextStyle(color: Colors.white)),
                  ),
                ),
                const SizedBox(height: 10),
              ],
            ),
          );
        },
      );
    },
  );
}


Future<List<int>?> _showPortSelectionDialog(BuildContext context, Device device) async {
  int numPorts = int.tryParse(RegExp(r'\d+').stringMatch(device.deviceName) ?? '') ?? 4;

  List<int> availablePorts = List.generate(numPorts, (index) => index + 1);

  List<int> selectedPorts = [];

  return await showDialog<List<int>>(
    context: context,
    barrierDismissible: false, // Prevent closing by tapping outside
    builder: (context) {
      return AlertDialog(
        title: Text("Select Ports for ${device.deviceName}"),
        content: StatefulBuilder(
          builder: (context, setDialogState) {
            return Column(
              mainAxisSize: MainAxisSize.min,
              children: availablePorts.map((port) {
                bool isSelected = selectedPorts.contains(port);
                return CheckboxListTile(
                  title: Text("Port $port"),
                  value: isSelected,
                  onChanged: (bool? value) {
                    setDialogState(() {
                      if (value == true) {
                        selectedPorts.add(port);
                      } else {
                        selectedPorts.remove(port);
                      }
                    });
                  },
                );
              }).toList(),
            );
          },
        ),
        actions: [
          TextButton(
            onPressed: () {
              if (selectedPorts.isNotEmpty) {
                Navigator.pop(context, selectedPorts);
              } else {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Please select at least one port!')),
                );
              }
            },
            child: const Text("Save"),
          ),
        ],
      );
    },
  );
}

void _showSceneNameDialog(BuildContext context) {
  TextEditingController sceneNameController = TextEditingController();

  if (_selectedTasks.isEmpty) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Please select at least one device!')),
    );
    return;
  }

  if (widget.selectedStartTime.isEmpty || widget.selectedEndTime.isEmpty) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Please select both start and end times!')),
    );
    return;
  }

  // Show dialog if conditions are met
  showDialog(
    context: context,
    builder: (BuildContext dialogContext) {
      return AlertDialog(
        title: const Text("Enter Scene Name"),
        content: TextField(
          controller: sceneNameController,
          decoration: const InputDecoration(hintText: "Scene Name"),
        ),
        actions: [
          TextButton(
            onPressed: () async {
              String sceneName = sceneNameController.text.trim();
              if (sceneName.isEmpty) {
                ScaffoldMessenger.of(dialogContext).showSnackBar(
                  const SnackBar(content: Text("Please enter a scene name!")),
                );
                return;
              }

              Navigator.pop(dialogContext);

              _showLoadingDialog(context);
              await _createScene(context, sceneName);
              // Hide loading dialog once process is completed
              Navigator.pop(context);
            },
            child: const Text("Save"),
          ),
        ],
      );
    },
  );
}

Future<void> _createScene(BuildContext context, String sceneName) async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  DatabaseReference dbRef = FirebaseDatabase.instance.ref().child("users").child(userId);
  final scheduler = Provider.of<BackgroundSchedulerService>(context, listen: false);

  String? storedJson = prefs.getString("EspDevice");
  Map<String, dynamic> storedData = storedJson != null ? jsonDecode(storedJson) : {};

  Map<String, dynamic> sceneData = {}; 
  Map<String, dynamic> autoOnOffData = {}; 

  for (var task in _selectedTasks) {
    String deviceId = task.deviceId;
    List<int> selectedPorts = _portService.getPorts(deviceId);

    storedData[userId] ??= {};
    storedData[userId]["scenes"] ??= {};
    storedData[userId]["scenes"][sceneName] ??= {};
    storedData[userId]["scenes"][sceneName][deviceId] ??= {"ports": {}};

    storedData[userId]["AutomaticOnOff"] ??= {};
    storedData[userId]["AutomaticOnOff"][deviceId] ??= {};

    for (int port in selectedPorts) {
      String portKey = "port$port";
      String formattedStartTime = _formatDate(widget.selectedStartTime);
      String formattedEndTime = _formatDate(widget.selectedEndTime);

      // Merge sceneData
      sceneData["$sceneName/$deviceId/ports/$portKey"] = {
        "onTime": formattedStartTime,
        "offTime": formattedEndTime
      };

      // Merge autoOnOffData
      autoOnOffData["$deviceId/$portKey"] = {
        "onTime": formattedStartTime,
        "offTime": formattedEndTime
      };

      // Merge into SharedPreferences data
      storedData[userId]["scenes"][sceneName][deviceId]["ports"][portKey] = {
        "onTime": formattedStartTime,
        "offTime": formattedEndTime
      };

      if (!storedData[userId]["AutomaticOnOff"][deviceId].containsKey(portKey)) {
  storedData[userId]["AutomaticOnOff"][deviceId][portKey] = {
    "onTime": formattedStartTime,
    "offTime": formattedEndTime
  };
}

    }
  }

  await dbRef.child("scenes").update(sceneData);
  await dbRef.child("AutomaticOnOff").update(autoOnOffData);

  List<String> savedScenes = prefs.getStringList("savedScenes") ?? [];
  if (!savedScenes.contains(sceneName)) {
    savedScenes.add(sceneName);
    await prefs.setStringList("savedScenes", savedScenes);
  }

  await prefs.setString("EspDevice", jsonEncode(storedData));
  try{
    await scheduler.startSceneScheduler(userId, sceneName);
      debugPrint("Scene scheduler started for $sceneName");
  }catch (e) {
      debugPrint("Error starting scene scheduler: $e");
    }

  if (!mounted) return;

  WidgetsBinding.instance.addPostFrameCallback((_) {
    Future.delayed(Duration(milliseconds: 300), () {
      if (mounted) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (context) => SceneCreatedPage()),
        );
      }
    });
  });
}

@override
void dispose() {
  super.dispose();
}

void _showLoadingDialog(BuildContext context) {
  showDialog(
    context: context,
    barrierDismissible: false, // Prevent dismissal by tapping outside
    builder: (BuildContext context) {
      return Dialog(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              CircularProgressIndicator(),
              SizedBox(height: 10),
              Text('Updating scene... Please wait.'),
            ],
          ),
        ),
      );
    },
  );
}

String _formatDate(String input) {
  DateTime? dateTime = DateTime.tryParse(input);
  if (dateTime != null) {
    return DateFormat("hh:mm a MMM d, yyyy").format(dateTime); 
  }

  List<String> possibleFormats = [
    "MMM d HH:mm",
    "yyyy-MM-dd HH:mm",
    "MM/dd/yyyy HH:mm",
    "hh:mm a MMM d, yyyy"
    "EEE, dd MMM yyyy HH:mm:ss",
    "yyyy/MM/dd HH:mm",
    "HH:mm MMM d",
  ];

  for (String format in possibleFormats) {
    try {
      dateTime = DateFormat(format).parse(input);
      return DateFormat("hh:mm a MMM d, yyyy").format(dateTime); 
    } catch (_) {
      continue;
    }
  }

  debugPrint("Unable to parse date: $input");
  return input;
}

Future<String?> getDeviceName(String userId, String deviceId) async {
  final dbRef = FirebaseDatabase.instance.ref()
      .child("users")
      .child(userId)
      .child("EspDevice")
      .child(deviceId)
      .child("name");

  final snapshot = await dbRef.get();

  if (snapshot.exists) {
    return snapshot.value.toString();
  } else {
    debugPrint("Device ID $deviceId not found for user $userId");
    return null;
  }
} 

void _editDeviceName(BuildContext context, String uid, String deviceId, String currentName) async {
  TextEditingController nameController = TextEditingController(text: currentName);

  showDialog(
    context: context,
    builder: (context) {
      return AlertDialog(
        title: const Text("Edit Device Name"),
        content: TextField(
          controller: nameController,
          decoration: const InputDecoration(hintText: "Enter new name"),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("Cancel"),
          ),
          TextButton(
            onPressed: () async {
              String newName = nameController.text.trim();
              if (newName.isNotEmpty && newName != currentName) {
                // Update in Firebase Realtime Database
                DatabaseReference dbRef = FirebaseDatabase.instance
                    .ref()
                    .child("users")
                    .child(uid)
                    .child("EspDevice")
                    .child(deviceId);

                await dbRef.update({"editname": newName});

                // Store in SharedPreferences
                SharedPreferences prefs = await SharedPreferences.getInstance();
                String sharedPrefKey = "EspDevice_${uid}_$deviceId";
                await prefs.setString(sharedPrefKey, newName);

                Navigator.pop(context);
              }
            },
            child: const Text("Save"),
          ),
        ],
      );
    },
  );
}
  
  Future<void> _addTimeCondition() async {
    final selectedTime = await Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const SetupTime()),
    );

    if (selectedTime != null && selectedTime is String) {
      setState(() {
        _timeConditions.add(selectedTime);
      });
    }
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
    return Scaffold(
  appBar: PreferredSize(
    preferredSize: const Size.fromHeight(50),
    child: AppBar(
      automaticallyImplyLeading: false,
      flexibleSpace: Stack(
        children: [
          Positioned(
            child: Image.asset(
              'assets/images/corner design.png',
              fit: BoxFit.cover,
            ),
          ),
          Positioned(
            top: 0,
            bottom: 0,
            right: 0,
            child: Image.asset(
              'assets/images/triangle design.png',
            ),
          ),
          Positioned(
            top: 45,
            left: 0,
            child: IconButton(
              onPressed: () => Navigator.pop(context),
              icon: const Icon(Icons.arrow_back_rounded, color: Colors.black, weight: 900,),
            ),
          ),
        ],
      ),
    ),
  ),
        body: Stack(
        children: [
      SafeArea(
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.only(top: 10),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [                
                Padding(
                  padding: const EdgeInsets.only(top: 30),
                  child: Divider(height: 2, color: theme.dividerColor),
                ),
                
                GestureDetector(
                  onTap: () {
                    setState(() {
                      isConditionExpanded = !isConditionExpanded;
                    });
                  },
                  child: Padding(
                    padding: const EdgeInsets.all(10.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text("Conditions",style: TextStyle(fontSize: 20),),
                        Icon(
                          isConditionExpanded ? Icons.keyboard_arrow_up : Icons.keyboard_arrow_down,
                          size: 40,
                          color: Colors.black,
                        ),
                      ],
                    ),
                  ),
                ),
                if (isConditionExpanded)
                      Padding(
                        padding: const EdgeInsets.only(right: 25, left: 25),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            // Display All Time Condition Cards
                            for (int i = 0; i < _timeConditions.length; i++)
                              Card(
                                color: Colors.grey[200],
                                elevation: 4,
                                shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(10)),
                                margin: const EdgeInsets.symmetric(vertical: 10),
                                child: Padding(
                                  padding: const EdgeInsets.all(10.0),
                                  child: Row(
                                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                    children: [
                                      Row(
                                        children: [
                                          Image.asset("assets/icon/clock.png", width: 35, height: 35),
                                          Padding(
                                            padding: const EdgeInsets.only(left: 10),
                                            child: const Text("Time", style: TextStyle(fontSize: 16)),
                                          ),
                                        ],
                                      ),
                                      Text(_timeConditions[i], style: const TextStyle(fontSize: 16)),
                                      GestureDetector(
                                        onTap: () => _addTimeCondition(),
                                        child: const ImageIcon(
                                          AssetImage('assets/images/edit.png'),
                                          color: Colors.black,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ),

                            // Add Button
                            Padding(
                              padding: const EdgeInsets.only(right: 0, left: 250, bottom: 7),
                              child: ElevatedButton.icon(
                                onPressed: _addTimeCondition,
                                label: const Text(
                                  "Add  +",
                                  style: TextStyle(color: Colors.white),
                                ),
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: const Color.fromARGB(255, 0, 188, 212),
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(10),
                                  ),
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                  Divider(height: isConditionExpanded ? 2 : 0, color: theme.dividerColor),
                // Tasks Section UI
GestureDetector(
  onTap: () {
    setState(() {
      isTaskExpanded = !isTaskExpanded;
    });
  },
  child: Padding(
    padding: const EdgeInsets.all(10.0),
    child: Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        const Text("Tasks", style: TextStyle(fontSize: 20)),
        Icon(
          isTaskExpanded ? Icons.keyboard_arrow_up : Icons.keyboard_arrow_down,
          size: 40,
          color: Colors.black,
        ),
      ],
    ),
  ),
),
if (isTaskExpanded)
  Padding(
  padding: const EdgeInsets.symmetric(horizontal: 25),
  child: Column(
    children: [
      for (int i = 0; i < _selectedTasks.length; i++)
        Stack(
          clipBehavior: Clip.none, 
          children: [
            Container(
              margin: const EdgeInsets.only(right: 40), 
              child: Card(
                color: Colors.grey[200],
                elevation: 4,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                margin: const EdgeInsets.symmetric(vertical: 10),
                child: ListTile(
                  leading: SizedBox(
                    width: 40, 
                    height: 40, 
                    child: Image.asset(
                      'assets/images/ondice.png', 
                      fit: BoxFit.cover,
                    ),
                  ),
                  title: Text(
                    _selectedTasks[i].deviceName,
                    style: const TextStyle(fontSize: 16),
                  ),
                  trailing: SizedBox(
    width: 25,
    height: 25,
    child: GestureDetector(
      onTap: () => _editDeviceName(context, userId,_selectedTasks[i].deviceId,_selectedTasks[i].deviceName),
      child: Image.asset(
        'assets/images/edit.png',
        fit: BoxFit.cover,
      ),
    ),
  ),
                ),
              ),
            ),
            Positioned(
              right: -10, 
              top: 15, 
              child: GestureDetector(
                onTap: () {
                  setState(() {
                    _selectedTasks.removeAt(i);
                  });
                },
                child: Image.asset(
                  'assets/icon/cross.png', 
                  width: 35, 
                  height: 35,
                ),
              ),
            ),
          ],
        ),

      // Add Button
      Padding(
        padding: const EdgeInsets.only(right: 0, left: 250, bottom: 7),
        child: ElevatedButton.icon(
          onPressed: _addTask,
          label: const Text(
            "Add +",
            style: TextStyle(color: Colors.white),
          ),
          style: ElevatedButton.styleFrom(
            backgroundColor: const Color.fromARGB(255, 0, 188, 212),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(10),
            ),
          ),
        ),
      ),
    ],
  ),
),

                  Divider(height: isTaskExpanded ? 2 : 0, color: theme.dividerColor),
              ],
            ),
          ),
        ),
      ),
      Padding(
  padding: const EdgeInsets.only(left: 125, top: 590),
  child: ElevatedButton.icon(
    onPressed: () => _showSceneNameDialog(context),  
    label: const Text(
      "Create Scene",
      style: TextStyle(color: Colors.white, fontSize: 17),
    ),
    style: ElevatedButton.styleFrom(
      backgroundColor: const Color.fromARGB(255, 0, 188, 212),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
      ),
    ),
  ),
)

        ],
      ),
      
      bottomNavigationBar:
            CustomTabBar(currentIndex: currentIndex, onTabTapped: _onTabtapped),
      );    
  }
}
