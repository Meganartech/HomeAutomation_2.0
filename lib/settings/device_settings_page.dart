import 'dart:convert';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/login&signup/sign_up_page.dart';
import 'package:home_auto_sample/profile/profile_page.dart';
import 'package:home_auto_sample/service/database_service.dart';
import 'package:home_auto_sample/settings/settings_1_page.dart';
import 'package:home_auto_sample/settings/themeSettings.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../add_devices_and_delete/no_device_page.dart';
import '../scheduler/scheduler_page.dart';
import '../tabBar/custom_appbar.dart';

class DeviceSettingsPage extends StatefulWidget {

  const DeviceSettingsPage({ super.key});

  @override
  State<DeviceSettingsPage> createState() => _DeviceSettingsPageState();
}

class _DeviceSettingsPageState extends State<DeviceSettingsPage> {
  int currentIndex = 2;
  List<Device> _devices = [];
  bool isNotificationEnabled = false;
  final DatabaseReference dbRef = FirebaseDatabase.instance.ref();


  final String userId = FirebaseAuth.instance.currentUser!.uid; 

  
  @override
  void initState(){
 super.initState();
 _fetchDevices();
 fetchNotificationStatus();
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
                  'assets/images/home_14.8.png', // Replace with your image path
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
                            'Device Settings',
                            style: TextStyle(color: Colors.white, fontSize: 18),
                          ),
                        ),
                      ],
                    ),
                    SizedBox(height: MediaQuery.sizeOf(context).height * 0.05),
                    _buildSettingsOption(
                      title: 'Theme Settings',
                      onTap: () => showThemeDialog(context),
                    ),
                   Container(
        padding:const EdgeInsets.symmetric( horizontal: 10),
        decoration: BoxDecoration(
            color:const Color.fromARGB(255, 248, 244, 244),
            border: Border(bottom: BorderSide(color: Colors.grey.shade700))),
        child: Row(
          children: [
            SizedBox(
              width: MediaQuery.sizeOf(context).width * 0.13,
            ),
          const  Text(
              'Manage Notification',
              style:TextStyle(
                color: Colors.black87,
                fontSize: 16,
                fontWeight: FontWeight.w400,
              ),
            ),
            //const Spacer(),
            SizedBox(width: MediaQuery.sizeOf(context).width * 0.2,),
            Transform.scale(
              scale: 0.9,
              child: Switch(
                activeColor: Colors.white,
                activeTrackColor: Colors.lightBlueAccent,
                inactiveTrackColor: Colors.white,
                value: isNotificationEnabled, onChanged: (bool value){
                setState(() {
                  isNotificationEnabled = value;
                });
                updateNotificationStatus(value);
              }),
            ),
          ],
        ),
      ),
                    _buildSettingsOption(
                      title: 'Update Device Name',
                      onTap: () => Navigator.push(
                        context,
                        MaterialPageRoute(builder: (context) => const ProfilePage()),
                      ),
                    ),
                    _buildSettingsOption(
                      title: 'Delete Account',
                      onTap: () => showDeleteAccountDialog(context),                    ),
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

  Widget _buildSettingsOption(
      {required String title, required VoidCallback onTap}) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding:const EdgeInsets.symmetric(vertical: 10, horizontal: 10),
        decoration: BoxDecoration(
            color:const Color.fromARGB(255, 248, 244, 244),
            border: Border(bottom: BorderSide(color: Colors.grey.shade700))),
        child: Row(
          children: [
            SizedBox(
              width: MediaQuery.sizeOf(context).width * 0.13,
            ),
            Text(
              title,
              style:const TextStyle(
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

Future<void> fetchNotificationStatus() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  String? espDeviceJson = prefs.getString('EspDevice');

  Map<String, dynamic> espDeviceMap = jsonDecode(espDeviceJson!);

  if (espDeviceMap.containsKey(userId) && espDeviceMap[userId].containsKey("notification")) {
    String savedStatus = espDeviceMap[userId]["notification"];
    
    setState(() {
      isNotificationEnabled = (savedStatus == "on");
    });

    print("Fetched notification status: $savedStatus");
    return;
  }

  // Default if not found
  setState(() {
    isNotificationEnabled = false;
  });

  print("Notification status not found. Defaulting to OFF.");
}


// Function to update Firebase & SharedPreferences when the switch is clicked
Future<void> updateNotificationStatus(bool value) async {
  String statusText = value ? "on" : "off";

  setState(() {
    isNotificationEnabled = value; // Instantly update UI
  });

  // Save to Firebase
  await dbRef.child("users/$userId/Settings/Notification").set(statusText);

  // Save to SharedPreferences
  SharedPreferences prefs = await SharedPreferences.getInstance();
  await prefs.setString('notification_status', statusText);

  // Also store inside EspDevice -> uid -> notification
  String? espDeviceJson = prefs.getString('EspDevice');
  Map<String, dynamic> espDeviceMap = espDeviceJson != null ? jsonDecode(espDeviceJson) : {};

  if (!espDeviceMap.containsKey(userId)) {
    espDeviceMap[userId] = {};
  }

  espDeviceMap[userId]["notification"] = statusText;
  await prefs.setString('EspDevice', jsonEncode(espDeviceMap));

  print("Updated Firebase & SharedPreferences: Notification -> $statusText");
}

  void showDeleteAccountDialog(BuildContext context) {
  showDialog(
    context: context,
    builder: (BuildContext context) {
      return AlertDialog(
        title: const Text('Delete Account'),
        content: const Text('Do you really want to Delete Account?'),
        actions: <Widget>[
          TextButton.icon(
             icon:const Icon(Icons.close),
            onPressed: () {
              Navigator.of(context).pop(); // Dismiss the dialog
            },
            label: const Text(
              'Cancel',
              style: TextStyle(color: Colors.blue), // Customize the color
            ),
          ),
          ElevatedButton.icon(
            onPressed: () async{
                try{
                  final user = FirebaseAuth.instance.currentUser!;
                  final userId = user.uid;

                   await FirebaseDatabase.instance.ref('users/$userId').remove();
                   await user.delete();
                   ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
                  content: Text("Account Deleted successfully"),
                ));
                   Navigator.push(context, MaterialPageRoute(builder: (context)=>const SignupPage()));
                                    
                 
                }catch(e){
                  print('Error in deletion:$e');
                }
             // Dismiss the dialog
             
            },
            icon: const Icon(Icons.delete, color: Colors.white), // Icon inside button
            label: const Text('Delete',style: TextStyle(color:Colors.white),),
            style: ElevatedButton.styleFrom(
              shape:RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)) ,
              backgroundColor: const Color.fromARGB(255, 132, 186, 230), // Customize the delete button color
            ),
          ),
        ],
      );
    },
  );
}

