// import 'package:flutter/material.dart';
// import 'package:home_auto_sample/Guide_page_for_scheduler/guide2.dart';

// class Guide1 extends StatelessWidget {
//   const Guide1({super.key});

//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//   appBar: PreferredSize(
//     preferredSize: const Size.fromHeight(50),
//     child: AppBar(
//       automaticallyImplyLeading: false,
//       flexibleSpace: Stack(
//         children: [
//           Positioned(
//             child: Image.asset(
//               'assets/images/corner design.png',
//               fit: BoxFit.cover,
//             ),
//           ),
//           Positioned(
//             top: 0,
//             bottom: 0,
//             right: 0,
//             child: Image.asset(
//               'assets/images/triangle design.png',
//             ),
//           ),
//           Positioned(
//             top: 45,
//             left: 0,
//             child: IconButton(
//               onPressed: () => Navigator.pop(context),
//               icon: const Icon(Icons.arrow_back_rounded, color: Colors.black, weight: 900,),
//             ),
//           ),
//         ],
//       ),
//     ),
//   ),
//         body: Column(
//           crossAxisAlignment: CrossAxisAlignment.center,
//           children: [
//             Padding(
//             padding: const EdgeInsets.only(left: 0, top: 40),
//             child: Align(
//               alignment: Alignment.topLeft,
//               child: Image.asset('assets/images/guide.png'),
//             ),
//           ),
//             Stack(
//               children: [
//                 Positioned(
//                   top: 20,
//                   bottom: 20,
//                   right: 5,
//                   left: 15,
//                   child: Image.asset(
//                     'assets/images/home_9.2.png',
//                     fit: BoxFit.contain,
//                     width: 200,
//                     height: 400,
//                   ),
//                 ),
//                 Positioned(
//                   left:0,
//                   top: 0,
//                   bottom: 0,
//                   right: 17,
//                   //height: 20,
//                   child: Image.asset(
//                     'assets/images/home_9.png',
//                     width: 400,
//                     height: 400,
//                   ),
//                 ),
//                 const Positioned(
//                   top: 35, 
//                  left: 0,
//                  right: 0,
//                   child: Text(
//                     'Reset the devices',
//                     textAlign: TextAlign.center,
//                     style: TextStyle(
//                       fontSize: 24,
//                       fontWeight: FontWeight.bold,
//                       color: Colors.black, 
//                     ),
//                   ),
//                 ),
//                 Center(
//                   child: Image.asset(
//                     'assets/images/home_9.1.png',
//                     width: 500,  
//                     height: 400,  
//                   ),
//                 ),
//                 const Positioned(
//                   bottom: 35, 
//                   left: 0,
//                   right: 0,
//                   child: Text(
//                     'Switch on-off-on-off-on',
//                     textAlign: TextAlign.center,
//                     style: TextStyle(
//                       fontSize: 18,
//                       fontWeight: FontWeight.normal,
//                       color: Colors.black, 
//                     ),
//                   ),
//                 ),
//               ],
//             ),
//             const SizedBox(height: 50),
//             Builder(
//               builder: (context) {
//                 return TextButton(
//                   style: TextButton.styleFrom(
//                     backgroundColor: const Color.fromARGB(255, 0, 188, 212),
//                   ),
//                   onPressed: () {
//                     Navigator.of(context).push(
//                       MaterialPageRoute(builder: (context) => const Guide2()),
//                     );
//                   },
//                   child: const Text(
//                     'Next  -->',
//                     style: TextStyle(color: Colors.white,fontSize: 15),
//                   ),
//                 );
//               },
//             ),
//           ],
//         ),
//       );
    
//   }
// }
