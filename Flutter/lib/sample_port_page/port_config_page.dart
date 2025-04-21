import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/add_devices_and_delete/no_device_page.dart';
import 'package:home_auto_sample/scheduler/scheduler_page.dart';
import 'package:home_auto_sample/service/database_service.dart';
import 'package:home_auto_sample/settings/settings_1_page.dart';
import 'package:home_auto_sample/tabBar/custom_appbar.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Port {
  final String name;
  bool state;
  final int pinNumber;

  Port({
    required this.name,
    required this.state,
    required this.pinNumber,
  });

  Map<String, dynamic> toJson() => {
        "name": name,
        "state": state,
        "pinNumber": pinNumber,
      };

  static Port fromJson(Map<String, dynamic> json) => Port(
        name: json["name"],
        state: json["state"],
        pinNumber: json["pinNumber"],
      );
}

class PortConfigPage extends StatefulWidget {
  final String deviceId;
  const PortConfigPage({super.key, required this.deviceId});

  @override
  State<PortConfigPage> createState() => _PortConfigPageState();
}

class _PortConfigPageState extends State<PortConfigPage> {
  int currentIndex = 0;
  List<Device> _devices = [];
  List<Port> _ports = [];
  StreamSubscription<DatabaseEvent>? _dbListener;
  late User currentUser;
  late SharedPreferences prefs;
  late int portCount;
  late String originalName;
  bool isConnected = true; 

  @override
  void initState() {
    super.initState();
    currentUser = FirebaseAuth.instance.currentUser!;
    _fetchDeviceName(widget.deviceId); 
    _fetchDevices();
    _initializeSharedPreferences();
  }

  Future<void> _initializeSharedPreferences() async {
    prefs = await SharedPreferences.getInstance();
    _setupPorts();
    _listenToFirebase();
  }

  Future<void> _debugSharedPreferences() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  String? espDeviceJson = prefs.getString('EspDevice');

  print("Stored ESP Device Data: $espDeviceJson");
}


Future<void> _fetchDeviceName(String deviceId) async {
  prefs = await SharedPreferences.getInstance();
  String uid = currentUser.uid;
  String? espDeviceJson = prefs.getString('EspDevice');

  
  Map<String, dynamic> espDeviceMap = jsonDecode(espDeviceJson!);
  if (espDeviceMap.containsKey(uid)) {
    Map<String, dynamic> userDeviceData = espDeviceMap[uid];

    if (userDeviceData.containsKey(deviceId)) {
      Map<String, dynamic> deviceData = userDeviceData[deviceId];
      String dName = deviceData["name"] ?? "Unknown Device";

      setState(() {
        originalName= dName;
        print("This is the original name: $originalName");
        portCount = int.tryParse(originalName.replaceAll(RegExp(r'[^0-9]'), '')) ?? 4;
      });
      _setupPorts();
      _debugSharedPreferences();
    } else {
      print("Device ID not found in user data.");
    }
  } else {
    print("UID not found in SharedPreferences.");
  }
}

  void _setupPorts() {
    _ports = List.generate(portCount, (index) {
      return Port(name: 'Port ${index + 1}', state: false, pinNumber: index + 1);
    });

    _updateFirebasePortNames();
    _loadFromPreferences();
  }

  // Listen for Firebase changes and update UI
  void _listenToFirebase() {
    _dbListener = FirebaseDatabase.instance
        .ref()
        .child("users")
        .child(currentUser.uid)
        .child("components")
        .onValue
        .listen((event) {
      final data = event.snapshot.value;
      if (data != null && data is Map<dynamic, dynamic>) {
        setState(() {
          for (var port in _ports) {
            port.state = data['${originalName}_${port.pinNumber}'] == 1;
          }
        });
        _saveToPreferences();
      }
    }, onError: (error) {
      debugPrint("Firebase error: $error");
    });
  }

  // Update Firebase with port states
  Future<void> _updateFirebasePortState() async {
    Map<String, dynamic> portData = {};
    for (var port in _ports) {
      portData['${originalName}_${port.pinNumber}'] = port.state ? 1 : 0;
    }
    await FirebaseDatabase.instance
        .ref()
        .child("users")
        .child(currentUser.uid)
        .child("components")
        .update(portData);
  }

  // Store port names inside "users → uid → Devicename"
  Future<void> _updateFirebasePortNames() async {
    Map<String, String> portNames = {
      for (var port in _ports) 'port${port.pinNumber}': port.name
    };

    await FirebaseDatabase.instance
        .ref()
        .child("users")
        .child(currentUser.uid)
        .child("Devicename") // Store under Devicename
        .set(portNames);
  }

 Future<void> _saveToPreferences() async {
  prefs = await SharedPreferences.getInstance();
  String? espDeviceJson = prefs.getString('EspDevice');
  Map<String, dynamic> espDeviceMap = espDeviceJson != null ? jsonDecode(espDeviceJson) : {};
  String uid = currentUser.uid;

  if (!espDeviceMap.containsKey(uid)) {
    espDeviceMap[uid] = {}; 
  }

  if (!espDeviceMap[uid].containsKey(widget.deviceId)) {
    espDeviceMap[uid][widget.deviceId] = {}; 
  }
  Map<String, dynamic> existingDeviceData = espDeviceMap[uid][widget.deviceId];
  existingDeviceData["ports"] = {
    ...existingDeviceData["ports"] ?? {}, 
    for (var port in _ports) 'port${port.pinNumber}': port.toJson(),
  };
  
  espDeviceMap[uid][widget.deviceId] = existingDeviceData;
  await prefs.setString('EspDevice', jsonEncode(espDeviceMap));
  print("Ports updated successfully for device ID: ${widget.deviceId}");
  
  _updateFirebasePorts();
}

