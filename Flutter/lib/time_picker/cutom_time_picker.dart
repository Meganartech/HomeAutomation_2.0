import 'package:flutter/material.dart';

class CustomTimePicker extends StatefulWidget {
  final TimeOfDay initialTime;
  final ValueChanged<TimeOfDay> onTimeChanged;

  const CustomTimePicker({
    super.key,
    required this.initialTime,
    required this.onTimeChanged,
  });

  @override
  _CustomTimePickerState createState() => _CustomTimePickerState();
}

class _CustomTimePickerState extends State<CustomTimePicker> {
  int _selectedHour;
  int _selectedMinute;

  _CustomTimePickerState()
      : _selectedHour = 0,
        _selectedMinute = 0;

  @override
  void initState() {
    super.initState();
    _selectedHour = widget.initialTime.hour;
    _selectedMinute = widget.initialTime.minute;
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        // Hours Picker
        _buildNumberPicker(
          initialValue: _selectedHour,
          minValue: 0,
          maxValue: 23,
          onChanged: (int value) {
            setState(() {
              _selectedHour = value;
            });
            widget.onTimeChanged(TimeOfDay(hour: _selectedHour, minute: _selectedMinute));
          },
        ),

        // Colon Separator
        const Text(
          " : ",
          style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
        ),

        // Minutes Picker
        _buildNumberPicker(
          initialValue: _selectedMinute,
          minValue: 0,
          maxValue: 59,
          onChanged: (int value) {
            setState(() {
              _selectedMinute = value;
            });
            widget.onTimeChanged(TimeOfDay(hour: _selectedHour, minute: _selectedMinute));
          },
        ),
      ],
    );
  }

  Widget _buildNumberPicker({
    required int initialValue,
    required int minValue,
    required int maxValue,
    required ValueChanged<int> onChanged,
  }) {
    return SizedBox(
      width: 80,
      height: 150,
      child: ListView.builder(
        itemCount: maxValue - minValue + 1,
        itemExtent: 40,
        controller: ScrollController(
          initialScrollOffset: (initialValue - minValue) * 40.0,
        ),
        itemBuilder: (BuildContext context, int index) {
          final value = minValue + index;
          return GestureDetector(
            onTap: () {
              onChanged(value);
            },
            child: Center(
              child: Text(
                value.toString().padLeft(2, '0'),
                style: TextStyle(
                  fontSize: 24,
                  color: value == initialValue ? Colors.blue : Colors.black,
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}