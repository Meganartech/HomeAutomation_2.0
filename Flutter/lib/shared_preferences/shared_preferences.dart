import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:firebase_auth/firebase_auth.dart';

class SharedPreferencesHelper {
  static late SharedPreferences _prefs;

  // Initialize SharedPreferences
  static Future<void> init() async {
    _prefs = await SharedPreferences.getInstance();
  }


  // Get all saved devices for the current user
  static Future<List<Map<String, dynamic>>> getAllDevices() async {
    List<Map<String, dynamic>> devices = [];
    Map<String, dynamic>? espDeviceMap = _prefs.getKeys().fold<Map<String, dynamic>>({}, (map, key) {
      if (key.startsWith('EspDevice_${FirebaseAuth.instance.currentUser?.uid}_')) {
        String? deviceJson = _prefs.getString(key);
        map[key] = jsonDecode(deviceJson!);
            }
      return map;
    });

    espDeviceMap.forEach((key, deviceJson) {
      devices.add(deviceJson);
    });
    return devices;
  }

  // Delete a specific device data by deviceId
  static Future<void> deleteDeviceData(String deviceId) async {
    await _prefs.remove('EspDevice_${FirebaseAuth.instance.currentUser?.uid}_$deviceId');
  }
}
