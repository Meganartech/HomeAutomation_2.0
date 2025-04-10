import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/scheduler/NoScenePage.dart';
import 'package:home_auto_sample/scheduler/available_scene_page.dart';

class SceneDeletePage extends StatefulWidget { 

  const SceneDeletePage({super.key});

  @override
  _SceneDeletePageState createState() => _SceneDeletePageState();
}

class _SceneDeletePageState extends State<SceneDeletePage> {
  @override
  void initState() {
    super.initState();
    Future.delayed(const Duration(seconds: 3), () {
      _fetchScenes(context);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: PreferredSize(
          preferredSize: const Size.fromHeight(50),
          child: AppBar(
            automaticallyImplyLeading: false,
            flexibleSpace: Stack(
              children: [
                Positioned(
                  child: Image.asset(
                    'assets/images/home_14.7.png',
                    fit: BoxFit.cover,
                  ),
                ),
                Positioned(
                  top: 0,
                  bottom: 0,
                  right: 0,
                  child: Image.asset(
                    'assets/images/home_14.8.png',
                  ),
                ),
              ],
            ),
          ),
        ),
      body: Padding(
        padding: const EdgeInsets.only(top: 80),
        child: Container(
          padding: const EdgeInsetsDirectional.all(50),
          margin: const EdgeInsets.all(25),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Image.asset('assets/images/home_7.png'),
              const SizedBox(height: 20),
              const Text(
                'Scene Deleted successfully',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 50),
            ],
          ),
        ),
      ),
    );
  }
  
    void _fetchScenes(BuildContext context) async {
    User? user = FirebaseAuth.instance.currentUser;
    if (user == null) {
      Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const NoScenePage()));
      return;
    }

    String userId = user.uid;
    DatabaseReference scenesRef = FirebaseDatabase.instance.ref("users/$userId/scenes");

    try {
      DatabaseEvent event = await scenesRef.once();
      DataSnapshot snapshot = event.snapshot;
      List<String> sceneNames = [];

      if (snapshot.exists && snapshot.value != null) {
        Map<dynamic, dynamic> scenesData = snapshot.value as Map<dynamic, dynamic>;
        sceneNames = scenesData.keys.map((key) => key.toString()).toList();
      }

      if (sceneNames.isNotEmpty) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (context) => AvailableScenesPage()),
        );
      } else {
        Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const NoScenePage()));
      }
    } catch (error) {
      debugPrint("Error fetching scenes: $error");
      Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const NoScenePage()));
    }
  }
}

