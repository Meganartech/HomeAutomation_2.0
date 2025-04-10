
//import 'dart:convert';
import 'dart:convert';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/add_devices_and_delete/no_device_page.dart';
import 'dart:io';
import 'package:home_auto_sample/scheduler/scheduler_page.dart';
import 'package:home_auto_sample/scheduler/setup_Routine_page.dart';
import 'package:home_auto_sample/service/database_service.dart';
import 'package:home_auto_sample/settings/settings_1_page.dart';
import 'package:home_auto_sample/tabBar/custom_appbar.dart';
import 'package:shared_preferences/shared_preferences.dart';
//import 'package:shared_preferences/shared_preferences.dart';

class NoScenePage extends StatefulWidget {
  const NoScenePage({super.key});

  @override
  State<NoScenePage> createState() => _NoDevicePageState();
}

class _NoDevicePageState extends State<NoScenePage> {
  int currentIndex = 1;
  List<Device> _devices = [];

  final String userId = FirebaseAuth.instance.currentUser!.uid;

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
        if (deviceData is Map<String, dynamic>) {
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
        child: SingleChildScrollView(
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
                const SizedBox(height: 50),
                Stack(
                  children: [
                    Container(
                      width: 500,
                      height: 250,
                      decoration: const BoxDecoration(
                        image: DecorationImage(
                          image: AssetImage('assets/images/home_14.png'),
                          fit: BoxFit.cover,
                        ),
                      ),
                    ),
                    Positioned(
                      top: 120,
                      bottom: 0,
                      left: 0,
                      right: 150,
                      child: Image.asset(
                        'assets/images/home_14.3.png',
                        height: 25,
                      ),
                    ),
                    Positioned(
                      top: 55,
                      bottom: 65,
                      right: 170,
                      child: Image.asset(
                        'assets/images/home_14.1.png',
                      ),
                    ),
                    Positioned(
                      top: 15,
                      bottom: 75,
                      right: 100,
                      child: Image.asset(
                        'assets/images/home_14.2.png',
                      ),
                    ),
                    Positioned(
                      top: 20,
                      bottom: 100,
                      right: 0,
                      child: Image.asset(
                        'assets/images/star.png',
                      ),
                    ),
                    Positioned(
                      top: 70,
                      bottom: 5,
                      right: 30,
                      child: Image.asset(
                        'assets/images/house_scene.png',
                      ),
                    ),
                    const Positioned(
                      top: 210,
                      left: 150,
                      bottom: 0,
                      child: Text(
                        "Click add to create new scene",                       
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontSize: 17,
                          fontWeight: FontWeight.normal,
                          color: Color.fromARGB(255, 205, 143, 121),
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 265),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  child: Align(
                    alignment: Alignment.bottomRight,
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
      ),
        ],
      ),
        bottomNavigationBar:
            CustomTabBar(currentIndex: currentIndex, onTabTapped: _onTabtapped),
      ),
    );
  }

}