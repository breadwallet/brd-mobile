import 'package:equatable/equatable.dart';
import 'package:kyc/kyc/models/kyc_doc_type.dart';

import '../models/kyc_pi.dart';

class KycState extends Equatable {
  const KycState({
    required this.pi,
    required this.fieldErrors,
    required this.docType,
  });

  final KycPi pi;
  final KycPiFieldErrors fieldErrors;
  final KycDocType docType;

  @override
  List<Object> get props => [pi, fieldErrors, docType];

  @override
  bool? get stringify => true;

  KycState copyWith({
    KycPi? pi,
    KycPiFieldErrors? fieldErrors,
    KycDocType? docType,
  }) {
    return KycState(
      pi: pi ?? this.pi,
      fieldErrors: fieldErrors ?? this.fieldErrors,
      docType: docType ?? this.docType,
    );
  }
}
