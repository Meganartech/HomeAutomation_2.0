import 'dart:convert';
import 'dart:io';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/login&signup/login_page.dart';

import 'package:home_auto_sample/profile/profile_page.dart';
import 'package:home_auto_sample/service/database_service.dart';
import 'package:home_auto_sample/settings/device_settings_page.dart';
import 'package:home_auto_sample/settings/faq_report_page.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../add_devices_and_delete/no_device_page.dart';
import '../scheduler/scheduler_page.dart';
import '../tabBar/custom_appbar.dart';

class Settings1Page extends StatefulWidget {

  const Settings1Page({super.key});

  @override
  State<Settings1Page> createState() => _Settings1PageState();
}

class _Settings1PageState extends State<Settings1Page> {
  int currentIndex = 2;
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
                  //left: 200,
                  child: Image.asset(
                    'assets/images/home_14.8.png', 
                    // fit: BoxFit, // Adjust the image to fill the space
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
                      const Padding(
                        padding: EdgeInsets.only(left: 5),
                        child: Text(
                          'Welcome',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 18,
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
                      const Center(
                        child: Stack(
                          alignment: Alignment.center,
                          children: [
                            ImageIcon(
                              AssetImage(
                                'assets/images/home_14.6.png',
                              ),
                              size: 50,
                              color: Colors.red,
                            ),
                            CircleAvatar(
                              radius: 20, // Adjust size as needed
                              backgroundImage:
                                  AssetImage('assets/images/home_14.5.png'),
                            ),
                          ],
                        ),
                      ),
                      // const SizedBox(
                      //   height: 5,
                      // ),
                      Stack(
                        children: [
                          Image.asset(
                            'assets/images/Rectangle.png',
                            height: MediaQuery.sizeOf(context).height * 0.08,
                            // width: MediaQuery.sizeOf(context).width * 0.09,
                          ),
                          const Padding(
                            padding:
                                EdgeInsets.only(left: 10, top: 30, bottom: 2),
                            child: Text(
                              'Settings',
                              style: TextStyle(color: Colors.white, fontSize: 18),
                            ),
                          ),
                        ],
                      ),
                      SizedBox(height: MediaQuery.sizeOf(context).height * 0.05),
                      _buildSettingsOption(
                        image: 'assets/icon/MaleUser.png',
                        title: 'Profile',
                        onTap: () => Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => const ProfilePage()),
                        ),
                      ),
                      _buildSettingsOption(
                        image: 'assets/icon/setting.png',
                        title: 'Device Settings',
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                                builder: (context) => const DeviceSettingsPage()),
                          );
                        },
                      ),
                      _buildSettingsOption(
                        image: 'assets/icon/message.png',
                        title: 'Messsage Center',
                        onTap: () => Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => const ProfilePage()),
                        ),
                      ),
                      _buildSettingsOption(
                        image: 'assets/icon/AskQuestion.png',
                        title: 'FAQ & Feedback',
                        onTap: () => Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => const FaqReportPage()),
                        ),
                      ),
                      _buildSettingsOption(
                        image: 'assets/icon/logout.png',
                        title: 'Logout',
                        onTap: () => showLogoutDialog(context)
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

  Widget _buildSettingsOption(
      {required String image,
      required String title,
      required VoidCallback onTap}) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 10),
        decoration: BoxDecoration(
            color: const Color.fromARGB(255, 248, 244, 244),
            border: Border(bottom: BorderSide(color: Colors.grey.shade700))),
        child: Row(
          children: [
            const SizedBox(
              width: 4,
            ),
            Image.asset(
              image,
              color: Colors.black87,
            ),
            const SizedBox(width: 15),
            Text(
              title,
              style: const TextStyle(
                color: Colors.black87,
                fontSize: 16,
                fontWeight: FontWeight.w400,
              ),
            ),
            const Spacer(),
          ],
        ),
      ),
    );
  }

  void showLogoutDialog(BuildContext context) {
  showDialog(
    context: context,
    builder: (BuildContext context) {
      return AlertDialog(
        title: const Text(
          'Logout',
          textAlign: TextAlign.center,
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        content: const Text('Do you want to Logout?'),
        actions: <Widget>[
          TextButton.icon(
            icon: const Icon(Icons.close, color: Colors.blue, size: 25),
            onPressed: () {
              Navigator.of(context).pop(); // Dismiss the dialog
            },
            label: const Text(
              'Cancel',
              style: TextStyle(color: Colors.blue), // Customize the color
            ),
          ),
          ElevatedButton.icon(
            onPressed: () async {
              try {
                await FirebaseAuth.instance.signOut(); // Sign out from Firebase
                Navigator.pushAndRemoveUntil(
                  context,
                  MaterialPageRoute(builder: (context) => const LoginPage()),
                  (route) => false, // Clears the navigation stack
                );
              } catch (e) {
                // Show error message if logout fails
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Logout failed: $e')),
                );
              }
            },
            icon: const Icon(Icons.logout, color: Colors.white), // Icon inside button
            label: const Text(
              'Logout',
              style: TextStyle(color: Colors.white),
            ),
            style: ElevatedButton.styleFrom(
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8)),
              backgroundColor:
                  const Color.fromARGB(255, 132, 186, 230), // Customize the delete button color
            ),
          ),
        ],
      );
    },
  );
}
}

// void main() {
//   runApp(MaterialApp(
//     home: Settings1Page(),
//     debugShowCheckedModeBanner: false,
//   ));
// }
