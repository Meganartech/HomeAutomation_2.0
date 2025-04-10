import 'package:flutter/material.dart';

class CustomTabBar extends StatelessWidget {
  final int currentIndex;
  final ValueChanged<int> onTabTapped;

  const CustomTabBar({
    super.key,
    required this.currentIndex,
    required this.onTabTapped,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDarkMode = theme.brightness == Brightness.dark; 

    return BottomNavigationBar(
      currentIndex: currentIndex,
      onTap: onTabTapped,
      backgroundColor: isDarkMode ? Colors.white54 : theme.colorScheme.outline, 
      selectedItemColor: isDarkMode ? Colors.white :  theme.colorScheme.onSurface, 
      unselectedItemColor: isDarkMode ? Colors.black : theme.colorScheme.surfaceBright, 
      selectedLabelStyle: TextStyle(color: isDarkMode ? Colors.white : theme.colorScheme.onSurface), 
      unselectedLabelStyle: TextStyle(color: isDarkMode ? Colors.black : theme.colorScheme.surfaceBright),
      items: const [
        BottomNavigationBarItem(
          icon: ImageIcon(AssetImage('assets/icon/home.png')),
          label: 'Home',
        ),
        BottomNavigationBarItem(
          icon: ImageIcon(AssetImage('assets/icon/Schedule.png')),
          label: 'Scheduler',
        ),
        BottomNavigationBarItem(
          icon: ImageIcon(AssetImage('assets/icon/setting.png')),
          label: 'Settings',
        ),
      ],
    );
  }
}