import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/scheduler/background_service.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:workmanager/workmanager.dart';
import 'package:timezone/timezone.dart' as tz;
import 'package:timezone/data/latest.dart' as tz;

@pragma('vm:entry-point')
void callbackDispatcher() {
  // Initialize binding FIRST
  WidgetsFlutterBinding.ensureInitialized();

  Workmanager().executeTask((taskName, inputData) async {
    try {
      // Initialize timezones
      tz.initializeTimeZones();

      // Initialize Firebase with retry logic
      bool firebaseInitialized = false;
      int attempts = 0;
      const maxAttempts = 3;
      const retryDelay = Duration(seconds: 1);
      
      while (!firebaseInitialized && attempts < maxAttempts) {
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
                measurementId: "G-37NJX1HHMG"
              ),
            );
          }
          firebaseInitialized = true;
        } catch (e) {
          attempts++;
          debugPrint("Firebase init attempt $attempts failed: $e");
          if (attempts >= maxAttempts) {
            return false;
          }
          await Future.delayed(retryDelay);
        }
      }

      if (!firebaseInitialized) {
        debugPrint("Failed to initialize Firebase after $maxAttempts attempts");
        return false;
      }

      // Get SharedPreferences instance
      final prefs = await SharedPreferences.getInstance();

      // Convert input data to proper format
      final Map<String, dynamic> taskData = Map<String, dynamic>.from(inputData ?? {});

      // Verify task expiration for scheduled tasks
      if (taskName == 'deviceActionTask') {
        final location = tz.getLocation('Asia/Kolkata');
        final now = tz.TZDateTime.now(location);
        final scheduledTime = taskData['scheduledTime'] != null 
            ? tz.TZDateTime.from(DateTime.parse(taskData['scheduledTime']), location)
            : null;
            
        if (scheduledTime == null || now.difference(scheduledTime).abs().inMinutes > 60) {
          debugPrint("Task expired or missing scheduled time");
          return false;
        }
      }

      // Delegate task execution to BackgroundService
      return await BackgroundService.executeBackgroundTask(
        taskName: taskName,
        inputData: taskData,
        prefs: prefs,
      );

    } catch (e, stack) {
      debugPrint("Background task error: $e\n$stack");
      return false;
    }
  });
}