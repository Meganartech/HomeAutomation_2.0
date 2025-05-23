import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:openhab_testing/add_devices_and_delete/device_delete_page.dart';
import 'package:openhab_testing/add_devices_and_delete/no_device_page.dart';
import 'package:openhab_testing/add_devices_and_delete/scanning_device_page.dart';
import 'package:openhab_testing/service/database_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AvailableDevicesPage extends StatefulWidget {
  const AvailableDevicesPage({super.key});

  @override
  _AvailableDevicesPageState createState() => _AvailableDevicesPageState();
}

class _AvailableDevicesPageState extends State<AvailableDevicesPage> {
  final TextEditingController _deviceNameController = TextEditingController();
  List<Device> _devices = [];
  dynamic _snapshot;
  int currentIndex = 0;
  WifiDetails? wifiDetails;
  

  @override
  void initState() {
    super.initState();
    _fetchDevices();
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
  _debugSharedPreferences();
  print('Fetched ESP Devices: $devices');

  setState(() {
    _devices = devices;
  });
}

Future<void> _debugSharedPreferences() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  String? espDeviceJson = prefs.getString('EspDevice');

  print("Stored ESP Device Data: $espDeviceJson");
}

void debugPrintScanResult(ScanResult r) {
  print('Scan Result:');
  print('Device Name: ${r.device.name}');
  print('Device ID: ${r.device.remoteId}');
  print('Advertisement Name: ${r.advertisementData.advName}');
  print('Manufacturer Data: ${r.advertisementData.manufacturerData}');
  print('Service Data: ${r.advertisementData.serviceData}');
  print('RSSI: ${r.rssi}');
  print('-----------------------');
}

