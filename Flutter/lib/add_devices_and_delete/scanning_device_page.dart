import 'package:flutter/material.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:home_auto_sample/add_devices_and_delete/add_available_device_page.dart';
import 'package:permission_handler/permission_handler.dart';

class ScanningDevicePage extends StatefulWidget {
  const ScanningDevicePage({super.key});

  @override
  State<ScanningDevicePage> createState() => _ScanningDevicePageState();
}

class _ScanningDevicePageState extends State<ScanningDevicePage> {

  List<BluetoothDevice> devicesList = [];
  List<BluetoothDevice> connectedDevices = [];
  bool _isScanning = false;

   @override
  void initState() {
    super.initState();
    requestPermissions(); 
  }

  Future<void> requestPermissions() async {
    if (await Permission.bluetooth.isDenied) {
      await Permission.bluetooth.request();
    }

    if (await Permission.bluetoothScan.isDenied) {
      await Permission.bluetoothScan.request();
    }

    if (await Permission.bluetoothConnect.isDenied) {
      await Permission.bluetoothConnect.request();
    }

    if (await Permission.location.isDenied) {
      await Permission.location.request();
    }

    if (await Permission.bluetooth.isGranted &&
        await Permission.bluetoothScan.isGranted &&
        await Permission.bluetoothConnect.isGranted &&
        await Permission.location.isGranted) {
      startScan(); 
    } else {
      print('Permissions not granted');
    }
  }  

  void startScan() {
  print('Scanning started');
  setState(() {
    devicesList.clear(); 
    _isScanning = true; 
  });

  FlutterBluePlus.startScan(timeout: const Duration(seconds: 4));

  FlutterBluePlus.scanResults.listen((results) {
    setState(() {
      for (ScanResult r in results) {
        print('Found device:');
        print('Name: ${r.device.name}');
        print('Advertisement Name: ${r.advertisementData.localName}');
        print('RemoteID: ${r.device.remoteId}');
        if (r.device.name.isNotEmpty) {
          print('first print in for Devices');
        }
        String deviceName = r.device.name.toLowerCase(); 
        if (deviceName.isNotEmpty && deviceName.startsWith('esp') && 
            !devicesList.contains(r.device)) {
          devicesList.add(r.device); 
        }
      }
    });
  });

  Future.delayed(const Duration(seconds: 5), stopScan);
}

void stopScan() {
  FlutterBluePlus.stopScan();
  setState(() {
    _isScanning = false; 
  });
}

@override
void dispose() {
  FlutterBluePlus.stopScan();
  super.dispose();
}

@override
Widget build(BuildContext context) {
  return MaterialApp(
    debugShowCheckedModeBanner: false,
    home: Scaffold(
      appBar: AppBar(
        title: const Text('Scanning for new devices'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () {
            Navigator.pop(context);
          },
        ),
      ),
      body: RefreshIndicator(
  onRefresh: _refreshPage,
  child: SingleChildScrollView(
    physics: const AlwaysScrollableScrollPhysics(),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Image.asset('assets/images/home_5.png'),
        const SizedBox(height: 20),
        if (_isScanning) 
          const Center(child: CircularProgressIndicator()),
        if (!_isScanning)
          Container(
            decoration: const BoxDecoration(
              color: Color.fromARGB(255, 184, 218, 246), 
              borderRadius: BorderRadius.only(
                topLeft: Radius.circular(20),
                topRight: Radius.circular(20),
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Padding(
                  padding: EdgeInsets.all(10.0),
                  child: Center(
                    child: Text(
                      'Available devices',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ),
                devicesList.isEmpty
                    ? SizedBox(
                        height: MediaQuery.of(context).size.height * 0.5,
                        child: const Center(
                          child: Text(
                            'No devices found',
                            style: TextStyle(fontSize: 16),
                          ),
                        ),
                      )
                    : SizedBox(
                        height: MediaQuery.of(context).size.height * 0.5, // Blue box height set to 1/5 of the screen
                        child: ListView.builder(
                          padding: const EdgeInsets.symmetric(horizontal: 10),
                          itemCount: devicesList.length,
                          itemBuilder: (context, index) {
                            return _buildDeviceRow(devicesList[index], context);
                          },
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
  );
}

Future<void> _refreshPage() async {
  print('Page refresh triggered');
  setState(() {
    devicesList.clear(); 
    _isScanning = true; 
  });

  FlutterBluePlus.startScan(timeout: const Duration(seconds: 5));
  await Future.delayed(const Duration(seconds: 5)); 
  stopScan();
}


  Widget _buildDeviceRow(BluetoothDevice device, BuildContext context) {
  return Padding(
    padding: const EdgeInsets.symmetric(vertical: 5, horizontal: 10),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Expanded(
              child: Text(
                device.name.isNotEmpty ? device.name : "Unnamed Device",
                style: const TextStyle(
                  fontSize: 16,
                  color: Colors.black,
                ),
                overflow: TextOverflow.ellipsis, 
              ),
            ),
            IconButton(
              icon: const Icon(Icons.add, color: Colors.black, size: 24),
              onPressed: () {
                Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (context) => AddAvailableDevicePage(device: device),
                  ),
                );
              },
            ),
          ],
        ),
        // Divider 
        const Divider(
          thickness: 1,
          color: Colors.grey,
          height: 0, 
          indent: 0,
          endIndent: 0,
        ),
      ],
    ),
  );
}
}

