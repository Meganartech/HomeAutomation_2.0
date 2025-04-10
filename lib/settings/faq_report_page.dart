import 'package:flutter/material.dart';
import 'package:home_auto_sample/add_devices_and_delete/no_device_page.dart';
import 'package:home_auto_sample/scheduler/scheduler_page.dart';
import 'package:home_auto_sample/tabBar/custom_appbar.dart';

import 'settings_1_page.dart';

class FaqReportPage extends StatefulWidget {
  const FaqReportPage({super.key});

  @override
  State<FaqReportPage> createState() => _FaqReportPageState();
}

class _FaqReportPageState extends State<FaqReportPage> {
  int currentIndex = 2;

  void _onTabtapped(int index) {
    setState(() {
      currentIndex = index;
    });
    switch (index) {
      case 0:
        Navigator.of(context)
            .push(MaterialPageRoute(builder: (context) => const NoDevicePage()));
        break;
      case 1:
        Navigator.of(context)
            .push(MaterialPageRoute(builder: (context) => const SchedulerPage()));
        break;
      case 2:
        Navigator.of(context)
            .push(MaterialPageRoute(builder: (context) => const Settings1Page()));
        break;
    }
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
                //left: 200,
                child: Image.asset(
                  'assets/images/home_14.8.png', // Replace with your image path
                  // fit: BoxFit, // Adjust the image to fill the space
                ),
              ),
            ],
          ),
        ),
      ),
      body: Stack(
        children: [
          SafeArea(
            left: true,
            child: SingleChildScrollView(
              child: Padding(
                padding: const EdgeInsets.only(top: 6.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Padding(
                      padding: EdgeInsets.only(left: 5),
                      child: Text(
                        'Welcome',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 18,
                        ),
                      ),
                    ),
                    const SizedBox(
                      height: 10,
                    ),
                    const Divider(
                      height: 2,
                      color: Colors.black,
                    ),
                    const SizedBox(
                      height: 10,
                    ),
                    const Center(
                      child: Stack(
                        alignment: Alignment.center,
                        children: [
                          ImageIcon(
                            AssetImage(
                              'assets/images/home_14.6.png',
                            ),
                            size: 50,
                            color: Colors.red,
                          ),
                          CircleAvatar(
                            radius: 20, // Adjust size as needed
                            backgroundImage:
                                AssetImage('assets/images/home_14.5.png'),
                          ),
                        ],
                      ),
                    ),
                    // const SizedBox(
                    //   height: 5,
                    // ),
                    Stack(
                      children: [
                        Image.asset(
                          'assets/images/Rectangle.png',
                          height: MediaQuery.sizeOf(context).height * 0.08,
                          // width: MediaQuery.sizeOf(context).width * 0.09,
                        ),
                        const Padding(
                          padding:
                              EdgeInsets.only(left: 10, top: 30, bottom: 2),
                          child: Text(
                            'Report Issue',
                            style: TextStyle(color: Colors.white, fontSize: 18),
                          ),
                        ),
                      ],
                    ),
                    SizedBox(height: MediaQuery.sizeOf(context).height * 0.07),
                    const Padding(
                      padding: EdgeInsets.all(8.0),
                      child: Text('Select One Issue:',style: TextStyle(color: Color.fromARGB(255, 110, 19, 126),fontWeight: FontWeight.bold,fontSize: 22),),

                    ),
                    Padding(
                      padding: const EdgeInsets.all(3.0),
                      child: _buildSettingsOption(title: 'App not working ', onTap: (){}),
                    ),

                     Padding(
                       padding: const EdgeInsets.all(3.0),
                       child: _buildSettingsOption(title: 'Iot device not working ', onTap: (){}),
                     ),
                     
                      Padding(
                        padding: const EdgeInsets.all(3.0),
                        child: _buildSettingsOption(title: 'Others ', onTap: (){}),
                      ),
                      SizedBox(height: MediaQuery.sizeOf(context).height * 0.07),
                       Center(
                         child: ElevatedButton(
                                     onPressed: () {
                                       // Call your delete account API or perform the delete action here
                                       //Navigator.of(context).pop(); // Dismiss the dialog
                                       // Navigator.push or pop to home page after delete if necessary
                                     },
                                     style: ElevatedButton.styleFrom(
                                      // shape:RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)) ,
                                       backgroundColor: Colors.purple, // Customize the delete button color
                                     ),
                                     //icon: const Icon(Icons.delete, color: Colors.white), // Icon inside button
                                     child: const Text('Submit',style: TextStyle(color:Colors.white),),
                                   ),
                       ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
      bottomNavigationBar:
          CustomTabBar(currentIndex: currentIndex, onTabTapped: _onTabtapped),
    );
  }

  Widget _buildSettingsOption(
      {
        //required String image,
      required String title,
      required VoidCallback onTap}) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 10),
        decoration: BoxDecoration(
            color: Colors.white60,
            border: Border(bottom: BorderSide(color: Colors.grey.shade200))),
        child: Row(
          children: [
            const SizedBox(
              width: 10,
            ),
            // Image.asset(
            //   image,
            //   color: Colors.black87,
            // ),
            const SizedBox(width: 15),
            Text(
              title,
              style: const TextStyle(
                color: Colors.black87,
                fontSize: 16,
                fontWeight: FontWeight.w400,
              ),
            ),
            const Spacer(),
          ],
        ),
      ),
    );
  }


}

// void main() {
//   runApp(MaterialApp(
//     home: Settings1Page(),
//     debugShowCheckedModeBanner: false,
//   ));
// }
