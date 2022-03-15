import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:kyc/common/app_theme.dart';
import 'package:kyc/kyc/cubit/kyc_cubit.dart';
import 'package:kyc/kyc/models/kyc_pi.dart';
import 'package:kyc/kyc/view/kyc_select_document_type.dart';
import 'package:kyc/l10n/l10n.dart';

import './kyc_finish_page.dart';
import './kyc_pi_other_page.dart';
import './kyc_pi_res_page.dart';
import './kyc_scan_back_page.dart';
import './kyc_scan_front_page.dart';
import './kyc_selfie_page.dart';

class KycPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _KycPageState();
}

class _KycPageState extends State<KycPage> {
  final navigatorKey = GlobalKey<NavigatorState>();

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) {
        final pi = ModalRoute.of(context)!.settings.arguments as KycPi?;
        final cubit = KycCubit();
        if (pi != null) {
          cubit.setPi(pi);
        }
        return cubit;
      },
      child: WillPopScope(
        child: Navigator(
          key: navigatorKey,
          initialRoute: routeKycPiRes,
          onGenerateRoute: _onGenerateRoute,
        ),
        onWillPop: () async {
          final navState = navigatorKey.currentState!;
          if (navState.canPop()) {
            await navState.maybePop();
            return Future<bool>.value(false);
          }

          return Future<bool>.value(true);
        },
      ),
    );
  }

  Route _onGenerateRoute(RouteSettings settings) {
    const totalSteps = 7;
    WidgetBuilder builder;
    String pageTitle;
    int step;

    final l10n = context.l10n;

    switch (settings.name) {
      case routeKycPiRes:
        builder = (context) => KycPiResPage();
        pageTitle = l10n.kycPiResidenceHeadline;
        step = 1;
        break;

      case routeKycPiOther:
        builder = (context) => KycPiOtherPage();
        pageTitle = l10n.kycPiOtherHeadline;
        step = 2;
        break;

      case routeKycSelectDocType:
        builder = (context) => KycSelectDocumentTypePage();
        pageTitle = l10n.kycSelectDocTypeHeadline;
        step = 3;
        break;

      case routeKycScanFront:
        builder = (context) => KycScanFrontPage();
        pageTitle = l10n.kycScanFrontHeadline;
        step = 4;
        break;

      case routeKycScanBack:
        builder = (context) => KycScanBackPage();
        pageTitle = l10n.kycScanBackHeadline;
        step = 5;
        break;

      case routeKycSelfie:
        builder = (context) => KycSelfiePage();
        pageTitle = l10n.kycSelfieHeadline;
        step = 6;
        break;

      case routeKycFinish:
      default:
        builder = (context) => KycFinishPage();
        pageTitle = l10n.kycFinishHeadline;
        step = 7;
        break;
    }

    return KycPageRoute<Route>(
      builder: (context) {
        return Scaffold(
          body: SafeArea(
            child: _routeBody(context, pageTitle, step, totalSteps, builder),
          ),
        );
      },
      settings: settings,
    );
  }
}

const _outerPageMargins = EdgeInsets.fromLTRB(40, 20 / 2, 40, 40 / 2);
const _innerPageMargins = EdgeInsets.fromLTRB(0, 20 / 2, 0, 40 / 2);

/// Wraps the child and applies proper margins
class KycPageBody extends StatelessWidget {
  const KycPageBody({required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: _innerPageMargins,
      child: child,
    );
  }
}

/// Wraps the child in a SingleChildScrollView and applies proper margins
class KycPageScrollBody extends StatelessWidget {
  const KycPageScrollBody({required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Container(
        margin: _innerPageMargins,
        child: child,
      ),
    );
  }
}

/// Wraps the column children in a SingleChildScrollView
/// and applies proper margins.
class KycPageScrollColumn extends StatelessWidget {
  const KycPageScrollColumn({required this.children});

  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Container(
        margin: _innerPageMargins,
        child: Column(
          children: children,
        ),
      ),
    );
  }
}

Widget _routeBody(
  BuildContext context,
  String pageTitle,
  num step,
  num totalSteps,
  WidgetBuilder builder,
) {
  final theme = Theme.of(context);

  return Center(
    child: ConstrainedBox(
      constraints: const BoxConstraints(maxWidth: 400),
      child: Column(
        children: [
          Container(
            margin: const EdgeInsets.symmetric(vertical: 20),
            child: Text(
              pageTitle,
              style: theme.textTheme.headline6,
            ),
          ),
          Container(
            margin: const EdgeInsets.symmetric(horizontal: 40),
            child: LinearProgressIndicator(
              value: step / totalSteps,
              minHeight: 6,
              color: almostBlack,
              backgroundColor: grey3,
            ),
          ),
          Expanded(
            child: Container(
              margin: _outerPageMargins,
              child: builder(context),
            ),
          ),
        ],
      ),
    ),
  );
}

Widget _routeBody2(
  BuildContext context,
  String pageTitle,
  num step,
  num totalSteps,
  WidgetBuilder builder,
) {
  final theme = Theme.of(context);

  return Center(
    child: LayoutBuilder(
      builder: (context, bodyConstraints) {
        return SingleChildScrollView(
          child: Center(
            child: ConstrainedBox(
              constraints: BoxConstraints(
                minHeight: bodyConstraints.maxHeight,
                maxWidth: 400,
              ),
              child: IntrinsicHeight(
                child: Column(
                  children: [
                    Container(
                      margin: const EdgeInsets.symmetric(vertical: 20),
                      child: Text(
                        pageTitle,
                        style: theme.textTheme.headline6,
                      ),
                    ),
                    Container(
                      margin: const EdgeInsets.symmetric(horizontal: 40),
                      child: LinearProgressIndicator(
                        value: step / totalSteps,
                        minHeight: 6,
                        color: almostBlack,
                        backgroundColor: grey3,
                      ),
                    ),
                    Expanded(
                      child: Container(
                        margin: const EdgeInsets.fromLTRB(40, 20, 40, 40),
                        child: builder(context),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        );
      },
    ),
  );
}

const routeKycPiRes = '/';
const routeKycPiOther = '/pi-other';
const routeKycPrepareScan = '/pi-prepare-scan';
const routeKycSelectDocType = '/select-doc';
const routeKycScanFront = '/scan-front';
const routeKycScanBack = '/scan-back';
const routeKycSelfie = '/selfie';
const routeKycFinish = '/finish';

/// Used to change the KYC navigation transition to a simple fade
class KycPageRoute<T> extends MaterialPageRoute<T> {
  KycPageRoute({
    required WidgetBuilder builder,
    required RouteSettings settings,
  }) : super(
          builder: builder,
          settings: settings,
        );

  @override
  Widget buildTransitions(
    BuildContext context,
    Animation<double> animation,
    Animation<double> secondaryAnimation,
    Widget child,
  ) {
    return FadeTransition(opacity: animation, child: child);
  }
}