void showThemeDialog(BuildContext context) {
  final themeNotifier = Provider.of<ThemeNotifier>(context, listen: false);

  showDialog(
    context: context,
    builder: (BuildContext context) {
      return AlertDialog(
        title: const Text('Choose Theme'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              title: const Text('Light Theme'),
              leading: Radio(
                value: ThemeMode.light,
                groupValue: themeNotifier.themeMode,
                onChanged: (ThemeMode? mode) {
                  themeNotifier.setTheme(mode!); // Set light theme
                  Navigator.pop(context);
                },
              ),
            ),
            ListTile(
              title: const Text('Dark Theme'),
              leading: Radio(
                value: ThemeMode.dark,
                groupValue: themeNotifier.themeMode,
                onChanged: (ThemeMode? mode) {
                  themeNotifier.setTheme(mode!); // Set custom black theme
                  Navigator.pop(context);
                },
              ),
            ),
            ListTile(
              title: const Text('System Default'),
              leading: Radio(
                value: ThemeMode.system,
                groupValue: themeNotifier.themeMode,
                onChanged: (ThemeMode? mode) {
                  themeNotifier.setTheme(mode!); // Set system default theme
                  Navigator.pop(context);
                },
              ),
            ),
          ],
        ),
      );
    },
  );
}
}
