
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:openhab_testing/login&signup/login_page.dart';
import 'package:openhab_testing/login&signup/sign_up_page.dart';
import 'package:openhab_testing/second_page.dart';


void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(
      options: const FirebaseOptions(
          apiKey: "AIzaSyC6tPaeMKjEPVxW6UYBQrX_aqPCgFW7H-M",
  authDomain: "home-automation-e7446.firebaseapp.com",
  databaseURL: "https://home-automation-e7446-default-rtdb.firebaseio.com",
  projectId: "home-automation-e7446",
  storageBucket: "home-automation-e7446.firebasestorage.app",
  messagingSenderId: "23896675164",
  appId: "1:23896675164:web:68c26e1d513d60bf9b2799"
          ));

  runApp(
      const MyApp(),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
      
    return   MaterialApp(
      theme: ThemeData.light(), // Light theme
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
