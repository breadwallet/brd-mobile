// ignore: lines_longer_than_80_chars
const String baseUrl = 'https://dev.just.cash/watch/watch/';

enum VsCurrency { usd, eur, chf, mxn, cad }

enum SprinklrApiKey {
  butlerTestChat,
  fabriikAppChat,
  butlerTestChatAppId,
  fabriikAppChatAppId
}

extension VsCurrencyExtension on VsCurrency {
  static const values = {
    VsCurrency.usd: {'usd': '\$', 'name': 'US Dollar'},
    VsCurrency.eur: {'eur': '€', 'name': 'Euro'},
    VsCurrency.chf: {'chf': 'CHF', 'name': 'Swiss Franc'},
    VsCurrency.mxn: {'mxn': '\$', 'name': 'Mexican Peso'},
    VsCurrency.cad: {'cad': '\$', 'name': 'Canadian Dollar'},
  };

  Map<String, String> get value => values[this]!;
}

extension SprinklrApiKeyExtension on SprinklrApiKey {
  static const values = {
    SprinklrApiKey.butlerTestChat: '2825bad1-6824-4e57-9c7c-f5391ba2b450',
    SprinklrApiKey.fabriikAppChat: 'f51fcd23-71ef-40c4-818e-739778ce1d1e',
    SprinklrApiKey.butlerTestChatAppId: '60884eefd5ecb727f98daa8f_app_929537',
    SprinklrApiKey.fabriikAppChatAppId: '60b7b3b4dc2cc1198b7d5644_app_948400',
  };

  String get value => values[this]!;
}

Map<String, String> globalCurrency = VsCurrency.usd.value;
String globalAppId = '';
