import 'dart:convert';
import 'dart:io';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/scheduler/NoScenePage.dart';
import 'package:home_auto_sample/scheduler/available_scene_page.dart';
import 'package:home_auto_sample/service/database_service.dart';
import 'package:home_auto_sample/settings/settings_1_page.dart';
import 'package:home_auto_sample/tabBar/custom_appbar.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../add_devices_and_delete/no_device_page.dart';

class SchedulerPage extends StatefulWidget {
  const SchedulerPage({super.key});

  @override
  State<SchedulerPage> createState() => _SchedulerPageState();
}

class _SchedulerPageState extends State<SchedulerPage> {
  int currentIndex = 1;
  List<Device> _devices = [];

  final String userId = FirebaseAuth.instance.currentUser!.uid;

  @override
  void initState() {
    super.initState();
    _fetchDevices();
  }

  void _fetchScenes(BuildContext context) async {
    User? user = FirebaseAuth.instance.currentUser;
    if (user == null) {
      Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const NoScenePage()));
      return;
    }

    String userId = user.uid;
    DatabaseReference scenesRef = FirebaseDatabase.instance.ref("users/$userId/scenes");

    try {
      DatabaseEvent event = await scenesRef.once();
      DataSnapshot snapshot = event.snapshot;
      List<String> sceneNames = [];

      if (snapshot.exists && snapshot.value != null) {
        Map<dynamic, dynamic> scenesData = snapshot.value as Map<dynamic, dynamic>;
        sceneNames = scenesData.keys.map((key) => key.toString()).toList();
      }

      if (sceneNames.isNotEmpty) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (context) => AvailableScenesPage()),
        );
      } else {
        Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const NoScenePage()));
      }
    } catch (error) {
      debugPrint("Error fetching scenes: $error");
      Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const NoScenePage()));
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
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.only(top: 6.0,left: 10),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Padding(
                  padding: EdgeInsets.only(left: 12),
                  child: Text(
                    'Scheduler',
                    style: TextStyle(
                      fontSize: 25,
                    ),
                  ),
                ),
                const SizedBox(height: 10),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    const SizedBox(height: 20),
                    Image.asset('assets/images/home_8.png'),
                    const SizedBox(height: 5),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Image.asset('assets/images/symbol.png'),
                        const SizedBox(width: 10),                        
                        Expanded(
                          child: const Text(
                            'Easily create helpful automations. Get the devices to work together and help to make your home safer, more convenient, and more efficient.',
                            textAlign: TextAlign.left,
                            style: TextStyle(
                              fontSize: 18,
                            ),
                          ),
                        ),
                        const SizedBox(width: 10,)
                      ],
                    ),
                    const SizedBox(height: 40),
                    Builder(
                      builder: (context) {
                        return TextButton(
                          style: TextButton.styleFrom(
                            backgroundColor: const Color.fromARGB(255, 0, 188, 212),
                          ),
                          onPressed: () => _fetchScenes(context),
                          child: const Text(
                            'Next  -->',
                            style: TextStyle(color: Colors.white,fontSize: 15),
                          ),
                        );
                      },
                    ),
                    SizedBox(
                      height: MediaQuery.sizeOf(context).height * 0.09,
                    ),
                  ],
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
),
    );
  }
}