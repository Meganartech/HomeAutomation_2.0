
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/add_devices_and_delete/no_device_page.dart';
import 'package:home_auto_sample/components/mytextfield.dart';
import 'package:home_auto_sample/settings/change_password.dart';
import 'package:home_auto_sample/settings/settings_1_page.dart';

import '../scheduler/scheduler_page.dart';
import '../tabBar/custom_appbar.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key});

  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  int currentIndex = 2;
  
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _phnumberController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final FocusNode _nameFocusNode = FocusNode();
  final FocusNode _phnoFocusNode = FocusNode();
   dynamic _snapshot;

  DatabaseReference? userRef;
  User? currentUser;
  final String userId = FirebaseAuth.instance.currentUser!.uid;

  @override
  void initState() {
    super.initState();
    _loadCurrentUser();
     _fetchDeviceData();
  }

  @override
  void dispose() {
   
    _nameController.dispose();
    _phnumberController.dispose();
    _nameFocusNode.dispose();
    _phnoFocusNode.dispose();
    
    super.dispose();
  }

  


  Future<void> _loadCurrentUser() async {
    currentUser = FirebaseAuth.instance.currentUser;

    if (currentUser != null) {
      String userId = currentUser!.uid;
      userRef = FirebaseDatabase.instance.ref().child('users/$userId');
      fetchUserData();
    } else {
      print("No user is logged in.");
    }
  }



  void fetchUserData() async {
    final snapshot = await userRef!.get();
    if (snapshot.exists) {
      final data = snapshot.value as Map;
      setState(() {
        _nameController.text = data['name'] ?? '';
        _phnumberController.text = data['phone'] ?? '';
        _emailController.text = data['email'] ?? '';
      });
    }
  }

  void updateUserData() {
    Map<String, String> updatedData = {
      'name': _nameController.text,
      'phone': _phnumberController.text,
      'email': _emailController.text,
    };
    userRef!.update(updatedData);
  }

   void _fetchDeviceData(){
    final  dref = FirebaseDatabase.instance.ref('wifi_credentials/$userId');
    dref.onValue.listen((event){
        setState(() {
          _snapshot = event.snapshot;
        });
    });
  }

  void _onTabtapped(int index) {
    setState(() {
      currentIndex = index;
    });
    switch (index) {
      case 0:
        bool hasDevices = _snapshot.value != null;
        print(_snapshot.value);
        print('Devices: $hasDevices');
       
         Navigator.of(context)
            .push(MaterialPageRoute(builder: (context) => hasDevices ? const AvailableDevicesPage() : const NoDevicePage()));
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
   // Size size = MediaQuery.of(context).size;
    return Scaffold(
      appBar: PreferredSize(
        preferredSize: const Size.fromHeight(50),
        child: AppBar(
          automaticallyImplyLeading: false,
          flexibleSpace: Stack(
            children: [
              Positioned(
                //textDirection: TextDirection.ltr,
                //right: 0.0,

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
                     Padding(
                      padding:const EdgeInsets.only(left: 5),
                      child: Text(
                        'Welcome ${_nameController.text}!',
                        style: const TextStyle(
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
                            radius: 20, 
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
                            'Profile',
                            style: TextStyle(color: Colors.white, fontSize: 18),
                          ),
                        ),
                      ],
                    ),
                    Padding(
                      padding: const EdgeInsets.only(left: 25, top: 15),
                      child: Text(
                        'Name',
                        style: TextStyle(color: Theme.of(context).textTheme.displaySmall?.color, fontSize: 15),
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.only(left: 25, top: 0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              MyTextField(
                                controller: _nameController,
                                focusNode: _nameFocusNode,
                                inputType: TextInputType.name,
                                inputAction: TextInputAction.next,
                                obscureText: false,
                              ),
                              SizedBox(
                                width: MediaQuery.sizeOf(context).width * 0.05,
                              ),
                              IconButton(
                                onPressed: () {
                                  _nameFocusNode.requestFocus();
                                },
                                icon: const ImageIcon(
                                    AssetImage('assets/images/edit.png')),
                                iconSize: 35,
                              )
                            ],
                          ),
                           Padding(
                            padding:const EdgeInsets.only(top: 20),
                            child: Text(
                              'Phone Number',
                              style:
                                  TextStyle(color: Theme.of(context).textTheme.displaySmall?.color, fontSize: 15),
                            ),
                          ),
                          Row(
                            children: [
                              MyTextField(
                                  controller: _phnumberController,
                                  focusNode: _phnoFocusNode,
                                  inputType: TextInputType.phone,
                                  inputAction: TextInputAction.next,
                                  obscureText: false),
                              SizedBox(
                                width: MediaQuery.sizeOf(context).width * 0.05,
                              ),
                              IconButton(
                                onPressed: () {
                                  _phnoFocusNode.requestFocus();
                                },
                                icon: const ImageIcon(
                                    AssetImage('assets/images/edit.png')),
                                iconSize: 35,
                              )
                            ],
                          ),
                          // TextFormField(
                          //   controller: _emailController,
                          //   decoration:
                          //       const InputDecoration(labelText: 'Email'),
                          // ),
                          const SizedBox(height: 20),
                          Padding(
                            padding: const EdgeInsets.only(top: 10, left: 30),
                            child: GestureDetector(
                              onTap: () {
                                Navigator.push(
                                    context,
                                    MaterialPageRoute(
                                        builder: (context) =>
                                            const ChangePasswordPage()));
                              },
                              child: const Text(
                                'Want to Change your Password ?',
                                style: TextStyle(
                                    color: Color.fromARGB(219, 60, 79, 182),
                                    fontSize: 15),
                              ),
                            ),
                          ),

                          const SizedBox(height: 20),
                          Padding(
                            padding: const EdgeInsets.only(top: 50, right: 50),
                            child: Align(
                              alignment: Alignment.bottomRight,
                              child: ElevatedButton(
                                style: ElevatedButton.styleFrom(
                                  shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(10)),
                                  // minimumSize: Size(double.infinity, 40),
                                  padding: EdgeInsets.symmetric(
                                      horizontal:
                                          MediaQuery.sizeOf(context).height *
                                              0.02,
                                      vertical: 10),
                                  backgroundColor:
                                      const Color.fromARGB(255, 68, 154, 225),
                                  foregroundColor: Colors.white,
                                ),
                                onPressed: updateUserData,
                                child: const Text(
                                  'Submit',
                                  style: TextStyle(fontSize: 15),
                                ),
                              ),
                            ),
                          ),
                          Padding(
                            padding:
                                const EdgeInsets.only(top: 50, bottom: 0, right: 15),
                            child: Center(
                              child: Text(_emailController.text,style: TextStyle(color:Theme.of(context).textTheme.displaySmall?.color),),
                            ),
                          ),
                        ],
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
}
