import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

const Color almostBlack = Color(0xff282828);
const Color yellow = Color(0xff2C78FF);
const Color lightYellow = Color(0xff2C78FF);
const Color lightGrey = Color(0xfff5f5f5);
const Color white = Color(0xffffffff);
const Color grey1 = Color(0xff696969);
const Color grey2 = Color(0xffa0a0a0);
const Color grey3 = Color(0xffc9c9c9);
const Color red = Color(0xffff5c4a);
const Color green = Color(0xff00a86e);
const buttonHeight = 48.0;
const buttonSideWidth = 2.0;

/// Alias
const Color _disabled = grey2;

class AppTheme {
  // static final darkTheme = _createDarkTheme();
  // static final lightTheme = _createLightTheme();

  // TODO: Remove these and use the final above
  // These are used for hot-reload purposes
  static ThemeData get darkTheme => _createDarkTheme();
  static ThemeData get lightTheme => _createLightTheme();

  static ThemeData _createDarkTheme() {
    const brightness = Brightness.dark;

    return ThemeData(
      brightness: brightness,
      primarySwatch: _createMaterialColor(yellow),
      backgroundColor: almostBlack,
      textTheme: _createTextTheme(brightness),
      appBarTheme: _createAppBarTheme(),
      inputDecorationTheme: _createInputDecorationTheme(),
      elevatedButtonTheme: _createElevatedButtonTheme(),
      outlinedButtonTheme: _createOutlinedButtonTheme(),
      textButtonTheme: _createTextButtonTheme(),
      errorColor: red,
      bottomNavigationBarTheme: _createBottomNavigationBarTheme(),
      snackBarTheme: _createSnackBarTheme(),
      scaffoldBackgroundColor: almostBlack,
    );
  }

  static ThemeData _createLightTheme() {
    const brightness = Brightness.light;

    return ThemeData(
      brightness: brightness,
      primarySwatch: _createMaterialColor(yellow),
      backgroundColor: lightGrey,
      textTheme: _createTextTheme(brightness),
      appBarTheme: _createAppBarTheme(),
      inputDecorationTheme: _createInputDecorationTheme(),
      elevatedButtonTheme: _createElevatedButtonTheme(),
      outlinedButtonTheme: _createOutlinedButtonTheme(),
      textButtonTheme: _createTextButtonTheme(),
      errorColor: red,
      bottomNavigationBarTheme: _createBottomNavigationBarTheme(),
      snackBarTheme: _createSnackBarTheme(),
    );
  }

