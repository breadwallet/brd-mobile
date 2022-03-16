import 'dart:developer';

import 'package:url_launcher/url_launcher.dart';

Future<bool> tryLaunchUrl(String url) async {
  try {
    if (await canLaunch(url)) {
      return await launch(url);
    }
  } on Exception catch (ex) {
    log(ex.toString());
  }

  return false;
}
