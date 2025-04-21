import 'dart:async';
import 'dart:convert';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:timezone/timezone.dart' as tz;
import 'package:timezone/data/latest.dart' as tz;
import 'package:intl/intl.dart';

class BackgroundService {
  static const _maxRetries = 3;
  static const _retryDelay = Duration(seconds: 2);
  static const _firebaseTimeout = Duration(seconds: 15);
  static const _prefsTimeout = Duration(seconds: 5);
  static const String _espDeviceKey = 'EspDevice';
  static const String _timeZone = 'Asia/Kolkata';

  @pragma('vm:entry-point')
  static Future<bool> executeBackgroundTask({
    required String taskName,
    required Map<String, dynamic> inputData,
    SharedPreferences? prefs,
  }) async {
    try {
      WidgetsFlutterBinding.ensureInitialized();
      await _initializeFirebaseWithRetry();
      
      final localPrefs = prefs ?? await _getSafePreferences();
      if (localPrefs == null) return false;

      switch (taskName) {
        case 'deviceActionTask':
          return await _executeWithRetry(
            () => _executeDeviceAction(
              userId: inputData['userId']?.toString() ?? '',
              deviceId: inputData['deviceId']?.toString() ?? '',
              portKey: inputData['portKey']?.toString() ?? 'port1',
              turnOn: inputData['turnOn'] == true,
              sceneName: inputData['sceneName']?.toString(),
              prefs: localPrefs,
            ),
          );
        
        case 'firebaseSyncTask':
          return await _executeWithRetry(
            () => _handleFirebaseSyncTask(
              inputData['userId']?.toString() ?? '',
              localPrefs,
            ),
          );
            
        default:
          debugPrint("Unknown background task: $taskName");
          return false;
      }
    } catch (e, stack) {
      debugPrint("Background task execution failed: $e\n$stack");
      return false;
    }
  }

  static Future<void> _initializeFirebaseWithRetry() async {
    if (Firebase.apps.isNotEmpty) return;

    for (var attempt = 1; attempt <= _maxRetries; attempt++) {
      try {
        await Firebase.initializeApp(
          name: 'BackgroundService',
          options: const FirebaseOptions(
            apiKey: "AIzaSyAuea-x6Y8clAUO2xuFE8Z79RcltpxoPP4",
            authDomain: "smart-home-19334.firebaseapp.com",
            databaseURL: "https://smart-home-19334-default-rtdb.firebaseio.com",
            projectId: "smart-home-19334",
            storageBucket: "smart-home-19334.firebasestorage.app",
            messagingSenderId: "856846462492",
            appId: "1:856846462492:web:20e5d9fbe843c5705039d9",
            measurementId: "G-37NJX1HHMG"
          ),
        ).timeout(_firebaseTimeout);
        tz.initializeTimeZones();
        return;
      } catch (e) {
        if (attempt == _maxRetries) rethrow;
        await Future.delayed(_retryDelay * attempt);
      }
    }
  }

  static Future<SharedPreferences?> _getSafePreferences() async {
    try {
      return await SharedPreferences.getInstance()
        .timeout(_prefsTimeout);
    } catch (e) {
      debugPrint("Failed to get SharedPreferences: $e");
      return null;
    }
  }

  static Future<bool> _executeWithRetry(Future<bool> Function() task) async {
    for (var attempt = 1; attempt <= _maxRetries; attempt++) {
      try {
        return await task().timeout(_firebaseTimeout);
      } catch (e) {
        if (attempt == _maxRetries) {
          debugPrint("Task failed after $_maxRetries attempts: $e");
          return false;
        }
        await Future.delayed(_retryDelay * attempt);
      }
    }
    return false;
  }

