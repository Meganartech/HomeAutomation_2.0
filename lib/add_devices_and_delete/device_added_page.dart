import 'package:flutter/material.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';

class DeviceAddedPage extends StatefulWidget { 

  const DeviceAddedPage({super.key});

  @override
  _DeviceAddedPageState createState() => _DeviceAddedPageState();
}

class _DeviceAddedPageState extends State<DeviceAddedPage> {
  @override
  void initState() {
    super.initState();
    Future.delayed(const Duration(seconds: 3), () {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (context) => const AvailableDevicesPage(),
        ),
      );
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
                'Device added successfully',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 50),
            ],
          ),
        ),
      ),
    );
  }
}
