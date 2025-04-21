import 'dart:async';
import 'dart:convert';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/scheduler/background_callback.dart';
import 'package:intl/intl.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:workmanager/workmanager.dart';
import 'package:timezone/timezone.dart' as tz;
import 'package:timezone/data/latest.dart' as tz;

class BackgroundSchedulerService {
  static final BackgroundSchedulerService _instance = BackgroundSchedulerService._internal();
  factory BackgroundSchedulerService() => _instance;

  final Map<String, StreamSubscription> _firebaseSubscriptions = {};
  bool _isInitialized = false;
  static const String _espDeviceKey = 'EspDevice';
  static const String _timeZone = 'Asia/Kolkata';
  late DatabaseReference _dbRef;
  
  BackgroundSchedulerService._internal();

  Future<void> initialize() async {
    if (_isInitialized) return;
    
    try {
      WidgetsFlutterBinding.ensureInitialized();
      await Firebase.initializeApp();
      tz.initializeTimeZones();
      _dbRef = FirebaseDatabase.instance.ref();
      
      await Workmanager().initialize(
        callbackDispatcher,
        isInDebugMode: true,
      );
      
      _setupFirebaseListeners();
      _isInitialized = true;
      debugPrint("BackgroundSchedulerService initialized");
    } catch (e, stack) {
      debugPrint("Initialization failed: $e\n$stack");
      rethrow;
    }
  }

  void _setupFirebaseListeners() {
    _cancelAllListeners();

    // Listener for scene changes
    _firebaseSubscriptions['userScenes'] = _dbRef.child("users")
      .onChildChanged
      .listen((event) async {
        final userId = event.snapshot.key;
        if (userId == null) return;
        
        final path = event.snapshot.ref.path;
if (path.contains('/scenes/')) {
  await _processSceneChanges(userId, event.snapshot);
}
      });

    // Listener for automatic on/off changes
    _firebaseSubscriptions['automaticOnOff'] = _dbRef.child("AutomaticOnOff")
      .onValue
      .listen((event) async {
        if (event.snapshot.value != null) {
          await _processAutomaticOnOff(event.snapshot);
        }
      });
  }

  Future<void> _processSceneChanges(String userId, DataSnapshot snapshot) async {
  try {
    // Extract scene name from path (users/{userId}/scenes/{sceneName}/...)
    final pathSegments = snapshot.ref.path.split('/');
    
    // The path should be: users > userId > scenes > sceneName > ...
    if (pathSegments.length < 4 || pathSegments[2] != 'scenes') return;
    
    final sceneName = pathSegments[3];
    
    final sceneData = await _getSceneDataFromFirebase(userId, sceneName);
    if (sceneData == null) return;

    await _scheduleTasksFromSceneData(userId, sceneName, sceneData);
  } catch (e, stack) {
    debugPrint("Error processing scene changes: $e\n$stack");
  }
}

  Future<Map<String, dynamic>?> _getSceneDataFromFirebase(String userId, String sceneName) async {
    try {
      final snapshot = await _dbRef.child("users/$userId/scenes/$sceneName").get();
      if (!snapshot.exists) return null;
      return Map<String, dynamic>.from(snapshot.value as Map? ?? {});
    } catch (e, stack) {
      debugPrint("Error getting scene data from Firebase: $e\n$stack");
      return null;
    }
  }

  Future<void> _scheduleTasksFromSceneData(
    String userId, 
    String sceneName, 
    Map<String, dynamic> sceneData
  ) async {
    try {
      final updates = <String, dynamic>{};

      for (final deviceEntry in sceneData.entries) {
        final deviceId = deviceEntry.key;
        final deviceData = deviceEntry.value as Map<String, dynamic>;
        final ports = deviceData['ports'] as Map<String, dynamic>? ?? {};

        for (final portEntry in ports.entries) {
          final portKey = portEntry.key;
          final portData = portEntry.value as Map<String, dynamic>;

          final onTimeStr = portData["onTime"]?.toString();
          final offTimeStr = portData["offTime"]?.toString();

          if (onTimeStr != null && onTimeStr.isNotEmpty) {
            final onDateTime = _parseSceneDateTime(onTimeStr);
            if (onDateTime != null) {
              updates["AutomaticOnOff/$userId/$deviceId/$portKey"] = {
                'onTime': onTimeStr,
                'sceneName': sceneName,
              };
            }
          }

          if (offTimeStr != null && offTimeStr.isNotEmpty) {
            final offDateTime = _parseSceneDateTime(offTimeStr);
            if (offDateTime != null) {
              updates["AutomaticOnOff/$userId/$deviceId/$portKey/offTime"] = offTimeStr;
            }
          }
        }
      }

      if (updates.isNotEmpty) {
        await _dbRef.update(updates).timeout(const Duration(seconds: 10));
        debugPrint("Scheduled tasks from scene: $sceneName");
      }
    } catch (e, stack) {
      debugPrint("Failed to schedule tasks from scene: $e\n$stack");
      await _scheduleWorkManagerFallback(userId, sceneName, sceneData);
    }
  }

