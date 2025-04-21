import 'package:flutter/material.dart';

class MyTextField extends StatefulWidget {
  const MyTextField({
    super.key,
    this.controller,
    required this.inputType,
    required this.inputAction,
    this.validator,
    required this.obscureText,
    this.focusNode,
  });

  final TextEditingController? controller;
  final String? Function(String?)? validator;
  final TextInputType inputType;
  final TextInputAction inputAction;
  final bool obscureText;
  final FocusNode? focusNode;

  @override
  State<MyTextField> createState() => _MyTextFieldState();
}

class _MyTextFieldState extends State<MyTextField> {
  String? errorText;

  @override
  Widget build(BuildContext context) {
    Size size = MediaQuery.of(context).size;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 10),
          child: Container(
            height: size.height * 0.05,
            width: size.width * 0.7,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(
                  color: errorText != null ? Colors.red : Colors.grey.shade400),
            ),
            child: Stack(
              children: [
                Center(
                  //constraints: BoxConstraints(minHeight: size.height * 0.07),
                  child: TextFormField(
                    controller: widget.controller,
                    focusNode: widget.focusNode,
                    cursorColor: Colors.black,
                    cursorRadius: const Radius.circular(2),
                    cursorWidth: 1,
                    decoration: const InputDecoration(
                      border: InputBorder.none,
                      contentPadding:
                          EdgeInsets.only(top: 10, bottom: 12, left: 16),
                      isDense: true,
                    ),
                    style: const TextStyle(fontSize: 18, color: Colors.black),
                    keyboardType: widget.inputType,
                    obscureText: widget.obscureText,
                    textInputAction: widget.inputAction,
                    validator: (value) {
                      setState(() {
                        errorText = _getErrorText(value);
                      });
                      return null;
                    },
                    onChanged: (value) {
                      if (errorText != null) {
                        setState(() {
                          errorText = _getErrorText(value);
                        });
                      }
                    },
                  ),
                ),
                if (errorText != null)
                  Positioned(
                    right: 10,
                    top: 0,
                    bottom: 0,
                    child: Center(
                      child: Tooltip(
                        message: errorText!,
                        preferBelow: false,
                        child: const Icon(
                          Icons.error,
                          color: Colors.red,
                          size: 20,
                        ),
                      ),
                    ),
                  ),
                if (errorText != null)
                  Padding(
                    padding: const EdgeInsets.only(top: 10, left: 16),
                    child: Text(
                      errorText!,
                      style: const TextStyle(
                        color: Colors.red,
                        fontSize: 12,
                      ),
                    ),
                  ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  String? _getErrorText(String? value) {
    if (widget.validator != null) {
      final validatorResult = widget.validator!(value);
      if (validatorResult != null) {
        return validatorResult;
      }
    }

    return null;
  }
}
