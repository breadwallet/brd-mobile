// Copyright (c) 2021, Very Good Ventures
// https://verygood.ventures
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file or at
// https://opensource.org/licenses/MIT.

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:kyc/common/app_theme.dart';
import 'package:kyc/common/dependency_provider.dart';
import 'package:kyc/l10n/l10n.dart';
import 'package:kyc/models/login_credentials.dart';

import '../routes.dart';

class App extends StatefulWidget {
  const App({Key? key}) : super(key: key);

  @override
  _AppState createState() => _AppState();
}

class _AppState extends State<App> with WidgetsBindingObserver {
  static const platform = MethodChannel('nakamoto-platform-channels');
  bool initialized = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance!.addObserver(this);
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    loadSessionInfo();
  }

  void loadSessionInfo() async {
    final userRepo = DependencyProvider.of(context).userRepo;
    try {
      final dynamic sessionKey =
          await platform.invokeMethod<dynamic>('getSessionKey');
      print("Session key");
      print(sessionKey);
      if ((sessionKey is String) && sessionKey.isNotEmpty) {
        userRepo.loginWithSessionKey(sessionKey);
      } else {
        throw Exception("");
      }
    } catch (e) {
      print('error happened');
      print(e);
      // await userRepo.loginWithEmailPass(
      //   LoginCredentials(
      //     email: 'test@test.com',
      //     password: 'password',
      //   ),
      // );
    }
    setState(() {
      initialized = true;
    });
  }

  void sendResultToNavive(dynamic res) {}

  @override
  Widget build(BuildContext context) {
    if (!initialized) {
      return const MaterialApp(
        home: Scaffold(
          body: Center(
            child: Text("Loading"),
          ),
        ),
      );
    }
    return MaterialApp(
      theme: AppTheme.lightTheme,
      navigatorKey: DependencyProvider.of(context).navigationCubit.navigatorKey,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
      ],
      supportedLocales: AppLocalizations.supportedLocales,
      routes: routes,
    );
  }

  @override
  void dispose() {
    DependencyProvider.of(context).dispose();
    WidgetsBinding.instance!.removeObserver(this);
    super.dispose();
  }
}
