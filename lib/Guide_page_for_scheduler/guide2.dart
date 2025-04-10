// import 'package:firebase_auth/firebase_auth.dart';
// import 'package:firebase_database/firebase_database.dart';
// import 'package:flutter/material.dart';
// import 'package:home_auto_sample/scheduler/NoScenePage.dart';
// import 'package:home_auto_sample/scheduler/available_scene_page.dart';

// class Guide2 extends StatelessWidget {
//   const Guide2({super.key});

//   void _fetchScenes(BuildContext context) async {
//     User? user = FirebaseAuth.instance.currentUser;
//     if (user == null) {
//       Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const NoScenePage()));
//       return;
//     }

//     String userId = user.uid;
//     DatabaseReference scenesRef = FirebaseDatabase.instance.ref("users/$userId/scenes");

//     try {
//       DatabaseEvent event = await scenesRef.once();
//       DataSnapshot snapshot = event.snapshot;
//       List<String> sceneNames = [];

//       if (snapshot.exists && snapshot.value != null) {
//         Map<dynamic, dynamic> scenesData = snapshot.value as Map<dynamic, dynamic>;
//         sceneNames = scenesData.keys.map((key) => key.toString()).toList();
//       }

//       if (sceneNames.isNotEmpty) {
//         Navigator.pushReplacement(
//           context,
//           MaterialPageRoute(builder: (context) => AvailableScenesPage()),
//         );
//       } else {
//         Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const NoScenePage()));
//       }
//     } catch (error) {
//       debugPrint("Error fetching scenes: $error");
//       Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => const NoScenePage()));
//     }
//   }

//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//       appBar: PreferredSize(
//         preferredSize: const Size.fromHeight(50),
//         child: AppBar(
//           automaticallyImplyLeading: false,
//           flexibleSpace: Stack(
//             children: [
//               Positioned(
//                 child: Image.asset(
//                   'assets/images/corner design.png',
//                   fit: BoxFit.cover,
//                 ),
//               ),
//               Positioned(
//                 top: 0,
//                 bottom: 0,
//                 right: 0,
//                 child: Image.asset(
//                   'assets/images/triangle design.png',
//                 ),
//               ),
//               Positioned(
//                 top: 45,
//                 left: 0,
//                 child: IconButton(
//                   onPressed: () => Navigator.pop(context),
//                   icon: const Icon(Icons.arrow_back_rounded, color: Colors.black, weight: 900),
//                 ),
//               ),
//             ],
//           ),
//         ),
//       ),
//       body: Column(
//         crossAxisAlignment: CrossAxisAlignment.center,
//         children: [
//           Padding(
//             padding: const EdgeInsets.only(left: 0, top: 40),
//             child: Align(
//               alignment: Alignment.topLeft,
//               child: Image.asset('assets/images/guide.png'),
//             ),
//           ),
//           Stack(
//             children: [
//               Positioned(
//                 top: 20,
//                 bottom: 20,
//                 right: 5,
//                 left: 15,
//                 child: Image.asset(
//                   'assets/images/home_9.2.png',
//                   fit: BoxFit.contain,
//                   width: 200,
//                   height: 400,
//                 ),
//               ),
//               Positioned(
//                 left: 0,
//                 top: 0,
//                 bottom: 0,
//                 right: 17,
//                 child: Image.asset(
//                   'assets/images/home_9.png',
//                   width: 400,
//                   height: 400,
//                 ),
//               ),
//               const Positioned(
//                 top: 35,
//                 left: 0,
//                 right: 0,
//                 child: Text(
//                   'Reset the devices',
//                   textAlign: TextAlign.center,
//                   style: TextStyle(
//                     fontSize: 24,
//                     fontWeight: FontWeight.bold,
//                     color: Colors.black,
//                   ),
//                 ),
//               ),
//               Center(
//                 child: Image.asset(
//                   'assets/images/home_10.png',
//                   width: 500,
//                   height: 400,
//                 ),
//               ),
//               const Positioned(
//                 bottom: 20,
//                 left: 0,
//                 right: 0,
//                 child: Text(
//                   " Wait for the buzzer to emit three \n"
//                   " beeps. These three beeps indicate \n"
//                   " the device search started.\n",
//                   textAlign: TextAlign.center,
//                   style: TextStyle(
//                     fontSize: 16,
//                     fontWeight: FontWeight.normal,
//                     color: Colors.black,
//                   ),
//                 ),
//               ),
//             ],
//           ),
//           const SizedBox(height: 50),
//           Builder(
//             builder: (context) {
//               return TextButton(
//                 style: TextButton.styleFrom(
//                   backgroundColor: const Color.fromARGB(255, 0, 188, 212),
//                 ),
//                 onPressed: () => _fetchScenes(context),
//                 child: const Text(
//                   '   Done   ',
//                   style: TextStyle(color: Colors.white, fontSize: 15),
//                 ),
//               );
//             },
//           ),
//         ],
//       ),
//     );
//   }
// }

