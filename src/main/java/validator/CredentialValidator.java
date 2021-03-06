package validator;

import java.security.KeyStore;
import java.util.Map;

import hirs.data.persist.AppraisalStatus;
import hirs.data.persist.DeviceInfoReport;
import hirs.data.persist.SupplyChainValidation;
import hirs.data.persist.certificate.EndorsementCredential;
import hirs.data.persist.certificate.PlatformCredential;

/**
 * A class used to support supply chain validation by performing the actual
 * validation of credentials.
 */
public interface CredentialValidator {
    /**
     * Checks if the platform credential is valid.
     *
     * @param pc The platform credential to verify.
     * @param trustStore trust store holding trusted certificates.
     * @param acceptExpired whether or not to accept expired certificates as valid.
     * @return The result of the validation.
     */
    AppraisalStatus validatePlatformCredential(PlatformCredential pc,
                                              KeyStore trustStore,
                                              boolean acceptExpired);

    /**
     * Checks if the platform credential's attributes are valid.
     * @param pc The platform credential to verify.
     * @param deviceInfoReport Report containing the serial numbers of the platform to be validated.
     * @param ec The endorsement credential supplied from the same identity request as
     *           the platform credential.
     * @return The result of the validation.
     */
    AppraisalStatus validatePlatformCredentialAttributes(PlatformCredential pc,
                                                         DeviceInfoReport deviceInfoReport,
                                                         EndorsementCredential ec);

    /**
     * Checks if the delta credential's attributes are valid.
     * @param delta the delta credential to verify
     * @param deviceInfoReport The device info report containing
     *                         serial number of the platform to be validated.
     * @param base the base credential from the same identity request
     *                              as the delta credential.
     * @param deltaMapping delta certificates associated with the
     *                          delta supply validation.
     * @return the result of the validation.
     */
    AppraisalStatus validateDeltaPlatformCredentialAttributes(PlatformCredential delta,
                                                        DeviceInfoReport deviceInfoReport,
                                                        PlatformCredential base,
                                                        Map<PlatformCredential,
                                                        SupplyChainValidation> deltaMapping);
    /**
     * Checks if the endorsement credential is valid.
     *
     * @param ec the endorsement credential to verify.
     * @param trustStore trust store holding trusted trusted certificates.
     * @param acceptExpired whether or not to accept expired certificates as valid.
     * @return the result of the validation.
     */
    AppraisalStatus validateEndorsementCredential(EndorsementCredential ec,
                                                       KeyStore trustStore,
                                                       boolean acceptExpired);

	AppraisalStatus validateDeltaPlatformCredentialAttributes(PlatformCredential deltaPlatformCredential,
			data.persist.DeviceInfoReport deviceInfoReport, PlatformCredential basePlatformCredential,
			Map<PlatformCredential, SupplyChainValidation> deltaMapping);

	AppraisalStatus validatePlatformCredentialAttributes(PlatformCredential platformCredential,
			data.persist.DeviceInfoReport deviceInfoReport, EndorsementCredential endorsementCredential);

	AppraisalStatus validateDeltaPlatformCredentialAttributes(data.persist.certificate.PlatformCredential delta,
			DeviceInfoReport deviceInfoReport, data.persist.certificate.PlatformCredential base,
			Map<data.persist.certificate.PlatformCredential, SupplyChainValidation> deltaMapping);

	AppraisalStatus validatePlatformCredentialAttributes(data.persist.certificate.PlatformCredential pc,
			DeviceInfoReport deviceInfoReport, EndorsementCredential ec);

	AppraisalStatus validatePlatformCredentialAttributes(data.persist.certificate.PlatformCredential platformCredential,
			data.persist.DeviceInfoReport deviceInfoReport, EndorsementCredential endorsementCredential);

	AppraisalStatus validatePlatformCredential(data.persist.certificate.PlatformCredential pc, KeyStore trustStore,
			boolean acceptExpired);

//	AppraisalStatus validateDeltaPlatformCredentialAttributes(
//			data.persist.certificate.PlatformCredential deltaPlatformCredential,
//			data.persist.DeviceInfoReport deviceInfoReport,
//			data.persist.certificate.PlatformCredential basePlatformCredential,
//			Map<data.persist.certificate.PlatformCredential, SupplyChainValidation> deltaMapping);
}
