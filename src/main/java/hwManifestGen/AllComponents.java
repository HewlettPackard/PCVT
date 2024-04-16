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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

//import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
//import javax.print.attribute.SetOfIntegerSyntax;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.SystemUtils;
import org.bouncycastle.cert.X509AttributeCertificateHolder;

import data.persist.DeviceInfoReport;
import data.persist.certificate.PlatformCredential;
import hirs.data.persist.AppraisalStatus;
import hirs.data.persist.certificate.EndorsementCredential;
import validator.SupplyChainCredentialValidator;
import validator.ValidatorCli;

public class AllComponents {

	public static void printUsage() {
		System.out.println("	");
		System.out.println(" Usage of the PCVT tool: ");
		System.out.println("	");
		System.out.println("	There are currently two main options to use: -genhwmanif and -checkplatcert");
		System.out.println("	");
		System.out.println("	For -genhwmanif the Hardware Manifest will be generated from SCL data");
		System.out.println("	");
		System.out.println("		In this case the input SCL file must be copied from ");
		System.out.println("		/sys/firmware/efi/efivars/HpePlatformCertificate-* ");
		System.out.println("		to your preferred location, specificed at the argument -scl.");
		System.out.println("	");
		System.out.println("		The target JSON Hardware Manifest ouput file");
		System.out.println("		location must be specified at the argument -o");
		System.out.println("	");
		System.out.println("		[optional] The intermediary ouput file with SCL decoded data");
		System.out.println("		in text format can be generated if specified at the argument -dscl");
		System.out.println("	");		
		System.out.println("		Please make sure that the libdiskscan.so is included at LD_LIBRARY_PATH.");
		System.out.println("		Command line example: ");
		System.out.println("		   export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.");
		System.out.println("		   java -jar pcvt.jar -genhwmanif");
		System.out.println("		   -scl /opt/hpe/scl/HpePlatformCertificateSCLata ");
		System.out.println("		   -o /opt/hpe/scl/HpePlatformCertificateSCLdata.json");
		System.out.println("	");
		
		System.out.println("	For -checkplatcert the HPE Signed Platform Certificate");
		System.out.println("	will be checked against the Hardware Manifest generated with -genhwmanif");
		System.out.println("	");
		System.out.println("		In is case the input options are -hwmanif, -spc , -iakcert, -idevidcert");
		System.out.println("		-hwmanif specifies the path for the Hardware Manifest JSON file");
		System.out.println("		-spc specifies the path for the HPE Signed Platform Certificate");
		System.out.println("		-iakcert specifies the path for the HPE Signed System/TPM IAK Certificate");
		System.out.println("		-idevidcert specifies the path for the HPE Signed System/TPM IDevID Certificate");		
		System.out.println("	");		
		System.out.println("		Command line example: ");
		System.out.println("		   java -jar pcvt.jar -checkplatcert");
		System.out.println("		   -hwmanif /opt/hpe/scl/HpePlatformCertificateSCLdata.json");
		System.out.println("		   -spc /opt/hpe/scl/certificates/signedPlatCert");
		System.out.println("		   -iakcert /opt/hpe/scl/certificates/iakCert");
		System.out.println("		   -idevidcert /opt/hpe/scl/certificates/idevidCert"); 
		System.out.println("	");
		
		System.out.println("		The output will be a valid or invalid state for each entry.");
		System.out.println("	");
		
		System.out.println("		For retrieving the HPE Signed Platform Certificate, ");
		System.out.println("		the HPE Signed IAK Certificate and the HPE Signed IDevID Certificate please");
                System.out.println("		fetch certificate using fetchcert option with respective redfish paths or ");
		System.out.println("		trigger a GET request to the iLO API endpoint ");
		System.out.println("		https://<iLO IP Address>/redfish/v1/managers/1/diagnostics/");
		System.out.println("		filtering for the fields PlatformCert, SystemIDevID and SystemIAKCert. ");
		System.out.println("		The same process can be done through the ilorest tool.");
		System.out.println("	");

                System.out.println("	To validate individual certs, -validateCerts option can be used it will be");
		System.out.println("	checked against all the certificates given by the user.");
		System.out.println("	");
		System.out.println("		In this case the input option is -validatecerts or --validateCerts");
		System.out.println("		input can be one or more certs given like host_iakcert,host_idevidcert,. etc");
                System.out.println("	");
		
	}
	
