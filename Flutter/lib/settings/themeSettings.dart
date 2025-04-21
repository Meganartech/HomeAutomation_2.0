import 'package:flutter/material.dart';
import 'package:home_auto_sample/custom%20black%20theme/custom_black.dart';

class ThemeNotifier with ChangeNotifier {
  ThemeMode _themeMode = ThemeMode.system;

  ThemeMode get themeMode => _themeMode;

  void setTheme(ThemeMode mode) {
    _themeMode = mode;
    notifyListeners();
  }

  ThemeData getTheme(BuildContext context) {
    switch (_themeMode) {
      case ThemeMode.light:
        return ThemeData.light();
      case ThemeMode.dark:
        return mildBlackTheme; 
      case ThemeMode.system:
      default:
        return MediaQuery.platformBrightnessOf(context) == Brightness.dark
            ? mildBlackTheme 
            : ThemeData.light();
    }
  }
}