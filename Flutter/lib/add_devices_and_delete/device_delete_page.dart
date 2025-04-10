import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/add_devices_and_delete/no_device_page.dart';

class DeviceDeletePage extends StatefulWidget { 

  const DeviceDeletePage({super.key});

  @override
  _DeviceDeletePageState createState() => _DeviceDeletePageState();
}

class _DeviceDeletePageState extends State<DeviceDeletePage> {
  @override
  void initState() {
    super.initState();
    Future.delayed(const Duration(seconds: 3), () {
      _fetchDevicesfornavigate();
    });
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
      body: Padding(
        padding: const EdgeInsets.only(top: 80),
        child: Container(
          padding: const EdgeInsetsDirectional.all(50),
          margin: const EdgeInsets.all(25),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Image.asset('assets/images/home_7.png'),
              const SizedBox(height: 20),
              const Text(
                'Device Deleted successfully',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 50),
            ],
          ),
        ),
      ),
    );
  }
  
  void _fetchDevicesfornavigate() async {
  String userId = FirebaseAuth.instance.currentUser!.uid;

  DatabaseReference devicesRef =
      FirebaseDatabase.instance.ref().child('devices').child(userId);

  devicesRef.once().then((DatabaseEvent event) {
    final data = event.snapshot.value;
    
    if (data != null && data is Map) {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => AvailableDevicesPage()),
      );
    } else {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => const NoDevicePage()),
      );
    }
  }).catchError((error) {
    print("Error fetching devices: $error");
  });
}
}