	public static void setOptions(Options options) {
		
	// ************************** 
	// Functionality mode options 
	    
	// Mode: Generate the Hardware Manifest
	Option genHwManifOption = new Option("genhwmanif", "generateHardwareManifest", false, "Select the Hardware Manifest generation, requires as parameters -scl and -o");
	genHwManifOption.setRequired(false);
        options.addOption(genHwManifOption);

	Option fetchoption = new Option("fetchcert", "fetchCertificate", false, "Select the Fetch Certificate, requires as parameters -u, -p, -s, -r and -c");
	fetchoption.setRequired(false);
	options.addOption(fetchoption);

        // Mode: Verify Signed Platform Certificate
	Option checkplatcertOption = new Option("checkplatcert", "checkPlatformCertificate", false, "Select the Signed Platform Certificate verification, requires as parameters -hwmanif, -spc, -iakcert, -idevidcert");
	checkplatcertOption.setRequired(false);
        options.addOption(checkplatcertOption);

        
        // **************************
	// Parameters for I/O 
        
        // I/O parameters for the Mode Generate the Hardware Manifest
	Option sclinput = new Option("scl", "sclinput", true, "SCL input file originated from /sys/firmware/efi/efivars/HpePlatformCertificate-*");
        sclinput.setRequired(false);
        options.addOption(sclinput);

        Option smbios = new Option("smbios", "SMBIOS", false, "Instructs to use SMBIOS instead of SCL as input");
        smbios.setRequired(false);
        options.addOption(smbios);

        Option paccordir = new Option("paccordir", "paccordirectory", true, "[Feature not yet available] Paccor installation directory, from where its Hardware Manifest can be generated");
        paccordir.setRequired(false);
        options.addOption(paccordir);

        Option paccorhwmanif = new Option("paccorhwmanif", "paccorhwmanifest", true, "[Feature not yet available] Generated Paccor Hardware Manifest file, by default named as localhost-componentlist.json");
        paccorhwmanif.setRequired(false);
        options.addOption(paccorhwmanif);
		     		    
        Option decscl = new Option("dscl", "decodedscl", true, "Intermediary output file with the SCL Data decoded in text");
        decscl.setRequired(false);
        options.addOption(decscl);

        Option output = new Option("o", "output", true, "Output file with the JSON Hardware Manifest");
        output.setRequired(false);
        options.addOption(output);

        // I/O parameters for the retrieve Certificate
        Option validatecerts = new Option("validatecerts", "validateCerts", true, "The Certificates to be validated, parse 1 or more like host_idevid,rmp_idevid,host_iak,rmp_iak");
	validatecerts.setRequired(false);
	options.addOption(validatecerts);

	Option usern = new Option("username", "username", true, "Enter the username");
    	usern.setRequired(false);
    	options.addOption(usern);

	Option pass = new Option("passwd", "password", true, "Enter the password");
	pass.setRequired(false);
    	options.addOption(pass);

   	Option sys = new Option("sys", "system", true, "Enter the system name");
  	sys.setRequired(false);
   	options.addOption(sys);

    	Option command = new Option("redfishcmd", "redfishCommand", true, "Enter the redfish url");
    	command.setRequired(false);
    	options.addOption(command);

    	Option path = new Option("outpath", "outputPath", true, "Enter the output path for storing the certificate");
    	path.setRequired(false);
    	options.addOption(path);

        Option filename = new Option("filename", "fileName", true, "To store the certificate, the file names can be host_idevid, rmp_idevid, host_iak, rmp_iak, etc");
    	filename.setRequired(false);
    	options.addOption(filename);

        // I/O parameters for the Mode Verify Signed Platform Certificate
        Option signedPlatCert = new Option("spc", "signedPlatCert ", true, "Signed Platform Certificate file");
        signedPlatCert .setRequired(false);
        options.addOption(signedPlatCert);
        
        Option iakCert = new Option("iakcert", "iakCert", true, "IAK Certificate file");
        iakCert.setRequired(false);
        options.addOption(iakCert);

        Option idevidCert = new Option("idevidcert", "idevidCert", true, "IDevID Certificate file");
        idevidCert.setRequired(false);
        options.addOption(idevidCert);

        Option hwManifestInput = new Option("hwmanif", "hwManifest", true, "Input file with the JSON Hardware Manifest from SCL data");
        hwManifestInput.setRequired(false);
        options.addOption(hwManifestInput);
        
        Option verbose = new Option("v", "verbose", false, "Verbose output to stdout");
        verbose.setRequired(false);
        options.addOption(verbose);

        Option altRootCert = new Option("forceRootCA", "forceRootCAcert", true, "Input file with an alternative Root CA Certificate");
        altRootCert.setRequired(false);
        options.addOption(altRootCert);
        
	}