  Future<void> _processAutomaticOnOff(DataSnapshot snapshot) async {
    try {
      final now = DateTime.now().toUtc();
      final prefs = await SharedPreferences.getInstance();
      final updates = <String, dynamic>{};
      final allDevices = Map<dynamic, dynamic>.from(snapshot.value as Map? ?? {});

      for (final userEntry in allDevices.entries) {
        final userId = userEntry.key.toString();
        final userDevices = Map<dynamic, dynamic>.from(userEntry.value as Map? ?? {});

        for (final deviceEntry in userDevices.entries) {
          final deviceId = deviceEntry.key.toString();
          final ports = Map<dynamic, dynamic>.from(deviceEntry.value as Map? ?? {});

          // Get device name once per device
          final deviceSnapshot = await _dbRef
            .child("users/$userId/Espdevice/$deviceId")
            .get()
            .timeout(const Duration(seconds: 5));

          if (!deviceSnapshot.exists) continue;

          final deviceName = deviceSnapshot.child('name').value?.toString() ?? '';
          if (deviceName.isEmpty) continue;

          for (final portEntry in ports.entries) {
            final portKey = portEntry.key.toString();
            final task = Map<dynamic, dynamic>.from(portEntry.value as Map? ?? {});

            await _processPortTask(
              userId,
              deviceId,
              deviceName,
              portKey,
              task,
              now,
              updates,
              prefs,
            );
          }
        }
      }

      if (updates.isNotEmpty) {
        await _dbRef.update(updates).timeout(const Duration(seconds: 10));
        debugPrint("Processed ${updates.length} automatic on/off tasks");
      }
    } catch (e, stack) {
      debugPrint("Error processing automatic on/off: $e\n$stack");
    }
  }

  Future<void> _processPortTask(
    String userId,
    String deviceId,
    String deviceName,
    String portKey,
    Map<dynamic, dynamic> task,
    DateTime now,
    Map<String, dynamic> updates,
    SharedPreferences prefs,
  ) async {
    try {
      final onTime = _parseDateTime(task['onTime']?.toString());
      final offTime = _parseDateTime(task['offTime']?.toString());
      final sceneName = task['sceneName']?.toString();
      final portNum = portKey.replaceFirst('port', '');
      final componentPath = "users/$userId/components/${deviceName}_$portNum";

      if (onTime != null && now.isAfter(onTime)) {
        updates[componentPath] = 1;
        updates["AutomaticOnOff/$userId/$deviceId/$portKey/onTime"] = null;
        debugPrint("Triggered ON for $deviceId/$portKey");
      }

      if (offTime != null && now.isAfter(offTime)) {
        updates[componentPath] = 0;
        updates["AutomaticOnOff/$userId/$deviceId/$portKey"] = null;
        if (sceneName != null) {
          updates["users/$userId/scenes/$sceneName"] = null;
        }
        debugPrint("Triggered OFF for $deviceId/$portKey");
      }

      if (updates.isNotEmpty) {
        await _updateLocalState(
          userId: userId,
          deviceId: deviceId,
          portKey: portKey,
          turnOn: updates[componentPath] == 1,
          actualDeviceName: deviceName,
          sceneName: sceneName,
          prefs: prefs,
        );
      }
    } catch (e, stack) {
      debugPrint("Port task processing failed: $e\n$stack");
    }
  }

