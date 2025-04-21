import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';

class AuthManager {
  final FirebaseAuth _auth = FirebaseAuth.instance;

  Future<User?> signUpWithEmailAndPassword(String email, String password) async {
    try {
      UserCredential credential = await _auth.createUserWithEmailAndPassword(
        email: email,
        password: password,
      );
      return credential.user;
    } catch (e) {
      print("Error occurred during sign up: $e");
      return null;
    }
  }

  Future<User?> logInWithEmailAndPassword(String email, String password) async {
    try {
      UserCredential userCredential = await _auth.signInWithEmailAndPassword(
        email: email,
        password: password,
      );
      return userCredential.user;
    } on FirebaseAuthException catch (e) {
      // Handle specific Firebase errors
      if (e.code == 'wrong-password') {
        print("Error: Incorrect Password");
        return null;
      } else if (e.code == 'user-not-found') {
        print("Error: User not found");
        return null;
      } else {
        print("Error: ${e.message}");
        return null;
      }
    } catch (e) {
      // General error handling
      print("An unexpected error occurred: $e");
      return null;
    }
  }

  //Store user information in database
  Future<void> storeUserData(
      String userId, String name, String phone, String email, String password) async {
    DatabaseReference userRef =
        FirebaseDatabase.instance.ref().child('users/$userId');

    Map<String, dynamic> userData = {
      'Wifi': {
      'ssid': "",      
      'password': "",  
      },
      'Devicename': {

      },
      'Message':{
        'Status':"",
        'port1':"",
        'port2':"",
        'port3':"",
        'port4':"",
      },
      'Settings':{
        'Notification':"",
        'themecolor':"",
      },
      'Weather':{
        "Humidity": "",
        "Location": "",
        "Pressure": "",
        "Temperature": "",
        "Wind speed": "",
      },
      'components':{
        
      },
      'name': name,
      'phone': phone,
      'email': email,
      'password': password,
    };

     print("Storing user data at: ${userRef.path}");
    await userRef.set(userData);
  }
}

  