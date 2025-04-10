import 'dart:convert';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class BackgroundService {
  @pragma('vm:entry-point')
  static Future<bool> executeBackgroundTask({
    required String taskName,
    required Map<String, dynamic> inputData,
    SharedPreferences? prefs,
  }) async {
    try {
      WidgetsFlutterBinding.ensureInitialized();

      switch (taskName) {
        case 'deviceActionTask':
          return await _handleDeviceAction(
            userId: inputData['userId']?.toString() ?? '',
            deviceId: inputData['deviceId']?.toString() ?? '',
            portKey: inputData['portKey']?.toString() ?? '', // Expecting "port1"
            turnOn: inputData['turnOn'] == true,
            sceneName: inputData['sceneName']?.toString(),
            prefs: prefs ?? await SharedPreferences.getInstance(),
          );
        default:
          debugPrint("Unknown background task type: $taskName");
          return false;
      }
    } catch (e, stack) {
      debugPrint("Background task execution failed: $e\n$stack");
      return false;
    }
  }

  static Future<bool> _handleDeviceAction({
    required String userId,
    required String deviceId,
    required String portKey, 
    required bool turnOn,
    String? sceneName,
    required SharedPreferences prefs
  }) async {
    try {
      if (!portKey.startsWith('port')) {
        debugPrint("Invalid portKey format - must be 'portX': $portKey");
        return false;
      }
      
      final numericPort = portKey.substring(4); 
      if (numericPort.isEmpty || int.tryParse(numericPort) == null) {
        debugPrint("Invalid port number in $portKey");
        return false;
      }

      final deviceRef = FirebaseDatabase.instance.ref("users/$userId/Espdevice/$deviceId");
      final deviceSnapshot = await deviceRef.get();

      if (!deviceSnapshot.exists) {
        debugPrint("Device not found in Firebase");
        return false;
      }

      final actualDeviceName = deviceSnapshot.child('name').value?.toString() ?? '';
      if (actualDeviceName.isEmpty) {
        debugPrint("Device name is empty");
        return false;
      }

      final firebasePath = "users/$userId/components/${actualDeviceName}_$numericPort";
      await FirebaseDatabase.instance.ref(firebasePath).set(turnOn ? 1 : 0);
      debugPrint("Firebase updated: $firebasePath = ${turnOn ? 1 : 0}");
      

    if (!turnOn) {
      // Remove from Firebase
      final autoOffPath = "users/$userId/AutomaticOnOff/$deviceId/$portKey";
      await FirebaseDatabase.instance.ref(autoOffPath).remove();
      debugPrint("Removed AutomaticOnOff from Firebase: $autoOffPath");
      
      final sceneRef = FirebaseDatabase.instance.ref("users/$userId/scenes/$sceneName");
      await sceneRef.remove();
      debugPrint("Removed scene '$sceneName' from Firebase");
    }

      await _updateLocalState(
        userId: userId,
        deviceId: deviceId,
        portKey: portKey, 
        turnOn: turnOn,
        actualDeviceName: actualDeviceName,
        sceneName: sceneName,
        prefs: prefs,
      );

      return true;
    } catch (e, stack) {
      debugPrint("Device action failed: $e\n$stack");
      return false;
    }
  }

  static Future<void> _updateLocalState({
    required String userId,
    required String deviceId,
    required String portKey,
    required bool turnOn,
    required String actualDeviceName,
    String? sceneName,
    required SharedPreferences prefs,
  }) async {
    try {
      final data = jsonDecode(prefs.getString('EspDevice') ?? '{}') as Map<String, dynamic>;
      
      data
        .putIfAbsent(userId, () => {})
        .putIfAbsent(deviceId, () => {'name': actualDeviceName, 'ports': {}});

      data[userId][deviceId]['ports'][portKey] = {
        'state': turnOn,
      };

      if (!turnOn && sceneName != null) {
        data[userId]['scenes']?.remove(sceneName);
      }

    if (!turnOn) {
      // Remove AutomaticOnOff entry from SharedPreferences
      if (data[userId].containsKey('AutomaticOnOff') && 
          data[userId]['AutomaticOnOff'] is Map &&
          data[userId]['AutomaticOnOff'].containsKey(deviceId)) {
        (data[userId]['AutomaticOnOff'][deviceId] as Map).remove(portKey);
        debugPrint("Removed AutomaticOnOff from SharedPreferences: $deviceId/$portKey");
      }

      // Remove scene if exists
      if (sceneName != null) {
        data[userId]['scenes']?.remove(sceneName);
        debugPrint("Removed scene '$sceneName' from SharedPreferences");
      }
    }

      await prefs.setString('EspDevice', jsonEncode(data));
      debugPrint("SharedPrefs updated: $portKey = $turnOn");
    } catch (e, stack) {
      debugPrint("Local state update failed: $e\n$stack");
    }
  }
}