  static SnackBarThemeData _createSnackBarTheme() {
    return SnackBarThemeData(
      backgroundColor: grey1,
      contentTextStyle: const TextStyle(
        color: white,
      ),
      behavior: SnackBarBehavior.floating,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
      ),
      elevation: 2,
    );
  }

  static BottomNavigationBarThemeData _createBottomNavigationBarTheme() {
    return const BottomNavigationBarThemeData(
      backgroundColor: almostBlack,
      showSelectedLabels: false,
      showUnselectedLabels: false,
      selectedItemColor: yellow,
      unselectedItemColor: grey2,
    );
  }

  static final buttonStyleIntrinsicSize =
      ButtonStyle(fixedSize: MaterialStateProperty.all(Size.infinite));
  static final buttonStyleBlock = ButtonStyle(
    fixedSize: MaterialStateProperty.all(const Size(400, buttonHeight)),
  );

  static ElevatedButtonThemeData _createElevatedButtonTheme() {
    return ElevatedButtonThemeData(
      style: ButtonStyle(
        elevation: MaterialStateProperty.all(0),
        fixedSize: MaterialStateProperty.all(const Size(400, buttonHeight)),
        shape: MaterialStateProperty.all(
          RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
        ),
      ),
    );
  }

  static OutlinedButtonThemeData _createOutlinedButtonTheme({
    Color color = yellow,
  }) {
    return OutlinedButtonThemeData(
      style: ButtonStyle(
        elevation: MaterialStateProperty.all(0),
        fixedSize: MaterialStateProperty.all(const Size(400, buttonHeight)),
        shape: MaterialStateProperty.all(
          RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
        ),
        side: MaterialStateProperty.resolveWith((states) {
          var c = color;
          if (states.contains(MaterialState.disabled)) {
            c = _disabled;
          }
          return BorderSide(
            color: c,
            width: buttonSideWidth,
          );
        }),
        overlayColor: MaterialStateProperty.all(color.withAlpha(96)),
        foregroundColor: MaterialStateProperty.resolveWith(
          (states) {
            if (states.contains(MaterialState.disabled)) {
              return _disabled;
            }
            // if (states.contains(MaterialState.pressed)) {
            //   return almostBlack;
            // }
            return color;
          },
        ),
      ),
    );
  }

  static final ButtonStyle redOutlinedButtonStyle =
      _createOutlinedButtonTheme(color: red).style!;
  static final ButtonStyle greenOutlinedButtonStyle =
      _createOutlinedButtonTheme(color: green).style!;

  static TextButtonThemeData _createTextButtonTheme() {
    return TextButtonThemeData(
      style: ButtonStyle(
        fixedSize: MaterialStateProperty.all(const Size(400, buttonHeight)),
      ),
    );
  }

  static AppBarTheme _createAppBarTheme() {
    return const AppBarTheme(
      backgroundColor: almostBlack,
      iconTheme: IconThemeData(color: lightGrey),
      foregroundColor: lightGrey,
      elevation: 0,
      centerTitle: true,
      systemOverlayStyle: SystemUiOverlayStyle.light,
    );
  }

  static InputDecorationTheme _createInputDecorationTheme() {
    return const InputDecorationTheme(
      border: OutlineInputBorder(),
      labelStyle: TextStyle(
        fontSize: 12,
        height: 16 / 12,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.6,
      ),
      isDense: true,
    );
  }

  static TextTheme _createTextTheme(Brightness brightness) {
    final textColor = brightness == Brightness.light ? almostBlack : lightGrey;

    return const TextTheme().copyWith(
      // headline1: const TextStyle(
      //   fontSize: 96,
      //   fontWeight: FontWeight.w300,
      //   letterSpacing: -1.5,
      // ),
      // headline2: const TextStyle(
      //   fontSize: 60,
      //   fontWeight: FontWeight.w300,
      //   letterSpacing: -0.5,
      // ),
      // headline3: const TextStyle(
      //   fontSize: 48,
      //   fontWeight: FontWeight.w400,
      //   letterSpacing: 0,
      // ),
      // headline4: const TextStyle(
      //   fontSize: 34,
      //   fontWeight: FontWeight.w400,
      //   letterSpacing: 0.25,
      // ),
      // headline5: const TextStyle(
      //   fontSize: 24,
      //   fontWeight: FontWeight.w400,
      //   letterSpacing: 0,
      // ),
      headline6: TextStyle(
        fontSize: 20,
        height: 28 / 20,
        fontWeight: FontWeight.bold,
        letterSpacing: 2,
        color: textColor,
      ),
      subtitle1: TextStyle(
        fontSize: 16,
        height: 18 / 16,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.8,
        color: textColor,
      ),
      subtitle2: const TextStyle(
        fontSize: 12,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.1,
        color: grey2,
      ),
      bodyText1: TextStyle(
        fontSize: 16,
        height: 20 / 16,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.8,
        color: textColor,
      ),
      bodyText2: TextStyle(
        fontSize: 14,
        height: 18 / 14,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.7,
        color: textColor,
      ),
      button: TextStyle(
        fontSize: 14,
        height: 20 / 14,
        fontWeight: FontWeight.w700,
        letterSpacing: 1.4,
        color: textColor,
      ),
      // caption: const TextStyle(
      //   fontSize: 12,
      //   fontWeight: FontWeight.w400,
      //   letterSpacing: 0.4,
      // ),
      // overline: const TextStyle(
      //   fontSize: 10,
      //   fontWeight: FontWeight.w400,
      //   letterSpacing: 1.5,
      // ),
    );
  }
}

MaterialColor _createMaterialColor(Color color) {
  final List strengths = <double>[.05];
  final swatch = <int, Color>{};
  final r = color.red, g = color.green, b = color.blue;

  for (var i = 1; i < 10; i++) {
    strengths.add(0.1 * i);
  }

  for (final strength in strengths) {
    final ds = 0.5 - (strength as num);
    swatch[(strength * 1000).round()] = Color.fromRGBO(
      r + ((ds < 0 ? r : (255 - r)) * ds).round(),
      g + ((ds < 0 ? g : (255 - g)) * ds).round(),
      b + ((ds < 0 ? b : (255 - b)) * ds).round(),
      1,
    );
  }

  return MaterialColor(color.value, swatch);
}