  static Future<bool> _handleFirebaseSyncTask(
    String userId,
    SharedPreferences prefs,
  ) async {
    try {
      final dbRef = FirebaseDatabase.instance.ref();
      final now = DateTime.now().toUtc();

      final tasksSnapshot = await dbRef.child("users/$userId/AutomaticOnOff")
        .once()
        .timeout(_firebaseTimeout);

      final tasks = tasksSnapshot.snapshot.value as Map<dynamic, dynamic>?;
      if (tasks == null) return true;

      final updates = <String, dynamic>{};
      
      for (final deviceEntry in tasks.entries) {
        final deviceId = deviceEntry.key.toString();
        final ports = deviceEntry.value as Map<dynamic, dynamic>;

        for (final portEntry in ports.entries) {
          final portKey = portEntry.key.toString();
          final task = portEntry.value as Map<dynamic, dynamic>;

          final onTime = _parseDateTime(task['onTime']?.toString());
          final offTime = _parseDateTime(task['offTime']?.toString());
          final sceneName = task['sceneName']?.toString();

          if (onTime != null && now.isAfter(onTime)) {
            updates["users/$userId/components/$deviceId/$portKey"] = 1;
            updates["users/$userId/AutomaticOnOff/$deviceId/$portKey/onTime"] = null;
          }

          if (offTime != null && now.isAfter(offTime)) {
            updates["users/$userId/components/$deviceId/$portKey"] = 0;
            updates["users/$userId/AutomaticOnOff/$deviceId/$portKey"] = null;
            if (sceneName != null) {
              updates["users/$userId/scenes/$sceneName"] = null;
            }
          }
        }
      }

      if (updates.isNotEmpty) {
        await dbRef.update(updates).timeout(_firebaseTimeout);
        await _updateLocalStateFromFirebase(userId, prefs);
      }

      return true;
    } catch (e, stack) {
      debugPrint("Firebase sync task failed: $e\n$stack");
      return false;
    }
  }