	private static void fetchcertificate(CommandLine cmd) throws Exception {
		String username = cmd.getOptionValue("username");  // Get the value of the "username" option from the command line
	    	String password = cmd.getOptionValue("passwd");    // Get the value of the "passwd" option from the command line
	    	String system = cmd.getOptionValue("sys");         // Get the value of the "sys" option from the command line
	    	String command = cmd.getOptionValue("redfishcmd"); // Get the value of the "redfishcmd" option from the command line

	    	System.out.println("Username: " + username);  // Print the username
	    	System.out.println("Password: " + password);  // Print the password
	    	System.out.println("System: " + system);      // Print the system
	    	System.out.println("Command: " + command);    // Print the command

	    	String output;
	    	String urlString = "https://" + system + "/redfish/v1/SessionService/Sessions/";

	    	// Disable SSL certificate validation
	    	TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			}
	    	};

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

			// To fetch the X-auth token
			URL url1 = new URL(urlString);
			try {
				HttpsURLConnection con1 = (HttpsURLConnection) url1.openConnection();
				con1.setRequestMethod("GET"); // Set the request method
				con1.setRequestProperty("Content-Type", "application/json"); // Set the request property
				con1.setDoOutput(true);
				String jsonInputString = "{\"UserName\":\"" + username + "\",\"Password\":\"" + password + "\"}";
				try (OutputStreamWriter out = new OutputStreamWriter(con1.getOutputStream())) {
					out.write(jsonInputString); // Write the JSON input string to the output stream
				}	
				// Read the input stream from con1 and store the content in content1
				BufferedReader in1 = new BufferedReader(new InputStreamReader(con1.getInputStream()));
				String inputLine1;
				StringBuffer content1 = new StringBuffer();
				while ((inputLine1 = in1.readLine()) != null) {
					content1.append(inputLine1);
				}
				in1.close();

				// Extract X-Auth-Token from the header of con1
				String authToken = con1.getHeaderField("X-Auth-Token");
				System.out.println("X-Auth Token:" + authToken);

				String urlString2 = "https://" + system + command;
				// Create URL object
				URL obj = new URL(urlString2);

				// Create HttpsURLConnection
				HttpsURLConnection con2 = (HttpsURLConnection) obj.openConnection();

				// Set request method to GET
				con2.setRequestMethod("GET");

				// Set the X-Auth-Token header
				con2.setRequestProperty("X-Auth-Token", authToken);

				// Get response code
				int responseCode = con2.getResponseCode();
				System.out.println("Response Code: " + responseCode);

				// Read response
				BufferedReader in = new BufferedReader(new InputStreamReader(con2.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				output = response.toString();

				// Extract the CertificateString
				String certificateString = "";

                                // Pattern to match key-value pairs
                                String patternString = "\"(.*?)\":\\s*\"(.*?)\"";

                                // Create a Pattern object
                                Pattern pattern = Pattern.compile(patternString);

                                // Create a Matcher object
                                Matcher matcher = pattern.matcher(output);

                                // Create a Map to hold key-value pairs
                                Map<String, String> dictionary = new HashMap<>();

                                // Find and store all key-value pairs
                                while (matcher.find()) {
                                        dictionary.put(matcher.group(1), matcher.group(2));
                                }

                                certificateString = dictionary.get("CertificateString");
                                
                                // Replace escape sequence for newline with actual newline character
				certificateString = certificateString.replace("\\n", "\n");

                                System.out.println("Certificate String: " + certificateString);
				
				// Set the output file path
				String filefullpath;
                                String parentdir = null;
                                // Set the file path to the specified directory with the filename given by the user
				// Check if "outpath" command option is provided
				if (cmd.hasOption("outpath")) {
					// Get the value of the "outpath" command option
					parentdir = cmd.getOptionValue("outpath");
				} else {
					System.out.println("Output Path option not provided");
                                        parentdir = "/opt/hpe/scl/";
				}

                                // Create the directory if it doesn't exist
                                File directory = new File(parentdir);
                                if (!directory.exists()) {
                                        boolean created = directory.mkdirs();
                                        if (created) {
                                                System.out.println("Directory created: " + parentdir);
                                        } else {
                                                System.out.println("Failed to create directory: " + parentdir);
                                        }
                                }

                                String filename = null;
                                if (cmd.hasOption("filename")){
                                        filename = cmd.getOptionValue("filename");
                                        System.out.println("The filename given by the user is " + filename);
                                } else {
                                        filename = system + "_cert.pem";
                                }

                                filefullpath = parentdir + filename;
                                int number = 1;
                                String newFileName;
                                File file = new File(filefullpath);
                                
                                while (file.exists()) {
                                        newFileName = filename + '_' + number;
                                        filefullpath = parentdir + newFileName;
                                        file = new File(filefullpath);
                                        number++;
                                };
                                
				try {
					// Create a FileWriter to write the certificate to the output file
					FileWriter fileWriter = new FileWriter(filefullpath);
					BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

					// Write the certificateString to the output file
					bufferedWriter.write(certificateString);

					// Close the BufferedWriter
					bufferedWriter.close();
					System.out.println("Successfully wrote certificate to " + filefullpath);
				} catch (IOException e) {
					// Handle any IOException that occurs during file writing
					System.out.println("Error writing certificate to output2.pem: " + e.getMessage());
				}
			}
			catch(Exception e) {
			    // Exception occurred
			    System.out.println("While downloading the certificate, error occured: " + e.toString());

			    // Check if the exception message contains "401" indicating incorrect credentials
			    if(e.toString().contains("401"))
					System.out.println("Credentials entered are incorrect.");
			    // Check if the exception message contains "java.net.UnknownHostException" indicating unavailability of the system
			    else if(e.toString().contains("java.net.UnknownHostException"))
					System.out.println("System is not reachable");
			}
	    	}
	    	catch (MalformedURLException e) {
	        	System.out.println(e.getMessage());
	    	}
	}
	
	private static void generateHardwareManifest(CommandLine cmd) {
		
      	System.out.println("Generating HW Manifest...");
    	String jsonHwManifestOutput = null;
    	
		String jsonHwManifestFile = "C:\\HpePlatformCertificate_HwManifest.json";
		if (cmd.getOptionValue("output") != null) {
			jsonHwManifestFile = cmd.getOptionValue("output");			
		}
    	
    	// The hw manifest will be generated from the SCL data file
    	if (cmd.getOptionValue("sclinput") != null || cmd.hasOption("smbios")) {
        	System.out.println("Generating HW Manifest from SCL/SMBIOS data.");
       		
        	try {
        	 
	    		String inputBinarySCLFile = "C:\\HpePlatformCertificate.bin";
	    		if (cmd.getOptionValue("sclinput") != null) {
	    			inputBinarySCLFile = cmd.getOptionValue("sclinput");
	    		}
	
	    		String intermediateOutputFile = null;
	    		if (cmd.getOptionValue("decodedscl") != null) {
	    			intermediateOutputFile = cmd.getOptionValue("decodedscl");	
	    		}
	
	    		JsonHwManifest jhwm = null;
    			jhwm = new JsonHwManifest(); 			
	
	    		// TODO: put this option as an parameter later
	    		boolean scanDisks = true; // default value set to TRUE
				boolean smbios = false;
				if(cmd.hasOption("smbios")) {
					smbios = true;
				}
	    			jsonHwManifestOutput = jhwm.genHwManifest(inputBinarySCLFile, 
	    					intermediateOutputFile, true, scanDisks, smbios);
    			
    		} catch (Error e) { 
    			e.printStackTrace();
        		System.err.println(e.toString());
            	System.err.println("Error: unable to generate the Hardware Manifest from SCL data");
    			System.exit(1);
        	} catch (Exception e) {
    			e.printStackTrace();
        		System.err.println(e.toString());
            	System.err.println("Exception: unable to generate the Hardware Manifest from SCL data");        		
    			System.exit(1);
        	}
    		
    		
    	} else if (cmd.getOptionValue("paccordir") != null || 
    			   cmd.getOptionValue("paccorhwmanif") != null) {
    		
        	System.out.println("Generating HW Manifest from Paccor.");
        	// This generation is not functional yet, to be done in the near future.
        	
    		if (cmd.getOptionValue("paccordir") != null) {
    			String paccorDir = cmd.getOptionValue("paccordir").trim();
    			// TODO: trigger sudo ./allcomponents.sh > paccorhwmanif.txt
    			ProcessBuilder processBuilder = new ProcessBuilder();

    			String paccorCmd = null;
    			if (SystemUtils.IS_OS_LINUX) {
    				// -- Linux --
    				paccorCmd = String.format("sudo %s/scripts/allcomponents.sh", paccorDir);
    				System.out.println("** DEBUG paccorCmd: " + paccorCmd);
    				processBuilder.command("bash", "-c", paccorCmd);
    			}

    			if (SystemUtils.IS_OS_WINDOWS) {
    				// -- Windows --
    				paccorCmd = String.format("C:\\Windows\\SysWOW64\\WindowsPowerShell\\v1.0\\powershell.exe -ExecutionPolicy Bypass \"%s\\scripts\\windows\\allcomponents.ps1\" \"%s\\scripts\\windows\\paccor_components-list.json\"", paccorDir, paccorDir);
    				System.out.println("** DEBUG paccorCmd: " + paccorCmd);
    				processBuilder.command(paccorCmd);
    			}
    			
    			try {
    				Process process = processBuilder.start();
    				StringBuilder output = new StringBuilder();
    				BufferedReader reader = new BufferedReader(
    						new InputStreamReader(process.getInputStream()));

    				String line;
    				while ((line = reader.readLine()) != null) {
    					output.append(line + "\n");
    				}
    				jsonHwManifestOutput = output.toString();

    				int exitVal = process.waitFor();
    				if (exitVal == 0) {
    					System.out.println("Success executing the command: " + paccorCmd);
    				} else {
    					System.out.println("Error executing the command: " + paccorCmd);
    				}

    			} catch (Error e) { // for dealing with UnsatisfiedLinkError
    				System.err.println(e.toString());
    				System.err.println("Error: unable to generate the Hardware Manifest from Paccor");
    				System.exit(1);
    			} catch (Exception e) {
    				System.err.println(e.toString());
    				System.err.println("Exception: unable to generate the Hardware Manifest from Paccor");        		
    				System.exit(1);
    			}
    			
    		} else {
    			// String paccorHwManifest = cmd.getOptionValue("paccorhwmanif");
    			// TODO: retrieve the file localhost-componentlist.json from the location
				System.err.println("Error: invalid option selected");        		
				System.exit(1);
    		}
			
    	}
    	
		BufferedWriter txtout = null;
		try {
			txtout = new BufferedWriter(new FileWriter(jsonHwManifestFile));
		
			// TODO: comment this line afterwards
			if (cmd.hasOption("verbose")) {
				System.out.println(jsonHwManifestOutput);
			}

			txtout.write(jsonHwManifestOutput);
			txtout.close();
			System.out.println("Hw manifest file written to "+jsonHwManifestFile);
			
		} catch (Error e) { 
			System.err.println(e.toString());
			System.err.println("Error: unable to write the Hardware Manifest content to " + jsonHwManifestFile);
			System.exit(1);
		} catch (Exception e) {
			System.err.println(e.toString());
			System.err.println("Exception: unable to write the Hardware Manifest content to " + jsonHwManifestFile);
			System.exit(1);
		}

	}

	private static void checkPlatformCertificate(CommandLine cmd, String[] args) {
		
    	String signedPlatCertFile = cmd.getOptionValue("spc");	    	
    	String iakCertFile = cmd.getOptionValue("iakCert"); 
    	String idevidCertFile = cmd.getOptionValue("idevidCert");
    	String hwManifestInputFile = cmd.getOptionValue("hwManifest");
    	String altRootCertFile = cmd.getOptionValue("forceRootCA");
    	
    	X509AttributeCertificateHolder atribCert = null;    	    	
    	ValidatorCli cli = new ValidatorCli();   
    	
        try {
        	cli.handleCommandLine(args, atribCert);
	        
        	// TODO: here we need to modify the CertCheck SPCCheck to enable loading an alternative root cert.        	
		System.out.println("Reading Signed Platform Trust Chain certificates");
	        CertCheck SPCCheck;
            if (altRootCertFile != null) {                         
	        	SPCCheck = new CertCheck(signedPlatCertFile, altRootCertFile);
	        } else {
	        	SPCCheck = new CertCheck(signedPlatCertFile);
	        }
	        boolean validSignature = SPCCheck.verifyX509AttributeCert();
			
			System.out.println("Reading IAK Trust Chain certificates");
			CertCheck IAKCheck;
            if (altRootCertFile != null) {
            	IAKCheck = new CertCheck(iakCertFile, altRootCertFile);
	        } else {
		        IAKCheck = new CertCheck(iakCertFile);
	        }
	        boolean iakCheck = IAKCheck.verifyX509Cert();

			System.out.println("Reading IDevID Trust Chain certificates");
			CertCheck IDevIDCheck;
            if (altRootCertFile != null) {
            	IDevIDCheck = new CertCheck(idevidCertFile, altRootCertFile);
	        } else {
	        	IDevIDCheck = new CertCheck(idevidCertFile);
	        }			
	        boolean idevidCheck = IDevIDCheck.verifyX509Cert();

            // Platform Certificate information 
	        PlatformCredential pc;
			pc = new PlatformCredential(SPCCheck.getCertContent().getBytes());
					        
	        // HW manifest information
			DeviceInfoReport deviceInfoReport = new DeviceInfoReport();
			deviceInfoReport.setPaccorOutputString(hwManifestInputFile);
	        
			// TODO: to be tested with the actual HSM and EK use by RAS 
			//       currently commented inside validatePlatformCredentialAttributes
			EndorsementCredential ec = null;
			// ec = new EndorsementCredential(Paths.get(cli.argList.getPublicKeyCert()));
			
			// TODO: later use the compClass argument value to locate the component-class.json file 
			//       within the ComponentClass.java class.
			SupplyChainCredentialValidator platCertCheck = new SupplyChainCredentialValidator();
	        AppraisalStatus checkPlatCertAndHWManifest = 
	        		platCertCheck.validatePlatformCredentialAttributes(pc, deviceInfoReport, ec);		        
	        
	        AppraisalStatus spcCertChainCheck = platCertCheck.validatePlatformCredential(
	        		pc, SPCCheck.getKS(), true);

			// // Alternative way to manually load certificate one by one
			// Certificate rootcacert, intermediateca02cert;
			// intermediateca02cert = new CertificateAuthorityCredential(Files.readAllBytes(Paths.get("C:\\test2_01.pem")));
			// rootcacert = new CertificateAuthorityCredential(Files.readAllBytes(Paths.get("C:\\test2_02.pem")));
			// ks.setCertificateEntry("CA cert", rootcacert.getX509Certificate());
			// ks.setCertificateEntry("Intermediate Cert", intermediateca02cert.getX509Certificate());
			// boolean cchainCheck = SupplyChainCredentialValidator.verifyCertificate(
			// pc.getX509AttributeCertificateHolder(), ks);
			// checkCertChain = platCertCheck.validatePlatformCredential(pc, ks, true);
			// System.out.println("** DEBUG checkCertChain: " + checkCertChain.getMessage());		        

	        System.out.printf("\n **** RESULTS ****\n");   

	        if (checkPlatCertAndHWManifest.getMessage().equalsIgnoreCase("Platform credential attributes validated")) {
		        System.out.printf("\n **** Platform Components Verification Status: **** \n%s\n", 
	        	"The platform components are VALID");
	        } else {
	        	System.out.printf("\n **** Platform Components Verification Status: **** \n%s\n%s\n", 
	    	    "The platform components are INVALID", checkPlatCertAndHWManifest.getMessage());
	        }
	        
	        if (spcCertChainCheck.getMessage().equalsIgnoreCase("Platform credential validated")) {
		        System.out.printf("\n **** Platform Certificate Trust Chain Status: **** \n%s\n", 
		        		"The Platform Certificate Trust Chain is VALID");
	        } else {
		        System.out.printf("\n **** Platform Certificate Trust Chain Status: **** \n%s\n", 
		        		"The Platform Certificate Trust Chain is INVALID");
	        }
	        
        	System.out.println("\n **** Platform Certificate Signature Status: ****");
	        if(!validSignature) {
            	System.out.println("The Platform Certificate signature is INVALID");
            } else {
            	System.out.println("The Platform Certificate signature is VALID");
            }

	        System.out.println("\n **** IAK Certificate Trust Chain Status: ****"); 
	        if(!iakCheck) {
            	System.out.println("The IAK Certificate Chain and signature are INVALID");
            } else {
            	System.out.println("The IAK Certificate Chain and signature are VALID");
            }

	        System.out.println("\n **** IDevID Certificate Trust Chain Status: ****"); 
	        if(!idevidCheck) {
            	System.out.println("The IDevID Certificate Chain and signature are INVALID");
            } else {
            	System.out.println("The IDevID Certificate Chain and signature are VALID");
            }

	        // Important return to get the failed status from the PCVT tool. 
	        if (!validSignature || 
	        	checkPlatCertAndHWManifest.getAppStatus() == AppraisalStatus.Status.FAIL ||
	        	spcCertChainCheck.getAppStatus() ==  AppraisalStatus.Status.FAIL || 
	        	iakCheck == false || 
	        	idevidCheck == false) {
	        		System.exit(1);
	        }
	        
		} catch (Error e) { 
			e.printStackTrace();
			System.err.println(e.toString());
			System.err.println("Error: unable to verify or check the Certificates");
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.toString());
			System.err.println("Exception: unable to verify or check the Certificates");
			System.exit(1);
		}	
	}

	private static void validateCertificates(CommandLine cmd){
		String certificates = cmd.getOptionValue("validatecerts");
                String altRootCertFile = cmd.getOptionValue("forceRootCA");
                
                String[] certs = certificates.split(",");
                
        	try {
			System.out.println("Reading Trust Chain certificates");
                        boolean certfile = true;
                        for (String cert : certs) {
                                CertCheck CertFile;
                                if (altRootCertFile != null) {
            	                        CertFile = new CertCheck(cert, altRootCertFile);
                                } else {
                                        CertFile = new CertCheck(cert);
                                }
                                
                                certfile = CertFile.verifyX509Cert();

                                System.out.println("\n **** Certificate Trust Chain Status: ****");
                                if(!certfile) {
                                        System.out.println("The Certificate Chain and signature are INVALID for " + cert + '\n');
                                } else {
                                        System.out.println("The Certificate Chain and signature are VALID for " + cert + '\n');
                                }
                        }
                        // Important return to get the failed status from the PCVT tool. 
                        if (certfile == false) {
                                System.exit(1);
                        }
			
		} catch (Error e) {
			e.printStackTrace();
			System.err.println(e.toString());
			System.err.println("Error: unable to verify or check the Certificates");
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.toString());
			System.err.println("Exception: unable to verify or check the Certificates");
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception {

	    Options options = new Options();
	    setOptions(options);
	    
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
            if (cmd.getOptions().length == 0 ) {
            	throw(new ParseException("Insufficient arguments: please select one of the current possible actions: -checkplatcert or -genhwmanif along with their parameters"));
            } 
        } catch (ParseException e) {
            System.out.println(e.getMessage());            
            formatter.printHelp("java -jar pcvt.jar [options]", options);
            printUsage();
            System.exit(1);
        }

        if (cmd.hasOption("genhwmanif")) {
        	generateHardwareManifest(cmd);
        }

        if (cmd.hasOption("fetchcert")) {
        	fetchcertificate(cmd);
        }

	if (cmd.hasOption("validatecerts")) {
        	validateCertificates(cmd);
        }

        if (cmd.hasOption("checkplatcert")) {
        	checkPlatformCertificate(cmd, args);
        }
        
        System.out.println("\n*** No further options selected, exiting. ***");                
	}

}
