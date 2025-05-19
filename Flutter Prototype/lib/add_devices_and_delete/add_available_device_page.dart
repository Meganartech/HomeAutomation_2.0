import 'dart:convert';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:openhab_testing/add_devices_and_delete/device_added_page.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AddAvailableDevicePage extends StatefulWidget {
  final dynamic device;
  final List<BluetoothService> services;
  const AddAvailableDevicePage({super.key, required this.device,required this.services,});

  @override
  State<AddAvailableDevicePage> createState() => _AddAvailableDevicePageState();
}

class _AddAvailableDevicePageState extends State<AddAvailableDevicePage> {
  final TextEditingController _ssidController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  BluetoothDevice? device;
  final _formKey = GlobalKey<FormState>();
  
   @override
  void initState() {
    super.initState();
    device = widget.device as BluetoothDevice?;  
  }

   User? getCurrentUser() {
    return FirebaseAuth.instance.currentUser;
  }

  Future<String> fetchFirebasePassword() async {
    try {
      User? currentUser = getCurrentUser();
      if (currentUser == null) {
        throw Exception("User is not logged in.");
      }

      DatabaseReference userRef = FirebaseDatabase.instance.ref("users/${currentUser.uid}/password");
      DataSnapshot snapshot = await userRef.get();

      if (snapshot.exists) {
        return snapshot.value.toString();
      } else {
        throw Exception("Firebase password not found in the database.");
      }
    } catch (e) {
      throw Exception("Error fetching Firebase password: $e");
    }
  }

Future<void> sendCredentialsToESP(String ssid, String password) async {
  try {
    if (device == null) {
      throw Exception("Device is not available.");
    }

    // Ensure the device is connected
    if (!device!.isConnected) {
      await device!.connect();
    }

    // Discover the services of the device
    List<BluetoothService> services = await device!.discoverServices();
    BluetoothService? wifiService;
    BluetoothCharacteristic? wifiCharacteristic;

    // Iterate through services to find the one with a valid characteristic
    for (var service in services) {
      for (var characteristic in service.characteristics) {
        if (characteristic.properties.write || characteristic.properties.writeWithoutResponse) {
          wifiService = service;
          wifiCharacteristic = characteristic;
          break;
        }
      }
      if (wifiCharacteristic != null) break; // Stop when we find the first writable characteristic
    }

    // If no valid service or characteristic is found, throw an exception
    if (wifiService == null || wifiCharacteristic == null) {
      throw Exception("Wi-Fi service or characteristic not found.");
    }


    // Get current user details
    User? currentUser = getCurrentUser();
    if (currentUser == null) {
      throw Exception("User is not logged in.");
    }

    // Prepare the credentials to send to the ESP device
    final credentials = jsonEncode({"ssid": ssid, "pass": password});

    // Write credentials to the characteristic
    await wifiCharacteristic.write(credentials.codeUnits);
    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Credentials sent to ESP.")));

    // Wait for ESP to process the credentials
    await Future.delayed(const Duration(seconds: 60));

    // Read the response from the ESP device
    List<int> response = await wifiCharacteristic.read();
    String receivedData = utf8.decode(response);
    print("Received from Wiz bulb: $receivedData");

    // If response is valid, update Firebase and SharedPreferences
    if (receivedData.isNotEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Received: $receivedData")));

      // Update Firebase with Wi-Fi credentials
      await FirebaseDatabase.instance.ref()
          .child("users")
          .child(currentUser.uid)
          .child("Wifi")
          .set({
        "ssid": ssid,
        "password": password,
      });

      // Store ESP device info in Firebase
      await FirebaseDatabase.instance.ref()
          .child("users")
          .child(currentUser.uid)
          .child("Espdevice")
          .child(device!.id.toString())
          .set({
        "name": device!.name,
        "editname": "",
      });

      // Update SharedPreferences with ESP device info
      SharedPreferences prefs = await SharedPreferences.getInstance();
      String? existingData = prefs.getString('EspDevice');
      Map<String, dynamic> espDeviceMap = existingData != null ? jsonDecode(existingData) : {};

      String uid = currentUser.uid;
      String deviceId = device!.id.toString();
      String deviceName = device!.name;

      if (!espDeviceMap.containsKey(uid)) {
        espDeviceMap[uid] = {};
      }
      espDeviceMap[uid][deviceId] = {
        "name": deviceName,
        "editname": ""
      };

      await prefs.setString('EspDevice', jsonEncode(espDeviceMap));
      print("Updated SharedPreferences: $espDeviceMap");

      // Print updated SharedPreferences
      await printSharedPreferences();

      // Navigate to the next page after successful operation
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => const DeviceAddedPage()),
      );
    } else {
      throw Exception("No response from ESP.");
    }
  } catch (e) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text("Error: ${e.toString()}")),
    );
  } finally {
    // Disconnect the device if it's still connected
    if (device != null && device!.isConnected) {
      await device!.disconnect();
    }
  }
}


  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: PreferredSize(
          preferredSize: const Size.fromHeight(50),
          child: AppBar(
            leading: IconButton(
              icon: const Icon(Icons.arrow_back),
              onPressed: () {
                Navigator.pop(context);
              },
            ),
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
        body: Column(
          children: [
            Center(child: Text(device!.name)),
            const SizedBox(height: 20),
            Image.asset(
              'assets/images/home_6.png',
              height: 250,
              width: 200,
            ),
            Expanded(
              child: Container(
                padding: const EdgeInsets.all(20),
                margin: const EdgeInsets.symmetric(horizontal: 5),
                decoration: BoxDecoration(
                  color: const Color(0xFFB3E5FC), // Light blue background
                  borderRadius: BorderRadius.circular(20),
                ),
                child: SingleChildScrollView(
                  child: Form(
                    key: _formKey,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Enter SSID',
                          style: TextStyle(
                            fontSize: 25,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 40),
                        Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 10),
                  child: TextFormField(
                    controller: _ssidController,
                    keyboardType: TextInputType.name,
                    decoration: const InputDecoration(
                      hintText: 'Username', // Updated hint text
                      border: UnderlineInputBorder(), // Underlined field
                    ),
                    validator: (value) {
                      return value!.isEmpty ? 'Please enter Name' : null;
                    },
                  ),
                ),
                const SizedBox(height: 30),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 10),
                  child: TextFormField(
                    controller: _passwordController,
                    keyboardType: TextInputType.visiblePassword,
                    decoration: const InputDecoration(
                      hintText: 'Password', 
                      border: UnderlineInputBorder(),
                    ),
                    obscureText: true, 
                    validator: (value) {
                      return value!.isEmpty ? 'Please enter Password' : null;
                    },
                  ),
                ),
                        const SizedBox(height: 120),
                        Center(
                          child: TextButton(
                            style: TextButton.styleFrom(
                              backgroundColor: const Color.fromARGB(
                                  255, 0, 188, 212), // Button color
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 30, vertical: 10),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(20),
                              ),
                            ),
                            onPressed: () async {
                              if (_formKey.currentState!.validate()) {
                                String ssid = _ssidController.text.trim();
                                String password = _passwordController.text.trim();
                                await sendCredentialsToESP(ssid, password);
                                }
                              },
                            child: const Row(
                              mainAxisSize: MainAxisSize.min, 
                              children: [
                                Icon(
                                  Icons.add,
                                  color: Colors.white,
                                ),
                                SizedBox(width: 10),
                                Text(
                                  ' Add ',
                                  style: TextStyle(
                                    color: Colors.white,
                                    fontSize: 18,
                                  ),
                                ),
                                SizedBox(width: 10),
                                Icon(
                                  Icons.arrow_forward,
                                  color: Colors.white,
                                ),
                              ],
                            ),
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
      ),
    );
  }
  

Future<void> printSharedPreferences() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  Map<String, dynamic> allPrefs = prefs.getKeys().fold({}, (map, key) {
    map[key] = prefs.get(key);
    return map;
  });

  print("Shared Preferences Data:\n$allPrefs");
}

}