  static Future<bool> _executeDeviceAction({
    required String userId,
    required String deviceId,
    required String portKey, 
    required bool turnOn,
    String? sceneName,
    required SharedPreferences prefs,
  }) async {
    try {
      final numericPort = portKey.startsWith('port') 
          ? portKey.substring(4) 
          : portKey.replaceAll(RegExp(r'[^0-9]'), '');

      if (numericPort.isEmpty) return false;

      final deviceRef = FirebaseDatabase.instance.ref("users/$userId/Espdevice/$deviceId");
      final deviceSnapshot = await deviceRef.get().timeout(_firebaseTimeout);
      
      if (!deviceSnapshot.exists) return false;
      
      final deviceName = deviceSnapshot.child('name').value?.toString() ?? '';
      if (deviceName.isEmpty) return false;

      await FirebaseDatabase.instance
        .ref("users/$userId/components/${deviceName}_$numericPort")
        .set(turnOn ? 1 : 0)
        .timeout(_firebaseTimeout);

      if (!turnOn) {
        await _cleanupAutomaticOnOff(userId, deviceId, portKey);
        if (sceneName != null) {
          await _cleanupScene(userId, sceneName);
        }
      }

      await _updateLocalState(
        userId: userId,
        deviceId: deviceId,
        portKey: portKey,
        turnOn: turnOn,
        actualDeviceName: deviceName,
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
      final rawData = prefs.getString(_espDeviceKey);
      if (rawData == null) return;

      final data = jsonDecode(rawData) as Map<String, dynamic>;
      
      data
        .putIfAbsent(userId, () => {})
        .putIfAbsent(deviceId, () => {'name': actualDeviceName, 'ports': {}});
      
      final prefPortKey = portKey.startsWith('port') ? portKey : 'port$portKey';
      
      data[userId][deviceId]['ports'][prefPortKey] = {
        'state': turnOn,
      };

      if (!turnOn) {
        if (data[userId].containsKey('AutomaticOnOff') &&
            data[userId]['AutomaticOnOff'] is Map &&
            data[userId]['AutomaticOnOff'].containsKey(deviceId)) {
          (data[userId]['AutomaticOnOff'][deviceId] as Map).remove(portKey);
        }

        if (sceneName != null && data[userId].containsKey('scenes')) {
          (data[userId]['scenes'] as Map).remove(sceneName);
        }
      }

      await prefs.setString(_espDeviceKey, jsonEncode(data));
    } catch (e, stack) {
      debugPrint("Local state update failed: $e\n$stack");
    }
  }

  static Future<void> _updateLocalStateFromFirebase(
    String userId,
    SharedPreferences prefs,
  ) async {
    try {
      final dbRef = FirebaseDatabase.instance.ref();
      final snapshot = await dbRef.child("users/$userId")
        .get()
        .timeout(_firebaseTimeout);

      if (!snapshot.exists) return;

      final userData = snapshot.value as Map<dynamic, dynamic>;
      await prefs.setString(_espDeviceKey, jsonEncode(userData));
    } catch (e, stack) {
      debugPrint("Failed to update local state from Firebase: $e\n$stack");
    }
  }

  static Future<void> _cleanupScene(
    String userId,
    String sceneName,
  ) async {
    try {
      await FirebaseDatabase.instance
        .ref("users/$userId/scenes/$sceneName")
        .remove()
        .timeout(_firebaseTimeout);

      final prefs = await SharedPreferences.getInstance();
      final rawData = prefs.getString(_espDeviceKey);
      if (rawData != null) {
        final data = jsonDecode(rawData) as Map<String, dynamic>;
        if (data.containsKey(userId) && data[userId].containsKey('scenes')) {
          (data[userId]['scenes'] as Map).remove(sceneName);
          await prefs.setString(_espDeviceKey, jsonEncode(data));
        }
      }
    } catch (e, stack) {
      debugPrint("Scene cleanup failed: $e\n$stack");
    }
  }

  static Future<void> _cleanupAutomaticOnOff(
    String userId,
    String deviceId,
    String portKey,
  ) async {
    try {
      await FirebaseDatabase.instance
        .ref("users/$userId/AutomaticOnOff/$deviceId/$portKey")
        .remove()
        .timeout(_firebaseTimeout);

      final prefs = await SharedPreferences.getInstance();
      final rawData = prefs.getString(_espDeviceKey);
      if (rawData != null) {
        final data = jsonDecode(rawData) as Map<String, dynamic>;
        if (data.containsKey(userId) && data[userId].containsKey('AutomaticOnOff')) {
          final autoOff = data[userId]['AutomaticOnOff'] as Map;
          if (autoOff.containsKey(deviceId)) {
            (autoOff[deviceId] as Map).remove(portKey);
            if ((autoOff[deviceId] as Map).isEmpty) {
              autoOff.remove(deviceId);
            }
            await prefs.setString(_espDeviceKey, jsonEncode(data));
          }
        }
      }
    } catch (e, stack) {
      debugPrint("AutomaticOnOff cleanup failed: $e\n$stack");
    }
  }

  static DateTime? _parseDateTime(String? dateTimeStr) {
    if (dateTimeStr == null || dateTimeStr.isEmpty) return null;
    
    try {
      return DateTime.tryParse(dateTimeStr) ?? 
        DateFormat('hh:mm a MMM d, yyyy').parse(dateTimeStr);
    } catch (e) {
      debugPrint("Failed to parse date/time: $dateTimeStr - $e");
      return null;
    }
  }

  static DateTime? _parseSceneDateTime(String dateTimeStr) {
    try {
      final location = tz.getLocation(_timeZone);
      final now = tz.TZDateTime.now(location);
      final parsedDate = DateFormat('hh:mm a MMM d, yyyy').parse(dateTimeStr);
      
      var localTime = tz.TZDateTime.from(parsedDate, location);
      if (localTime.isBefore(now)) {
        localTime = tz.TZDateTime(
          location,
          now.year,
          now.month,
          now.day + 1,
          localTime.hour,
          localTime.minute
        );
      }
      return localTime;
    } catch (e) {
      debugPrint("Failed to parse date: $e");
      return null;
    }
  }
}