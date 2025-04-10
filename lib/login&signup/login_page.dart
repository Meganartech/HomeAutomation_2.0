
import 'dart:convert';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/add_devices_and_delete/no_device_page.dart';
import 'package:home_auto_sample/authentication/firebase_auth.dart';
import 'package:home_auto_sample/components/mytextfield.dart';

import 'package:home_auto_sample/login&signup/sign_up_page.dart';
import 'package:home_auto_sample/service/database_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<StatefulWidget> createState() {
    return _LoginPageState();
  }
}

class _LoginPageState extends State<LoginPage> {
  final _formKey = GlobalKey<FormState>();
  final AuthManager _auth = AuthManager(); // Renamed to AuthManager

  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }
  

  @override
  Widget build(BuildContext context) {
    return  Scaffold(
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
        
        body: SingleChildScrollView(
          child: SizedBox(
            height: MediaQuery.of(context).size.height -
                AppBar().preferredSize.height,
            child: Stack(
              //fit: StackFit.expand,
              children: [
                // Background Image
          
                Positioned.fill(
                  top: MediaQuery.sizeOf(context).height * 0.20,
                  left: 0,
                  right: 0,
                  bottom: 0,
                  //preferredSize: Size.fromWidth(MediaQuery.of(context).size.width),
                  // width: MediaQuery.of(context).size.width,
                  // height: MediaQuery.of(context).size.height,
                  child: Image.asset(
                    'assets/images/home_3.png',
                    fit: BoxFit.fill, // Change this to your desired fit
                  ),
                ),
                Positioned(
                  top: 0,
                  left: 0,
                  right: MediaQuery.sizeOf(context).width * 0.4,
                  bottom: MediaQuery.sizeOf(context).height * 0.8,
                  child: Image.asset(
                    'assets/icon/login.png',
          
                    fit: BoxFit.contain, // Change this to your desired fit
                  ),
                ),
                Padding(
                    padding:
                        const EdgeInsets.only(left: 10, top: 42, bottom: 2),
                    child: Image.asset(
                      'assets/icon/User.png',
                      color: Colors.white,
                    )),
                const Padding(
                  padding: EdgeInsets.only(left: 45, top: 43, bottom: 2),
                  child: Text(
                    'Login',
                    style: TextStyle(color: Colors.white, fontSize: 18),
                  ),
                ),
                // Login Form
          
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 40),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    // crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Form(
                        key: _formKey,
                        child: Column(
                          crossAxisAlignment:
                              CrossAxisAlignment.start, // Stretch form fields
                          children: [
                            const Padding(
                              padding: EdgeInsets.symmetric(horizontal: 20),
                              child: Text(
                                'Email',
                                style: TextStyle(
                                    color: Colors.black, fontSize: 14),
                              ),
                            ),
                            Padding(
                              padding:
                                  const EdgeInsets.symmetric(horizontal: 20),
                              child: MyTextField(
                                controller: _emailController,
                                validator: (value) {
                                  return value!.isEmpty
                                      ? 'Please enter your email'
                                      : null;
                                },
                                inputType: TextInputType.emailAddress,
                                obscureText: false,
                                inputAction: TextInputAction.next,
                              ),
                            ),
                            const SizedBox(height: 10), 
                            const Padding(
                              padding:
                                  EdgeInsets.symmetric(horizontal: 20),
                              child: Text(
                                'Password',
                                style: TextStyle(
                                    color: Colors.black, fontSize: 14),
                              ),
                            ),
                            // Password Field
                            Padding(
                              padding:
                                  const EdgeInsets.symmetric(horizontal: 20),
                              child: MyTextField(
                                controller: _passwordController,
                                validator: (value) {
                                  return value!.isEmpty
                                      ? 'Please enter your password'
                                      : null;
                                },
                                inputType: TextInputType.visiblePassword,
                                obscureText: true,
                                inputAction: TextInputAction.done,
                              ),
                            ),
                            const SizedBox(height: 10), // Added for spacing
                            // Login Button
                            _buildLoginButton(context),
                            const SizedBox(height: 20),
                            _buildDontHaveAccount(context),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                //),
                // ),
              ],
            ),
          ),
        ),
      );
    
  }

  Widget _buildLoginButton(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 10, right: 100, left: 80),
      child: Align(
        alignment: Alignment.bottomCenter,
        child: ElevatedButton(
          style: ElevatedButton.styleFrom(
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
            // minimumSize: Size(double.infinity, 40),
            padding: EdgeInsets.symmetric(
                horizontal: MediaQuery.sizeOf(context).height * 0.02,
                vertical: MediaQuery.sizeOf(context).width * 0.02),
            backgroundColor: const Color.fromRGBO(182, 176, 236, 1),
            foregroundColor: Colors.white,
          ),
          onPressed: () async {
            if (_formKey.currentState!.validate()) {
              // Attempt to log in
              await _login(context);
            }
          },
          child: const Text(
            'Login',
            style: TextStyle(fontSize: 15),
          ),
        ),
      ),
    );
  }

  Widget _buildDontHaveAccount(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        const Text("Don't have an account?",style: TextStyle(color:Colors.black)),
        const SizedBox(width: 10),
        GestureDetector(
          onTap: () {
            Navigator.pushReplacement(context,
                MaterialPageRoute(builder: (context) => const SignupPage()));
          },
          child: const Text(
            "Sign Up",
            style: TextStyle(
              color: Color.fromRGBO(151, 121, 234, 1),
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
      ],
    );
  }

Future<List<Device>> fetchDevicesFromFirebase() async {
  User? currentUser = FirebaseAuth.instance.currentUser;
  if (currentUser == null) return [];

  DatabaseReference ref = FirebaseDatabase.instance
      .ref()
      .child("users")
      .child(currentUser.uid)
      .child("Espdevice");

  DatabaseEvent event = await ref.once();
  DataSnapshot snapshot = event.snapshot;

  if (snapshot.value != null && snapshot.value is Map) {
    Map<String, dynamic> devicesMap = Map<String, dynamic>.from(snapshot.value as Map);
    return devicesMap.entries.map((entry) {
      return Device(
  deviceId: entry.key,
  deviceName: (entry.value["editname"]?.isNotEmpty == true) 
      ? entry.value["editname"] 
      : entry.value["name"] ?? "Unknown Device",
);
    }).toList();
  }
  return [];
}

Future<void> updateSharedPreferencesFromFirebase() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  User? currentUser = FirebaseAuth.instance.currentUser;
  if (currentUser == null) return;

  String uid = currentUser.uid;

  String? existingData = prefs.getString('EspDevice');
  if (existingData?.isNotEmpty == true) {
    print("SharedPreferences already contains data. Skipping update.");
    return;
  }

  DatabaseReference ref = FirebaseDatabase.instance.ref()
      .child("users")
      .child(uid)
      .child("Espdevice");

  DatabaseEvent event = await ref.once();
  if (event.snapshot.value == null) return;

  Map<String, dynamic> espDeviceMap = {};
  espDeviceMap[uid] = {}; 

  Map<dynamic, dynamic> firebaseData = event.snapshot.value as Map<dynamic, dynamic>;

  firebaseData.forEach((deviceId, deviceData) {
    Map<String, dynamic> deviceEntry = {
      "name": deviceData["name"] ?? "Unknown Device",
      "editname": deviceData["editname"] ?? "",
    };
    if (deviceData["ports"] != null && deviceData["ports"] is Map) {
      deviceEntry["ports"] = Map<String, dynamic>.from(deviceData["ports"]);
    }

    espDeviceMap[uid][deviceId] = deviceEntry;
  });

  DatabaseReference notificationRef = FirebaseDatabase.instance.ref()
      .child("users")
      .child(uid)
      .child("Settings")
      .child("Notification");

  DatabaseEvent notificationEvent = await notificationRef.once();
  if (notificationEvent.snapshot.value != null) {
    String notificationStatus = notificationEvent.snapshot.value.toString(); // "on" or "off"
    espDeviceMap[uid]["notification"] = notificationStatus;
  } else {
    espDeviceMap[uid]["notification"] = "off"; // Default to "off" if not found
  }

  await prefs.setString('EspDevice', jsonEncode(espDeviceMap));
  print("Updated SharedPreferences from Firebase: $espDeviceMap");
}

