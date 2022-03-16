import 'package:country_picker/country_picker.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:kyc/l10n/l10n.dart';
import 'package:kyc/middleware/models/post_kyc_pi.dart';
import 'package:kyc/utils/form_validator.dart';
import 'package:kyc/utils/url_launcher.dart';

import './kyc_page.dart';
import '../cubit/kyc_cubit.dart';
import '../cubit/kyc_state.dart';

class KycPiResPage extends StatefulWidget {
  @override
  _KycPiResPageState createState() => _KycPiResPageState();
}

class _KycPiResPageState extends State<KycPiResPage> {
  final _formKey = GlobalKey<FormState>();
  final _countryCodeKey = GlobalKey<FormFieldState>();

  @override
  Widget build(BuildContext context) {
    return KycPageScrollColumn(
      children: [
        _piForm(),
        const SizedBox(height: 20),
        ElevatedButton(
          onPressed: _onSubmit,
          child: Text(context.l10n.nextButtonText),
        ),
        const SizedBox(height: 20),
        Consent(),
      ],
    );
  }

  Widget _piForm() {
    final l10n = context.l10n;

    return Form(
      key: _formKey,
      child: Column(
        children: [
          BlocBuilder<KycCubit, KycState>(
            buildWhen: (previous, current) =>
                (previous.pi.countryCode != current.pi.countryCode) ||
                (previous.fieldErrors
                        .getError(PostKycPiRequest.countryCodeParamName) !=
                    current.fieldErrors
                        .getError(PostKycPiRequest.countryCodeParamName)),
            builder: (context, state) {
              return TextFormField(
                key: _countryCodeKey,
                textInputAction: TextInputAction.next,
                initialValue: state.pi.countryCode,
                readOnly: true,
                onTap: () {
                  showCountryPicker(
                    context: context,
                    onSelect: (Country country) {
                      context
                          .read<KycCubit>()
                          .setCountryCode(country.countryCode);
                      _countryCodeKey.currentState!
                          .didChange(country.countryCode);
                    },
                  );
                },
                autovalidateMode: AutovalidateMode.onUserInteraction,
                validator: FormValidator.nonNullOrEmptyValidator,
                decoration: InputDecoration(
                  border: const OutlineInputBorder(),
                  labelText: l10n.kycCountryLabel,
                  hintText: l10n.kycCountryHint,
                  errorText: state.fieldErrors
                      .getError(PostKycPiRequest.countryCodeParamName),
                ),
              );
            },
          ),
          const SizedBox(height: 16),
          BlocBuilder<KycCubit, KycState>(
            buildWhen: (previous, current) =>
                (previous.pi.street != current.pi.street) ||
                (previous.fieldErrors
                        .getError(PostKycPiRequest.streetParamName) !=
                    current.fieldErrors
                        .getError(PostKycPiRequest.streetParamName)),
            builder: (context, state) {
              return TextFormField(
                key: const Key('street'),
                textInputAction: TextInputAction.next,
                initialValue: state.pi.street,
                onChanged: (value) => context.read<KycCubit>().setStreet(value),
                autovalidateMode: AutovalidateMode.onUserInteraction,
                validator: FormValidator.nonNullOrEmptyValidator,
                onSaved: (newValue) => newValue?.trim(),
                decoration: InputDecoration(
                  border: const OutlineInputBorder(),
                  labelText: l10n.kycStreetLabel,
                  hintText: l10n.kycStreetHint,
                  errorText: state.fieldErrors
                      .getError(PostKycPiRequest.streetParamName),
                ),
              );
            },
          ),
          const SizedBox(height: 16),
          BlocBuilder<KycCubit, KycState>(
            buildWhen: (previous, current) =>
                (previous.pi.city != current.pi.city) ||
                (previous.fieldErrors
                        .getError(PostKycPiRequest.cityParamName) !=
                    current.fieldErrors
                        .getError(PostKycPiRequest.cityParamName)),
            builder: (context, state) {
              return TextFormField(
                key: const Key('city'),
                textInputAction: TextInputAction.next,
                initialValue: state.pi.city,
                onChanged: (value) => context.read<KycCubit>().setCity(value),
                autovalidateMode: AutovalidateMode.onUserInteraction,
                validator: FormValidator.nonNullOrEmptyValidator,
                onSaved: (newValue) => newValue?.trim(),
                decoration: InputDecoration(
                  border: const OutlineInputBorder(),
                  labelText: l10n.kycCityLabel,
                  hintText: l10n.kycCityHint,
                  errorText: state.fieldErrors
                      .getError(PostKycPiRequest.cityParamName),
                ),
              );
            },
          ),
          BlocBuilder<KycCubit, KycState>(
            buildWhen: (previous, current) =>
                (previous.pi.stateCode != current.pi.stateCode) ||
                (previous.pi.countryRequiresState(false) !=
                    current.pi.countryRequiresState(false)) ||
                (previous.fieldErrors
                        .getError(PostKycPiRequest.stateParamName) !=
                    current.fieldErrors
                        .getError(PostKycPiRequest.stateParamName)),
            builder: (context, state) {
              if (!state.pi.countryRequiresState(false)) {
                return Container();
              }

              return Column(
                children: [
                  const SizedBox(height: 16),
                  TextFormField(
                    key: const Key('stateCode'),
                    textInputAction: TextInputAction.next,
                    initialValue: state.pi.stateCode,
                    onChanged: (value) =>
                        context.read<KycCubit>().setStateCode(value),
                    autovalidateMode: AutovalidateMode.onUserInteraction,
                    validator: FormValidator.nonNullOrEmptyValidator,
                    onSaved: (newValue) => newValue?.trim(),
                    decoration: InputDecoration(
                      border: const OutlineInputBorder(),
                      labelText: l10n.kycStateLabel,
                      hintText: l10n.kycStateHint,
                      errorText: state.fieldErrors
                          .getError(PostKycPiRequest.stateParamName),
                    ),
                  )
                ],
              );
            },
          ),
          const SizedBox(height: 16),
          BlocBuilder<KycCubit, KycState>(
            buildWhen: (previous, current) =>
                (previous.pi.zipCode != current.pi.zipCode) ||
                (previous.fieldErrors.getError(PostKycPiRequest.zipParamName) !=
                    current.fieldErrors
                        .getError(PostKycPiRequest.zipParamName)),
            builder: (context, state) {
              return TextFormField(
                key: const Key('zipCode'),
                textInputAction: TextInputAction.done,
                onFieldSubmitted: (_) {
                  _onSubmit();
                },
                initialValue: state.pi.zipCode,
                onChanged: (value) =>
                    context.read<KycCubit>().setZipCode(value),
                autovalidateMode: AutovalidateMode.onUserInteraction,
                validator: FormValidator.nonNullOrEmptyValidator,
                onSaved: (newValue) => newValue?.trim(),
                decoration: InputDecoration(
                  border: const OutlineInputBorder(),
                  labelText: l10n.kycZipCodeLabel,
                  hintText: l10n.kycZipCodeHint,
                  errorText:
                      state.fieldErrors.getError(PostKycPiRequest.zipParamName),
                ),
              );
            },
          ),
        ],
      ),
    );
  }

  void _onSubmit() {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    _formKey.currentState!.save();

    Navigator.of(context).pushNamed(routeKycPiOther);
  }
}

class Consent extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final theme = Theme.of(context);

    return RichText(
      text: TextSpan(
        children: [
          TextSpan(
            text: '${l10n.kycConsentPart1Fabriik} ',
            style: theme.textTheme.subtitle2,
          ),
          TextSpan(
            text: l10n.kycConsentPart2TermsOfUse,
            style: theme.textTheme.subtitle2!
                .copyWith(decoration: TextDecoration.underline),
            recognizer: TapGestureRecognizer()
              ..onTap = () async {
                await tryLaunchUrl(l10n.kycConsentTermsUrl);
              },
          ),
          TextSpan(
            text: ' ${l10n.kycConsentPart3And} ',
            style: theme.textTheme.subtitle2,
          ),
          TextSpan(
            text: l10n.kycConsentPart4PrivacyPolicy,
            style: theme.textTheme.subtitle2!
                .copyWith(decoration: TextDecoration.underline),
            recognizer: TapGestureRecognizer()
              ..onTap = () async {
                await tryLaunchUrl(l10n.kycConsentPrivacyUrl);
              },
          ),
        ],
      ),
    );
  }
}
