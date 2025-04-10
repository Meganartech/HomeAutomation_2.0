import 'dart:convert';
import 'package:home_auto_sample/scheduler/condition_task_page.dart';
import 'package:home_auto_sample/time_picker/cutom_time_picker.dart';
import 'package:intl/intl.dart'; 
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:home_auto_sample/add_devices_and_delete/available_devices_page.dart';
import 'package:home_auto_sample/add_devices_and_delete/no_device_page.dart';
import 'package:home_auto_sample/scheduler/scheduler_page.dart';
import 'package:home_auto_sample/service/database_service.dart';
import 'package:home_auto_sample/settings/settings_1_page.dart';
import 'package:home_auto_sample/tabBar/custom_appbar.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SetupTime extends StatefulWidget {
  const SetupTime({super.key});

  @override
  State<SetupTime> createState() => _SetupTimeState();
}

class _SetupTimeState extends State<SetupTime> {
  int currentIndex = 1;
  List<Device> _devices = [];
  bool _isStartExpanded = false;
  bool _isEndExpanded = false;
  TimeOfDay _startTime = TimeOfDay.now();
  TimeOfDay _endTime = TimeOfDay.now();
  DateTime _selectedStartDate = DateTime.now();
  DateTime _selectedEndDate = DateTime.now();
  int _selectedStartWeekday = DateTime.now().weekday;
  int _selectedEndWeekday = DateTime.now().weekday;
  bool _isEndTimeSelected = false; 

  final String userId = FirebaseAuth.instance.currentUser!.uid;

  @override
  void initState() {
    super.initState();
    _fetchDevices();
  }

  Future<void> _fetchDevices() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    String? espDeviceJson = prefs.getString('EspDevice');

    List<Device> devices = [];

    Map<String, dynamic> espDeviceMap = jsonDecode(espDeviceJson!);

    espDeviceMap.forEach((uid, userDeviceData) {
      if (userDeviceData is Map<String, dynamic>) {
        userDeviceData.forEach((deviceId, deviceData) {
          if (deviceData is Map<String, dynamic>) {
            String deviceName = deviceData["name"] ?? "Unknown Device";
            String editedName = deviceData["editname"] ?? "";

            String finalDeviceName = editedName.isNotEmpty ? editedName : deviceName;
            devices.add(Device(
              deviceId: deviceId,
              deviceName: finalDeviceName,
            ));
          }
        });
      }
    });

    print('Fetched ESP Devices: $devices');

