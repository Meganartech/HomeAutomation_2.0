
import 'package:firebase_database/firebase_database.dart';

class DatabaseService {
  // Fetch device details for a user from the 'Devices' node
  Future<List<Device>> fetchDevicesForUser(String uid) async {
    final DatabaseReference ref = FirebaseDatabase.instance.ref('users/$uid/EspDevice');

    final DataSnapshot snapshot = await ref.get();

    if (snapshot.exists) {
      Map<dynamic, dynamic> devicesMap = snapshot.value as Map<dynamic, dynamic>;

      // Create a list of Device objects from the retrieved data
      List<Device> devicesList = devicesMap.entries.map((entry) {
        return Device(
          deviceId: entry.key,  // Device ID is the key in Firebase
          deviceName: entry.value['name'] as String,
        );
      }).toList();

      return devicesList;
    } else {
      return []; // Return empty if no devices are found
    }
  }

  // Fetch Wi-Fi credentials for a user from the 'Wifi' node
  Future<WifiDetails?> fetchWifiDetailsForUser(String uid) async {
    final DatabaseReference ref = FirebaseDatabase.instance.ref('users/$uid/Wifi');

    final DataSnapshot snapshot = await ref.get();

    if (snapshot.exists) {
      Map<dynamic, dynamic> wifiMap = snapshot.value as Map<dynamic, dynamic>;

      // Retrieve the Wi-Fi credentials directly from the map
      String ssid = wifiMap['ssid'] ?? '';
      String password = wifiMap['password'] ?? '';

      return WifiDetails(
        name: ssid,
        password: password,
      );
    }

    return null; // Return null if Wi-Fi credentials are not found
  }
}

Future<String?> getDeviceNameFromFirebase(String deviceId) async {
  try {
    DatabaseReference ref = FirebaseDatabase.instance.ref("EspDevice/$deviceId/name");
    DatabaseEvent event = await ref.once();
    return event.snapshot.value as String?;
  } catch (e) {
    print("Error fetching device name from Firebase: $e");
    return null;
  }
}

class Device {
  final String deviceId;  // Device ID (key in Firebase)
  final String deviceName; // Name of the device

  Device({
    required this.deviceId,
    required this.deviceName,
  });

  /// Factory method to create a Device instance from a Firebase data map
  factory Device.fromMap(Map<dynamic, dynamic> data) {
    return Device(
      deviceId: data['deviceId'] ?? 'Unknown', 
      deviceName: data['name'] ?? 'Unknown Device',
    );
  }

  /// Convert the Device instance back to a Map for Firebase storage
  Map<String, dynamic> toMap() {
    return {
      "deviceId": deviceId,
      "name": deviceName,
    };
  }
}



class PortService {
  final Map<String, List<int>> _portData = {};

  // Add or update port data for a device
  void updatePorts(String deviceId, List<int> ports) {
    _portData[deviceId] = ports;
  }

  // Get port data for a device
  List<int> getPorts(String deviceId) {
    return _portData[deviceId] ?? [];
  }

  // Remove port data for a device
  void removePorts(String deviceId) {
    _portData.remove(deviceId);
  }

  // Convert the entire port data to a map (e.g., for Firebase storage)
  Map<String, dynamic> toMap() {
    return _portData.map((key, value) => MapEntry(key, value));
  }

  // Load port data from a map (e.g., from Firebase)
  void loadFromMap(Map<String, dynamic> data) {
    _portData.clear();
    data.forEach((key, value) {
      _portData[key] = List<int>.from(value);
    });
  }
}


class Scene {
  final List<String> sceneNames; // List of scene names

  Scene({required this.sceneNames});

  /// Factory method to create a Scene instance from a Firebase data map
  factory Scene.fromMap(Map<dynamic, dynamic> data) {
    return Scene(
      sceneNames: List<String>.from(data['sceneNames'] ?? []),
    );
  }

  /// Convert the Scene instance back to a Map for Firebase storage
  Map<String, dynamic> toMap() {
    return {
      "sceneNames": sceneNames,
    };
  }
}



class WifiDetails {
  final String name; // Wi-Fi name (SSID)
  final String password; // Wi-Fi password

  WifiDetails({
    required this.name,
    required this.password,
  });

  factory WifiDetails.fromMap(Map<dynamic, dynamic> data) {
    return WifiDetails(
      name: data['name'] ?? '',
      password: data['password'] ?? '',
    );
  }
}
