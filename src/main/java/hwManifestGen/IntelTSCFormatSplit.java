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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class IntelTSCFormatSplit {
	
	private String certChainContent;
	private String certContent;
	
	public IntelTSCFormatSplit(String inputFile) {
		parseFile(inputFile);
	}
	
	private void parseFile(String signedPlatCertFile) {
	    try {
	        File signedPlatCertFileReader = new File(signedPlatCertFile);
	        Scanner spcReader = new Scanner(signedPlatCertFileReader);
	        StringBuilder dataCert = new StringBuilder();
	        StringBuilder dataCertChain = new StringBuilder();
	        boolean firstCert = false, certchainPart = false;
	        int certCount = 0;
	        
	        // Split the signed platform certificate file between the 
	        // attribute certificate the cert chains and the 
	        while (spcReader.hasNextLine()) {
	          String temp = spcReader.nextLine();
		      // System.out.println(temp);
	          if ((temp.contains("BEGIN ATTRIBUTE CERTIFICATE")||
	        	   temp.contains("BEGIN CERTIFICATE")) && certCount == 0) {
	        	  firstCert = true;
	        	  certchainPart = false;
	          }
	          if ((temp.contains("END CERTIFICATE") || 
	        	   temp.contains("END ATTRIBUTE CERTIFICATE")) && certCount == 0) {
	        	  dataCert.append(temp+"\n");
	        	  certCount++;
	        	  firstCert = false;
	        	  certchainPart = true;
	        	  continue; // skip the rest of the routines 
	          }
	          if ((temp.contains("END CERTIFICATE") || 
		           temp.contains("END ATTRIBUTE CERTIFICATE")) && certCount != 0) {
	        	  certCount++;
	          }
	          if (firstCert == true) {
		          dataCert.append(temp+"\n");
	          }
	          if (certchainPart == true) {
		          dataCertChain.append(temp+"\n");
	          }
	        }
	        certContent = dataCert.toString();
	        certChainContent = dataCertChain.toString();
	        spcReader.close();

	        // System.out.println("** ATTRIBUTE CERTIFICATE lenght = " + spcContent.length());
	        // System.out.println(spcContent);
	        // System.out.println("** CERT CHAIN lenght = " + spcCertChainContent.length());
	        // System.out.println(spcCertChainContent);
	        
	    } catch (FileNotFoundException e) {
	        System.out.println("An error occurred.");
	        e.printStackTrace();
	    }
	}

	public String getCertChainContent() {
		return certChainContent;
	}

	public String getCertContent() {
		return certContent;
	}

	
	
}
