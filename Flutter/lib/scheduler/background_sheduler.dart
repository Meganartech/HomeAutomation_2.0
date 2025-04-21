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

  final Map<String, Timer> _activeTimers = {};
  bool _isInitialized = false;
  final Completer<void> _initCompleter = Completer<void>();
  static const String _espDeviceKey = 'EspDevice';
  
  BackgroundSchedulerService._internal(); 

  Future<void> initialize() async {
    if (_isInitialized) return _initCompleter.future;
    
    try {
      WidgetsFlutterBinding.ensureInitialized();
      await Firebase.initializeApp();
      tz.initializeTimeZones();
      
      await Workmanager().initialize(
        callbackDispatcher,
        isInDebugMode: true, // Set to false in production
      );
      
      _isInitialized = true;
      _initCompleter.complete();
      debugPrint("BackgroundSchedulerService initialized");
    } catch (e, stack) {
      debugPrint("Initialization failed: $e\n$stack");
      _initCompleter.completeError(e);
      rethrow;
    }
    return _initCompleter.future;
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
            inputData['userId']?.toString() ?? '',
            inputData['deviceId']?.toString() ?? '',
            inputData['portKey']?.toString() ?? '',
            inputData['turnOn'] == true,
            inputData['sceneName']?.toString(),
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
    _cancelAllTimers();
    await Workmanager().cancelAll();
  }

  Future<void> _scheduleSceneTasks(String userId, String sceneName) async {
    final sceneData = await _getSceneData(userId, sceneName);
    if (sceneData == null) {
      debugPrint("No scene data found for $sceneName");
      return;
    }

    debugPrint("Scheduling tasks for scene: $sceneName");
    
    for (final deviceEntry in sceneData.entries) {
      final deviceId = deviceEntry.key;
      final ports = (deviceEntry.value["ports"] as Map<String, dynamic>? ?? {});
      
      for (final portEntry in ports.entries) {
        final portKey = portEntry.key;
        final portData = portEntry.value as Map<String, dynamic>;
        
        _schedulePortActions(
          userId: userId,
          deviceId: deviceId,
          portKey: portKey,
          portData: portData,
          sceneName: sceneName,
        );
      }
    }
  }
  

  void _schedulePortActions({
    required String userId,
    required String deviceId,
    required String portKey,
    required Map<String, dynamic> portData,
    required String sceneName,
  }) {
    try {
      final onTimeStr = portData["onTime"]?.toString();
      final offTimeStr = portData["offTime"]?.toString();
      
      debugPrint("Scheduling actions for $deviceId-$portKey");
      
      if (onTimeStr != null && onTimeStr.isNotEmpty) {
        final onDateTime = _parseSceneDateTime(onTimeStr);
        if (onDateTime != null) {
          _scheduleDeviceAction(
            userId: userId,
            deviceId: deviceId,
            portKey: portKey,
            triggerTime: onDateTime,
            turnOn: true,
            sceneName: sceneName,
          );
        } else {
          debugPrint("Failed to parse ON time: $onTimeStr");
        }
      }
      
      if (offTimeStr != null && offTimeStr.isNotEmpty) {
        final offDateTime = _parseSceneDateTime(offTimeStr);
        if (offDateTime != null) {
          _scheduleDeviceAction(
            userId: userId,
            deviceId: deviceId,
            portKey: portKey,
            triggerTime: offDateTime,
            turnOn: false,
            sceneName: sceneName,
          );
        } else {
          debugPrint("Failed to parse OFF time: $offTimeStr");
        }
      }
    } catch (e) {
      debugPrint("Error scheduling port actions: $e");
    }
  }

  Future<Map<String, dynamic>?> _getSceneData(String userId, String sceneName) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final storedJson = prefs.getString(_espDeviceKey);
      if (storedJson == null) {
        debugPrint("No ESP device data found in SharedPreferences");
        return null;
      }

      final storedData = jsonDecode(storedJson) as Map<String, dynamic>;
      if (!storedData.containsKey(userId)) {
        debugPrint("No data found for user $userId");
        return null;
      }

      final userDevices = storedData[userId] as Map<String, dynamic>;
      final scenes = userDevices["scenes"] as Map<String, dynamic>?;
      
      if (scenes == null || !scenes.containsKey(sceneName)) {
        debugPrint("Scene $sceneName not found for user $userId");
        return null;
      }
      
      return scenes[sceneName] as Map<String, dynamic>;
    } catch (e) {
      debugPrint("Error getting scene data: $e");
      return null;
    }
  }

   void _scheduleDeviceAction({
    required String userId,
    required String deviceId,
    required String portKey,
    required DateTime triggerTime,
    required bool turnOn,
    required String sceneName,
  }) async{
    try {
      final location = tz.getLocation('Asia/Kolkata');
      final now = tz.TZDateTime.now(location);
      var scheduledTime = tz.TZDateTime.from(triggerTime, location);

      if (scheduledTime.isBefore(now)) {
        scheduledTime = tz.TZDateTime(
          location,
          now.year,
          now.month,
          now.day + 1,
          scheduledTime.hour,
          scheduledTime.minute,
        );
      }

      final delay = scheduledTime.difference(now);
      if (delay.isNegative) {
        debugPrint("Scheduled time is in the past, skipping");
        return;
      }

      final taskId = '${sceneName}_${deviceId}_${portKey}_${turnOn ? 'on' : 'off'}'
          .replaceAll(RegExp(r'[^a-zA-Z0-9_]'), '_');

      debugPrint('''
        Scheduling ${turnOn ? 'ON' : 'OFF'} action for:
        - Device: $deviceId-$portKey
        - At: ${DateFormat('hh:mm a MMM d, yyyy').format(scheduledTime)}
        - In: ${delay.inHours}h ${delay.inMinutes.remainder(60)}m
      ''');

      Workmanager().registerOneOffTask(
        taskId,
        'deviceActionTask',
        inputData: {
          'userId': userId,
          'deviceId': deviceId,
          'portKey': portKey,
          'turnOn': turnOn,
          'sceneName': sceneName,
          'scheduledTime': scheduledTime.toUtc().toIso8601String(),
        },
        initialDelay: delay,
        constraints: Constraints(
          networkType: NetworkType.connected,
          requiresBatteryNotLow: true,
        ),
        existingWorkPolicy: ExistingWorkPolicy.replace,
      );
    } catch (e, stack) {
      debugPrint("Failed to schedule device action: $e\n$stack");
      // Perform immediate execution with cleanup
    final success = await _executeDeviceAction(userId, deviceId, portKey, turnOn, sceneName);
    if (!success && !turnOn) {
      await _cleanupAutomaticOnOff(userId, deviceId, portKey);
      await _cleanupScene(userId, sceneName);
    }
  }
  }

