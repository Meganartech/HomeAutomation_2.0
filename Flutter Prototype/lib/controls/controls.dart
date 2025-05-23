import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'dart:async';

class WizBulbControl extends StatefulWidget {
  const WizBulbControl({super.key});

  @override
  State<WizBulbControl> createState() => _WizBulbControlState();
}

class _WizBulbControlState extends State<WizBulbControl> {
  double _hue = 0;
  double _saturation = 50;
  double _brightness = 50;
  bool _isLoading = false;
  Timer? _timer;

  // Configuration - REPLACE THESE WITH YOUR DETAILS
  final String _openhabUrl = 'http://192.168.0.4:8080/rest/items';
  final String _token = 'oh.automation.UlpPa77SKGSiI8Y8yGArWfGywvfjlPRW3aDyYvIwHHIwx0IC7RmdEVhgKCX7iQPmfArFIS7InL0s8YWKi2SA';
  final String _colorItem = 'WiZ_Full_Color_Bulb_at_192168030_Color';

  @override
  void initState() {
    super.initState();
    _fetchCurrentValues();

    _timer = Timer.periodic(const Duration(seconds: 5), (Timer t) {
      _fetchCurrentValues();
    });
  }

  @override
  void dispose() {
    _timer?.cancel(); 
    super.dispose();
  }

  Future<void> _fetchCurrentValues() async {
    try {
      final uri = Uri.parse('$_openhabUrl/$_colorItem');
      final response = await http.get(
        uri,
        headers: {'Authorization': 'Bearer $_token'},
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);

        if (data['state'] == null) {
          throw Exception('State is null in response');
        }

        final parts = data['state'].toString().split(',');

        if (parts.length == 3) {
          setState(() {
            _hue = double.tryParse(parts[0]) ?? 0;
            _saturation = double.tryParse(parts[1]) ?? 50;
            _brightness = double.tryParse(parts[2]) ?? 50;
          });
        } else {
          throw Exception('Invalid state format: ${data['state']}');
        }
      } else {
        throw Exception('HTTP ${response.statusCode}: ${response.body}');
      }
    } catch (e) {
      _showError('Fetch error: $e');
    }
  }

  Future<void> _updateColor() async {
    setState(() => _isLoading = true);
    try {
      final response = await http.post(
        Uri.parse('$_openhabUrl/$_colorItem'),
        headers: {
          'Authorization': 'Bearer $_token',
          'Content-Type': 'text/plain',
        },
        body: '${_hue.round()},${_saturation.round()},${_brightness.round()}',
      );

      if (response.statusCode != 200 && response.statusCode != 201) {
        throw Exception('HTTP ${response.statusCode}');
      }
    } catch (e) {
      _showError('Failed to update: $e');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }

  Widget _buildSlider(String label, double value, double maxRange, Function(double) onChanged) {
    return Column(
      children: [
        Text('$label: ${value.round()}'),
        Slider(
          value: value,
          min: 0,
          max: maxRange,
          divisions: maxRange.toInt(),
          onChanged: (v) => setState(() => onChanged(v)),
          onChangeEnd: (_) => _updateColor(),
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Light Control')),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                children: [
                  _buildSlider('Hue', _hue, 360, (v) => _hue = v), 
                  _buildSlider('Saturation', _saturation, 100, (v) => _saturation = v), 
                  _buildSlider('Brightness', _brightness, 100, (v) => _brightness = v),
                ],
              ),
            ),
    );
  }
}
