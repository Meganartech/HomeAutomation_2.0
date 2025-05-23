import 'package:flutter/material.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'add_available_device_page.dart'; // import your next page

class FindUuidPage extends StatefulWidget {
  final BluetoothDevice device;

  const FindUuidPage({Key? key, required this.device}) : super(key: key);

  @override
  _FindUuidPageState createState() => _FindUuidPageState();
}

class _FindUuidPageState extends State<FindUuidPage> {
  bool _isConnecting = true;

  @override
  void initState() {
    super.initState();
    _connectAndDiscover();
  }

  Future<void> _connectAndDiscover() async {
    try {
      await widget.device.connect();
      List<BluetoothService> services = await widget.device.discoverServices();

      // (Optional) print all UUIDs for debugging
      for (var service in services) {
        print('Service UUID: ${service.uuid}');
        for (var characteristic in service.characteristics) {
          print('Characteristic UUID: ${characteristic.uuid}');
        }
      }

      // After discovering, move to AddAvailableDevicePage
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (context) => AddAvailableDevicePage(
            device: widget.device,
            services: services, // Pass the discovered services
          ),
        ),
      );
    } catch (e) {
      print("Error connecting or discovering: $e");
      setState(() {
        _isConnecting = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Finding UUIDs...'),
      ),
      body: Center(
        child: _isConnecting
            ? const CircularProgressIndicator()
            : const Text('Failed to connect or discover services'),
      ),
    );
  }
}