void _onTabtapped(int index) {
    setState(() {
      currentIndex = index;
    });
    switch (index) {
      case 0:
        bool hasDevices = _snapshot.value != null;
        print(_snapshot.value);
        print('EspDevice: $hasDevices');
       
        Navigator.of(context)
            .push(MaterialPageRoute(builder: (context) => hasDevices ? const AvailableDevicesPage() : const NoDevicePage()));
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
              onPressed: () => Navigator.of(context).pop(false), // Stay in the app
              child: const Text('No'),
            ),
            TextButton(
              onPressed: () => exit(0), // Directly close the app
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
            backgroundColor: theme.appBarTheme.backgroundColor,
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
              left: true,
              child: SingleChildScrollView(
                child: Padding(
                  padding: const EdgeInsets.only(top: 6.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Padding(
                        padding: const EdgeInsets.only(left: 5),
                        child: Text(
                          'Welcome!',
                          style: theme.textTheme.headlineMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                            fontSize: 18,
                            color: theme.textTheme.displaySmall?.color,
                          ),
                        ),
                      ),
                      const SizedBox(
                        height: 10,
                      ),
                      Divider(
                        height: 2,
                        color: theme.dividerColor,
                      ),
                      const SizedBox(
                        height: 10,
                      ),
                      Center(
                        child: Stack(
                          alignment: Alignment.center,
                          children: [
                            ImageIcon(
                              const AssetImage(
                                'assets/images/home_14.6.png',
                              ),
                              size: 50,
                              color: Colors.red,
                            ),
                            const CircleAvatar(
                              radius: 20,
                              backgroundImage:
                                  AssetImage('assets/images/home_14.5.png'),
                            ),
                          ],
                        ),
                      ),
                      Stack(
                        children: [
                          Image.asset(
                            'assets/images/Rectangle.png',
                            height: MediaQuery.sizeOf(context).height * 0.09,
                            ),
                            Padding(
                              padding: const EdgeInsets.only(left: 10, top: 30, bottom: 2),
                              child: Row(
                                children: [
                                  Text(
                                    'Your Devices',
                                    style: theme.textTheme.titleMedium?.copyWith(
                                      color: Colors.white,
                                      ),
                                    ),
                                      SizedBox(width: MediaQuery.sizeOf(context).width * 0.01),
                                      Image.asset('assets/images/dice.png', height: 35),
                                      ],
                                    ),
                                  ),
                                ],
                              ),
                              SizedBox(height: MediaQuery.sizeOf(context).height * 0.04),
                              SizedBox(
                                height: MediaQuery.sizeOf(context).height * 0.40,
                                child: Padding(
                                  padding: const EdgeInsets.all(8.0),
                                  child: GridView.builder(
                                    gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                                      crossAxisCount: 2, // Two devices per row
                                      childAspectRatio: 1, // Adjust size ratio to suit your design
                                      crossAxisSpacing: 10,
                                      mainAxisSpacing: 10,
                                    ),
                                      itemCount: _devices.length,
                                      itemBuilder: (context, index) {
                                        String deviceId = _devices[index].deviceId;
                                        String deviceName = _devices[index].deviceName;
                                        return Column(
                                          mainAxisSize: MainAxisSize.min,
                                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                          children: [
                                            Stack(
                                              children: [
                                                GestureDetector(
                                                  onTap: (){},
                                          //         async {
                                          //           try {
                                          //             String selectedDeviceId = _devices[index].deviceId; 
                                          //       if (mounted) {
                                          //         Navigator.push(
                                          //           context,
                                          //           MaterialPageRoute(
                                          //             builder: (context) => PortConfigPage(
                                          //               deviceId:selectedDeviceId, // Pass the name fetched from SharedPreferences
                                          //             ),
                                          //           ),
                                          //         );
                                          //       }
                                          //     } catch (e) {
                                          //       if (mounted) {
                                          //         ScaffoldMessenger.of(context).showSnackBar(
                                          //           SnackBar(content: Text('Failed to navigate: $e')),
                                          //       );
                                          //     }
                                          //   }
                                          // },
                                        child: Image.asset('assets/images/device_img.png'),
                                      ),
                                    // Device Name
                                    Positioned(
                                      top: 55,
                                      left: 35,
                                      right: 20,
                                      child: Text(
                                        deviceName,
                                        style: theme.textTheme.labelLarge?.copyWith(
                                          color: theme.primaryColor,
                                        ),
                                      ),
                                    ),
                                  // Edit Button
                                  Positioned(
                                    bottom: 8,
                                    left: 10,
                                    child: GestureDetector(
                                      onTap: () {
                                        showEditDeviceDialog(context,FirebaseAuth.instance.currentUser!.uid,deviceId,deviceName ); },
                                        child: const ImageIcon(
                                          AssetImage('assets/images/edit.png'),
                                          color: Colors.black,
                                        ),
                                      ),
                                    ),
                                  // Delete Button
                                  Positioned(
                                    bottom: 8,
                                    right: 8,
                                    child: GestureDetector(
                                      onTap: () async{ 
                                        String originalName= await _fetchDeviceName(FirebaseAuth.instance.currentUser!.uid,deviceId) as String;
                                        showDeleteDeviceDialog(context, FirebaseAuth.instance.currentUser!.uid, deviceId,originalName);},
                                      child: const ImageIcon(
                                        AssetImage('assets/images/delete.png'),
                                        color: Color.fromARGB(255, 100, 57, 41),
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            ],
                          );
                        },
                      ),
                    ),
                  ),
                  Padding(
                      padding:  EdgeInsets.only(
                          bottom: 0, left: 20, right: 20, top: MediaQuery.sizeOf(context).height * 0.07),
                      child: Align(
                        alignment: Alignment.bottomRight,
                        //padding: const EdgeInsets.only(left: 250),
                        child: Builder(
                          builder: (context) {
                            return TextButton(
                              style: TextButton.styleFrom(
                                backgroundColor:
                                    const Color.fromARGB(255, 0, 188, 212),
                              ),
                              onPressed: () async{                               
                                // await clearSharedPreferences();
                                // await printSharedPreferences();
                                Navigator.of(context).push(
                                  MaterialPageRoute(
                                      builder: (context) =>
                                          const ScanningDevicePage()),
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
                    SizedBox(
                      height: MediaQuery.sizeOf(context).height * 0.03,
                    ),
                   
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

// Future<void> printSharedPreferences() async {
//   SharedPreferences prefs = await SharedPreferences.getInstance();
//   Map<String, dynamic> allPrefs = prefs.getKeys().fold({}, (map, key) {
//     map[key] = prefs.get(key);
//     return map;
//   });

//   print("Shared Preferences Data:\n$allPrefs");
// }

// Future<void> clearSharedPreferences() async {
//   final SharedPreferences prefs = await SharedPreferences.getInstance();
//   await prefs.clear();
//   print("All SharedPreferences data cleared!");
// }

void showEditDeviceDialog(BuildContext context, String userId, String deviceId, String deviceName) {
  showDialog(
    context: context,
    builder: (BuildContext context) {
      return AlertDialog(
        title: const Text('Edit'),
        content: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('Name'),
            TextField(
              controller: _deviceNameController,
              cursorColor: Colors.black,
              decoration: InputDecoration(
                hintText: deviceName, // Show current device name as hint
              ),
            ),
          ],
        ),
        actions: [
          Align(
            alignment: Alignment.center,
            child: ElevatedButton(
              onPressed: () async {
                String newDeviceName = _deviceNameController.text.trim();
                if (newDeviceName.isNotEmpty) {
                  await _saveDeviceNameToPreferences(userId, deviceId, newDeviceName);
                  // Fetch devices again to update UI with edited name
                  await _fetchDevices();
                }
                Navigator.of(context).pop();
              },
              style: ElevatedButton.styleFrom(
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                backgroundColor: const Color.fromARGB(255, 132, 186, 230),
              ),
              child: const Text('Change', style: TextStyle(color: Colors.white)),
            ),
          ),
        ],
      );
    },
  );
}

Future<void> _saveDeviceNameToPreferences(String userId, String deviceId, String editedName) async {
  final prefs = await SharedPreferences.getInstance();

  String? espDeviceJson = prefs.getString('EspDevice');
  Map<String, dynamic> espDeviceMap = espDeviceJson != null ? jsonDecode(espDeviceJson) : {};

  if (!espDeviceMap.containsKey(userId)) {
    espDeviceMap[userId] = {}; 
  }

  Map<String, dynamic> existingDeviceData = espDeviceMap[userId][deviceId] ?? {};
  existingDeviceData["editname"] = editedName;
  espDeviceMap[userId][deviceId] = existingDeviceData;
  await prefs.setString('EspDevice', jsonEncode(espDeviceMap));
  
  print("Updated SharedPreferences: $espDeviceMap");

   // Update Firebase with the edited name
  DatabaseReference ref = FirebaseDatabase.instance
      .ref()
      .child("users")
      .child(userId)
      .child("Espdevice")
      .child(deviceId);

  await ref.update({"editname": editedName});
  print("Updated Firebase: $deviceId -> editname: $editedName");
}


void showDeleteDeviceDialog(BuildContext context, String userId, String deviceId, String originalName){
  showDialog(
    context: context,
    builder: (BuildContext context) {
      return AlertDialog(
        title: const Align(
          alignment: Alignment.center,
          child: Text(
            'Are you sure you want to delete this device?',
            style: TextStyle(fontSize: 15),
            textAlign: TextAlign.center,
          ),
        ),
        actions: [
          Row(
            children: [
              ElevatedButton(
                onPressed: () async {
                  try{
                  // Delete device from Firebase and SharedPreferences
                  await _removeDeviceFromSharedPreferences(userId,deviceId);
                  await _removeComponentFromFirebase(userId,originalName);
                  await _removePortNamesFromFirebase(userId);

                  Navigator.of(context).pushReplacement(
                      MaterialPageRoute(builder: (context) =>  const DeviceDeletePage()),
                    );

                } catch (error) {
                    Navigator.pop(context);
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text('Error: $error'),
                        backgroundColor: Colors.red,
                      ),
                    );
                  }
                },
                style: ElevatedButton.styleFrom(
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  backgroundColor: const Color.fromARGB(255, 132, 186, 230), 
                ),
                child: const Text('Yes', style: TextStyle(color: Colors.white)),
              ),
              const Spacer(),
              ElevatedButton(
                onPressed: () {
                  Navigator.pop(context); // Just close the dialog
                },
                style: ElevatedButton.styleFrom(
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  backgroundColor: const Color.fromARGB(255, 132, 186, 230),
                ),
                child: const Text('No', style: TextStyle(color: Colors.white)),
              ),
            ],
          ),
        ],
      );
    },
  );
}

Future<void> _removeDeviceFromSharedPreferences(String userId,String deviceId) async {
  SharedPreferences prefs = await SharedPreferences.getInstance(); 
  String? espDeviceJson = prefs.getString('EspDevice');

  Map<String, dynamic> espDeviceMap = jsonDecode(espDeviceJson!);

  if (espDeviceMap.containsKey(userId) && espDeviceMap[userId].containsKey(deviceId)) {
    espDeviceMap[userId].remove(deviceId); 

    if (espDeviceMap[userId].isEmpty) {
      espDeviceMap.remove(userId); 
    }

    await prefs.setString('EspDevice', jsonEncode(espDeviceMap));
    print("Deleted device data from SharedPreferences for device ID: $deviceId");
  } else {
    print("Device data not found in SharedPreferences.");
  }
    // Delete from Firebase
  DatabaseReference ref = FirebaseDatabase.instance
      .ref()
      .child("users")
      .child(userId)
      .child("Espdevice")
      .child(deviceId);

  await ref.remove(); 
  print("Deleted device data from Firebase for device ID: $deviceId");
}

Future<void> _removeComponentFromFirebase(String userId, String originalName) async {
  DatabaseReference userRef = FirebaseDatabase.instance.ref()
      .child("users")
      .child(userId)
      .child("components");

  DataSnapshot snapshot = await userRef.get();  

  if (snapshot.exists && snapshot.value is Map) {
    Map<dynamic, dynamic> data = snapshot.value as Map<dynamic, dynamic>;

    for (var key in data.keys) {
      if (key.startsWith(originalName)) {  
        await userRef.child(key).remove(); 
        print("Deleted component: $key");
      }
    }
    
    print("Deleted all components matching: $originalName");
  } else {
    print("No components found for user: $userId");
  }
}

Future<void> _removePortNamesFromFirebase(String userId) async {
  DatabaseReference userRef = FirebaseDatabase.instance.ref()
      .child("users")
      .child(userId)
      .child("Devicename");

  await userRef.get().then((snapshot) {
    if (snapshot.exists) {
      Map<dynamic, dynamic> data = snapshot.value as Map<dynamic, dynamic>;
      List<String> keysToRemove = [];

      for (var key in data.keys) {
        if (key.startsWith("port")) {  
          keysToRemove.add(key);
        }
      }

      for (var key in keysToRemove) {
        userRef.child(key).remove();
      }

      print("Deleted port names (port1, port2, etc.) from Firebase under 'Devicename'.");
    } else {
      print("No ports found under 'Devicename' in Firebase.");
    }
  });
}

Future<String?> _fetchDeviceName(String userId,String deviceId) async {
  SharedPreferences prefs = await SharedPreferences.getInstance(); 
  String? espDeviceJson = prefs.getString('EspDevice');

  
  Map<String, dynamic> espDeviceMap = jsonDecode(espDeviceJson!);
  if (espDeviceMap.containsKey(userId)) {
    Map<String, dynamic> userDeviceData = espDeviceMap[userId];

    if (userDeviceData.containsKey(deviceId)) {
      String originalName = userDeviceData[deviceId]["name"];
      return originalName;
    }
  }
  return null; // Return null if name not found
}

}

