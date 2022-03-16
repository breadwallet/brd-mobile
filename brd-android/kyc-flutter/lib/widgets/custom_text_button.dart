import 'package:flutter/material.dart';

class CustomTextButton extends StatelessWidget {
  const CustomTextButton(
      {required this.active, required this.onPressed, required this.title});

  final bool active;
  final Function() onPressed;
  final String title;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onPressed,
      child: Container(
        margin: const EdgeInsets.all(10),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(10),
          boxShadow: [
            if (active)
              const BoxShadow(
                offset: Offset(0, 20),
                spreadRadius: -7,
              ),
          ],
        ),
        child: Text(
          title,
          style: Theme.of(context).textTheme.subtitle1!.copyWith(
                color: !active ? Theme.of(context).unselectedWidgetColor : null,
              ),
        ),
      ),
    );
  }
}
