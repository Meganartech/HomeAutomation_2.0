import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/scheduler/background_sheduler.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:workmanager/workmanager.dart';
import 'package:timezone/timezone.dart' as tz;
import 'package:timezone/data/latest.dart' as tz;

@pragma('vm:entry-point')
void callbackDispatcher() {
  // Initialize binding FIRST
  WidgetsFlutterBinding.ensureInitialized();

  Workmanager().executeTask((taskName, inputData) async {
    SharedPreferences? prefs;
    try {
      prefs = await SharedPreferences.getInstance();
      debugPrint("SharedPreferences initialized in background task");
      try {
        if (Firebase.apps.isEmpty) {
          await Firebase.initializeApp(
            name: 'BackgroundTask',
            options: const FirebaseOptions(
          apiKey: "AIzaSyAuea-x6Y8clAUO2xuFE8Z79RcltpxoPP4",
          authDomain: "smart-home-19334.firebaseapp.com",
          databaseURL: "https://smart-home-19334-default-rtdb.firebaseio.com",
          projectId: "smart-home-19334",
          storageBucket: "smart-home-19334.firebasestorage.app",
          messagingSenderId: "856846462492",
          appId: "1:856846462492:web:20e5d9fbe843c5705039d9",
          measurementId: "G-37NJX1HHMG"));
        }
      } catch (e) {
        debugPrint("Firebase init failed: $e");
        return false;
      }
      
      tz.initializeTimeZones();
      final location = tz.getLocation('Asia/Kolkata');
      final now = tz.TZDateTime.now(location);

      if (inputData == null || inputData['scheduledTime'] == null) {
        debugPrint("Invalid input data in background task");
        return false;
      }

      final scheduledTime = tz.TZDateTime.from(
        DateTime.tryParse(inputData['scheduledTime']) ?? now,
        location
      );
      
      if (now.difference(scheduledTime).abs().inMinutes > 60) {
        debugPrint("Task expired in background");
        return false;
      }

      return await BackgroundSchedulerService.executeBackgroundTask(
        taskName: taskName,
        inputData: Map<String, dynamic>.from(inputData),
        prefs: prefs,
      );
    } catch (e, stack) {
      debugPrint("Background task error: $e\n$stack");
      return false;
    }
  });
}