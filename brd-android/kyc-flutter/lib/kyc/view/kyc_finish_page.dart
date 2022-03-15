import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:kyc/kyc/view/kyc_page.dart';
import 'package:kyc/l10n/l10n.dart';

class KycFinishPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = context.l10n;

    return KycPageBody(
      child: LayoutBuilder(
        builder: (context, layoutConstraints) {
          return ConstrainedBox(
            constraints: BoxConstraints(
              maxHeight: layoutConstraints.maxHeight,
            ),
            child: Column(
              children: [
                ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 256),
                  child: Text(
                    l10n.kycFinish,
                    textAlign: TextAlign.center,
                    style: theme.textTheme.bodyText2,
                  ),
                ),
                Expanded(
                  child: ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 256),
                    child: LayoutBuilder(
                      builder: (context, subLayoutConstraints) {
                        if (subLayoutConstraints.maxHeight >= 200 + 30) {
                          return Padding(
                            padding: const EdgeInsets.only(top: 30),
                            child: Image.asset('assets/kyc_finish.png'),
                          );
                        } else {
                          return Container();
                        }
                      },
                    ),
                  ),
                ),
                const SizedBox(height: 40),
                ElevatedButton(
                  onPressed: () {
                    SystemNavigator.pop();
                  },
                  child: Text(l10n.doneButtonText),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
