import 'dart:convert';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/add_devices_and_delete/no_device_page.dart';
import 'package:home_auto_sample/scheduler/scheduler_page.dart';
import 'package:home_auto_sample/scheduler/setup_time.dart';
import 'package:home_auto_sample/service/database_service.dart';
import 'package:home_auto_sample/settings/settings_1_page.dart';
import 'package:home_auto_sample/tabBar/custom_appbar.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SetupRoutinePage extends StatefulWidget {
  const SetupRoutinePage({super.key});

  @override
  State<SetupRoutinePage> createState() => _SetupRoutinePageState();
}

class _SetupRoutinePageState extends State<SetupRoutinePage> {
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
            padding: const EdgeInsets.only(top: 6.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Padding(
                  padding: EdgeInsets.only(left: 10),
                  child: Text(
                    'Set Up a Routine',
                    style: TextStyle(
                      fontSize: 25,
                    ),
                  ),
                ),
                const SizedBox(height: 20),
                const Divider(
                  height: 2,
                  color: Colors.black,
                ),
                const SizedBox(
                  height: 30,
                ),
                Padding(
                  padding: const EdgeInsets.only(left: 10,right: 10),
                  child: Card(
                        color: Colors.grey[200], 
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: ListTile(
                          leading: Image.asset(
                            'assets/icon/clock.png', 
                            width: 40,
                            height: 40,
                          ),
                          title: const Text(
                            'Time',
                            style: TextStyle(fontSize: 20),
                          ),
                          trailing: Icon(
                            Icons.play_arrow_rounded,
                            color: Colors.grey[600], // Grey color
                            size: 35,
                          ),
                          onTap: () {
                            Navigator.of(context).push(
                                MaterialPageRoute(
                                  builder: (context) => const SetupTime(),
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
);
}
}
