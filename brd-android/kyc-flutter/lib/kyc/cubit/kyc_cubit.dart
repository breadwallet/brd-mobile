import 'package:bloc/bloc.dart';
import 'package:kyc/kyc/models/kyc_doc_type.dart';

import './kyc_state.dart';
import '../models/kyc_pi.dart';

class KycCubit extends Cubit<KycState> {
  KycCubit()
      : super(
          KycState(
            pi: const KycPi(),
            fieldErrors: KycPiFieldErrors(),
            docType: KycDocType.driversLicense,
          ),
        );

  void setPi(KycPi pi) {
    emit(state.copyWith(pi: pi));
  }

  void setCountryCode(String countryCode) {
    emit(state.copyWith(pi: state.pi.copyWith(countryCode: countryCode)));
  }

  void setStreet(String street) {
    emit(state.copyWith(pi: state.pi.copyWith(street: street)));
  }

  void setCity(String city) {
    emit(state.copyWith(pi: state.pi.copyWith(city: city)));
  }

  void setStateCode(String stateCode) {
    emit(state.copyWith(pi: state.pi.copyWith(stateCode: stateCode)));
  }

  void setZipCode(String zipCode) {
    emit(state.copyWith(pi: state.pi.copyWith(zipCode: zipCode)));
  }

  void setDateOfBirth(DateTime? dateOfBirth) {
    emit(state.copyWith(pi: state.pi.copyWith(dateOfBirth: dateOfBirth)));
  }

  void setTaxIdNumber(String taxIdNumber) {
    emit(state.copyWith(pi: state.pi.copyWith(taxIdNumber: taxIdNumber)));
  }

  void clearFieldErrors() {
    emit(state.copyWith(fieldErrors: KycPiFieldErrors()));
  }

  void setFieldErrors(KycPiFieldErrors fieldErrors) {
    emit(state.copyWith(fieldErrors: fieldErrors));
  }

  void setDocType(KycDocType docType) {
    emit(state.copyWith(docType: docType));
  }
}
