import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class NavigationCubit extends Cubit<dynamic> {
  NavigationCubit() : super(null);

  final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

  NavigatorState get navigator => navigatorKey.currentState!;

  void navigateToHome() {
    navigator.pushNamedAndRemoveUntil('/home', (route) => false);
  }

  void navigateToLogin() {
    navigator.pushNamedAndRemoveUntil('/login', (route) => false);
  }
}