Future<void> _updateFirebasePorts() async {
  String uid = currentUser.uid;

  Map<String, dynamic> portsData = {
    for (var port in _ports) 'port${port.pinNumber}': port.toJson(),
  };

  await FirebaseDatabase.instance
      .ref()
      .child("users")
      .child(uid)
      .child("Espdevice")
      .child(widget.deviceId)
      .child("ports")
      .set(portsData);

  print("Ports synced to Firebase for device ID: ${widget.deviceId}");
}

 Future<void> _loadFromPreferences() async {
  prefs = await SharedPreferences.getInstance(); 
  String? espDeviceJson = prefs.getString('EspDevice');
  try {
    Map<String, dynamic> espDeviceMap = jsonDecode(espDeviceJson!);
    String uid = currentUser.uid;

    if (!espDeviceMap.containsKey(uid)) {
      print("UID not found in SharedPreferences.");
      return;
    }

    Map<String, dynamic> userDeviceData = espDeviceMap[uid];

    if (!userDeviceData.containsKey(widget.deviceId)) {
      print("Device ID not found for user.");
      return;
    }

    Map<String, dynamic> deviceData = userDeviceData[widget.deviceId];

    if (!deviceData.containsKey("ports") || deviceData["ports"] == null) {
      print("No ports found for device ID: ${widget.deviceId}");
      return;
    }

    Map<String, dynamic> portsMap = deviceData["ports"];

    setState(() {
      _ports = portsMap.entries.map((entry) {
        return Port.fromJson(entry.value);
      }).toList();
    });

    print("Loaded ports for device: ${widget.deviceId}");
  } catch (e) {
    print("Error loading ESP device data: $e");
  }
}

Future<void> _updateFirebasePortStatus(bool isConnected) async {
  Map<String, dynamic> messageData = {
    "Status": "$originalName ${isConnected ? 'Connected' : 'Disconnected'}", // Device status
    for (var port in _ports) 'port${port.pinNumber}': "Port ${port.pinNumber} ${port.state ? 'ON' : 'OFF'}"
  };

  await FirebaseDatabase.instance
      .ref()
      .child("users")
      .child(currentUser.uid)
      .child("Message") // Store under "Message"
      .set(messageData);
}

Future<String> _fetchMessageFromFirebase(String portName) async {
  try {
    DatabaseReference ref = FirebaseDatabase.instance.ref("Message/$portName");
    DatabaseEvent event = await ref.once();
    
    return event.snapshot.value?.toString() ?? "No message found"; 
  } catch (e) {
    return "Error fetching message";
  }
}
Future<bool> _isNotificationEnabled() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  String? notificationSetting = prefs.getString('notification'); // Check the stored value

  return notificationSetting == "on"; // Return true if it's "on", otherwise false
}

Future<void> _fetchDevices() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  String? espDeviceJson = prefs.getString('EspDevice');

  List<Device> devices = [];

  Map<String, dynamic> espDeviceMap = jsonDecode(espDeviceJson!);

  espDeviceMap.forEach((uid, userDeviceData) {
    if (userDeviceData is Map<String, dynamic>) {
      userDeviceData.forEach((deviceId, deviceData) {
        if (deviceData is Map<String, dynamic>) {
          String deviceName = deviceData["name"] ?? "Unknown Device";
          String editedName = deviceData["editname"] ?? "";

          String finalDeviceName = editedName.isNotEmpty ? editedName : deviceName;
          // Add device to the list
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
        left: true,
        bottom: true,
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.only(top: 25),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: EdgeInsets.only(left: 15),
                child: Text(
                  'Port Configuration',
                  style: TextStyle(
                    fontSize: 22,
                  ),
                ),
                ),
                const SizedBox(
                  height: 0,
                ),
                const Divider(
                  height: 1,
                  color: Colors.black,
                ),
                const SizedBox(
                  height: 10,
                ),
                Padding(
                  padding: EdgeInsets.only(left: 15, right: 25),
                  child: ListView.builder(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    itemCount: _ports.length,
                    itemBuilder: (context, index) {
                      final port = _ports[index];
                      return ListTile(
                        title: Text(port.name,
                        style: TextStyle(
                           fontSize: 19,
                             ),
                        ),
                        trailing: Switch(
                          value: port.state,
                          activeColor: Colors.blue,
                          onChanged: (value) async{
                            setState(() {
                              port.state = value;
                            });
                            _updateFirebasePortState();
                            _updateFirebasePortStatus(isConnected);
                            _saveToPreferences();
                             // Check notification setting before showing Snackbar
                             bool isNotificationOn = await _isNotificationEnabled();

                            if (isNotificationOn && port.state) {
    String message = await _fetchMessageFromFirebase(port.name);

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message), // Display message from Firebase
        duration: const Duration(seconds: 2),
      ),
    );
                            }
                          },
                        ),
                      );
                    },
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    ],
  ),
  bottomNavigationBar:
      CustomTabBar(currentIndex: currentIndex, onTabTapped: _onTabtapped),
);
}

  @override
  void dispose() {
    _dbListener?.cancel();
    super.dispose();
  }
}

  

