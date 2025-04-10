import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/components/mytextfield.dart';
import 'package:home_auto_sample/service/database_service.dart';
import 'package:home_auto_sample/settings/settings_1_page.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../add_devices_and_delete/no_device_page.dart';
import '../scheduler/scheduler_page.dart';
import '../tabBar/custom_appbar.dart';

class ChangePasswordPage extends StatefulWidget {
  const ChangePasswordPage({super.key});

  @override
  _ChangePasswordPageState createState() => _ChangePasswordPageState();
}

class _ChangePasswordPageState extends State<ChangePasswordPage> {
  int currentIndex = 2;
  List<Device> _devices = [];
  final FirebaseAuth _auth = FirebaseAuth.instance;
  final TextEditingController _currentPasswordController =
      TextEditingController();
  final TextEditingController _newPasswordController = TextEditingController();
  final TextEditingController _confirmPasswordController =
      TextEditingController();
  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();
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

  @override
  void dispose() {
    _currentPasswordController.dispose();
    _newPasswordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
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

  Future<void> _updatePassword() async {
    final user = _auth.currentUser;

    if (user == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('No user is currently logged in.'),
          backgroundColor: Colors.red,
        ),
      );
      return; // Exit if no user is logged in
    }

    final String currentPassword = _currentPasswordController.text.trim();
    final String newPassword = _newPasswordController.text.trim();
    final String confirmPassword = _confirmPasswordController.text.trim();

    if (currentPassword.isNotEmpty &&
        newPassword.isNotEmpty &&
        confirmPassword.isNotEmpty) {
      try {
        // Check if the email is not null
        if (user.email != null) {
          // Reauthenticate the user with the current password
          final cred = EmailAuthProvider.credential(
            email: user.email!,
            password: currentPassword,
          );
          await user.reauthenticateWithCredential(cred);

          // If reauthentication is successful, update the password
          await user.updatePassword(newPassword);

          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Password successfully updated.'),
              backgroundColor: Colors.green,
            ),
          );
        } else {
          // Handle if email is null
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Failed to update password. User email is null.'),
              backgroundColor: Colors.red,
            ),
          );
          print('Failed to update password. User email is null.');
        }
      } catch (e) {
        // Handle the exception with more context
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to update password. Error: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } else {
      // Fields are empty
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please fill required fields.'),
          backgroundColor: Colors.red,
        ),
      );
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
                //textDirection: TextDirection.ltr,
                //right: 0.0,

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
                child: Form(
                  key: _formKey,
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
                      const Divider(
                        height: 2,
                        color: Colors.black,
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
                              'Change Password',
                              style:
                                  TextStyle(color: Colors.white, fontSize: 18),
                            ),
                          ),
                        ],
                      ),
                      const Padding(
                        padding: EdgeInsets.only(left: 25, top: 15),
                        child: Text(
                          'Current Password',
                          style: TextStyle(color: Colors.black, fontSize: 15),
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.only(left: 25, top: 0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            // Row(
                            //   children: [
                            MyTextField(
                                controller: _currentPasswordController,
                                inputType: TextInputType.visiblePassword,
                                inputAction: TextInputAction.next,
                                obscureText: true),
                            SizedBox(
                              width: MediaQuery.sizeOf(context).width * 0.05,
                            ),

                            //  ],
                            //),
                            const Padding(
                              padding: EdgeInsets.only(top: 10),
                              child: Text(
                                'New Password',
                                style: TextStyle(
                                    color: Colors.black, fontSize: 15),
                              ),
                            ),
                            // Row(
                            //   children: [
                            MyTextField(
                                controller: _newPasswordController,
                                inputType: TextInputType.visiblePassword,
                                inputAction: TextInputAction.next,
                                obscureText: true),
                            SizedBox(
                              width: MediaQuery.sizeOf(context).width * 0.05,
                            ),
                            const Padding(
                              padding: EdgeInsets.only(top: 10),
                              child: Text(
                                'Confirm Password',
                                style: TextStyle(
                                    color: Colors.black, fontSize: 15),
                              ),
                            ),
                            // Row(
                            //   children: [
                            MyTextField(
                                controller: _confirmPasswordController,
                                inputType: TextInputType.visiblePassword,
                                inputAction: TextInputAction.done,
                                obscureText: true),
                            SizedBox(
                              width: MediaQuery.sizeOf(context).width * 0.05,
                            ),

                            //   ],
                            // ),
                            // TextFormField(
                            //   controller: _emailController,
                            //   decoration:
                            //       const InputDecoration(labelText: 'Email'),
                            // ),
                            const SizedBox(height: 20),
                            Padding(
                              padding: const EdgeInsets.only(top: 50, right: 50),
                              child: Align(
                                alignment: Alignment.bottomRight,
                                child: ElevatedButton(
                                  style: ElevatedButton.styleFrom(
                                    shape: RoundedRectangleBorder(
                                        borderRadius:
                                            BorderRadius.circular(10)),
                                    // minimumSize: Size(double.infinity, 40),
                                    padding: EdgeInsets.symmetric(
                                        horizontal:
                                            MediaQuery.sizeOf(context).height *
                                                0.02,
                                        vertical: 10),
                                    backgroundColor:
                                        const Color.fromARGB(255, 68, 154, 225),
                                    foregroundColor: Colors.white,
                                  ),
                                  onPressed: () {
                                    if (_formKey.currentState!.validate()) {
                                      _updatePassword();
                                    }
                                  },
                                  child: const Text(
                                    'Submit',
                                    style: TextStyle(fontSize: 15),
                                  ),
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
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
