
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/custom%20black%20theme/custom_black.dart';
import 'package:home_auto_sample/login&signup/login_page.dart';
import 'package:home_auto_sample/scheduler/background_sheduler.dart';
import 'package:provider/provider.dart';
import 'package:home_auto_sample/login&signup/sign_up_page.dart';
import 'package:home_auto_sample/second_page.dart';
import 'package:home_auto_sample/settings/themeSettings.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(
      options: const FirebaseOptions(
          apiKey: "AIzaSyAuea-x6Y8clAUO2xuFE8Z79RcltpxoPP4",
          authDomain: "smart-home-19334.firebaseapp.com",
          databaseURL: "https://smart-home-19334-default-rtdb.firebaseio.com",
          projectId: "smart-home-19334",
          storageBucket: "smart-home-19334.firebasestorage.app",
          messagingSenderId: "856846462492",
          appId: "1:856846462492:web:20e5d9fbe843c5705039d9",
          measurementId: "G-37NJX1HHMG"));


   // Initialize Background Scheduler
  final scheduler = BackgroundSchedulerService();
  await scheduler.initialize();
    

  runApp(
      MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ThemeNotifier()),
        Provider<BackgroundSchedulerService>.value(value: scheduler),
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    final themeNotifier = Provider.of<ThemeNotifier>(context);
      
    return   MaterialApp(
      theme: ThemeData.light(), // Light theme
      darkTheme: mildBlackTheme, // Custom mild black theme
      themeMode: themeNotifier.themeMode, // Dynamic theme mode
      debugShowCheckedModeBanner: false,
      initialRoute: '/', // Set initial route
      routes: {
        '/': (context) => const HomePage(), // Set the home pagev
        '/second' : (context) => const SecondPage(),
        '/signup': (context) => const SignupPage(), // Set the signup page
        '/login': (context) => const LoginPage(),
        // Add other routes here as needed
      },
       );
  }
}


class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
         // title: const Text('Home_auto_sample'),
        ),
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [ 
            Image.asset('assets/images/home_0.png'),
            const SizedBox(height: 10),
              // Page Indicators
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // First Indicator (Big)
              Container(
                width: 15,
                height: 5,
                decoration: BoxDecoration(
                  color: const Color.fromARGB(255, 184, 161, 246),
                  borderRadius: BorderRadius.circular(10),
                ),
              ),
              const SizedBox(width: 3),
              // Second Indicator (Small)
              Container(
                width: 5,
                height: 5,
                decoration: BoxDecoration(
                  color: Colors.blueGrey[50],
                  borderRadius: BorderRadius.circular(10),
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),
            const Text(
              'Manage your devices anywhere',
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 5),
            const Text(
              'Experience the benefits of',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 16,
              ),
            ),
            const SizedBox(height: 5),
            const Text(
              'intelligent home management.',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 16,
              ),
            ),
            const SizedBox(
              height: 40,
            ),
            Builder(
              builder: (context) {
                return TextButton(
                  style: TextButton.styleFrom(
                    backgroundColor: const Color.fromARGB(255, 184, 161, 246),
                  ),
                  onPressed: () {
                     Navigator.of(context).pushReplacement(
                      MaterialPageRoute(
                          builder: (context) => const SecondPage()),
                    );
                  },
                  child: const Text(
                    'Next',
                    style: TextStyle(color: Colors.white),
                  ),
                );
              },
            ),
          ],
        ),
      );
  }
}