    setState(() {
      _devices = devices;
    });
  }

  void _updateDateAndWeekday(bool isStartTime, DateTime newDate) {
    setState(() {
      if (isStartTime) {
        _selectedStartDate = newDate;
        _selectedStartWeekday = newDate.weekday;
      } else {
        _selectedEndDate = newDate;
        _selectedEndWeekday = newDate.weekday;
      }
    });
  }

  Future<void> _showDatePicker(bool isStartTime) async {
    DateTime initialDate = isStartTime ? _selectedStartDate : _selectedEndDate;
    DateTime? pickedDate = await showDatePicker(
      context: context,
      initialDate: initialDate,
      firstDate: DateTime(2023),
      lastDate: DateTime(2030),
    );
if (pickedDate != null) {
    _updateDateAndWeekday(isStartTime, pickedDate);
  }
    }

  Widget _buildTimeCard(String title, bool isStartTime) {
    TimeOfDay selectedTime = isStartTime ? _startTime : _endTime;
    DateTime selectedDate = isStartTime ? _selectedStartDate : _selectedEndDate;
    int selectedWeekday = isStartTime ? _selectedStartWeekday : _selectedEndWeekday;
    bool isExpanded = isStartTime ? _isStartExpanded : _isEndExpanded;

    List<String> shortWeekdays = ["S", "M", "T", "W", "T", "F", "S"];
    List<int> weekdayMapping = [7, 1, 2, 3, 4, 5, 6]; // Correct weekday mapping

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      child: Column(
        children: [
          // Main Expandable Card
          GestureDetector(
            onTap: () {
              setState(() {
                if (isStartTime) {
                  _isStartExpanded = !_isStartExpanded;
                } else {
                  _isEndExpanded = !_isEndExpanded;
                }
              });
            },
            child: Card(
              color: Colors.grey[200],
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
              child: ListTile(
                leading: Image.asset("assets/icon/clock.png", width: 30, height: 30),
                title: Text(title, style: const TextStyle(fontSize: 19)),
                trailing: Icon(
                  Icons.keyboard_arrow_down_rounded,
                  size: 40,
                  color: Colors.black,
                ),
              ),
            ),
          ),

          // Inner Expanded Card
          if (isExpanded)
            Card(
              color: Colors.grey[200],
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
              child: Column(
                children: [
                  // Custom Time Picker
                  Container(
                    height: 150,
                    color: Colors.white,
                    child: CustomTimePicker(
                      initialTime: selectedTime,
                      onTimeChanged: (TimeOfDay newTime) {
                        setState(() {
                          if (isStartTime) {
                            _startTime = newTime;
                          } else {
                            _endTime = newTime;
                            _isEndTimeSelected = true; // Set flag to true when end time is selected
                          }
                        });
                      },
                    ),
                  ),

                  // Date Picker
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: ListTile(
                      title: Text(
                        "Starting ${DateFormat('E, d MMM').format(selectedDate)}",
                        style: const TextStyle(fontSize: 16),
                      ),
                      trailing: IconButton(
                        icon: const Icon(Icons.calendar_today),
                        onPressed: () => _showDatePicker(isStartTime),
                      ),
                    ),
                  ),

                  // Weekday Selection
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: shortWeekdays.asMap().entries.map((entry) {
                        int index = entry.key;
                        String dayLetter = entry.value;
                        int actualWeekday = weekdayMapping[index];
                        bool isSelected = selectedWeekday == actualWeekday;

                        return GestureDetector(
                          onTap: () {
                            DateTime newDate = DateTime.now();
                            while (newDate.weekday != actualWeekday) {
                              newDate = newDate.add(const Duration(days: 1));
                            }
                            _updateDateAndWeekday(isStartTime, newDate);
                          },
                          child: Container(
                            padding: const EdgeInsets.all(8),
                            child: Text(
                              dayLetter,
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                                color: isSelected ? Colors.blue : Colors.black,
                              ),
                            ),
                          ),
                        );
                      }).toList(),
                    ),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }

  void _onTabtapped(int index) {
    if (index == currentIndex) return;
    setState(() {
      currentIndex = index;
    });

    switch (index) {
      case 0:
        bool hasDevices = _devices.isNotEmpty;
        print('Devices: $hasDevices');

        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (context) => hasDevices ? const AvailableDevicesPage() : const NoDevicePage(),
          ),
        );
        break;
      case 1:
        Navigator.of(context).push(
          MaterialPageRoute(builder: (context) => const SchedulerPage()),
        );
        break;
      case 2:
        Navigator.of(context).push(
          MaterialPageRoute(builder: (context) => const Settings1Page()),
        );
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
                child: Image.asset(
                  'assets/images/home_14.8.png',
                ),
              ),
            ],
          ),
        ),
      ),
      body: Column(
      children: [
        Expanded(
          child: SingleChildScrollView(
            child: Padding(
              padding: const EdgeInsets.only(top: 6.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Padding(
                    padding: EdgeInsets.only(left: 10),
                    child: Text(
                      'Set Up a Routine',
                      style: TextStyle(
                        fontSize: 25,
                      ),
                    ),
                  ),
                  const SizedBox(height: 20),
                  const Divider(
                    height: 2,
                    color: Colors.black,
                  ),
                  const SizedBox(height: 30),
                  _buildTimeCard("Start Time", true),
                  _buildTimeCard("End Time", false),
                ],
              ),
            ),
          ),
        ),
        if (_isEndTimeSelected)
          Padding(
            padding: const EdgeInsets.all(20.0),
            child: TextButton(
                style: TextButton.styleFrom(
                  backgroundColor: const Color.fromARGB(255, 0, 188, 212),
                ),
                onPressed: () {
  String formattedStartTime = DateFormat("hh:mm a MMM d, yyyy").format(
    DateTime(
      _selectedStartDate.year, 
      _selectedStartDate.month, 
      _selectedStartDate.day, 
      _startTime.hour, 
      _startTime.minute,
    ),
  );

  String formattedEndTime = DateFormat("hh:mm a MMM d, yyyy").format(
    DateTime(
      _selectedEndDate.year, 
      _selectedEndDate.month, 
      _selectedEndDate.day, 
      _endTime.hour, 
      _endTime.minute,
    ),
  );

  print("Start Time: $formattedStartTime, End Time: $formattedEndTime");

  Navigator.of(context).push(
    MaterialPageRoute(
      builder: (context) => ConditionTaskPage(
        selectedStartTime: formattedStartTime,
        selectedEndTime: formattedEndTime,
      ),
    ),
  );
},

                child: const Text(
                  'Next  -->',
                  style: TextStyle(color: Colors.white,fontSize: 18),
                ),
              ),
          ),
      ],
    ),
    bottomNavigationBar: CustomTabBar(currentIndex: currentIndex, onTabTapped: _onTabtapped),
  );
}
}

