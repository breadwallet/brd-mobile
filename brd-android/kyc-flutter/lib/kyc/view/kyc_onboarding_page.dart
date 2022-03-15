import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:kyc/common/app_theme.dart';
import 'package:kyc/common/dependency_provider.dart';
import 'package:kyc/kyc/models/kyc_pi.dart';
import 'package:kyc/l10n/l10n.dart';
import 'package:kyc/middleware/models/merapi.dart';
import 'package:kyc/widgets/error_snackbar.dart';

class KycOnboardingPage extends StatefulWidget {
  @override
  _KycOnboardingPageState createState() => _KycOnboardingPageState();
}

class _KycOnboardingPageState extends State<KycOnboardingPage> {
  bool working = false;
  final pageController = PageController();

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Theme(
        data: AppTheme.darkTheme,
        child: Builder(
          builder: (context) {
            return Scaffold(
              body: Center(
                child: ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 400),
                  child: Container(
                    margin: const EdgeInsets.all(40),
                    child: PageView(
                      controller: pageController,
                      children: [
                        _page1(context),
                        _page2(context),
                      ],
                    ),
                  ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }

  Widget _page1(BuildContext context) {
    final l10n = context.l10n;
    final theme = Theme.of(context);

    return Container(
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
                    l10n.kycIntro1,
                    textAlign: TextAlign.center,
                    style: theme.textTheme.bodyText1!.copyWith(
                      fontSize: 20,
                      height: 28 / 20,
                    ),
                  ),
                ),
                Expanded(
                  child: ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 256),
                    child: LayoutBuilder(
                      builder: (context, subLayoutConstraints) {
                        if (subLayoutConstraints.maxHeight >= 200 + 40) {
                          return Padding(
                            padding: const EdgeInsets.only(top: 40),
                            child: Image.asset('assets/phone_prep.png'),
                          );
                        } else {
                          return Container();
                        }
                      },
                    ),
                  ),
                ),
                const SizedBox(height: 20),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    TextButton(
                      onPressed: () {
                        if (Navigator.canPop(context)) {
                          Navigator.pop(context);
                        } else {
                          SystemNavigator.pop();
                        }
                      },
                      style: AppTheme.buttonStyleIntrinsicSize.copyWith(
                        foregroundColor: MaterialStateProperty.all(grey2),
                      ),
                      child: Text(l10n.closeButtonText),
                    ),
                    TextButton(
                      onPressed: () {
                        pageController.nextPage(
                          duration: const Duration(milliseconds: 200),
                          curve: Curves.easeIn,
                        );
                      },
                      style: AppTheme.buttonStyleIntrinsicSize,
                      child: Text(l10n.nextButtonText),
                    ),
                  ],
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _page2(BuildContext context) {
    final l10n = context.l10n;
    final theme = Theme.of(context);

    return Container(
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
                    l10n.kycIntro2,
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
                            child: Image.asset('assets/phone_id.png'),
                          );
                        } else {
                          return Container();
                        }
                      },
                    ),
                  ),
                ),
                const SizedBox(height: 40),
                if (working)
                  ConstrainedBox(
                    constraints: const BoxConstraints(minHeight: buttonHeight),
                    child: const Center(
                      child: CircularProgressIndicator(),
                    ),
                  )
                else
                  ElevatedButton(
                    onPressed: _onSubmit,
                    child: Text(l10n.beginButtonText),
                  ),
              ],
            ),
          );
        },
      ),
    );
  }

  Future<void> _onSubmit() async {
    setState(() {
      working = true;
    });
    try {
      final pi = await DependencyProvider.of(context).userRepo.getKycPi();
      print(pi);
      Navigator.of(context).pushNamed('/kyc-main', arguments: pi);
    } catch (error, stacktrace) {
      print(error);
      print(stacktrace);
      // ScaffoldMessenger.of(context)
      //   ..removeCurrentSnackBar()
      //   ..showSnackBar(
      //     ErrorSnackBar.fromLocaleMerapiError(error, context.l10n),
      //   );
    } finally {
      setState(() {
        working = false;
      });
    }
  }
}
