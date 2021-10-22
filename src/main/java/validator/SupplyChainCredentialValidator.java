package validator;

import static hirs.data.persist.AppraisalStatus.Status.ERROR;
import static hirs.data.persist.AppraisalStatus.Status.FAIL;
import static hirs.data.persist.AppraisalStatus.Status.PASS;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
//import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
//import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sun.jndi.toolkit.ctx.PartialCompositeContext;

import data.persist.DeviceInfoReport;
import data.persist.FirmwareInfo;
import data.persist.HardwareInfo;
import data.persist.NetworkInfo;
import data.persist.OSInfo;
import data.persist.TPMInfo;
import data.persist.certificate.PlatformCredential;
//import hirs.data.persist.FirmwareInfo;
//import hirs.data.persist.NetworkInfo;
import hirs.data.persist.AppraisalStatus;
//import hirs.data.persist.DeviceInfoReport;
//import hirs.data.persist.HardwareInfo;
//import hirs.data.persist.OSInfo;
import hirs.data.persist.ComponentInfo;
import hirs.data.persist.SupplyChainValidation;
import hirs.data.persist.certificate.Certificate;
import hirs.data.persist.certificate.EndorsementCredential;
import hirs.data.persist.certificate.attributes.ComponentIdentifier;
import hirs.data.persist.certificate.attributes.PlatformConfiguration;
import hirs.data.persist.certificate.attributes.PlatformProperty;
import hirs.data.persist.certificate.attributes.V2.ComponentIdentifierV2;
import tcg.credential.PlatformProperties;


/**
 * Validates elements of the supply chain.
 */
// @Service // TODO: evaluate if this is necessary or not
public final class SupplyChainCredentialValidator implements CredentialValidator {
    private static final int NUC_VARIABLE_BIT = 159;

    private static final Logger LOGGER = LogManager.getLogger(
            SupplyChainCredentialValidator.class);

    /**
     * AppraisalStatus message for a valid endorsement credential appraisal.
     */
    public static final String ENDORSEMENT_VALID = "Endorsement credential validated";

    /**
     * AppraisalStatus message for a valid platform credential appraisal.
     */
    public static final String PLATFORM_VALID = "Platform credential validated";

    /**
     * AppraisalStatus message for a valid platform credential attributes appraisal.
     */
    public static final String PLATFORM_ATTRIBUTES_VALID =
            "Platform credential attributes validated";

    private static final Map<PlatformCredential, StringBuilder> DELTA_FAILURES = new HashMap<>();

    /*
     * Ensure that BouncyCastle is configured as a javax.security.Security provider, as this
     * class expects it to be available.
     */
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Default constructor, should only be instantiated for testing.
     */
    public SupplyChainCredentialValidator() {

    }

    /**
     * Parses the output from PACCOR's allcomponents.sh script into ComponentInfo objects.
     * @param paccorOutput the output from PACCOR's allcomoponents.sh
     * @return a list of ComponentInfo objects built from paccorOutput
     * @throws IOException if something goes wrong parsing the JSON
     */
    public static List<ComponentInfo> getComponentInfoFromPaccorOutput(final String paccorOutput)
            throws IOException {
        List<ComponentInfo> componentInfoList = new ArrayList<>();

        if (StringUtils.isNotEmpty(paccorOutput)) {
        	// System.out.println("** DEBUG getComponentInfoFromPaccorOutput : "+paccorOutput);
            
            // ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
            
            // System.out.println("** DEBUG getComponentInfoFromPaccorOutput : LOADED ObjectMapper");
            
            File paccorFile = new File(paccorOutput);
//            JsonNode rootNode = objectMapper.readTree(paccorOutput);
            JsonNode rootNode = objectMapper.readTree(paccorFile);
            
            // System.out.println("** DEBUG getComponentInfoFromPaccorOutput : READTREE PACCOROUTPUT");
            
            Iterator<JsonNode> jsonComponentNodes
                    = rootNode.findValue("COMPONENTS").elements();
            while (jsonComponentNodes.hasNext()) {
                JsonNode next = jsonComponentNodes.next();
                componentInfoList.add(new ComponentInfo(
                        getJSONNodeValueAsText(next, "MANUFACTURER"),
                        getJSONNodeValueAsText(next, "MODEL"),
                        getJSONNodeValueAsText(next, "SERIAL"),
                        getJSONNodeValueAsText(next, "REVISION")));
                // System.out.println("** DEBUG getComponentInfoFromPaccorOutput, component: " + jsonComponentNodes.toString());
            }
        }

        return componentInfoList;
    }

    public static List<PlatformProperties> getPropertiesInfoFromPaccorOutput(final String paccorOutput)
            throws IOException {
        List<PlatformProperties> propertiesInfoList = new ArrayList<>();

        if (StringUtils.isNotEmpty(paccorOutput)) {
        	// System.out.println("** DEBUG getPropertiesInfoFromPaccorOutput : "+paccorOutput);
            
            // ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
            
            // System.out.println("** DEBUG getPropertiesInfoFromPaccorOutput : LOADED ObjectMapper");
            
            File paccorFile = new File(paccorOutput);
//            JsonNode rootNode = objectMapper.readTree(paccorOutput);
            JsonNode rootNode = objectMapper.readTree(paccorFile);
            
            // System.out.println("** DEBUG getPropertiesInfoFromPaccorOutput : READTREE PACCOROUTPUT");
            
            Iterator<JsonNode> jsonPropertiesNodes
                    = rootNode.findValue("PROPERTIES").elements();
            
            
            while (jsonPropertiesNodes.hasNext()) {
                JsonNode next = jsonPropertiesNodes.next();
                
                // Only getting the properties on the SCL data case. Otherwise, ignore.
                if (getJSONNodeValueAsText(next, "PROPERTYNAME") != null && 
                	getJSONNodeValueAsText(next, "PROPERTYVALUE") != null) {
                	propertiesInfoList.add(new PlatformProperties(
                    		new DERUTF8String(getJSONNodeValueAsText(next, "PROPERTYNAME")),
                    		new DERUTF8String(getJSONNodeValueAsText(next, "PROPERTYVALUE"))));
//                  System.out.println("** DEBUG getPropertiesInfoFromPaccorOutput, component: " + jsonPropertiesNodes.toString());                
                }
                
                // For now let's let the NAME and VALUE PROPERTIES not being loaded
                // This is are the entries generated by Paccor                
//                if (getJSONNodeValueAsText(next, "NAME") != null && 
//                    getJSONNodeValueAsText(next, "VALUE") != null) {
//                    propertiesInfoList.add(new PlatformProperties(
//                    		new DERUTF8String(getJSONNodeValueAsText(next, "NAME")),
//                       		new DERUTF8String(getJSONNodeValueAsText(next, "VALUE"))));
//                    System.out.println("** DEBUG getPropertiesInfoFromPaccorOutput, component: " + jsonPropertiesNodes.toString());                
//                }
            }
        }

        return propertiesInfoList;
    }
    
    public static HardwareInfo getHardwareInfoFromPaccorOutput(final String paccorOutput)
            throws IOException {
    	
        HardwareInfo hwInfo = null;            
        if (StringUtils.isNotEmpty(paccorOutput)) {
//        	System.out.println("** DEBUG getHardwareInfoFromPaccorOutput: "+paccorOutput);
            
            // ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
            
            // System.out.println("** DEBUG getHardwareInfoFromPaccorOutput : LOADED ObjectMapper");
            
            File paccorFile = new File(paccorOutput);
            JsonNode rootNode = objectMapper.readTree(paccorFile);
            
            // System.out.println("** DEBUG getHardwareInfoFromPaccorOutput : READTREE PACCOROUTPUT");

            JsonNode jsonHwNodes = rootNode.get("PLATFORM");            
            String platMan = getJSONNodeValueAsText(jsonHwNodes, "PLATFORMMANUFACTURERSTR");
            String platMod = getJSONNodeValueAsText(jsonHwNodes, "PLATFORMMODEL");
            String platVer = getJSONNodeValueAsText(jsonHwNodes, "PLATFORMVERSION");
            String platSer = getJSONNodeValueAsText(jsonHwNodes, "PLATFORMSERIAL");
            try {
            	hwInfo = new HardwareInfo(platMan, platMod, platVer, platSer,
            			null,  //TODO: check exact description for chassisSerialNumber  
            			null); //TODO: check exact description for baseboardSerialNumber
            	// System.out.println("** DEBUG getHardwareInfoFromPaccorOutput, component: " + hwInfo);
            	// } else {
            	// 		System.out.println("Error: could not find a PLATFORM section on the Hardware Manifest");
            	// }
            } catch (Exception e) {
				// TODO: handle exception
            	System.out.println("HardwareInfo creation error, creating it with empty values.");
            	hwInfo = new HardwareInfo();
			}
        }        
        
        return hwInfo;
    }

    private static String getJSONNodeValueAsText(JsonNode node, String fieldName) {
        if (node.hasNonNull(fieldName)) {
            return node.findValue(fieldName).asText();
        }
        return null;
    }

