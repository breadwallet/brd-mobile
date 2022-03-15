class KycStatus {
  KycStatus(
      {this.piiCleared,
      this.amlCleared,
      this.identitiyConfirmed,
      this.identityDocsVerified,
      this.readyForReview,
      this.piiStatus,
      this.amlStatus});

  factory KycStatus.fromJson(Map<String, dynamic> json) => KycStatus(
        piiCleared: json['pii_cleared'] as bool?,
        amlCleared: json['aml_cleared'] as bool?,
        identitiyConfirmed: json['identity_confirmed'] as bool?,
        identityDocsVerified: json['identity_documents_verified'] as bool?,
        readyForReview: json['ready_for_review'] as bool?,
        piiStatus: json['pii_status'] as String?,
        amlStatus: json['aml_status'] as bool?,
      );

  final bool? piiCleared;

  final bool? amlCleared;

  final bool? identitiyConfirmed;

  final bool? identityDocsVerified;

  final bool? readyForReview;

  final String? piiStatus;

  final bool? amlStatus;

  @override
  String toString() {
    return 'KycStatus{piCleared: $piiCleared, amlCleared: $amlCleared,'
        'identitiyConfirmed: $identitiyConfirmed,'
        'identityDocsVerified: $identityDocsVerified,'
        'readyForReview: $readyForReview, piiStatus: $piiStatus,'
        'amlStatus: $amlStatus}';
  }
}