Future<bool> _hasDevice(String uid) async{
  await updateSharedPreferencesFromFirebase();
    var devices = await fetchDevicesFromFirebase();
    return devices.isNotEmpty;
  }

  Future<void> _login(BuildContext context) async {
  String email = _emailController.text.trim();
  String password = _passwordController.text;

  try {
    User? user = await _auth.logInWithEmailAndPassword(email, password);

    if (user != null) {
      debugPrint('Logged in user ID: ${user.uid}');
      try {
         bool hasDevice = await _hasDevice(user.uid);

        if (!hasDevice) {
          Navigator.pushAndRemoveUntil(
            context,
            MaterialPageRoute(builder: (context) => const NoDevicePage()),
            (route) => false,
          );
        } else {
          Navigator.pushAndRemoveUntil(
            context,
            MaterialPageRoute(builder: (context) => const AvailableDevicesPage()),
            (route) => false,
          );
        }
      } catch (deviceError) {
        _showErrorSnackbar(context,"Failed to retrieve device data: $deviceError");
      }
    } else {
      try {
        throw FirebaseAuthException(
          code: 'unexpected-error',
          message: "User is null. Authentication failed unexpectedly.",
        );
      } on FirebaseAuthException catch (e) {
        switch (e.code) {
          case 'wrong-password':
            _showErrorSnackbar(context,"The password you entered is incorrect.");
            break;
          case 'user-not-found':
            _showErrorSnackbar(context,"No account found for the provided email.");
            break;
          case 'invalid-email':
            _showErrorSnackbar(context,"Please enter a valid email address.");
            break;
          case 'user-disabled':
            _showErrorSnackbar(context,"This account has been disabled. Please contact support.");
            break;
          default:
            _showErrorSnackbar(context, e.message ?? "An unexpected error occurred.");
        }
      }
    }
  } catch (e) {
    // Handle non-Firebase-related unexpected errors
    _showErrorSnackbar(context, "An unexpected error occurred: $e");
  }
}

void _showErrorSnackbar(BuildContext context, String message) {
  debugPrint("Displaying Snackbar: $message");
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(
      content: Text(
        message,
        style: const TextStyle(color: Colors.white, fontSize: 16.0),
      ),
      backgroundColor: Colors.red,
      behavior: SnackBarBehavior.floating,
      duration: const Duration(seconds: 3),
    ),
  );
}


  Future<bool> _isFirstLogin(String userId) async {
    SharedPreferences prefs = await SharedPreferences.getInstance();

    bool isFirstLogin = prefs.getBool('firstLogin_$userId') ?? true;
    if (isFirstLogin) {
      await prefs.setBool('firstLogin_$userId', false);
    }
    return isFirstLogin;
  }
}
