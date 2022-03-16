import 'package:flutter/material.dart';
import 'package:kyc/kyc/view/kyc_onboarding_page.dart';

import 'kyc/view/kyc_page.dart';

final Map<String, Widget Function(BuildContext)> routes = {
  '/': (context) => KycOnboardingPage(),
  '/kyc-main': (context) => KycPage(),
};
