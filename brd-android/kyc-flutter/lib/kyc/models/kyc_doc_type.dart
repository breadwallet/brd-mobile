enum KycDocType {
  driversLicense,
  identityCard,
  passport,
  residencePermit,
  selfie,
}

enum KycDocSide {
  front,
  back,
}

const kycDocTypeDriversLicenseFront = 'DRIVERS_LICENSE_FRONT';
const kycDocTypeDriversLicenseBack = 'DRIVERS_LICENSE_BACK';
const kycDocTypeIdentityCardFront = 'ID_FRONT';
const kycDocTypeIdentityCardBack = 'ID_BACK';
const kycDocTypePassportFront = 'PASSPORT_FRONT';
const kycDocTypePassportBack = 'PASSPORT_BACK';
const kycDocTypeResidencePermitFront = 'RESIDENCE_PERMIT_FRONT';
const kycDocTypeResidencePermitBack = 'RESIDENCE_PERMIT_BACK';
const kycDocTypeSelfie = 'SELFIE';

String _getApiType(KycDocType type, KycDocSide side) {
  switch (type) {
    case KycDocType.driversLicense:
      return side == KycDocSide.front
          ? kycDocTypeDriversLicenseFront
          : kycDocTypeDriversLicenseBack;

    case KycDocType.identityCard:
      return side == KycDocSide.front
          ? kycDocTypeIdentityCardFront
          : kycDocTypeIdentityCardBack;

    case KycDocType.passport:
      return side == KycDocSide.front
          ? kycDocTypePassportFront
          : kycDocTypePassportBack;

    case KycDocType.residencePermit:
      return side == KycDocSide.front
          ? kycDocTypeResidencePermitFront
          : kycDocTypeResidencePermitBack;

    case KycDocType.selfie:
      return kycDocTypeSelfie;
  }
}

extension MerapiKycDocType on KycDocType {
  String toMerapiDocType(KycDocSide side) {
    return _getApiType(this, side);
  }
}
