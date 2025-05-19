
//import 'dart:convert';
import 'package:firebase_auth/firebase_auth.dart';
// import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'dart:io';

import 'package:openhab_testing/controls/controls.dart';
// import 'package:shared_preferences/shared_preferences.dart';

class NoDevicePage extends StatefulWidget {
  const NoDevicePage({super.key});

  @override
  State<NoDevicePage> createState() => _NoDevicePageState();
}

class _NoDevicePageState extends State<NoDevicePage> {
  int currentIndex = 0;

  User? getCurrentUser() {
    return FirebaseAuth.instance.currentUser;
  }

  void _onTabtapped(int index) {
    setState(() {
      currentIndex = index;
    });
    switch (index) {
      case 0:
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
     final theme = Theme.of(context);
    final isDarkMode = theme.brightness == Brightness.dark; 
    final textColor = isDarkMode ? Colors.white : Colors.black;  
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
                  padding: EdgeInsets.only(left: 5),
                  child: Text(
                    'Welcome',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 18,
                    ),
                  ),
                ),
                const SizedBox(height: 10),
                Divider(height: 2, color: theme.dividerColor),
                const SizedBox(height: 10),
                const Center(
                  child: Stack(
                    alignment: Alignment.center,
                    children: [
                      ImageIcon(
                        AssetImage('assets/images/home_14.6.png'),
                        size: 50,
                        color: Colors.red,
                      ),
                      CircleAvatar(
                        radius: 20, 
                        backgroundImage: AssetImage('assets/images/home_14.5.png'),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 10),
                Padding(
                  padding: const EdgeInsets.only(left: 5.0),
                  child: Text(
                    'Oops! No devices found',
                    style: TextStyle(
                      color: textColor,
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                ),
                const SizedBox(height: 5),
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
                        'assets/images/home_14.4.png',
                      ),
                    ),
                    const Positioned(
                      top: 180,
                      left: 180,
                      bottom: 0,
                      child: Text(
                        "Click add to connect\n"
                        "devices\n",
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
                const SizedBox(height: 230),
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
                          onPressed: () async{
                            //  await _storeDeviceName("b8:d6:1a:41:00:bc", "ESP2");
                            // _storeDeviceToFirebase("b8:d6:1a:41:00:bc","ESP2");
                            // await clearSharedPreferences();
                            // await printSharedPreferences();
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (context) => WizBulbControl(),
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
        
      ),
    );
  }

//   Future<void> clearSharedPreferences() async {
//   final SharedPreferences prefs = await SharedPreferences.getInstance();
//   await prefs.clear();
//   print("All SharedPreferences data cleared!");
// }

// Future<void> _storeDeviceName(String deviceId, String name) async {
//   SharedPreferences prefs = await SharedPreferences.getInstance();
//   String? uid = FirebaseAuth.instance.currentUser?.uid;

//   if (uid != null) {
//     String? espDeviceJson = prefs.getString('EspDevice');
//     Map<String, dynamic> espDeviceMap = espDeviceJson != null
//         ? jsonDecode(espDeviceJson)
//         : {}; // Initialize if empty

//     if (!espDeviceMap.containsKey(uid)) {
//       espDeviceMap[uid] = {}; // Create user entry if not present
//     }

//     espDeviceMap[uid][deviceId] = {
//       "name": name,
//       "editname": "" // Store editname as an empty string initially
//     };

//     await prefs.setString('EspDevice', jsonEncode(espDeviceMap));
//     print("Device stored successfully: $name for UID: $uid and Device ID: $deviceId");
//   } else {
//     print("No user logged in. Cannot store device name.");
//   }
// }

// Future<void> _storeDeviceToFirebase(String deviceId, String name) async {
//   try {
//     String? uid = FirebaseAuth.instance.currentUser?.uid;
//     DatabaseReference dbRef = FirebaseDatabase.instance.ref("users/$uid/Espdevice/$deviceId");

//     // Store device data in Firebase
//     await dbRef.set({
//       "name": name,
//       "editname": "",
//     });

//     DatabaseReference ref = FirebaseDatabase.instance.ref("users/$uid/Wifi");

//     // Store device data in Firebase
//     await ref.set({
//       "ssid": "Miyav",
//       "password": "Meena0503",
//     });

//     print("Device stored in Firebase: users/$uid/devices/$deviceId");
//   } catch (e) {
//     print("Error storing device to Firebase: $e");
//   }
// }


// Future<void> printSharedPreferences() async {
//   SharedPreferences prefs = await SharedPreferences.getInstance();
//   Map<String, dynamic> allPrefs = prefs.getKeys().fold({}, (map, key) {
//     map[key] = prefs.get(key);
//     return map;
//   });

//   print("Shared Preferences Data:\n$allPrefs");
// }

}