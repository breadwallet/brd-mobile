import 'package:datetime_picker_formfield/datetime_picker_formfield.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import 'package:kyc/common/app_theme.dart';
import 'package:kyc/common/dependency_provider.dart';
import 'package:kyc/kyc/models/kyc_pi.dart';
import 'package:kyc/l10n/l10n.dart';
import 'package:kyc/middleware/models/merapi.dart';
import 'package:kyc/middleware/models/post_kyc_pi.dart';
import 'package:kyc/utils/form_validator.dart';
import 'package:kyc/widgets/error_snackbar.dart';

import './kyc_page.dart';
import '../cubit/kyc_cubit.dart';
import '../cubit/kyc_state.dart';

class KycPiOtherPage extends StatefulWidget {
  @override
  _KycPiOtherPageState createState() => _KycPiOtherPageState();
}

class _KycPiOtherPageState extends State<KycPiOtherPage> {
  final _formKey = GlobalKey<FormState>();
  bool working = false;

  @override
  Widget build(BuildContext context) {
    return KycPageScrollColumn(
      children: [
        piForm(),
        const SizedBox(height: 20),
        if (working)
          ConstrainedBox(
            constraints: const BoxConstraints(minHeight: buttonHeight),
            child: const Center(child: CircularProgressIndicator()),
          )
        else
          ElevatedButton(
            onPressed: _onSubmit,
            child: Text(context.l10n.nextButtonText),
          ),
      ],
    );
  }

  Widget piForm() {
    final l10n = context.l10n;

    return Form(
      key: _formKey,
      child: Column(
        children: [
          BlocBuilder<KycCubit, KycState>(
            buildWhen: (previous, current) =>
                (previous.pi.dateOfBirth != current.pi.dateOfBirth) ||
                (previous.fieldErrors
                        .getError(PostKycPiRequest.dateOfBirthParamName) !=
                    current.fieldErrors
                        .getError(PostKycPiRequest.dateOfBirthParamName)),
            builder: (context, state) {
              return DateTimeField(
                key: const Key('dateOfBirth'),
                format: DateFormat.yMd(),
                textInputAction: TextInputAction.next,
                readOnly: working,
                initialValue: state.pi.dateOfBirth,
                onChanged: (value) =>
                    context.read<KycCubit>().setDateOfBirth(value),
                autovalidateMode: AutovalidateMode.onUserInteraction,
                validator: FormValidator.nonNullValidator,
                decoration: InputDecoration(
                  border: const OutlineInputBorder(),
                  labelText: l10n.kycDateOfBirthLabel,
                  hintText: l10n.kycDateOfBirthHint,
                  errorText: state.fieldErrors
                      .getError(PostKycPiRequest.dateOfBirthParamName),
                ),
                onShowPicker: (context, currentValue) {
                  return showDatePicker(
                    context: context,
                    initialDate: state.pi.dateOfBirth ?? DateTime(1980, 12, 31),
                    firstDate: DateTime(1900),
                    lastDate: DateTime.now(),
                  );
                },
              );
            },
          ),
          const SizedBox(height: 16),
          BlocBuilder<KycCubit, KycState>(
            buildWhen: (previous, current) =>
                (previous.pi.taxIdNumber != current.pi.taxIdNumber) ||
                (previous.fieldErrors
                        .getError(PostKycPiRequest.taxIdNumberParamName) !=
                    current.fieldErrors
                        .getError(PostKycPiRequest.taxIdNumberParamName)),
            builder: (context, state) {
              return TextFormField(
                key: const Key('taxIdNumber'),
                textInputAction: TextInputAction.done,
                onFieldSubmitted: (_) {
                  _onSubmit();
                },
                readOnly: working,
                initialValue: state.pi.taxIdNumber,
                onChanged: (value) =>
                    context.read<KycCubit>().setTaxIdNumber(value),
                autovalidateMode: AutovalidateMode.onUserInteraction,
                validator: FormValidator.nonNullOrEmptyValidator,
                onSaved: (newValue) => newValue?.trim(),
                decoration: InputDecoration(
                  border: const OutlineInputBorder(),
                  labelText: l10n.kycTaxIdNumberLabel,
                  hintText: l10n.kycTaxIdNumberHint,
                  errorText: state.fieldErrors
                      .getError(PostKycPiRequest.taxIdNumberParamName),
                ),
              );
            },
          ),
        ],
      ),
    );
  }

  Future<void> _onSubmit() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    _formKey.currentState!.save();

    setState(() {
      working = true;
    });

    final cubit = context.read<KycCubit>()..clearFieldErrors();

    try {
      final pi = cubit.state.pi;
      await DependencyProvider.of(context).userRepo.setKycPi(pi);

      await Navigator.of(context).pushNamed(routeKycSelectDocType);
    } on MerapiError catch (error) {
      ScaffoldMessenger.of(context)
        ..removeCurrentSnackBar()
        ..showSnackBar(
          ErrorSnackBar.fromLocaleMerapiError(error, context.l10n),
        );

      if (error.data != null) {
        final ed = PostKycPiErrorData.fromMerapiError(error);
        cubit.setFieldErrors(KycPiFieldErrors.fromApi(ed));

        final firstPageParamNames = [
          PostKycPiRequest.countryCodeParamName,
          PostKycPiRequest.stateParamName,
          PostKycPiRequest.streetParamName,
          PostKycPiRequest.cityParamName,
          PostKycPiRequest.zipParamName
        ];
        final hasFirstPageError =
            firstPageParamNames.any(ed.parameters.containsKey);

        if (hasFirstPageError) {
          Navigator.of(context).pop();
        }
      }
    } on Exception catch (error) {
      ScaffoldMessenger.of(context)
        ..removeCurrentSnackBar()
        ..showSnackBar(ErrorSnackBar.fromException(error));
      print(error);
    } finally {
      setState(() {
        working = false;
      });
    }
  }
}