@pragma('vm:entry-point')
static Future<bool> _executeDeviceAction(
  String userId,
  String deviceId,
  String portKey, 
  bool turnOn,
  String? sceneName,
) async {
  try {
    final numericPort = portKey.startsWith('port') 
        ? portKey.substring(4) 
        : portKey.replaceAll(RegExp(r'[^0-9]'), '');

    if (numericPort.isEmpty) {
      debugPrint("Invalid port format: $portKey");
      return false;
    }

    await Firebase.initializeApp(
      name: 'BackgroundExecution',
      options: Firebase.app().options,
    );
    
    final deviceRef = FirebaseDatabase.instance.ref("users/$userId/Espdevice/$deviceId");
    final deviceSnapshot = await deviceRef.get();
    
    if (!deviceSnapshot.exists) {
      debugPrint("Device $deviceId not found");
      return false;
    }
    
    final actualDeviceName = deviceSnapshot.child('name').value?.toString() ?? '';
    if (actualDeviceName.isEmpty) {
      debugPrint("Empty device name");
      return false;
    }

    final firebasePath = "users/$userId/components/${actualDeviceName}_$numericPort";
    await FirebaseDatabase.instance.ref(firebasePath).set(turnOn ? 1 : 0);
    debugPrint("Firebase updated: $firebasePath = ${turnOn ? 1 : 0}");

    // Cleanup AutomaticOnOff when turning off
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
      actualDeviceName: actualDeviceName,
      sceneName: sceneName,
    );
    
    return true;
  } catch (e, stack) {
    debugPrint("Background execution failed: $e\n$stack");
    return false;
  }
}

@pragma('vm:entry-point')
static Future<void> _updateLocalState({
  required String userId,
  required String deviceId,
  required String portKey, 
  required bool turnOn,
  required String actualDeviceName,
  String? sceneName,
}) async {
  try {
    final prefs = await SharedPreferences.getInstance();
    final data = jsonDecode(prefs.getString('EspDevice') ?? '{}') as Map<String, dynamic>;
    
    data
      .putIfAbsent(userId, () => {})
      .putIfAbsent(deviceId, () => {'name': actualDeviceName, 'ports': {}});
    
    final prefPortKey = portKey.startsWith('port') ? portKey : 'port$portKey';
    
    data[userId][deviceId]['ports'][prefPortKey] = {
      'state': turnOn,
    };

    if (!turnOn) {
      // Clean AutomaticOnOff from SharedPreferences
      if (data[userId].containsKey('AutomaticOnOff') &&
          data[userId]['AutomaticOnOff'] is Map &&
          data[userId]['AutomaticOnOff'].containsKey(deviceId)) {
        (data[userId]['AutomaticOnOff'][deviceId] as Map).remove(portKey);
        debugPrint("Removed AutomaticOnOff from SharedPreferences: $deviceId/$portKey");
      }

      // Clean scene from SharedPreferences if provided
      if (sceneName != null && 
          data[userId].containsKey('scenes') &&
          data[userId]['scenes'] is Map) {
        (data[userId]['scenes'] as Map).remove(sceneName);
        debugPrint("Removed scene '$sceneName' from SharedPreferences");
      }
    }
    // Save changes
    final success = await prefs.setString('EspDevice', jsonEncode(data));
    debugPrint("SharedPrefs update ${success ? 'successful' : 'failed'}");
  } catch (e, stack) {
    debugPrint("Local state update failed: $e\n$stack");
  }
}

