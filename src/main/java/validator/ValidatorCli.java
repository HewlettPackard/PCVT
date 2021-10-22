package validator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.bouncycastle.cert.X509AttributeCertificateHolder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import hirs.data.persist.AppraisalStatus;
import hirs.data.persist.DeviceInfoReport;
import hirs.data.persist.certificate.EndorsementCredential;
import hirs.data.persist.certificate.PlatformCredential;


public class ValidatorCli {
    public ValidatorArgs argList;
    
    public ValidatorCli() {
        argList = new ValidatorArgs();
    }
    
    public void  handleCommandLine(String[] args, X509AttributeCertificateHolder atribCert) throws IOException {
        JCommander jCommanderBuild = JCommander.newBuilder().addObject(argList).build();
        jCommanderBuild.setProgramName("validator");
        jCommanderBuild.setAcceptUnknownOptions(true);
        jCommanderBuild.parse(args);
        if (argList.isHelp()) {
            jCommanderBuild.usage();
            System.exit(1);
        }

//        // load the public certificate of the key that signed the attribute cert
//        X509CertificateHolder signingKeyPublicCert = (X509CertificateHolder)CliHelper.loadCert(
//        		argList.getPublicKeyCert(), x509type.CERTIFICATE);
////        System.out.println("");
////        System.out.println(" ----> signingKeyPublicCert: " + signingKeyPublicCert);
//
//        // load the signed attribute certificate which requires signature verification 
//        X509AttributeCertificateHolder attributeCert = (X509AttributeCertificateHolder)CliHelper.loadCert(
//        		argList.getX509v2AttrCert(), x509type.ATTRIBUTE_CERTIFICATE);
//        System.out.println("");
//        System.out.println(" ----> attributeCert : " + attributeCert );
//        atribCert = attributeCert;
        
//        boolean valid = false;
//        try {
//            // Choose the verifier based on the public certificate signature algorithm
//            ContentVerifierProvider cvp = new PcBcContentVerifierProviderBuilder(
//            		new DefaultDigestAlgorithmIdentifierFinder()).build(signingKeyPublicCert);
//            
//            // Verify the signature
//            valid = attributeCert.isSignatureValid(cvp);
//            
//        } catch (OperatorCreationException e) {
//        	System.out.println("** DEBUG OperatorCreationException: Error on signature validation");
//            throw new IllegalArgumentException(e);
//	    } catch (CertException e) {
//	    	System.out.println("** DEBUG CertException: Error on signature validation");
//	        throw new IllegalArgumentException(e);
//	    }

//        // Different path using HIRS routines... ends up on the same code as the one above.
//        FileInputStream fin = new FileInputStream(argList.getPublicKeyCert());
//        boolean hirsValid = false;        
//		try {
//			CertificateFactory f = CertificateFactory.getInstance("X.509");
//			X509Certificate certificate = (X509Certificate)f.generateCertificate(fin);
//			PublicKey pk = certificate.getPublicKey();
//			
//			hirsValid = SupplyChainCredentialValidator.signatureMatchesPublicKey(attributeCert, pk);
//			System.out.println("** DEBUG: Hirs validation = "+ hirsValid);
//			
//		    BufferedWriter writer = new BufferedWriter(new FileWriter("d:/temp/scldata/filesDump/atributeCert.bin"));		    
//			System.out.println("** DEBUG attributeCert: " + Hex.toHexString(attributeCert.getEncoded()));
//		    writer.write(Hex.toHexString(attributeCert.getEncoded()));
//		    writer.close();
//		    
//		    writer = new BufferedWriter(new FileWriter("d:/temp/scldata/filesDump/atributeCertInfo.bin"));
//		    AttributeCertificate atribCert_ = attributeCert.toASN1Structure();
//		    AttributeCertificateInfo atribCertInfo = atribCert_.getAcinfo();
//		    System.out.println("** DEBUG atribCertInfo: " + Hex.toHexString(atribCertInfo.getEncoded()));
//		    writer.write(Hex.toHexString(atribCertInfo.getEncoded()));
//		    writer.close();		    
//		} catch (SupplyChainValidatorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (CertificateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}        
        
//        return valid;
    }
    
    
    public static final void main(String[] args) throws Exception {
        
    	X509AttributeCertificateHolder atribCert = null;    	
    	
    	ValidatorCli cli = new ValidatorCli();        
        try {
        	// TODO correct this behavior later by adding the previous validation funcionality 
        	// from handleCommandLine at another function
        	boolean validSignature = false;
        	cli.handleCommandLine(args, atribCert);

        	// if(!cli.handleCommandLine(args)) {
            if(!validSignature) {
            	System.out.println("Invalid signature: " + validSignature);
                // System.exit(1);
            } else {
            	System.out.println("Valid signature: " + validSignature);
            }
            
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
        }
        
        
        // CURRENT WIP the check of the fields
        
		// Platform Certificate information 
        String pcArg = cli.argList.getX509v2AttrCert();        
        Path pcPath = Path.of(pcArg);
        System.out.println("Plat Cert Path: " + pcPath + ", " + pcPath);
        PlatformCredential pc = new PlatformCredential(Files.readAllBytes(pcPath));
		
        // HW manifest information
		DeviceInfoReport deviceInfoReport = new DeviceInfoReport();
		deviceInfoReport.setPaccorOutputString(cli.argList.getHwManifest());
        
		// Public Key information 
		EndorsementCredential ec = new EndorsementCredential(Paths.get(cli.argList.getPublicKeyCert()));

		SupplyChainCredentialValidator platCertCheck = new SupplyChainCredentialValidator();
        AppraisalStatus checkPlatCertAndHWManifest = 
        		platCertCheck.validatePlatformCredentialAttributes(pc, deviceInfoReport, ec);
        
        // Only components were found here, TODO: 
        // 1. understand where are the properties previously present at the hw manifest
        // 2. how to obtain them through the current pc methods.
        Map<String, Object> atribs = pc.getAllAttributes();        
        for (Map.Entry<String, Object> atrib : atribs.entrySet()) {
        	System.out.println("** DEBUG. Key = " + atrib.getKey() + 
        					   ", Value = " + atrib.getValue());			
		}
//        System.out.println("** DEBUG: properties? " + pc.getAttribute("COMPONENTS"));

        
        System.out.printf("\n **** Appraisal Status: **** \n%s\n", checkPlatCertAndHWManifest.getMessage());
        // atribCert.toASN1Structure();
        
        
    }
}
