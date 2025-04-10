
import 'package:firebase_auth/firebase_auth.dart' show User;
import 'package:flutter/material.dart';
import 'package:home_auto_sample/authentication/firebase_auth.dart';
import '../components/mytextfield.dart';
import 'login_page.dart';

class SignupPage extends StatefulWidget {
  const SignupPage({super.key});

  @override
  State<StatefulWidget> createState() {
    return _SignupPageState();
  }
}

class _SignupPageState extends State<SignupPage> {
  final _formKey = GlobalKey<FormState>();

  final AuthManager _auth = AuthManager(); // Renamed to AuthManager

  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _phoneNumberController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  @override
  void dispose() {
    _nameController.dispose();
    _phoneNumberController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
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
      body: SingleChildScrollView(
        child: SizedBox(
          height: MediaQuery.of(context).size.height -
              AppBar().preferredSize.height,
          child: Stack(
            children: [
              Positioned.fill(
                top: MediaQuery.sizeOf(context).height * 0.11,
                left: 0,
                right: 0,
                bottom: 0,
                child: Image.asset(
                  'assets/images/home_3.png',
                  fit: BoxFit.fill, // Change this to your desired fit
                ),
              ),
              Positioned(
                top: 0,
                left: 0,
                right: MediaQuery.sizeOf(context).width * 0.4,
                bottom: MediaQuery.sizeOf(context).height * 0.8,
                child: Image.asset(
                  'assets/icon/login.png',
        
                  fit: BoxFit.contain, // Change this to your desired fit
                ),
              ),
              Padding(
                  padding: const EdgeInsets.only(left: 10, top: 42, bottom: 2),
                  child: Image.asset(
                    'assets/icon/User.png',
                    color: Colors.white,
                  )),
               const Padding(
                padding: EdgeInsets.only(left: 45, top: 43, bottom: 2),
                child: Text(
                  'Sign Up',
                  style: TextStyle(color: Colors.white, fontSize: 18),
                ),
              ),
              // Sign Up Form
              // Center(
              //   child: SingleChildScrollView(
              //child:
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20.0),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                 
                    Padding(
                      padding: const EdgeInsets.symmetric(vertical: 30),
                      child: Form(
                        key: _formKey,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            const Padding(
                              padding: EdgeInsets.symmetric(horizontal: 20),
                              child: Text(
                                'Name',
                                style: TextStyle(
                                    color: Colors.black, fontSize: 14),
                              ),
                            ),
                            Padding(
                              padding:
                                  const EdgeInsets.symmetric(horizontal: 20),
                              child: MyTextField(
                                controller: _nameController,
                                validator: (value) {
                                  return value!.isEmpty
                                      ? 'Please enter your name'
                                      : null;
                                },
                                inputType: TextInputType.name,
                                obscureText: false,
                                inputAction: TextInputAction.next,
                              ),
                            ),
                            const SizedBox(height: 10),
                            const Padding(
                              padding: EdgeInsets.symmetric(horizontal: 20),
                              child: Text(
                                'Phone Number',
                                style: TextStyle(
                                    color: Colors.black, fontSize: 14),
                              ),
                            ),
                            Padding(
                              padding:
                                  const EdgeInsets.symmetric(horizontal: 20),
                              child: MyTextField(
                                controller: _phoneNumberController,
                                validator: (value) {
                                  return value!.isEmpty
                                      ? 'Please enter your phone number'
                                      : null;
                                },
                                inputType: TextInputType.phone,
                                obscureText: false,
                                inputAction: TextInputAction.next,
                              ),
                            ),
                            const SizedBox(height: 10),
                            const Padding(
                              padding: EdgeInsets.symmetric(horizontal: 20),
                              child: Text(
                                'Email',
                                style: TextStyle(
                                    color: Colors.black, fontSize: 14),
                              ),
                            ),
                            Padding(
                              padding:
                                  const EdgeInsets.symmetric(horizontal: 20),
                              child: MyTextField(
                                controller: _emailController,
                                validator: (value) {
                                  return value!.isEmpty
                                      ? 'Please enter your email'
                                      : null;
                                },
                                inputType: TextInputType.emailAddress,
                                obscureText: false,
                                inputAction: TextInputAction.next,
                              ),
                            ),
                            const SizedBox(height: 10),
                            const Padding(
                              padding: EdgeInsets.symmetric(horizontal: 20),
                              child: Text(
                                'Password',
                                style: TextStyle(
                                    color: Colors.black, fontSize: 14),
                              ),
                            ),
                            Padding(
                              padding:
                                  const EdgeInsets.symmetric(horizontal: 20),
                              child: MyTextField(
                                controller: _passwordController,
                                validator: (value) {
                                  return value!.isEmpty
                                      ? 'Please enter your password'
                                      : null;
                                },
                                inputType: TextInputType.visiblePassword,
                                obscureText: true,
                                inputAction: TextInputAction.done,
                              ),
                            ),
                            const SizedBox(height: 10),
                            _buildSignUpButton(context),
                            const SizedBox(height: 20),
                            _buildAlreadyHaveAccount(context),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
                //   ),
                // ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSignUpButton(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 10, right: 100, left: 80),
      // child: Align(
      //   alignment: Alignment.bottomCenter,
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          // minimumSize: Size(double.infinity, 40),
          padding: EdgeInsets.symmetric(
              horizontal: MediaQuery.sizeOf(context).height * 0.02,
              vertical: MediaQuery.sizeOf(context).width * 0.02),
          backgroundColor: const Color.fromRGBO(182, 176, 236, 1),
          foregroundColor: Colors.white,
        ),
        onPressed: () async {
          if (_formKey.currentState!.validate()) {
            // Signup the user
            await _signup(context);
          }
        },
        child: const Text(
          'Sign Up',
          style: TextStyle(fontSize: 15),
        ),
      ),
      // ),
    );
  }

  Widget _buildAlreadyHaveAccount(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        const Text("Already have an account?",
        style: TextStyle(color:Colors.black),),
        const SizedBox(width: 10),
        GestureDetector(
          onTap: () {
            Navigator.pushReplacement(
                context, MaterialPageRoute(builder: (context) => const LoginPage()));
          },
          child: const Text(
            "Login",
            style: TextStyle(
              color: Color.fromRGBO(151, 121, 234, 1),
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _signup(BuildContext context) async {
    String name = _nameController.text;
    String email = _emailController.text;
    String phoneNumber = _phoneNumberController.text;
    String password = _passwordController.text;

    User? user = await _auth.signUpWithEmailAndPassword(email, password);

    if (user != null) {
      print("User signed up successfully. UID: ${user.uid}");

       if (user.email != null) {
        print("User email: ${user.email}");
      } else {
        print("User email is null");
      }
      
      await _auth.storeUserData(user.uid, name, phoneNumber, email, password);

      print("User data stored successfully in Realtime Database");
      print("User successfully created");
      Navigator.pushReplacement(
          context, MaterialPageRoute(builder: (context) => const LoginPage()));
    } else {
      print("An error occurred");
    }
  }
}