    /**
     * Checks if the platform credential is valid.
     *
     * @param pc The platform credential to verify.
     * @param trustStore trust store holding trusted certificates.
     * @param acceptExpired whether or not to accept expired certificates as valid.
     * @return The result of the validation.
     */
    @Override
    public AppraisalStatus validatePlatformCredential(final PlatformCredential pc,
                                                     final KeyStore trustStore,
                                                     final boolean acceptExpired) {
    	
        final String baseErrorMessage = "Can't validate platform credential without ";
        String message;
        if (pc == null) {
            message = baseErrorMessage + "a platform credential\n";
            LOGGER.error(message);
            return new AppraisalStatus(FAIL, message);
        }
        try {
            if (trustStore == null || trustStore.size() == 0) {
                message = baseErrorMessage + "a trust store\n";
                LOGGER.error(message);
                return new AppraisalStatus(FAIL, message);
            }
        } catch (KeyStoreException e) {
            message = baseErrorMessage + "an intitialized trust store";
            LOGGER.error(message);
            return new AppraisalStatus(FAIL, message);
        }

        X509AttributeCertificateHolder attributeCert = null;
        try {
            attributeCert = pc.getX509AttributeCertificateHolder();
        } catch (IOException e) {
            message = "Could not retrieve X509 Attribute certificate";
            LOGGER.error(message, e);
            return new AppraisalStatus(FAIL, message + " " + e.getMessage());
        }

        // check validity period, currently acceptExpired will also accept not yet
        // valid certificates
        if (!acceptExpired && !pc.isValidOn(new Date())) {
            message = "Platform credential has expired";
            // if not valid at the current time
            LOGGER.warn(message);
            return new AppraisalStatus(FAIL, message);
        }

        // verify cert against truststore
        try {
            if (verifyCertificate(attributeCert, trustStore)) {
                message = PLATFORM_VALID;
                LOGGER.info(message);
                return new AppraisalStatus(PASS, message);
            } else {
                message = "Platform credential failed verification";
                LOGGER.error(message);
                return new AppraisalStatus(FAIL, message);
            }
        } catch (SupplyChainValidatorException e) {
            message = "An error occurred indicating the credential is not valid";
            LOGGER.warn(message, e);
            return new AppraisalStatus(FAIL, message + " " + e.getMessage());
        }
    }

    
    // Temporary routine to fake all data but the hardwareInfo, which matters most
    private DeviceInfoReport buildReport(final HardwareInfo hardwareInfo) {
        final InetAddress ipAddress = getTestIpAddress();
        final byte[] macAddress = new byte[] {11, 22, 33, 44, 55, 66};

        OSInfo osInfo = new OSInfo();
        NetworkInfo networkInfo = new NetworkInfo("test", ipAddress, macAddress);
        FirmwareInfo firmwareInfo = new FirmwareInfo();
        TPMInfo tpmInfo = new TPMInfo();

        return new DeviceInfoReport(networkInfo, osInfo, firmwareInfo, hardwareInfo, tpmInfo, "1");
    }
    // Temporary routine to fake the ip address
    private static InetAddress getTestIpAddress() {
        try {
            return InetAddress.getByAddress(new byte[] {127, 0, 0, 1});
        } catch (UnknownHostException e) {
            return null;
        }
    }

    
    /**
     * Checks if the platform credential's attributes are valid.
     * @param platformCredential The platform credential to verify.
     * @param deviceInfoReport The device info report containing
     *                         serial number of the platform to be validated.
     * @param endorsementCredential The endorsement credential supplied from the same
     *          identity request as the platform credential.
     * @return The result of the validation.
     */
    @Override
    public AppraisalStatus validatePlatformCredentialAttributes(
            PlatformCredential platformCredential,
            DeviceInfoReport deviceInfoReport,
            final EndorsementCredential endorsementCredential) {
        final String baseErrorMessage = "Can't validate platform credential attributes without ";
        String message;
        if (platformCredential == null) {
            message = baseErrorMessage + "a platform credential";
            LOGGER.error(message);
            return new AppraisalStatus(FAIL, message);
        }
        if (deviceInfoReport == null) {
            message = baseErrorMessage + "a device info report";
            LOGGER.error(message);
            return new AppraisalStatus(FAIL, message);
        }
//        if (endorsementCredential == null) {
//            message = baseErrorMessage + "an endorsement credential";
//            LOGGER.error(message);
//            return new AppraisalStatus(FAIL, message);
//        }

        HardwareInfo hwInfo = null;
        
        // ********** BEGIN OF part to be commented out if not checking the platform properties
        String paccorFile = deviceInfoReport.getPaccorOutputString();
		try {
			hwInfo = getHardwareInfoFromPaccorOutput(paccorFile);
			// buildReport creates a stub DeviceInfoReport instance with real HardwareInfo
			deviceInfoReport = buildReport(hwInfo);
			deviceInfoReport.setPaccorOutputString(paccorFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // ********** END OF part to be commented out if not checking the platform properties

		
//		System.out.println("** DEBUG: Hardware info: " + hwInfo);
        
        // Quick, early check if the platform credential references the endorsement credential
        //TODO: temporarily commented
//        if (!endorsementCredential.getSerialNumber()
//                .equals(platformCredential.getHolderSerialNumber())) {
//            message = "Platform Credential holder serial number does not match "
//                    + "the Endorsement Credential's serial number";
//            LOGGER.error(message);
//            return new AppraisalStatus(FAIL, message);
//        }

        String credentialType = platformCredential.getCredentialType();
        if (PlatformCredential.CERTIFICATE_TYPE_2_0.equals(credentialType)) {
            return validatePlatformCredentialAttributesV2p0(platformCredential, deviceInfoReport);
        }
        return validatePlatformCredentialAttributesV1p2(platformCredential, deviceInfoReport);
    }

    /**
     * Checks if the delta credential's attributes are valid.
     * @param deltaPlatformCredential the delta credential to verify
     * @param deviceInfoReport The device info report containing
     *                         serial number of the platform to be validated.
     * @param basePlatformCredential the base credential from the same identity request
     *                              as the delta credential.
     * @param deltaMapping delta certificates associated with the
     *                          delta supply validation.
     * @return the result of the validation.
     */
    public AppraisalStatus validateDeltaPlatformCredentialAttributes(
            final PlatformCredential deltaPlatformCredential,
            final DeviceInfoReport deviceInfoReport,
            final PlatformCredential basePlatformCredential,
            final Map<PlatformCredential, SupplyChainValidation> deltaMapping) {
        final String baseErrorMessage = "Can't validate delta platform"
                + "certificate attributes without ";
        String message;
        if (deltaPlatformCredential == null) {
            message = baseErrorMessage + "a delta platform certificate";
            LOGGER.error(message);
            return new AppraisalStatus(FAIL, message);
        }
        if (deviceInfoReport == null) {
            message = baseErrorMessage + "a device info report";
            LOGGER.error(message);
            return new AppraisalStatus(FAIL, message);
        }
        if (basePlatformCredential == null) {
            message = baseErrorMessage + "a base platform credential";
            LOGGER.error(message);
            return new AppraisalStatus(FAIL, message);
        }

        if (!basePlatformCredential.getPlatformSerial()
                .equals(deltaPlatformCredential.getPlatformSerial())) {
            message = String.format("Delta platform certificate "
                    + "platform serial number (%s) does not match "
                    + "the base certificate's platform serial number (%s)",
                    deltaPlatformCredential.getPlatformSerial(),
                    basePlatformCredential.getPlatformSerial());
            LOGGER.error(message);
            return new AppraisalStatus(FAIL, message);
        }

        // parse out the provided delta and its specific chain.
        List<ComponentIdentifier> origPcComponents
                = new LinkedList<>(basePlatformCredential.getComponentIdentifiers());

        return validateDeltaAttributesChainV2p0(deviceInfoReport,
                deltaMapping, origPcComponents);
    }

    private static AppraisalStatus validatePlatformCredentialAttributesV1p2(
            final PlatformCredential platformCredential,
            final DeviceInfoReport deviceInfoReport) {

        // check the device's board serial number, and compare against this
        // platform credential's board serial number.
        // Retrieve the various device serial numbers.
        String credentialBoardSerialNumber = platformCredential.getPlatformSerial();
        String credentialChassisSerialNumber = platformCredential.getChassisSerialNumber();

        HardwareInfo hardwareInfo = deviceInfoReport.getHardwareInfo();
        String deviceBaseboardSerialNumber = hardwareInfo.getBaseboardSerialNumber();
        String deviceChassisSerialNumber = hardwareInfo.getChassisSerialNumber();
        String deviceSystemSerialNumber = hardwareInfo.getSystemSerialNumber();

        // log serial numbers that weren't collected. Force "not specified" serial numbers
        // to be ignored in below case checks
        Map<String, String> deviceInfoSerialNumbers = new HashMap<>();

        if (StringUtils.isEmpty(deviceBaseboardSerialNumber)
                || DeviceInfoReport.NOT_SPECIFIED.equalsIgnoreCase(deviceBaseboardSerialNumber)) {
            LOGGER.error("Failed to retrieve device baseboard serial number");
            deviceBaseboardSerialNumber = null;
        } else {
            deviceInfoSerialNumbers.put("board serial number", deviceBaseboardSerialNumber);
            LOGGER.info("Using device board serial number for validation: "
                    + deviceBaseboardSerialNumber);
        }

        if (StringUtils.isEmpty(deviceChassisSerialNumber)
                || DeviceInfoReport.NOT_SPECIFIED.equalsIgnoreCase(deviceChassisSerialNumber)) {
            LOGGER.error("Failed to retrieve device chassis serial number");
        } else {
            deviceInfoSerialNumbers.put("chassis serial number", deviceChassisSerialNumber);
            LOGGER.info("Using device chassis serial number for validation: "
                    + deviceChassisSerialNumber);
        }
        if (StringUtils.isEmpty(deviceSystemSerialNumber)
                || DeviceInfoReport.NOT_SPECIFIED.equalsIgnoreCase(deviceSystemSerialNumber)) {
            LOGGER.error("Failed to retrieve device system serial number");
        } else {
            deviceInfoSerialNumbers.put("system serial number", deviceSystemSerialNumber);
            LOGGER.info("Using device system serial number for validation: "
                    + deviceSystemSerialNumber);
        }

        AppraisalStatus status;

        // Test 1: If the board serial number or chassis is set on the PC,
        // compare with each of the device serial numbers for any match
        if (StringUtils.isNotEmpty(credentialBoardSerialNumber)
                || StringUtils.isNotEmpty(credentialChassisSerialNumber)) {
            status = validatePlatformSerialsWithDeviceSerials(credentialBoardSerialNumber,
                    credentialChassisSerialNumber, deviceInfoSerialNumbers);
            // Test 2: If the board and chassis serial numbers are not set on the PC,
            // compare the SHA1 hash of the device baseboard serial number to
            // the certificate serial number
        } else {
            String message;
            LOGGER.debug("Credential Serial Number was null");
            if (StringUtils.isEmpty(deviceBaseboardSerialNumber)) {
                message = "Device Serial Number was null";
                LOGGER.error(message);
                status = new AppraisalStatus(FAIL, message);
            } else {
                // Calculate the SHA1 hash of the UTF8 encoded baseboard serial number
                BigInteger baseboardSha1 = new BigInteger(1,
                        DigestUtils.sha1(deviceBaseboardSerialNumber.getBytes(Charsets.UTF_8)));
                BigInteger certificateSerialNumber = platformCredential.getSerialNumber();

                // compare the SHA1 hash of the baseboard serial number to the certificate SN
                if (certificateSerialNumber != null
                        && certificateSerialNumber.equals(baseboardSha1)) {
                    LOGGER.info("Device Baseboard Serial Number matches "
                            + "the Certificate Serial Number");
                    status = new AppraisalStatus(PASS, PLATFORM_ATTRIBUTES_VALID);
                } else if (certificateSerialNumber != null
                        && certificateSerialNumber.equals(
                                baseboardSha1.clearBit(NUC_VARIABLE_BIT))) {
                    LOGGER.info("Warning! The Certificate serial number had the most significant "
                            + "bit truncated.  159 bits of it matched the device baseboard "
                            + "serial number.");
                    status = new AppraisalStatus(PASS, PLATFORM_ATTRIBUTES_VALID);
                } else {
                    message = "The SHA1 hash of the Device Baseboard Serial Number "
                            + deviceBaseboardSerialNumber
                            + " did not match the Certificate's Serial Number";
                    LOGGER.error(message);
                    status = new AppraisalStatus(FAIL, message);

                }
            }
        }

        return status;
    }

    /**
     * Validates device info report against the new platform credential.
     * @param platformCredential the Platform Credential
     * @param deviceInfoReport the Device Info Report
     * @return either PASS or FAIL
     */
    static AppraisalStatus validatePlatformCredentialAttributesV2p0(
            final PlatformCredential platformCredential,
            final DeviceInfoReport deviceInfoReport) {
        boolean passesValidation = true;
        StringBuilder resultMessage = new StringBuilder();

        boolean fieldValidation = false;

        // ********** BEGIN OF block checking the platform properties
        HardwareInfo hardwareInfo = deviceInfoReport.getHardwareInfo();

        fieldValidation = requiredPlatformCredentialFieldIsNonEmptyAndMatches(
                "PlatformManufacturerStr",
                platformCredential.getManufacturer(),
                hardwareInfo.getManufacturer());

        if (!fieldValidation) {
            resultMessage.append("Platform manufacturer did not match\n");
        }
        passesValidation &= fieldValidation;

        fieldValidation = requiredPlatformCredentialFieldIsNonEmptyAndMatches(
                "PlatformModel",
                platformCredential.getModel(),
                hardwareInfo.getProductName());

        if (!fieldValidation) {
            resultMessage.append("Platform model did not match\n");
        }
        passesValidation &= fieldValidation;

        fieldValidation = requiredPlatformCredentialFieldIsNonEmptyAndMatches(
                "PlatformVersion",
                platformCredential.getVersion(),
                hardwareInfo.getVersion());

        if (!fieldValidation) {
            resultMessage.append("Platform version did not match\n");
        }
        passesValidation &= fieldValidation;

        // check PlatformSerial against both system-serial-number and baseboard-serial-number
        fieldValidation = (
                (
                optionalPlatformCredentialFieldNullOrMatches(
                    "PlatformSerial",
                    platformCredential.getPlatformSerial(),
                    hardwareInfo.getSystemSerialNumber())
                ) || (
                optionalPlatformCredentialFieldNullOrMatches(
                        "PlatformSerial",
                        platformCredential.getPlatformSerial(),
                        hardwareInfo.getBaseboardSerialNumber())
                )
        );

        if (!fieldValidation) {
            resultMessage.append("Platform serial did not match\n");
        }
//        passesValidation &= fieldValidation;
        // ********** END OF part checking the PLATFORM PROPERTIES

        
        
        // ********** BEGIN OF block checking the PLATFORM COMPONENTS
        
        // Retrieve the list of all components from the Platform Credential
        List<ComponentIdentifier> allPcComponents
                = new ArrayList<>(platformCredential.getComponentIdentifiers());

        // All components listed in the Platform Credential must have a manufacturer and model
        for (ComponentIdentifier pcComponent : allPcComponents) {
            fieldValidation = !hasEmptyValueForRequiredField("componentManufacturer",
                    pcComponent.getComponentManufacturer());

            if (!fieldValidation) {
                resultMessage.append(" Component manufacturer is empty\n");
            }

            passesValidation &= fieldValidation;

            fieldValidation = !hasEmptyValueForRequiredField("componentModel",
                    pcComponent.getComponentModel());

            if (!fieldValidation) {
                resultMessage.append(" Component model is empty\n");
            }

            passesValidation &= fieldValidation;
        }

        // There is no need to do comparisons with components that are invalid because
        // they did not have a manufacturer or model.
        List<ComponentIdentifier> validPcComponents = allPcComponents.stream()
                .filter(identifier -> identifier.getComponentManufacturer() != null
                        && identifier.getComponentModel() != null)
                .collect(Collectors.toList());
        
        String paccorOutputString = deviceInfoReport.getPaccorOutputString();
        // System.out.println("** DEBUG paccorOutputString : " + paccorOutputString );
        
        String unmatchedComponents;
        try {
            List<ComponentInfo> componentInfoList
                    = getComponentInfoFromPaccorOutput(paccorOutputString);
            
//            for (Iterator<ComponentInfo> it = componentInfoList.iterator(); it.hasNext();) {
//				ComponentInfo ci = (ComponentInfo) it.next();
//	             System.out.println("** DEBUG comp. item: " + ci.toString());
//			}
            // System.out.println("** DEBUG componentInfoList : " + componentInfoList );
            
            unmatchedComponents = validateV2p0PlatformCredentialComponentsExpectingExactMatch(
                    validPcComponents, componentInfoList);
            
            fieldValidation &= unmatchedComponents.isEmpty();
            
        } catch (IOException e) {
            final String baseErrorMessage = "Error parsing JSON output from PACCOR: ";
            LOGGER.error(baseErrorMessage + e.toString());
            LOGGER.error("PACCOR output string:\n" + paccorOutputString);
            return new AppraisalStatus(ERROR, baseErrorMessage + e.getMessage());
        }
        
        // ********** END OF part checking the PLATFORM COMPONENTS

        
        // ********** BEGIN OF part checking the PROPERTIES
        StringBuilder propErrors = new StringBuilder();
        try {
            List<PlatformProperties> hwManifestProperties
                    = getPropertiesInfoFromPaccorOutput(paccorOutputString);
            
//            for (Iterator<PlatformProperties> it = hwManifestProperties.iterator(); it.hasNext();) {
//				PlatformProperties ci = (PlatformProperties) it.next();
//	              System.out.printf("** DEBUG Hw manifest properties. Name: %s, Value: %s\n",
//	              	ci.getPropertyName(), ci.getPropertyValue());
//			  }
            
			Map<String, Object> pcres = platformCredential.getAllAttributes();
			List<PlatformProperty> platCertProperties = null;
			for (Map.Entry<String, Object> e : pcres.entrySet()) {
			    // System.out.println("** DEBUG getAllAttributes: " + e.getKey() + "/" + e.getValue());
			    if (e.getKey().equalsIgnoreCase("platformConfiguration")) {
//			    	 System.out.println("** DEBUG platformConfiguration key found: " + e.getKey());				    	
			    	PlatformConfiguration temp =  (PlatformConfiguration) e.getValue();
			    	
			    	platCertProperties = temp.getPlatformProperties();
			    	// System.out.println("** DEBUG PlatformProperty list size: " + platprop.size());
			    	System.out.println("Number of properties found at the Platform Certificate: " + platCertProperties.size());
			    	
//			    	for (Iterator<PlatformProperty> iterator = platCertProperties.iterator(); iterator.hasNext();) {
//						PlatformProperty platformProperty = (PlatformProperty) iterator.next();
//						 System.out.println("** DEBUG platformProperty: "+ platformProperty.toString());
//					}
			    }
			}
			
	        for (PlatformProperties hwManifEntry : hwManifestProperties) {
		        List<PlatformProperty> checkHashes = platCertProperties.stream()
		                .filter(identifier -> identifier.getPropertyName() != null
		                        		   && identifier.getPropertyValue() != null)
		                .filter(identifier -> identifier.getPropertyName().equals(
		                					hwManifEntry.getPropertyName()))
		                .filter(identifier -> identifier.getPropertyValue().equals(
		                				    hwManifEntry.getPropertyValue()))            
		                .collect(Collectors.toList());
		        if (checkHashes.isEmpty()) {
		        	propErrors.append(String.format(
		        			"Mismatch entry found at Platform Certificate: %s. Value: %s.\n", 
		        			hwManifEntry.getPropertyName(), hwManifEntry.getPropertyValue()));
		        } else if (checkHashes.size() == 1){
		        	// System.out.printf("Property entry matched for : %s\n", hwManifEntry.getPropertyName());		        	
		        } else {
		        	propErrors.append(String.format(
		        			"Multiple entries found at the Platform Certificate: %s. Value: %s.\n", 
		        			hwManifEntry.getPropertyName(), hwManifEntry.getPropertyValue()));
		        }
	        }
	        for (PlatformProperty platCertEntry : platCertProperties) {
		        List<PlatformProperties> checkHashes = hwManifestProperties.stream()
		                .filter(identifier -> identifier.getPropertyName() != null
		                        		   && identifier.getPropertyValue() != null)
		                .filter(identifier -> identifier.getPropertyName().equals(
		                					platCertEntry.getPropertyName()))
		                .filter(identifier -> identifier.getPropertyValue().equals(
		                				    platCertEntry.getPropertyValue()))            
		                .collect(Collectors.toList());
		        if (checkHashes.isEmpty()) {
		        	propErrors.append(String.format(
		        			"Mismatch entry found at the Hardware Manifest: %s. Value: %s.\n", 
		        			platCertEntry.getPropertyName(), platCertEntry.getPropertyValue()));
		        } else if (checkHashes.size() == 1){
		        	// System.out.printf("Property entry match: %s\n", platCertEntry.getPropertyName());
		        } else {
		        	propErrors.append(String.format(
		        			"Multiple entries found at the Hardware Manifest: %s. Value: %s.\n", 
		        			platCertEntry.getPropertyName(), platCertEntry.getPropertyValue()));
		        }
	        }
	        // ********** END OF part checking the PROPERTIES
        
	        fieldValidation &= (propErrors.length() == 0);
        	passesValidation &= fieldValidation;

        } catch (Exception e) {
        	e.printStackTrace();
        }

        if (!fieldValidation) {
        	if (!unmatchedComponents.isEmpty()) {
//	            resultMessage.append("There are unmatched components:\n");
	            resultMessage.append(unmatchedComponents);
        	}
            if (propErrors.length() > 0) {
                resultMessage.append("There are unmatched properties:\n");
	        	resultMessage.append(propErrors);
	        } 
        }

        passesValidation &= fieldValidation;

        if (passesValidation) {
            return new AppraisalStatus(PASS, PLATFORM_ATTRIBUTES_VALID);
        } else {
            return new AppraisalStatus(FAIL, resultMessage.toString());
        }
    }

    /**
     * The main purpose of this method, the in process of validation, is to
     * pick out the changes that lead to the delta cert and make sure the changes
     * are valid.
     *
     * @param deviceInfoReport The paccor profile of device being validated against.
     * @param deltaMapping map of delta certificates to their validated status
     * @param origPcComponents The component identifier list associated with the
     * base cert for this specific chain
     * @return Appraisal Status of delta being validated.
     */
    static AppraisalStatus validateDeltaAttributesChainV2p0(
            final DeviceInfoReport deviceInfoReport,
            final Map<PlatformCredential, SupplyChainValidation> deltaMapping,
            final List<ComponentIdentifier> origPcComponents) {
        boolean fieldValidation = true;
        StringBuilder resultMessage = new StringBuilder();
        List<ComponentIdentifier> validOrigPcComponents = origPcComponents.stream()
                .filter(identifier -> identifier.getComponentManufacturer() != null
                        && identifier.getComponentModel() != null)
                .collect(Collectors.toList());
        List<PlatformCredential> chainCertificates = new LinkedList<>(deltaMapping.keySet());

        // map the components throughout the chain
        Map<String, ComponentIdentifier> chainCiMapping = new HashMap<>();
        List<ComponentIdentifier> deltaBuildList = new LinkedList<>(validOrigPcComponents);
        deltaBuildList.stream().forEach((ci) -> {
            chainCiMapping.put(ci.getComponentSerial().toString(), ci);
        });

        Collections.sort(chainCertificates, new Comparator<PlatformCredential>() {
            @Override
            public int compare(final PlatformCredential obj1,
                               final PlatformCredential obj2) {
                if (obj1 == null) {
                    return 0;
                }
                if (obj2 == null) {
                    return 0;
                }
                if (obj1.getBeginValidity() == null || obj2.getBeginValidity() == null) {
                    return 0;
                }
                return obj1.getBeginValidity().compareTo(obj2.getBeginValidity());
            }
        });

        String ciSerial;
        List<Certificate> certificateList = null;
        SupplyChainValidation scv = null;
        resultMessage.append("There are errors with Delta "
                    + "Component Statuses components:\n");
        // go through the leaf and check the changes against the valid components
        // forget modifying validOrigPcComponents
        for (PlatformCredential delta : chainCertificates) {
            StringBuilder failureMsg = new StringBuilder();
            certificateList = new ArrayList<>();
            certificateList.add(delta);

            for (ComponentIdentifier ci : delta.getComponentIdentifiers()) {
                if (ci.isVersion2()) {
                    ciSerial = ci.getComponentSerial().toString();
                    ComponentIdentifierV2 ciV2 = (ComponentIdentifierV2) ci;
                    if (ciV2.isModified())  {
                        // this won't match
                        // check it is there
                        if (!chainCiMapping.containsKey(ciSerial)) {
                            fieldValidation = false;
                            failureMsg.append(String.format(
                                    "%s attempted MODIFIED with no prior instance.%n",
                                    ciSerial));
                            scv = deltaMapping.get(delta);
                            if (scv.getResult() != AppraisalStatus.Status.PASS) {
                                failureMsg.append(scv.getMessage());
                            }
                            deltaMapping.put(delta, new SupplyChainValidation(
                                    SupplyChainValidation.ValidationType.PLATFORM_CREDENTIAL,
                                    AppraisalStatus.Status.FAIL,
                                    certificateList,
                                    failureMsg.toString()));
                        } else {
                            chainCiMapping.put(ciSerial, ci);
                        }
                    } else if (ciV2.isRemoved()) {
                        if (!chainCiMapping.containsKey(ciSerial)) {
                            // error thrown, can't remove if it doesn't exist
                            fieldValidation = false;
                            failureMsg.append(String.format(
                                    "%s attempted REMOVED with no prior instance.%n",
                                    ciSerial));
                            scv = deltaMapping.get(delta);
                            if (scv.getResult() != AppraisalStatus.Status.PASS) {
                                failureMsg.append(scv.getMessage());
                            }
                            deltaMapping.put(delta, new SupplyChainValidation(
                                    SupplyChainValidation.ValidationType.PLATFORM_CREDENTIAL,
                                    AppraisalStatus.Status.FAIL,
                                    certificateList,
                                    failureMsg.toString()));
                        } else {
                            chainCiMapping.remove(ciSerial);
                        }
                    } else if (ciV2.isAdded()) {
                        // ADDED
                        if (chainCiMapping.containsKey(ciSerial)) {
                            // error, shouldn't exist
                            fieldValidation = false;
                            failureMsg.append(String.format(
                                    "%s was ADDED, the serial already exists.%n",
                                    ciSerial));
                            scv = deltaMapping.get(delta);
                            if (scv.getResult() != AppraisalStatus.Status.PASS) {
                                failureMsg.append(scv.getMessage());
                            }
                            deltaMapping.put(delta, new SupplyChainValidation(
                                    SupplyChainValidation.ValidationType.PLATFORM_CREDENTIAL,
                                    AppraisalStatus.Status.FAIL,
                                    certificateList,
                                    failureMsg.toString()));
                        } else {
                            // have to add in case later it is removed
                            chainCiMapping.put(ciSerial, ci);
                        }
                    }
                }
            }

            resultMessage.append(failureMsg.toString());
        }

        if (!fieldValidation) {
            return new AppraisalStatus(FAIL, resultMessage.toString());
        }

        String paccorOutputString = deviceInfoReport.getPaccorOutputString();
        String unmatchedComponents;
        try {
            List<ComponentInfo> componentInfoList
                    = getComponentInfoFromPaccorOutput(paccorOutputString);
            unmatchedComponents = validateV2p0PlatformCredentialComponentsExpectingExactMatch(
                    new LinkedList<>(chainCiMapping.values()), componentInfoList);
            fieldValidation &= unmatchedComponents.isEmpty();
        } catch (IOException e) {
            final String baseErrorMessage = "Error parsing JSON output from PACCOR: ";
            LOGGER.error(baseErrorMessage + e.toString());
            LOGGER.error("PACCOR output string:\n" + paccorOutputString);
            return new AppraisalStatus(ERROR, baseErrorMessage + e.getMessage());
        }

        if (!fieldValidation) {
            resultMessage = new StringBuilder();
            resultMessage.append("There are unmatched components:\n");
            resultMessage.append(unmatchedComponents);

            return new AppraisalStatus(FAIL, resultMessage.toString());
        }

        return new AppraisalStatus(PASS, PLATFORM_ATTRIBUTES_VALID);
    }

    /**
     * Compares the component information from the device info report against those of the
     * platform credential. All components in the platform credential should exactly match one
     * component in the device info report.  The device info report is allowed to have extra
     * components not represented in the platform credential.
     *
     * @param untrimmedPcComponents the platform credential components (may contain end whitespace)
     * @param allDeviceInfoComponents the device info report components
     * @return true if validation passes
     */
    private static String validateV2p0PlatformCredentialComponentsExpectingExactMatch(
            final List<ComponentIdentifier> untrimmedPcComponents,
            final List<ComponentInfo> allDeviceInfoComponents) {
        // For each manufacturer listed in the platform credential, create two lists:
        // 1. a list of components listed in the platform credential for the manufacturer, and
        // 2. a list of components listed in the device info for the same manufacturer
        // Then eliminate matches from both lists. Finally, decide if the validation passes based
        // on the leftovers in the lists and the policy in place.
        final List<ComponentIdentifier> pcComponents = new ArrayList<>();
        for (ComponentIdentifier component : untrimmedPcComponents) {
            DERUTF8String componentSerial = new DERUTF8String("");
            DERUTF8String componentRevision = new DERUTF8String("");
            if (component.getComponentSerial() != null) {
                componentSerial = new DERUTF8String(
                        component.getComponentSerial().getString().trim());
            }
            if (component.getComponentRevision() != null) {
                componentRevision = new DERUTF8String(
                        component.getComponentRevision().getString().trim());
            }
            pcComponents.add(
                new ComponentIdentifier(
                        new DERUTF8String(component.getComponentManufacturer().getString().trim()),
                        new DERUTF8String(component.getComponentModel().getString().trim()),
                        componentSerial, componentRevision,
                        component.getComponentManufacturerId(),
                        component.getFieldReplaceable(),
                        component.getComponentAddress()
                ));
        }
        
        // Debug test routine do add more random items from the platform certificate 
        int[] compi = {10, 20, 40};
//		for (int comp : compi) {
//        pcComponents.add(new ComponentIdentifier(
//                        new DERUTF8String(untrimmedPcComponents.get(comp).getComponentManufacturer().getString().trim()),
//                        new DERUTF8String(untrimmedPcComponents.get(comp).getComponentModel().getString().trim()),
//                        new DERUTF8String(untrimmedPcComponents.get(comp).getComponentSerial().getString().trim()), 
//                        new DERUTF8String(untrimmedPcComponents.get(comp).getComponentRevision().getString().trim()),
//                        untrimmedPcComponents.get(comp).getComponentManufacturerId(),
//                        untrimmedPcComponents.get(comp).getFieldReplaceable(),
//                        untrimmedPcComponents.get(comp).getComponentAddress()
//                ));
//        }

        LOGGER.info("Validating the following Platform Cert components...");
        pcComponents.forEach(component -> LOGGER.info(component.toString()));
        LOGGER.info("...against the the following DeviceInfoReport components:");
        allDeviceInfoComponents.forEach(component -> LOGGER.info(component.toString()));

        // Debug prints
//        System.out.println("Validating the following Platform Cert components...");
//        pcComponents.forEach(component -> System.out.println(component.toString()));
//        System.out.println("...against the the following Hardware Manifest components:");
//        allDeviceInfoComponents.forEach(component -> System.out.println(component.toString()));
        
        HashSet<DERUTF8String> manufacturerSet = new HashSet<>();
        pcComponents.forEach(component -> manufacturerSet.add(
                component.getComponentManufacturer()));

        Set<String> hwManifestManufacturerSet = new HashSet<>();
        allDeviceInfoComponents.forEach(component -> hwManifestManufacturerSet.add(
                component.getComponentManufacturer()));

        // Create a list for unmatched components across all manufacturers to display at the end.
        List<ComponentIdentifier> pcUnmatchedComponents = new ArrayList<>();
        List<ComponentInfo> hwManifestUnmatchedComponents = new ArrayList<>();

        
        
        
        // Checking if the Platform Certificate items are found in the Hardware Manifest
        // This is a one way check. Below we have the other way around.
        for (DERUTF8String derUtf8Manufacturer : manufacturerSet) {
            List<ComponentIdentifier> pcComponentsFromManufacturer
                    = pcComponents.stream().filter(compIdentifier
                    -> compIdentifier.getComponentManufacturer().equals(derUtf8Manufacturer))
                    .collect(Collectors.toList());

            String pcManufacturer = derUtf8Manufacturer.getString();
            List<ComponentInfo> deviceInfoComponentsFromManufacturer
                    = allDeviceInfoComponents.stream().filter(componentInfo
                    -> componentInfo.getComponentManufacturer().equals(pcManufacturer))
                    .collect(Collectors.toList());

            // For each component listed in the platform credential from this manufacturer
            // find the ones that specify a serial number so we can match the most specific ones
            // first.
            List<ComponentIdentifier> pcComponentsFromManufacturerWithSerialNumber
                    = pcComponentsFromManufacturer.stream().filter(compIdentifier
                    -> compIdentifier.getComponentSerial() != null
                    && StringUtils.isNotEmpty(compIdentifier.getComponentSerial().getString()))
                    .collect(Collectors.toList());

            // Now match up the components from the device info that are from the same
            // manufacturer and have a serial number. As matches are found, remove them from
            // both lists.
            for (ComponentIdentifier pcComponent
                    : pcComponentsFromManufacturerWithSerialNumber) {
                Optional<ComponentInfo> first
                        = deviceInfoComponentsFromManufacturer.stream()
                        .filter(componentInfo
                                -> StringUtils.isNotEmpty(componentInfo.getComponentSerial()))
                        .filter(componentInfo -> componentInfo.getComponentSerial()
                                .equals(pcComponent.getComponentSerial().getString()))
                        .findFirst();

                if (first.isPresent()) {
                    ComponentInfo potentialMatch = first.get();
                    if (isMatch(pcComponent, potentialMatch)) {
                        pcComponentsFromManufacturer.remove(pcComponent);
                        deviceInfoComponentsFromManufacturer.remove(potentialMatch);
                    }
                }
            }

            // For each component listed in the platform credential from this manufacturer
            // find the ones that specify value for the revision field so we can match the most
            // specific ones first.
            List<ComponentIdentifier> pcComponentsFromManufacturerWithRevision
                    = pcComponentsFromManufacturer.stream().filter(compIdentifier
                    -> compIdentifier.getComponentRevision() != null
                    && StringUtils.isNotEmpty(compIdentifier.getComponentRevision().getString()))
                    .collect(Collectors.toList());

            // Now match up the components from the device info that are from the same
            // manufacturer and specify a value for the revision field. As matches are found,
            // remove them from both lists.
            for (ComponentIdentifier pcComponent
                    : pcComponentsFromManufacturerWithRevision) {
                Optional<ComponentInfo> first
                        = deviceInfoComponentsFromManufacturer.stream()
                        .filter(info -> StringUtils.isNotEmpty(info.getComponentRevision()))
                        .filter(info -> info.getComponentRevision()
                                .equals(pcComponent.getComponentRevision().getString()))
                        .findFirst();

                if (first.isPresent()) {
                    ComponentInfo potentialMatch = first.get();
                    if (isMatch(pcComponent, potentialMatch)) {
                        pcComponentsFromManufacturer.remove(pcComponent);
                        deviceInfoComponentsFromManufacturer.remove(potentialMatch);
                    }
                }
            }
            
            List<ComponentIdentifier> pcComponentsWithManufacturer
		        = pcComponentsFromManufacturer.stream().filter(compIdentifier
		        -> compIdentifier.getComponentModel() != null
		        && StringUtils.isNotEmpty(compIdentifier.getComponentModel().getString()))
		        .collect(Collectors.toList());
            
            for (ComponentIdentifier pcComponent
                    : pcComponentsWithManufacturer) {
                Optional<ComponentInfo> first
                        = deviceInfoComponentsFromManufacturer.stream()
                        .filter(info -> StringUtils.isNotEmpty(info.getComponentModel()))
                        .filter(info -> info.getComponentModel()
                                .equals(pcComponent.getComponentModel().getString()))
                        .findFirst();

                if (first.isPresent()) {
                    ComponentInfo potentialMatch = first.get();
                    if (isMatch(pcComponent, potentialMatch)) {
                        pcComponentsFromManufacturer.remove(pcComponent);
                        deviceInfoComponentsFromManufacturer.remove(potentialMatch);
                    }
                }
            }
            
            pcUnmatchedComponents.addAll(pcComponentsFromManufacturer);
        }

        
        
        
        // Checking if the Hardware Manifest items are found in the Platform Certificate
        // This is a one way check. Above we have the other way around.
        for (String manufacturer : hwManifestManufacturerSet) {

        	// Items from the Platform Certificate
            List<ComponentIdentifier> pcComponentsFromManufacturer
		            = pcComponents.stream().filter(compIdentifier
		            -> compIdentifier.getComponentManufacturer().getString().equals(manufacturer))
		            .collect(Collectors.toList());
	
            // Items from the Hardware Manifest
		    String pcManufacturer = manufacturer;
		    List<ComponentInfo> deviceInfoComponentsFromManufacturer
		            = allDeviceInfoComponents.stream().filter(componentInfo
		            -> componentInfo.getComponentManufacturer().equals(pcManufacturer))
		            .collect(Collectors.toList());

            // For each component listed in the platform credential from this manufacturer
            // find the ones that specify a serial number so we can match the most specific ones
            // first.
            List<ComponentInfo> hwManifComponentsWithSerialNumber
                    = deviceInfoComponentsFromManufacturer.stream().filter(compIdentifier
                    -> compIdentifier.getComponentSerial() != null
                    && StringUtils.isNotEmpty(compIdentifier.getComponentSerial()))
                    .collect(Collectors.toList());

            // Now match up the components from the device info that are from the same
            // manufacturer and have a serial number. As matches are found, remove them from
            // both lists.
            for (ComponentInfo hwManifestComponent
                    : hwManifComponentsWithSerialNumber) {
                Optional<ComponentIdentifier> first
                        = pcComponentsFromManufacturer.stream()
                        .filter(ComponentIdentifier
                                -> StringUtils.isNotEmpty(ComponentIdentifier.getComponentSerial().getString()))
                        .filter(ComponentIdentifier -> ComponentIdentifier.getComponentSerial().getString()
                                .equals(hwManifestComponent.getComponentSerial()))
                        .findFirst();

                if (first.isPresent()) {
                    ComponentIdentifier potentialMatch = first.get();
                    if (isMatchToHwManifest(potentialMatch, hwManifestComponent)) {
                    	deviceInfoComponentsFromManufacturer.remove(hwManifestComponent);
                    	pcComponentsFromManufacturer.remove(potentialMatch);
                    }
                }
            }

            // For each component listed in the platform credential from this manufacturer
            // find the ones that specify value for the revision field so we can match the most
            // specific ones first.
            List<ComponentInfo> hwManifComponentsWithRevision
                    = deviceInfoComponentsFromManufacturer.stream().filter(compIdentifier
                    -> compIdentifier.getComponentRevision() != null
                    && StringUtils.isNotEmpty(compIdentifier.getComponentRevision()))
                    .collect(Collectors.toList());

            // Now match up the components from the device info that are from the same
            // manufacturer and specify a value for the revision field. As matches are found,
            // remove them from both lists.
            for (ComponentInfo hwManifestComponent
                    : hwManifComponentsWithRevision) {
                Optional<ComponentIdentifier> first
                        = pcComponentsFromManufacturer.stream()
                        .filter(info -> StringUtils.isNotEmpty(info.getComponentRevision().getString()))
                        .filter(info -> info.getComponentRevision().getString()
                                .equals(hwManifestComponent.getComponentRevision()))
                        .findFirst();

                if (first.isPresent()) {
                	ComponentIdentifier potentialMatch = first.get();
                    if (isMatchToHwManifest(potentialMatch, hwManifestComponent)) {
		            	deviceInfoComponentsFromManufacturer.remove(hwManifestComponent);
		            	pcComponentsFromManufacturer.remove(potentialMatch);
                    }
                }
            }
            
            List<ComponentInfo> hwManifComponentsWithManufacturer
            = deviceInfoComponentsFromManufacturer.stream().filter(compIdentifier
            -> compIdentifier.getComponentModel() != null
            && StringUtils.isNotEmpty(compIdentifier.getComponentModel()))
            .collect(Collectors.toList());

		    // Now match up the components from the device info that are from the same
		    // manufacturer and specify a value for the revision field. As matches are found,
		    // remove them from both lists.
		    for (ComponentInfo hwManifestComponent
		            : hwManifComponentsWithManufacturer) {
		        Optional<ComponentIdentifier> first
		                = pcComponentsFromManufacturer.stream()
		                .filter(info -> StringUtils.isNotEmpty(info.getComponentModel().getString()))
		                .filter(info -> info.getComponentModel().getString()
		                        .equals(hwManifestComponent.getComponentModel()))
		                .findFirst();
		
		        if (first.isPresent()) {
		        	ComponentIdentifier potentialMatch = first.get();
		            if (isMatchToHwManifest(potentialMatch, hwManifestComponent)) {
		            	deviceInfoComponentsFromManufacturer.remove(hwManifestComponent);
		            	pcComponentsFromManufacturer.remove(potentialMatch);
		            }
		        }
		    } 
		    
            hwManifestUnmatchedComponents.addAll(deviceInfoComponentsFromManufacturer);
        }

        // TODO: print the Serial and revision fields only if they are not empty.
        StringBuilder sbPCUnmatched = new StringBuilder();
        StringBuilder sbHMUnmatch = new StringBuilder();
        
        if (!pcUnmatchedComponents.isEmpty()) {
            LOGGER.error(String.format("Platform Certificate contains %d unmatched components:",
                    pcUnmatchedComponents.size()));
            
            int umatchedComponentCounter = 0;
            for (ComponentIdentifier unmatchedComponent : pcUnmatchedComponents) {
                LOGGER.error("Unmatched components at the Platform Certificate " + umatchedComponentCounter++ + ": "
                        + unmatchedComponent);
                System.out.println("Unmatched components at the Platform Certificate " + umatchedComponentCounter++ + ": "
                        + unmatchedComponent);

                if (unmatchedComponent.getComponentSerial().getString().isEmpty() && 
                		unmatchedComponent.getComponentRevision().getString().isEmpty()) {
                    sbPCUnmatched.append(String.format("Manufacturer=%s, Model=%s%n",
                            unmatchedComponent.getComponentManufacturer(),
                            unmatchedComponent.getComponentModel()));
                	
                } else if (unmatchedComponent.getComponentSerial().getString().isEmpty()) {
                    sbPCUnmatched.append(String.format("Manufacturer=%s, Model=%s, Revision=%s%n",
                            unmatchedComponent.getComponentManufacturer(),
                            unmatchedComponent.getComponentModel(),
                            unmatchedComponent.getComponentRevision()));
                	
                } else if (unmatchedComponent.getComponentRevision().getString().isEmpty()) {
                    sbPCUnmatched.append(String.format("Manufacturer=%s, Model=%s, Serial=%s%n",
                            unmatchedComponent.getComponentManufacturer(),
                            unmatchedComponent.getComponentModel(),
                            unmatchedComponent.getComponentSerial()));
                	
                } else {
                    sbPCUnmatched.append(String.format("Manufacturer=%s, Model=%s, Serial=%s, Revision=%s%n",
                            unmatchedComponent.getComponentManufacturer(),
                            unmatchedComponent.getComponentModel(),
                            unmatchedComponent.getComponentSerial(),
                            unmatchedComponent.getComponentRevision()));
                }
            }
            // return sbPCUnmatched.toString();
        }
        
        if (!hwManifestUnmatchedComponents.isEmpty()) {
            LOGGER.error(String.format("The Hardware Manifest contains %d unmatched components:",
            		hwManifestUnmatchedComponents.size()));
            
            int umatchedComponentCounter = 0;
            for (ComponentInfo unmatchedComponent : hwManifestUnmatchedComponents) {
                LOGGER.error("Unmatched components at the Hardware Manifest " + umatchedComponentCounter++ + ": "
                        + unmatchedComponent);
                System.out.println("Unmatched components at the Hardware Manifest " + umatchedComponentCounter++ + ": "
                        + unmatchedComponent);
                
                if (unmatchedComponent.getComponentSerial().isEmpty() && 
                		unmatchedComponent.getComponentRevision().isEmpty()) {
                	sbHMUnmatch.append(String.format("Manufacturer=%s, Model=%s%n",
                            unmatchedComponent.getComponentManufacturer(),
                            unmatchedComponent.getComponentModel()));
                	
                } else if (unmatchedComponent.getComponentSerial().isEmpty()) {
                	sbHMUnmatch.append(String.format("Manufacturer=%s, Model=%s, Revision=%s%n",
                            unmatchedComponent.getComponentManufacturer(),
                            unmatchedComponent.getComponentModel(),
                            unmatchedComponent.getComponentRevision()));
                	
                } else if (unmatchedComponent.getComponentRevision().isEmpty()) {
                	sbHMUnmatch.append(String.format("Manufacturer=%s, Model=%s, Serial=%s%n",
                            unmatchedComponent.getComponentManufacturer(),
                            unmatchedComponent.getComponentModel(),
                            unmatchedComponent.getComponentSerial()));
                	
                } else {
                	sbHMUnmatch.append(String.format("Manufacturer=%s, Model=%s, Serial=%s, Revision=%s%n",
                            unmatchedComponent.getComponentManufacturer(),
                            unmatchedComponent.getComponentModel(),
                            unmatchedComponent.getComponentSerial(),
                            unmatchedComponent.getComponentRevision()));
                }
            }
            // return sbHMUnmatch.toString();
        }
        
        String missingPlatCertItems =  "Warning: The following component(s) of the Platform Certificate are currently absent from the platform: ";
        String missingHwManifestItems = "Warning: The following component(s) from the platform are not listed in the Platform Certificate: ";

        if (!pcUnmatchedComponents.isEmpty() && !hwManifestUnmatchedComponents.isEmpty()) {
        	return (missingPlatCertItems + "\n" + sbPCUnmatched.toString() + 
                    missingHwManifestItems + "\n" + sbHMUnmatch.toString());
        	
        } else if (!pcUnmatchedComponents.isEmpty()) {
        	return (missingPlatCertItems + "\n" + sbPCUnmatched.toString());
        	
        } else if (!hwManifestUnmatchedComponents.isEmpty()) {
        	return (missingHwManifestItems + "\n" + sbHMUnmatch.toString());
        }
        	
        return Strings.EMPTY;
    }

    /**
     * Returns true if fieldValue is null or empty.
     * @param description description of the value
     * @param fieldValue value of the field
     * @return true if fieldValue is null or empty; false otherwise
     */
    private static boolean hasEmptyValueForRequiredField(final String description,
                                                  final String fieldValue) {
        if (StringUtils.isEmpty(fieldValue)) {
            LOGGER.error("Required field was empty or null in Platform Credential: "
                    + description);
            return true;
        }
        return false;
    }

    /**
     * Returns true if fieldValue is null or empty.
     * @param description description of the value
     * @param fieldValue value of the field
     * @return true if fieldValue is null or empty; false otherwise
     */
    private static boolean hasEmptyValueForRequiredField(final String description,
                                                  final DERUTF8String fieldValue) {
        if (fieldValue == null || StringUtils.isEmpty(fieldValue.getString().trim())) {
            LOGGER.error("Required field was empty or null in Platform Credential: "
                    + description);
            return true;
        }
        return false;
    }

    /**
     * Validates the information supplied for the Platform Credential.  This
     * method checks if the field is required and therefore if the value is
     * present then verifies that the values match.
     * @param platformCredentialFieldName name of field to be compared
     * @param platformCredentialFieldValue first value to compare
     * @param otherValue second value to compare
     * @return true if values match
     */
    private static boolean requiredPlatformCredentialFieldIsNonEmptyAndMatches(
            final String platformCredentialFieldName,
            final String platformCredentialFieldValue,
            final String otherValue) {
    	
    	  // Since April 30th 2020 the SMBIOS is returning an empty field for 
    	  // the version, and Intel TSC tool is ignoring the fact it is empty
    	  // So, modifying here to ignore it as well.
    	  //        if (hasEmptyValueForRequiredField(platformCredentialFieldName,
    	  //                platformCredentialFieldValue)) {
    	  //            return false;
    	  //        }

        return platformCredentialFieldMatches(platformCredentialFieldName,
                platformCredentialFieldValue, otherValue);
    }

    /**
     * Validates the information supplied for the Platform Credential.  This
     * method checks if the value is present then verifies that the values match.
     * If not present, then returns true.
     * @param platformCredentialFieldName name of field to be compared
     * @param platformCredentialFieldValue first value to compare
     * @param otherValue second value to compare
     * @return true if values match or null
     */
    private static boolean optionalPlatformCredentialFieldNullOrMatches(
            final String platformCredentialFieldName,
            final String platformCredentialFieldValue,
            final String otherValue) {
        if (platformCredentialFieldValue == null) {
            return true;
        }

        return platformCredentialFieldMatches(platformCredentialFieldName,
                platformCredentialFieldValue, otherValue);
    }

    private static boolean platformCredentialFieldMatches(
            final String platformCredentialFieldName,
            final String platformCredentialFieldValue,
            final String otherValue) {
        String trimmedFieldValue = platformCredentialFieldValue.trim();
        String trimmedOtherValue = otherValue.trim();

        // Ignoring the version field when not specified or blank,
        // this value is like that in the SMBIOS. 
        // System.out.println("Plat cert value: " + trimmedFieldValue);
        // System.out.println("Hw Manif  value: " + trimmedOtherValue);
        if (trimmedOtherValue.equals("Not Specified")) {
        	trimmedOtherValue = "";
        }
        
        if (!trimmedFieldValue.equals(trimmedOtherValue)) {
            String msg =(String.format("%s field in Platform Credential (%s) does not match "
                            + "a related field in the DeviceInfoReport (%s)",
                    platformCredentialFieldName, trimmedFieldValue, trimmedOtherValue));
            LOGGER.debug(msg);
            System.out.println(msg);            
            return false;
        }

        String msg = (String.format("%s field in Platform Credential matches "
                + "a related field in the DeviceInfoReport (%s)",
                platformCredentialFieldName, trimmedFieldValue));
        LOGGER.debug(msg);
        System.out.println(msg);

        return true;
    }

    /**
     * Checks if the fields in the potentialMatch match the fields in the pcComponent,
     * or if the relevant field in the pcComponent is empty.
     * @param pcComponent the platform credential component
     * @param potentialMatch the component info from a device info report
     * @return true if the fields match exactly (null is considered the same as an empty string)
     */
    static boolean isMatch(final ComponentIdentifier pcComponent,
                           final ComponentInfo potentialMatch) {
        boolean matchesSoFar = true;

        matchesSoFar &= isMatchOrEmptyInPlatformCert(
                potentialMatch.getComponentManufacturer(),
                pcComponent.getComponentManufacturer()
        );

        matchesSoFar &= isMatchOrEmptyInPlatformCert(
                potentialMatch.getComponentModel(),
                pcComponent.getComponentModel()
        );

        matchesSoFar &= isMatchOrEmptyInPlatformCert(
                potentialMatch.getComponentSerial(),
                pcComponent.getComponentSerial()
        );

        matchesSoFar &= isMatchOrEmptyInPlatformCert(
                potentialMatch.getComponentRevision(),
                pcComponent.getComponentRevision()
        );

        return matchesSoFar;
    }

    
    private static boolean isMatchOrEmptyInPlatformCert(
            final String evidenceFromDevice,
            final DERUTF8String valueInPlatformCert) {
        if (valueInPlatformCert == null || StringUtils.isEmpty(valueInPlatformCert.getString())) {
            return true;
        }
        return valueInPlatformCert.getString().equals(evidenceFromDevice);
    }

    /**
     * Checks if the fields in the potentialMatch match the fields in the pcComponent,
     * or if the relevant field in the pcComponent is empty.
     * @param pcComponent the platform credential component
     * @param potentialMatch the component info from a device info report
     * @return true if the fields match exactly (null is considered the same as an empty string)
     */
    static boolean isMatchToHwManifest(final ComponentIdentifier pcComponent,
                           final ComponentInfo potentialMatch) {
        boolean matchesSoFar = true;

        matchesSoFar &= isMatchOrEmptyInHardwareManifest(
                potentialMatch.getComponentManufacturer(),
                pcComponent.getComponentManufacturer()
        );

        matchesSoFar &= isMatchOrEmptyInHardwareManifest(
                potentialMatch.getComponentModel(),
                pcComponent.getComponentModel()
        );

        matchesSoFar &= isMatchOrEmptyInHardwareManifest(
                potentialMatch.getComponentSerial(),
                pcComponent.getComponentSerial()
        );

        matchesSoFar &= isMatchOrEmptyInHardwareManifest(
                potentialMatch.getComponentRevision(),
                pcComponent.getComponentRevision()
        );

        return matchesSoFar;
    }

    private static boolean isMatchOrEmptyInHardwareManifest(
            final String evidenceFromDevice,
            final DERUTF8String valueInPlatformCert) {
        if (evidenceFromDevice == null || StringUtils.isEmpty(evidenceFromDevice)) {
            return true;
        }
        return valueInPlatformCert.getString().equals(evidenceFromDevice);
    }

    /**
     * Validates the platform credential's serial numbers with the device info's set of
     * serial numbers.
     * @param credentialBoardSerialNumber the PC board S/N
     * @param credentialChassisSerialNumber the PC chassis S/N
     * @param deviceInfoSerialNumbers the map of device info serial numbers with descriptions.
     * @return the changed validation status
     */
    private static AppraisalStatus validatePlatformSerialsWithDeviceSerials(
            final String credentialBoardSerialNumber, final String credentialChassisSerialNumber,
            final Map<String, String> deviceInfoSerialNumbers) {
        boolean boardSerialNumberFound = false;
        boolean chassisSerialNumberFound = false;

        if (StringUtils.isNotEmpty(credentialBoardSerialNumber)) {
            boardSerialNumberFound = deviceInfoContainsPlatformSerialNumber(
                credentialBoardSerialNumber, "board serial number", deviceInfoSerialNumbers);
        }
        if (StringUtils.isNotEmpty(credentialChassisSerialNumber)) {
            chassisSerialNumberFound = deviceInfoContainsPlatformSerialNumber(
                credentialChassisSerialNumber,
                    "chassis serial number", deviceInfoSerialNumbers);
        }

        if (boardSerialNumberFound || chassisSerialNumberFound) {
            LOGGER.info("The platform credential's board or chassis serial number matched"
                    + " with a serial number from the client's device information");
            return new AppraisalStatus(PASS, PLATFORM_ATTRIBUTES_VALID);
        }
        LOGGER.error("The platform credential's board and chassis serial numbers did"
                + " not match with any device info's serial numbers");

        return new AppraisalStatus(FAIL, "Platform serial did not match device info");
    }


    /**
     * Checks if a platform credential's serial number matches ANY of the device information's
     * set of serial numbers.
     * @param platformSerialNumber the platform serial number to compare
     * @param platformSerialNumberDescription description of the serial number for logging purposes.
     * @param deviceInfoSerialNumbers the map of device info serial numbers
     *                                (key = description, value = serial number)
     * @return true if the platform serial number was found (case insensitive search),
     *          false otherwise
     */
    private static boolean deviceInfoContainsPlatformSerialNumber(
            final String platformSerialNumber, final String platformSerialNumberDescription,
            final Map<String, String> deviceInfoSerialNumbers) {
        // check to see if the platform serial number is contained in the map of device info's
        // serial numbers
        for (Map.Entry<String, String> entry : deviceInfoSerialNumbers.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(platformSerialNumber)) {
                LOGGER.info("Device info contained platform {} {}"
                        + " in the device info's {}", platformSerialNumberDescription,
                        platformSerialNumber, entry.getKey());
                return true;
            }
        }

        LOGGER.warn("Platform {}, {}, did not match any device info serial numbers",
                platformSerialNumberDescription, platformSerialNumber);
        return false;
    }

    /**
     * Checks if the endorsement credential is valid.
     *
     * @param ec the endorsement credential to verify.
     * @param trustStore trust store holding trusted trusted certificates.
     * @param acceptExpired whether or not to accept expired and not yet valid certificates
     *                      as valid.
     * @return the result of the validation.
     */
    @Override
    public AppraisalStatus validateEndorsementCredential(final EndorsementCredential ec,
                                                       final KeyStore trustStore,
                                                       final boolean acceptExpired) {
        final String baseErrorMessage = "Can't validate endorsement credential attributes without ";
        String message;
        if (ec == null) {
            message = baseErrorMessage + "an endorsement credential";
            LOGGER.error(message);
            return new AppraisalStatus(FAIL, message);
        }
        if (trustStore == null) {
            message = baseErrorMessage + "a trust store";
            LOGGER.error(message);
            return new AppraisalStatus(FAIL, message);
        }

        try {
            X509Certificate verifiableCert = ec.getX509Certificate();

            // check validity period, currently acceptExpired will also accept not yet
            // valid certificates
            if (!acceptExpired) {
                verifiableCert.checkValidity();
            }

            if (verifyCertificate(verifiableCert, trustStore)) {
                return new AppraisalStatus(PASS, ENDORSEMENT_VALID);
            } else {
                return new AppraisalStatus(FAIL, "Endorsement credential does not have a valid "
                        + "signature chain in the trust store");
            }
        } catch (IOException e) {
            message = "Couldn't retrieve X509 certificate from endorsement credential";
            LOGGER.error(message, e);
            return new AppraisalStatus(ERROR, message + " " + e.getMessage());
        } catch (SupplyChainValidatorException e) {
            message = "An error occurred indicating the credential is not valid";
            LOGGER.warn(message, e);
            return new AppraisalStatus(ERROR, message + " " + e.getMessage());
        } catch (CertificateExpiredException e) {
            message = "The endorsement credential is expired";
            LOGGER.warn(message, e);
            return new AppraisalStatus(FAIL, message + " " + e.getMessage());
        } catch (CertificateNotYetValidException e) {
            message = "The endorsement credential is not yet valid";
            LOGGER.warn(message, e);
            return new AppraisalStatus(FAIL, message + " " + e.getMessage());
        }
    }

    /**
     * Attempts to check if the certificate is validated by certificates in a cert chain. The cert
     * chain is expected to be stored in a non-ordered KeyStore (trust store). If the signing
     * certificate for the target cert is found, but it is an intermediate cert, the validation will
     * continue to try to find the signing cert of the intermediate cert. It will continue searching
     * until it follows the chain up to a root (self-signed) cert.
     *
     * @param cert
     *            certificate to validate
     * @param trustStore
     *            trust store holding trusted root certificates and intermediate certificates
     * @return the certificate chain if validation is successful
     * @throws SupplyChainValidatorException
     *             if the verification is not successful
     */
    public static boolean verifyCertificate(final X509AttributeCertificateHolder cert,
            final KeyStore trustStore) throws SupplyChainValidatorException {
        if (cert == null || trustStore == null) {
            throw new SupplyChainValidatorException("Certificate or trust store is null");
        }

        try {
            Set<X509Certificate> trustedCerts = new HashSet<X509Certificate>();

            Enumeration<String> alias = trustStore.aliases();

            while (alias.hasMoreElements()) {
                trustedCerts.add((X509Certificate) trustStore.getCertificate(alias.nextElement()));
            }

            boolean certChainValidated = validateCertChain(cert, trustedCerts);
            if (!certChainValidated) {
                LOGGER.error("Cert chain could not be validated");
            }
            return certChainValidated;
        } catch (KeyStoreException e) {
            throw new SupplyChainValidatorException("Error with the trust store", e);
        }

    }

    /**
     * Attempts to check if the certificate is validated by certificates in a cert chain. The cert
     * chain is expected to be stored in a non-ordered KeyStore (trust store). If the signing
     * certificate for the target cert is found, but it is an intermediate cert, the validation will
     * continue to try to find the signing cert of the intermediate cert. It will continue searching
     * until it follows the chain up to a root (self-signed) cert.
     *
     * @param cert
     *            certificate to validate
     * @param trustStore
     *            trust store holding trusted root certificates and intermediate certificates
     * @return the certificate chain if validation is successful
     * @throws SupplyChainValidatorException
     *             if the verification is not successful
     */
    public static boolean verifyCertificate(final X509Certificate cert,
            final KeyStore trustStore) throws SupplyChainValidatorException {
        if (cert == null || trustStore == null) {
            throw new SupplyChainValidatorException("Certificate or trust store is null");
        }
        try {
            Set<X509Certificate> trustedCerts = new HashSet<X509Certificate>();

            Enumeration<String> alias = trustStore.aliases();

            while (alias.hasMoreElements()) {
                trustedCerts.add((X509Certificate) trustStore.getCertificate(alias.nextElement()));
            }

            return validateCertChain(cert, trustedCerts);
        } catch (KeyStoreException e) {
            LOGGER.error("Error accessing keystore", e);
            throw new SupplyChainValidatorException("Error with the trust store", e);
        }

    }

    /**
     * Attempts to check if an attribute certificate is validated by certificates in a cert chain.
     * The cert chain is represented as a Set of X509Certificates. If the signing certificate for
     * the target cert is found, but it is an intermediate cert, the validation will continue to try
     * to find the signing cert of the intermediate cert. It will continue searching until it
     * follows the chain up to a root (self-signed) cert.
     *
     * @param cert
     *            certificate to validate
     * @param additionalCerts
     *            Set of certs to validate against
     * @return boolean indicating if the validation was successful
     * @throws SupplyChainValidatorException tried to validate using null certificates
     */
    public static boolean validateCertChain(final X509AttributeCertificateHolder cert,
            final Set<X509Certificate> additionalCerts) throws SupplyChainValidatorException {
        if (cert == null || additionalCerts == null) {
            throw new SupplyChainValidatorException(
                    "Certificate or validation certificates are null");
        }
        boolean foundRootOfCertChain = false;
        Iterator<X509Certificate> certIterator = additionalCerts.iterator();
        X509Certificate trustedCert;

        while (!foundRootOfCertChain && certIterator.hasNext()) {
            trustedCert = certIterator.next();
            if (issuerMatchesSubjectDN(cert, trustedCert)
                    && signatureMatchesPublicKey(cert, trustedCert)) {
                if (isSelfSigned(trustedCert)) {
                    LOGGER.info("CA Root found.");
                    foundRootOfCertChain = true;
                } else {
                    foundRootOfCertChain = validateCertChain(trustedCert, additionalCerts);

                    if (!foundRootOfCertChain) {
                        LOGGER.error("Root of certificate chain not found. Check for CA Cert: "
                                + cert.getIssuer().getNames()[0]);
                    }
                }
            }
        }

        return foundRootOfCertChain;
    }

    /**
     * Attempts to check if a public-key certificate is validated by certificates in a cert chain.
     * The cert chain is represented as a Set of X509Certificates. If the signing certificate for
     * the target cert is found, but it is an intermediate cert, the validation will continue to try
     * to find the signing cert of the intermediate cert. It will continue searching until it
     * follows the chain up to a root (self-signed) cert.
     *
     * @param cert
     *            certificate to validate
     * @param additionalCerts
     *            Set of certs to validate against
     * @return boolean indicating if the validation was successful
     * @throws SupplyChainValidatorException tried to validate using null certificates
     */
    public static boolean validateCertChain(
    		final X509Certificate cert,
            final Set<X509Certificate> additionalCerts) 
            		throws SupplyChainValidatorException {
    	
        if (cert == null || additionalCerts == null) {
            throw new SupplyChainValidatorException(
                    "Certificate or validation certificates are null");
        }
        boolean foundRootOfCertChain = false;
        Iterator<X509Certificate> certIterator = additionalCerts.iterator();
        X509Certificate trustedCert;

        while (!foundRootOfCertChain && certIterator.hasNext()) {
            trustedCert = certIterator.next();
            // System.out.println(trustedCert.toString());
            if (issuerMatchesSubjectDN(cert, trustedCert) && 
            	signatureMatchesPublicKey(cert, trustedCert)) {
            	
                if (isSelfSigned(trustedCert)) {
                    LOGGER.info("CA Root found.");
                    foundRootOfCertChain = true;
                } else if (!cert.equals(trustedCert)) {
                    foundRootOfCertChain = validateCertChain(trustedCert, additionalCerts);
                    if (!foundRootOfCertChain) {
                        LOGGER.error("Root of certificate chain not found. Check for CA Cert: "
                                + cert.getIssuerDN().getName());
                    }
                }
            }
        }

        return foundRootOfCertChain;
    }

    // Temporary function, to be deleted later
    public static boolean validateCertChainDebug(
    		final X509Certificate cert,
            final Set<X509Certificate> additionalCerts) 
            		throws SupplyChainValidatorException {
    	
        if (cert == null || additionalCerts == null) {
            throw new SupplyChainValidatorException(
                    "Certificate or validation certificates are null");
        }
        boolean foundRootOfCertChain = false;
        Iterator<X509Certificate> certIterator = additionalCerts.iterator();
        X509Certificate trustedCert;

        while (!foundRootOfCertChain && certIterator.hasNext()) {
            trustedCert = certIterator.next();

            System.out.println("\n--> Cert SubjectDN : " + cert.getSubjectDN());
            System.out.println("--> Cert IssuerDN  : " + cert.getIssuerDN());
            System.out.println("--> Trusted cert SubjectDN : " + trustedCert.getSubjectDN());
            System.out.println("--> Trusted cert IssuerDN  : " + trustedCert.getIssuerDN());
            
            String signingCertSubjectDN = cert.getSubjectX500Principal().getName(X500Principal.RFC1779);
            String certIssuerDN = trustedCert.getIssuerDN().getName();
            X500Name namedSubjectDN = new X500Name(signingCertSubjectDN);
            X500Name namedIssuerDN = new X500Name(certIssuerDN);
            System.out.println("--> 1. signingCertSubjectDN: " + signingCertSubjectDN);
            System.out.println("--> 2. certIssuerDN        : " + certIssuerDN);
            System.out.println("--> 1. namedSubjectDN      : " + namedSubjectDN);
            System.out.println("--> 2. namedIssuerDN       : " + namedIssuerDN);
            System.out.println("--> 1 maches 2 ? " + namedIssuerDN.equals(namedSubjectDN));

	        try {
				byte []namedSubjectDNRaw = namedSubjectDN.getEncoded();
				byte []namedIssuerDNRaw  = namedIssuerDN.getEncoded();
				StringBuilder sbS = new StringBuilder();
				StringBuilder sbI = new StringBuilder();
			    for (byte b : namedSubjectDNRaw) {
			    	sbS.append(String.format("%02X", b));
			    }
			    for (byte b : namedIssuerDNRaw) {
			    	sbI.append(String.format("%02X", b));
			    }
				System.out.printf("--> namedSubjectDNRaw: %s\n", sbS.toString());
				System.out.printf("--> namedIssuerDNRaw : %s\n", sbI.toString());
	
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            
            if (issuerMatchesSubjectDN(cert, trustedCert) && 
            	signatureMatchesPublicKey(cert, trustedCert)) {
            	
                if (isSelfSigned(trustedCert)) {
                    System.out.println("--> Root found!");
                    LOGGER.info("CA Root found.");
                    foundRootOfCertChain = true;
                    
                } else if (!cert.equals(trustedCert)) {
                    System.out.println("--> Going for recursive call!");
                    foundRootOfCertChain = validateCertChainDebug(trustedCert, additionalCerts);
                    if (!foundRootOfCertChain) {
                        System.out.println("--> Root of cert chain NOT FOUND!");
                        LOGGER.error("Root of certificate chain not found. Check for CA Cert: " +
                                	  cert.getIssuerDN().getName());
                    }
                }
            }
        }

        return foundRootOfCertChain;
    }

    /**
     * Checks if the issuer info of an attribute cert matches the supposed signing cert's
     * distinguished name.
     *
     * @param cert
     *            the attribute certificate with the signature to validate
     * @param signingCert
     *            the certificate with the public key to validate
     * @return boolean indicating if the names
     * @throws SupplyChainValidatorException tried to validate using null certificates
     */
    public static boolean issuerMatchesSubjectDN(final X509AttributeCertificateHolder cert,
            final X509Certificate signingCert) throws SupplyChainValidatorException {
        if (cert == null || signingCert == null) {
            throw new SupplyChainValidatorException("Certificate or signing certificate is null");
        }
        String signingCertSubjectDN = signingCert.getSubjectX500Principal().getName();
        X500Name namedSubjectDN = new X500Name(signingCertSubjectDN);

        X500Name issuerDN = cert.getIssuer().getNames()[0];

        // equality check ignore DN component ordering
        return issuerDN.equals(namedSubjectDN);
    }

    /**
     * Checks if the issuer info of a public-key cert matches the supposed signing cert's
     * distinguished name.
     *
     * @param cert
     *            the public-key certificate with the signature to validate
     * @param signingCert
     *            the certificate with the public key to validate
     * @return boolean indicating if the names
     * @throws SupplyChainValidatorException tried to validate using null certificates
     */
    public static boolean issuerMatchesSubjectDN(final X509Certificate cert,
            final X509Certificate signingCert) throws SupplyChainValidatorException {
        if (cert == null) {
            throw new SupplyChainValidatorException("Certificate is null");
        }
        if (signingCert == null) {
            throw new SupplyChainValidatorException("Signing certificate is null");
        }
        String signingCertSubjectDN = signingCert.getSubjectX500Principal().
                                                       getName(X500Principal.RFC1779);
        X500Name namedSubjectDN = new X500Name(signingCertSubjectDN);

        String certIssuerDN = cert.getIssuerDN().getName();
        X500Name namedIssuerDN = new X500Name(certIssuerDN);

        // equality check ignore DN component ordering
//        System.out.printf("** DEBUG: namedIssuerDN equals namedSubjectDN ? %b \n %s \n %s\n",
//        		namedIssuerDN.equals(namedSubjectDN), namedIssuerDN, namedSubjectDN);
        
        return namedIssuerDN.equals(namedSubjectDN);
    }

    /**
     * Checks if the signature of an attribute cert is validated against the signing cert's public
     * key.
     *
     * @param cert
     *            the public-key certificate with the signature to validate
     * @param signingCert
     *            the certificate with the public key to validate
     * @return boolean indicating if the validation passed
     * @throws SupplyChainValidatorException tried to validate using null certificates
     */
    public static boolean signatureMatchesPublicKey(
    		final X509Certificate cert,
            final X509Certificate signingCert) 
            		throws SupplyChainValidatorException {
    	
        if (cert == null || signingCert == null) {
            throw new SupplyChainValidatorException("Certificate or signing certificate is null");
        }
        
        try {
            cert.verify(signingCert.getPublicKey(), BouncyCastleProvider.PROVIDER_NAME);
            // System.out.println("** DEBUG: cert.verify passed, returning true from verify ");
            return true;
            
        } catch (InvalidKeyException | 
        		 CertificateException | 
        		 NoSuchAlgorithmException |
                 NoSuchProviderException | 
                 SignatureException e) {
        	
            // System.out.println("** DEBUG: cert.verify returning exception: " + e.getMessage());
            LOGGER.error("Exception thrown while verifying certificate", e);
            return false;
        }

    }

    /**
     * Checks if the signature of a public-key cert is validated against the signing cert's public
     * key.
     *
     * @param cert
     *            the attribute certificate with the signature to validate
     * @param signingCert
     *            the certificate with the public key to validate
     * @return boolean indicating if the validation passed
     * @throws SupplyChainValidatorException tried to validate using null certificates
     */
    public static boolean signatureMatchesPublicKey(final X509AttributeCertificateHolder cert,
            final X509Certificate signingCert) throws SupplyChainValidatorException {
        if (cert == null || signingCert == null) {
            throw new SupplyChainValidatorException("Certificate or signing certificate is null");
        }
        return signatureMatchesPublicKey(cert, signingCert.getPublicKey());
    }

    /**
     * Checks if an X509 Attribute Certificate is valid directly against a public key.
     *
     * @param cert
     *            the attribute certificate with the signature to validate
     * @param signingKey
     *            the key to use to check the attribute cert
     * @return boolean indicating if the validation passed
     * @throws SupplyChainValidatorException tried to validate using null certificates
     */
    public static boolean signatureMatchesPublicKey(final X509AttributeCertificateHolder cert,
        final PublicKey signingKey) throws SupplyChainValidatorException {
        if (cert == null || signingKey == null) {
            throw new SupplyChainValidatorException("Certificate or signing certificate is null");
        }
        ContentVerifierProvider contentVerifierProvider;
        try {
            contentVerifierProvider =
                    new JcaContentVerifierProviderBuilder().setProvider("BC").build(signingKey);
            return cert.isSignatureValid(contentVerifierProvider);
        } catch (OperatorCreationException | CertException e) {
            LOGGER.error("Exception thrown while verifying certificate", e);
            return false;
        }
    }

    /**
     * Checks whether given X.509 public-key certificate is self-signed. If the cert can be
     * verified using its own public key, that means it was self-signed.
     *
     * @param cert
     *            X.509 Certificate
     * @return boolean indicating if the cert was self-signed
     */
    private static boolean isSelfSigned(final X509Certificate cert)
            throws SupplyChainValidatorException {
        if (cert == null) {
            throw new SupplyChainValidatorException("Certificate is null");
        }
        try {
            PublicKey key = cert.getPublicKey();
            cert.verify(key);
            return true;
        } catch (SignatureException | InvalidKeyException e) {
            return false;
        } catch (CertificateException | NoSuchAlgorithmException | NoSuchProviderException e) {
            LOGGER.error("Exception occurred while checking if cert is self-signed", e);
            return false;
        }
    }

    /**
     * Getter for the collection of delta certificates that have failed and the
     * associated message.
     * @return unmodifiable list of failed certificates
     */
    public Map<PlatformCredential, StringBuilder> getDeltaFailures() {
        return Collections.unmodifiableMap(DELTA_FAILURES);
    }

	@Override
	public AppraisalStatus validatePlatformCredentialAttributes(PlatformCredential pc,
			hirs.data.persist.DeviceInfoReport deviceInfoReport, EndorsementCredential ec) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppraisalStatus validateDeltaPlatformCredentialAttributes(PlatformCredential delta,
			hirs.data.persist.DeviceInfoReport deviceInfoReport, PlatformCredential base,
			Map<PlatformCredential, SupplyChainValidation> deltaMapping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppraisalStatus validatePlatformCredential(hirs.data.persist.certificate.PlatformCredential pc,
			KeyStore trustStore, boolean acceptExpired) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppraisalStatus validatePlatformCredentialAttributes(hirs.data.persist.certificate.PlatformCredential pc,
			hirs.data.persist.DeviceInfoReport deviceInfoReport, EndorsementCredential ec) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppraisalStatus validatePlatformCredentialAttributes(hirs.data.persist.certificate.PlatformCredential pc,
			DeviceInfoReport deviceInfoReport, EndorsementCredential ec) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppraisalStatus validateDeltaPlatformCredentialAttributes(
			hirs.data.persist.certificate.PlatformCredential delta, hirs.data.persist.DeviceInfoReport deviceInfoReport,
			hirs.data.persist.certificate.PlatformCredential base,
			Map<hirs.data.persist.certificate.PlatformCredential, SupplyChainValidation> deltaMapping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppraisalStatus validateDeltaPlatformCredentialAttributes(
			hirs.data.persist.certificate.PlatformCredential delta, DeviceInfoReport deviceInfoReport,
			hirs.data.persist.certificate.PlatformCredential base,
			Map<hirs.data.persist.certificate.PlatformCredential, SupplyChainValidation> deltaMapping) {
		// TODO Auto-generated method stub
		return null;
	}

}
