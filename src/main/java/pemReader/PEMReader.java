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

package pemReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.codec.binary.Base64;

public class PEMReader {

	/**
	 * Adds a PEM formatted string
	 * 
	 * @param pem							pem formated certificate
	 * @throws JKSKeyStoreUtilException
	 * @throws IOException
	 */
	public static void importPEM(String pem, KeyStore ks) throws Exception, IOException {
		if (! Base64.isBase64(pem) ) {
			throw new Exception("PEM File does not contain valid Base64 data");
		}
		
		byte[] der = Base64.decodeBase64(pem);
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(der);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(bais); 
			
			String alias = getDN(cert);
			
			if (alias == null) {
				System.out.println(String.format("Can not suitable alias for cert %s", getDN(cert)));
			} else if (containsAlias(alias, ks)) {
				System.out.println(String.format( "%s alias already exists (%s)", alias, getDN(cert) )); 
			} else {
				System.out.println("Adding Cert with alias: " + alias);
				ks.setCertificateEntry(alias, cert);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	
	private static String getDN(X509Certificate certificate) {
		X500Principal x509principal = certificate.getSubjectX500Principal();
		String dn = x509principal.getName("RFC2253");
		return dn;
	}
	
	public static boolean containsAlias(String alias, KeyStore ks) throws KeyStoreException {
		return ks.containsAlias(alias);
	}
	
	/**
	 * @param pemFile						imports a whole {@linkplain PEMFile} object
	 * @throws JKSKeyStoreUtilException
	 * @throws IOException
	 */
	public static void importPEMFile(PEMFile pemFile, KeyStore ks) throws Exception, IOException {
		ArrayList<String> pems = pemFile.getPEMBlocks();
		
		if (ks != null) {
			for (String pemString : pems) {
				importPEM(pemString, ks);
			}
		}
	}

	public static void importPEMString(PEMString pemString, KeyStore ks) throws Exception, IOException {
		ArrayList<String> pems = pemString.getPEMBlocks();
		
		if (ks != null) {
			for (String pemStr : pems) {
				importPEM(pemStr, ks);
			}
		}
	}

}
