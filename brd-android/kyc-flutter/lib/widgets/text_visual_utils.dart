import 'package:flutter/material.dart';
import 'package:kyc/constants/constants.dart';

Widget priceChangePercentageText(
    double priceChangePercentage, BuildContext context) {
  return Text(
    '${priceChangePercentage.toStringAsFixed(2)}%',
    style: Theme.of(context)
        .textTheme
        .subtitle2!
        .copyWith(color: priceChangePercentage > 0 ? Colors.green : Colors.red),
  );
}

Widget fiatPriceText(double price, BuildContext context) {
  var priceText = price.toString();
  if (price > 99999) {
    priceText = '${price.toString().substring(0, 3)}K';
  } else {
    priceText = price.toStringAsFixed(2);
  }
  return Text(
    '${globalCurrency.values.first}$priceText',
    style: Theme.of(context).textTheme.subtitle1,
  );
}

Widget assetValueText(double price, BuildContext context) {
  var priceText = price.toString();
  if (price > 999) {
    priceText = price.toStringAsFixed(0);
  } else if (price < 100) {
    priceText = price.toStringAsFixed(4);
  } else {
    priceText = price.toStringAsFixed(2);
  }
  return Text(
    priceText,
    style: Theme.of(context).textTheme.subtitle1,
  );
}

extension ExtendedString on String {
  String capitalizeWord() =>
      '${this[0].toUpperCase()}${substring(1).toLowerCase()}';
}