static Future<void> _cleanupScene(String userId, String sceneName) async {
  try {
    // Firebase cleanup
    final sceneRef = FirebaseDatabase.instance.ref("users/$userId/scenes/$sceneName");
    await sceneRef.remove();
    debugPrint("Removed scene '$sceneName' from Firebase");

    // SharedPreferences cleanup
    final prefs = await SharedPreferences.getInstance();
    final rawData = prefs.getString('EspDevice');
    if (rawData != null) {
      final data = jsonDecode(rawData) as Map<String, dynamic>;
      if (data.containsKey(userId) &&
          data[userId].containsKey('scenes') &&
          data[userId]['scenes'] is Map) {
        (data[userId]['scenes'] as Map).remove(sceneName);
        await prefs.setString('EspDevice', jsonEncode(data));
        debugPrint("Removed scene '$sceneName' from SharedPreferences");
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
    // Firebase cleanup
    final autoOffPath = "users/$userId/AutomaticOnOff/$deviceId/$portKey";
    await FirebaseDatabase.instance.ref(autoOffPath).remove();
    debugPrint("Removed AutomaticOnOff from Firebase: $autoOffPath");

    // SharedPreferences cleanup
    final prefs = await SharedPreferences.getInstance();
    final rawData = prefs.getString('EspDevice');
    if (rawData != null) {
      final data = jsonDecode(rawData) as Map<String, dynamic>;
      if (data.containsKey(userId) &&
          data[userId].containsKey('AutomaticOnOff') &&
          data[userId]['AutomaticOnOff'] is Map &&
          (data[userId]['AutomaticOnOff'] as Map).containsKey(deviceId)) {
        
        (data[userId]['AutomaticOnOff'][deviceId] as Map).remove(portKey);
        
        // Remove empty device entries
        if ((data[userId]['AutomaticOnOff'][deviceId] as Map).isEmpty) {
          (data[userId]['AutomaticOnOff'] as Map).remove(deviceId);
        }
        
        await prefs.setString('EspDevice', jsonEncode(data));
        debugPrint("Removed AutomaticOnOff from SharedPreferences: $deviceId/$portKey");
      }
    }
  } catch (e, stack) {
    debugPrint("AutomaticOnOff cleanup failed: $e\n$stack");
  }
}

  DateTime? _parseSceneDateTime(String dateTimeStr) {
  try {
    final cleanedInput = dateTimeStr.replaceAll(RegExp(r'\s+'), ' ').trim();
    debugPrint("Cleaned time string: '$cleanedInput'");

    final indiaLocation = tz.getLocation('Asia/Kolkata');
    final now = tz.TZDateTime.now(indiaLocation);

    try {
      final format = DateFormat('hh:mm a MMM d, yyyy');
      final parsedDate = format.parse(cleanedInput);
      var localTime = tz.TZDateTime.from(parsedDate, indiaLocation);

      if (localTime.isBefore(now)) {
        localTime = tz.TZDateTime(
          indiaLocation,
          now.year,
          now.month,
          now.day + 1,
          localTime.hour,
          localTime.minute
        );
      }

      debugPrint("""
        Successfully parsed time:
        Input: '$cleanedInput'
        IST Time: ${localTime.toString()}
        Now (IST): ${now.toString()}
      """);

      return localTime;
    } catch (e) {
      debugPrint("Failed to parse with AM/PM format: $e");
    }

    final fallbackFormats = [
      'hh:mm a MMM d yyyy',    
      'hh:mm a MMM d, yyyy z', 
      'yyyy-MM-dd HH:mm:ss',   
    ];

    for (final fmt in fallbackFormats) {
      try {
        final format = DateFormat(fmt);
        final parsedDate = format.parse(cleanedInput);
        var localTime = tz.TZDateTime.from(parsedDate, indiaLocation);

        if (localTime.isBefore(now)) {
          localTime = tz.TZDateTime(
            indiaLocation,
            now.year,
            now.month,
            now.day + 1,
            localTime.hour,
            localTime.minute
          );
        }

        debugPrint("Parsed with fallback format '$fmt': ${localTime.toString()}");
        return localTime;
      } catch (e) {
        debugPrint("Failed to parse with format '$fmt': $e");
      }
    }

    debugPrint("Using fallback time (now + 1min)");
    return now.add(Duration(minutes: 1));
  } catch (e) {
    debugPrint("Critical error in time parsing: $e");
    return null;
  }
}

  void _cancelAllTimers() {
    debugPrint("Cancelling all active timers (${_activeTimers.length})");
    _activeTimers.forEach((key, timer) {
      timer.cancel();
      debugPrint("Cancelled timer: $key");
    });
    _activeTimers.clear();
  }
}