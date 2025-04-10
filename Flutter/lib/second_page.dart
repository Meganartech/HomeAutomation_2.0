import 'package:flutter/material.dart';
import 'package:home_auto_sample/login&signup/sign_up_page.dart';

class SecondPage extends StatelessWidget {
  const SecondPage({super.key});

  @override
  Widget build(BuildContext context) {
    return  Scaffold(
        appBar: AppBar(
          automaticallyImplyLeading: false,
        ),
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Image.asset('assets/images/home_1.png'),
            const SizedBox(height: 10),
             // Page Indicators (Now the second one is bigger)
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // First Indicator (Small)
              Container(
                width: 5,
                height: 5,
                decoration: BoxDecoration(
                  color: Colors.blueGrey[50],
                  borderRadius: BorderRadius.circular(10),
                ),
              ),
              const SizedBox(width: 3),
              // Second Indicator (Big)
              Container(
                width: 15,
                height: 5,
                decoration: BoxDecoration(
                  color: const Color.fromARGB(255, 184, 161, 246),
                  borderRadius: BorderRadius.circular(10),
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),
            const Text(
              'Power in your hands',
              style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 5),
            const Text(
              'You can control your',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 16,
              ),
            ),const SizedBox(height: 5),
            const Text(
              'devices from anywhere , anytime.',
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
                    // Use the correct context here
                    Navigator.of(context).pushReplacement(
                      MaterialPageRoute(
                          builder: (context) => const SignupPage()),
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

