import 'package:flutter/material.dart';

ThemeData get mildBlackTheme {
  return ThemeData.dark().copyWith(
    colorScheme: const ColorScheme.dark(
      primary: Color.fromARGB(255, 0, 188, 212),  
      surface: Color.fromARGB(221, 56, 55, 55),
      onSurface: Colors.white, // Text color on the background
    ),
    scaffoldBackgroundColor: const Color.fromARGB(221, 56, 55, 55), 
    appBarTheme: const AppBarTheme(
      color: Colors.white54, 
      elevation: 0,
      iconTheme: IconThemeData(color: Colors.white),
      titleTextStyle: TextStyle(
        color: Colors.white,
        fontSize: 20,
        fontWeight: FontWeight.bold,
      ),
    ),
    cardTheme: const CardTheme(
      color: Color(0xFF1E1E1E), // Mild black for cards
      elevation: 2,
      margin: EdgeInsets.all(8),
    ),
    textTheme: const TextTheme(
      bodyLarge: TextStyle(color: Colors.white),
      bodyMedium: TextStyle(color: Colors.white),
      titleLarge: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
    ),
    iconTheme: const IconThemeData(color: Colors.white),
    buttonTheme: const ButtonThemeData(
      buttonColor: Color.fromARGB(255, 0, 188, 212), 
      textTheme: ButtonTextTheme.primary,
    ),
    bottomNavigationBarTheme: const BottomNavigationBarThemeData(
      selectedIconTheme: IconThemeData(
        color: Colors.white, 
      ),
      unselectedIconTheme: IconThemeData(
        color: Colors.black, 
      ),
    ),
  );
}