  @pragma('vm:entry-point')
  static Future<bool> executeBackgroundTask({
    required String taskName,
    required Map<String, dynamic> inputData, 
    required SharedPreferences prefs,
  }) async {
    try {
      switch (taskName) {
        case 'deviceActionTask':
          return await _executeDeviceAction(
            userId: inputData['userId']?.toString() ?? '',
            deviceId: inputData['deviceId']?.toString() ?? '',
            portKey: inputData['portKey']?.toString() ?? 'port1',
            turnOn: inputData['turnOn'] == true,
            sceneName: inputData['sceneName']?.toString(),
            prefs: prefs,
          );
        case 'firebaseSyncTask':
          return await _handleFirebaseSyncTask(
            inputData['userId']?.toString() ?? '',
            prefs,
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

  Future<void> startSceneScheduler(String userId, String sceneName) async {
    if (!_isInitialized) await initialize();
    await _scheduleSceneTasks(userId, sceneName);
  }

  Future<void> stopAllScheduling() async {
    _cancelAllListeners();
    await Workmanager().cancelAll();
    debugPrint("All scheduling stopped");
  }

  void _cancelAllListeners() {
    _firebaseSubscriptions.forEach((key, subscription) {
      subscription.cancel();
      debugPrint("Cancelled listener: $key");
    });
    _firebaseSubscriptions.clear();
  }

  Future<void> _scheduleSceneTasks(String userId, String sceneName) async {
    try {
      final sceneData = await _getSceneData(userId, sceneName);
      if (sceneData == null) {
        debugPrint("No scene data found for $sceneName");
        return;
      }

      debugPrint("Scheduling tasks for scene: $sceneName");
      await _scheduleTasksFromSceneData(userId, sceneName, sceneData);
    } catch (e, stack) {
      debugPrint("Error scheduling scene tasks: $e\n$stack");
    }
  }

  Future<void> _scheduleWorkManagerFallback(
    String userId, 
    String sceneName, 
    Map<String, dynamic> sceneData
  ) async {
    try {
      debugPrint("Using WorkManager fallback for scene: $sceneName");
      
      for (final deviceEntry in sceneData.entries) {
        final deviceId = deviceEntry.key;
        final ports = (deviceEntry.value["ports"] as Map<String, dynamic>? ?? {});
        
        for (final portEntry in ports.entries) {
          final portKey = portEntry.key;
          final portData = portEntry.value as Map<String, dynamic>;
          
          final onTimeStr = portData["onTime"]?.toString();
          final offTimeStr = portData["offTime"]?.toString();
          
          if (onTimeStr != null && onTimeStr.isNotEmpty) {
            final onDateTime = _parseSceneDateTime(onTimeStr);
            if (onDateTime != null) {
              _scheduleWorkManagerTask(
                userId: userId,
                deviceId: deviceId,
                portKey: portKey,
                triggerTime: onDateTime,
                turnOn: true,
                sceneName: sceneName,
              );
            }
          }
          
          if (offTimeStr != null && offTimeStr.isNotEmpty) {
            final offDateTime = _parseSceneDateTime(offTimeStr);
            if (offDateTime != null) {
              _scheduleWorkManagerTask(
                userId: userId,
                deviceId: deviceId,
                portKey: portKey,
                triggerTime: offDateTime,
                turnOn: false,
                sceneName: sceneName,
              );
            }
          }
        }
      }
    } catch (e, stack) {
      debugPrint("WorkManager fallback scheduling failed: $e\n$stack");
    }
  }

  void _scheduleWorkManagerTask({
    required String userId,
    required String deviceId,
    required String portKey,
    required DateTime triggerTime,
    required bool turnOn,
    required String sceneName,
  }) {
    try {
      final location = tz.getLocation(_timeZone);
      final now = tz.TZDateTime.now(location);
      var scheduledTime = tz.TZDateTime.from(triggerTime, location);

      if (scheduledTime.isBefore(now)) {
        scheduledTime = scheduledTime.add(const Duration(days: 1));
      }

      final taskId = '${sceneName}_${deviceId}_${portKey}_${turnOn ? 'on' : 'off'}'
          .replaceAll(RegExp(r'[^a-zA-Z0-9_]'), '_');

      final taskData = {
        'userId': userId,
        'deviceId': deviceId,
        'portKey': portKey,
        'turnOn': turnOn,
        'sceneName': sceneName,
        'scheduledTime': scheduledTime.toUtc().toIso8601String(),
      };

      Workmanager().registerOneOffTask(
        taskId,
        'deviceActionTask',
        inputData: taskData,
        initialDelay: scheduledTime.difference(now),
        constraints: Constraints(
          networkType: NetworkType.connected,
        ),
      );

      debugPrint('Fallback WorkManager task scheduled for ${scheduledTime.toLocal()}');
    } catch (e, stack) {
      debugPrint("Error scheduling WorkManager task: $e\n$stack");
    }
  }

  Future<Map<String, dynamic>?> _getSceneData(String userId, String sceneName) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final storedJson = prefs.getString(_espDeviceKey);
      if (storedJson == null) return null;

      final storedData = jsonDecode(storedJson) as Map<String, dynamic>;
      final userDevices = storedData[userId] as Map<String, dynamic>?;
      final scenes = userDevices?["scenes"] as Map<String, dynamic>?;
      
      return scenes?[sceneName] as Map<String, dynamic>?;
    } catch (e, stack) {
      debugPrint("Error getting scene data: $e\n$stack");
      return null;
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
      final deviceSnapshot = await deviceRef.get().timeout(const Duration(seconds: 10));
      
      if (!deviceSnapshot.exists) return false;
      
      final deviceName = deviceSnapshot.child('name').value?.toString() ?? '';
      if (deviceName.isEmpty) return false;

      await FirebaseDatabase.instance
        .ref("users/$userId/components/${deviceName}_$numericPort")
        .set(turnOn ? 1 : 0)
        .timeout(const Duration(seconds: 10));

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

  static Future<bool> _handleFirebaseSyncTask(
    String userId,
    SharedPreferences prefs,
  ) async {
    try {
      final dbRef = FirebaseDatabase.instance.ref();
      final now = DateTime.now().toUtc();

      final tasksSnapshot = await dbRef.child("users/$userId/AutomaticOnOff")
        .once()
        .timeout(const Duration(seconds: 15));

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
        await dbRef.update(updates).timeout(const Duration(seconds: 10));
        await _updateLocalStateFromFirebase(userId, prefs);
      }

      return true;
    } catch (e, stack) {
      debugPrint("Firebase sync task failed: $e\n$stack");
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
        .timeout(const Duration(seconds: 15));

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
        .timeout(const Duration(seconds: 10));

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
        .timeout(const Duration(seconds: 10));

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