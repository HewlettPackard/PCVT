//**********************************************************************
// (C) Copyright 2020-2021 Hewlett Packard Enterprise Development LP
// 
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//**********************************************************************

package hwManifestGen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;

import operator.PcBcContentVerifierProviderBuilder;
import pemReader.PEMReader;
import pemReader.PEMString;
import pemReader.PEMFile;
import validator.CliHelper;
import validator.CliHelper.x509type;
import validator.SupplyChainCredentialValidator;
import validator.SupplyChainValidatorException;

public class CertCheck {
	private KeyStore ks;
	private Random rand;
	//private AppraisalStatus checkStatus;
	private IntelTSCFormatSplit certificateContent;
	private String chainContent;
	private String certContent;
	private Set<X509Certificate> trustedCerts;
	
	public CertCheck(String cert) {
		rand = new Random();
		//checkStatus = null;
		trustedCerts = new HashSet<X509Certificate>();
		createKS();
		loadCertificates(cert);
	}
	
	public CertCheck(String cert, String rootCert) {
		rand = new Random();
		//checkStatus = null;
		trustedCerts = new HashSet<X509Certificate>();
		createKS();
		
		// here we ned 
		loadCertificates(cert, rootCert);
	}
	
	private void createKS( ) {
		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
			char[] ksPasswd = String.valueOf(rand.nextInt()).toCharArray();
			ks.load(null, ksPasswd);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Format the files to work as:
	// 1x Signed platform certificate
	// 1x Cert Chain for the signed platform certificate
	private void loadCertificates(String cert) {
	    certificateContent = new IntelTSCFormatSplit(cert);
	    chainContent = certificateContent.getCertChainContent();
	    certContent = certificateContent.getCertContent();
	    try {
			PEMReader.importPEMString(new PEMString(chainContent), ks);
		    // HPE Production Root CA Certificate is hardcoded, loading it at the KeyStore		    
		    rootCert rootCertificate = new rootCert();
			PEMReader.importPEMString(new PEMString(rootCertificate.getRootCert()), ks);		
	
			Enumeration<String> ksAlias = ks.aliases();
		    while (ksAlias.hasMoreElements()) {
		    	trustedCerts.add((X509Certificate) ks.getCertificate(ksAlias.nextElement()));
		    }
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    private void loadCertificates(String cert, String rootCertificatePath) {
        certificateContent = new IntelTSCFormatSplit(cert);
        chainContent = certificateContent.getCertChainContent();
        certContent = certificateContent.getCertContent();
        try {
            PEMReader.importPEMString(new PEMString(chainContent), ks);
            // Other Root CA Certificates can be loaded from file, loading it at the KeyStore               
            PEMFile rootCertContent = new PEMFile(new File(rootCertificatePath)); 
            PEMReader.importPEMFile(rootCertContent, ks);
            System.out.println("Root CA Certificate imported from: " + rootCertificatePath);

		    Enumeration<String> ksAlias = ks.aliases();
		    while (ksAlias.hasMoreElements()) {
		    	trustedCerts.add((X509Certificate) ks.getCertificate(ksAlias.nextElement()));
		    }
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// Load alternative Trust Chain, in the same format as 
	// generated by the loadCertificates() call 
	public void loadAlternateChain(String newChainContent) {
	    try {
			PEMReader.importPEMString(new PEMString(newChainContent), ks);
		    rootCert rootCertificate = new rootCert();
			PEMReader.importPEMString(new PEMString(rootCertificate.getRootCert()), ks);
	
			Enumeration<String> ksAlias = ks.aliases();
		    while (ksAlias.hasMoreElements()) {
		    	trustedCerts.add((X509Certificate) ks.getCertificate(ksAlias.nextElement()));
		    }
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	// Verification of the Certificate signature and trustchain
	@SuppressWarnings("static-access")
	public boolean verifyX509Cert() {
	    CertificateFactory certFact;
	    boolean checkStatus = false;
		try {
			certFact = CertificateFactory.getInstance("X.509");
			InputStream certStream = new ByteArrayInputStream(certContent.getBytes());
			X509Certificate cert = (X509Certificate) certFact.generateCertificate(certStream);
	    
			SupplyChainCredentialValidator certCheck = new SupplyChainCredentialValidator();
			checkStatus = certCheck.validateCertChain(cert, trustedCerts);
//			checkStatus = certCheck.validateCertChainDebug(cert, trustedCerts);
		} catch (SupplyChainValidatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	    
	    
	    return checkStatus;
	}
	
	// Verification of the Certificate signature and trustchain
	public boolean verifyX509AttributeCert() throws SupplyChainValidatorException {
		
		boolean validSignature = false;
    	// TODO: try using signatureMatchesPublicKey from HIRS library in the future avoid paccor routines.

    	// load the signed attribute certificate which requires signature verification 
        X509AttributeCertificateHolder attributeCert = null;
		try {
			attributeCert = (X509AttributeCertificateHolder)CliHelper.loadCertFromString(
			certContent, x509type.ATTRIBUTE_CERTIFICATE);

			Iterator<X509Certificate> certIterator = trustedCerts.iterator();
            X509Certificate trustedCert;
            while (!validSignature && certIterator.hasNext()) {
                trustedCert = certIterator.next();
                X509CertificateHolder signingKeyPublicCert = new X509CertificateHolder(trustedCert.getEncoded());
	            try {
	                // Choose the verifier based on the public certificate signature algorithm
	                ContentVerifierProvider cvp = new PcBcContentVerifierProviderBuilder(
	                		new DefaultDigestAlgorithmIdentifierFinder()).build(signingKeyPublicCert);
	                // Verify the signature
	                validSignature = attributeCert.isSignatureValid(cvp);
	            } catch (OperatorCreationException e) {
	            	// System.out.println("** DEBUG OperatorCreationException: Error on signature validation");
	            	e.printStackTrace();
	            	throw new IllegalArgumentException(e);
	    	    } catch (CertException e) {
	    	    	// System.out.println("** DEBUG CertException: Error on signature validation");
	    	    	e.printStackTrace();
	    	        throw new IllegalArgumentException(e);
	    	    }
            }

        } catch (CertificateEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    
		return validSignature;
	}
	
	public String getCertContent( ) {
		return certContent;
	}
	
	public KeyStore getKS( ) {
		return ks;
	}
	
}
