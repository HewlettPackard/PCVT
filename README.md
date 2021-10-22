
Platform Certificate Verification Tool (PCVT):

  This is tool has as objective to enable:

  1) the verification of the current device's hardware manifest against the Platform 
    Certificate that HPE issued at its factory. The Platform Certificate is an Attribute
    Certificate signed by HPE. 

  2) the certificates signature verification against the intermediate certificate that 
    signed the Platform Certificate, System IDevID Certificate, System IAK Certificate.

  3) the certificates trustchain verification, verifying the chain from the signed 
    certificate up to the HPE Root CA certificate.



PCVT compilation:

  PCVT version uses Maven tool in order to be compiled.
  First execute the prepare-env.sh bash script in order to prepare the environment 
    to compile the binaries.

  Three dependencies are being locally copied to the lib directory under the pcvt 
    repository folder. Please manually copy them to the respective Maven repository 
    directories such as ~/.m2/repository/HIRS_Structs/, ~/.m2/repository/HIRS_Utils/
    and  ~/.m2/repository/paccor/. In case another location is being used as the 
    Maven repository, please redirect the file copies there.

  Compile the source code with the command below:
    mvn clean compile assembly:single

  The file build/pcvt-mvn-0.0.1-jar-with-dependencies.jar will be generated.
  Under the same directory, please make sure to compile the content under the
    diskscan folder at this repository, generating the libdiskscan.so library
    that should be visible under LD_LIBRARY_PATH when executing the PCVT jar.



PCVT usage:

  First of all it is necessary to download the Platform Certificate, the System 
IDevID Certificate and the System IAK Certificate from iLO. This can be achieved
by doing a GET request to the iLO API endpoint below filtering for the fields 
"PlatformCert", "SystemIDevID" and "SystemIAKCert". The same process can be done
through the ilorest tool.

  https://<iLO IP Address>/redfish/v1/managers/1/diagnostics/

  After retrieving the content mentioned above and saving them to files (e.g.:
     /opt/hpe/scl/certificates/signedPlatCert, 
     /opt/hpe/scl/certificates/iakCert 
     /opt/hpe/scl/certificates/idevidCert), 

  Please run the PCVT tool for generate the Hardware Manifest of the current state of your device:

    mkdir -p /opt/hpe/scl/certificates

    cp /sys/firmware/efi/efivars/HpePlatformCertificate-b250b9d5-40e6-b2bb-af7c-4f9e95a15b31 /opt/hpe/scl/HpePlatformCertificateSCLdata

    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:<path where the libdiskscan.so is located> ; \n
    java -jar target/pcvt-mvn-0.0.1-jar-with-dependencies.jar \n
    -genhwmanif -scl /opt/hpe/scl/HpePlatformCertificateSCLdata \n
    -o /opt/hpe/scl/HpePlatformCertificateSCLdata.json 
  
  Finally, run the PCVT tool to verify the Platform Components within the 
  Platform Certificate (signedPlatCert file) against the Hardware Manifest 
  (HpePlatformCertificateSCLdata.json file). This process will:
         b) Verify the Platform Certificate signature against the leaf certificate of the Trust Chain.
         c) Verify the Platform Certificate Trust Chain Status up to the Root CA Certificate.
         d) Verify the signature and the Trust Chain for the IAK Certificate.
         e) Verify the signature and the Trust Chain for the IDevID Certificate.
        

    java -jar build/pcvt-mvn-0.0.1-jar-with-dependencies.jar \n
    -checkplatcert -hwmanif /opt/hpe/scl/HpePlatformCertificateSCLdata.json \n
    -spc /opt/hpe/scl/certificates/signedPlatCert \n
    -iakcert /opt/hpe/scl/certificates/iakCert \n
    -idevidcert /opt/hpe/scl/certificates/idevidCert

  Below you can find the expected output execution for the PCVT check.  

                Reading Signed Platform Trust Chain certificates
                Adding Cert with alias: CN=HPE Platform CM00 CA A2P01,OU=Compute Devices,O=Hewlett Packard Enterprise Development,ST=Texas,C=US
                Adding Cert with alias: CN=HPE Platform Policy CA A1P01,OU=Compute Devices,O=Hewlett Packard Enterprise Development,ST=Texas,C=US
                Adding Cert with alias: CN=HPE Device Identity Root CA A0001,OU=Compute Devices,O=Hewlett Packard Enterprise Development,ST=Texas,C=US
                Reading IAK Trust Chain certificates
                Adding Cert with alias: CN=HPE Device Intermediate CM00 CA A2D01,OU=Compute Devices,O=Hewlett Packard Enterprise Development,ST=Texas,C=US
                Adding Cert with alias: CN=HPE Device Policy CA A1D01,OU=Compute Devices,O=Hewlett Packard Enterprise Development,ST=Texas,C=US
                Adding Cert with alias: CN=HPE Device Identity Root CA A0001,OU=Compute Devices,O=Hewlett Packard Enterprise Development,ST=Texas,C=US
                Reading IDevID Trust Chain certificates
                Adding Cert with alias: CN=HPE Device Intermediate CM00 CA A2D01,OU=Compute Devices,O=Hewlett Packard Enterprise Development,ST=Texas,C=US
                Adding Cert with alias: CN=HPE Device Policy CA A1D01,OU=Compute Devices,O=Hewlett Packard Enterprise Development,ST=Texas,C=US
                Adding Cert with alias: CN=HPE Device Identity Root CA A0001,OU=Compute Devices,O=Hewlett Packard Enterprise Development,ST=Texas,C=US

                PlatformManufacturerStr field in Platform Credential matches a related field in the DeviceInfoReport (HPE)
                PlatformModel field in Platform Credential matches a related field in the DeviceInfoReport (ProLiant DL380 Gen10 Plus)
                PlatformVersion field in Platform Credential matches a related field in the DeviceInfoReport ()
                PlatformSerial field in Platform Credential matches a related field in the DeviceInfoReport (2M212100CR)
                Number of properties found at the Platform Certificate: 6
                
                 **** RESULTS ****
                
                 **** Platform Components Verification Status: ****
                The platform components are VALID
                
                 **** Platform Certificate Trust Chain Status: ****
                The Platform Certificate Trust Chain is VALID
                
                 **** Platform Certificate Signature Status: ****
                The Platform Certificate signature is VALID
                
                 **** IAK Certificate Trust Chain Status: ****
                The IAK Certificate Chain and signature are VALID
                
                 **** IDevID Certificate Trust Chain Status: ****
                The IDevID Certificate Chain and signature are VALID
                
                *** No further options selected, exiting. ***

