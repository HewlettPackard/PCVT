# Platform Certificate Verification Tool (PCVT):

PCVT allows you to verify that the hardware and platform firmware configuration of your Hewlett Packard Enterprise server has not been modified since leaving the factory.

PCVT performs the following operations:

  1) PCVT verifies the current hardware and relevant firmware configuration of the server
     against the information encoded in the Platform Certificate that HPE issued at its factory.
     The Platform Certificate is an X.509 Attribute Certificate signed by HPE.

  2) PCVT cryptographically verifies the validity of the HPE-issued system certificates:
    Platform Certificate, System IDevID Certificate and System IAK Certificate, and their
    trust chains.

## PCVT delivery options
The PCVT is available as a bootable ISO image. The PCVT ISO is available on the 'Releases' section of this GitHub repository.

## PCVT documentation
For more information on PCVT and its operation, please refer to the User Guide document that accompanies the ISO image for each release.

## PCVT usage:
Follow the steps in this section to run PCVT using a bootable ISO:

1. Download the PCVT bootable ISO from the 'Releases' section of this GitHub repository.

2. Verify the hash digest of the downloaded ISO image against the corresponding hash digest value provided for each release.

```
NOTICE
To prevent changes to the HPE server that you plan to test,
HPE recommends that you download the bootable ISO from a different computer.
```

3. Configure the HPE server to boot to the ISO image:
 - If you downloaded the bootable ISO image on another computer, transfer the ISO image to the HPE server such as by using iLO virtual drive or a USB flash drive.
 - Configure the HPE server to boot to the ISO image using iLO.

4. Boot the ISO image and run PCVT:
 - Boot the HPE server to the ISO image. After the Linux operating system on the ISO image loads, PCVT runs automatically.

```
If iLO requires authentication, a user prompt will be displayed for iLO account credentials. Please provide the username and password for an iLO account configured on the server to allow the tool to discover the current hardware and firmware configuration of the server. If running PCVT on a newly-unboxed server, the default iLO credentials will be provided with the server packaging.
```

Below you can find sample output from a successful execution of PCVT:


      localhost login: root (automatic login)
      Have a lot of fun ...
      Setting up PCVT environment .. .
      iLO requires authentication - please provide iLO account credentials:
      Username:
      Password:
      Reading Platform Certificate
      Reading System IAK Certificate
      Reading System IDevID Certificate
      Reading IDevID Certificate

      Discovering current platform state ..
      RedFish version: 1.13.0
      Ignoring device: Embedded Video Controller
      Final inventory:
      Type:00010002/ Manuf.:Intel(R) Corporation/ Model:Intel(R) Xeon(R) Gold 6442Y/ SN:-/ Rev.:8/ FRU:true
      Type:00130003/ Manuf.:HPE/ Model:U63/ SN:-/ Rev.:1.40/ FRU:false
      Type:00040009/ Manuf.:STMicro/ Model:TPM2_0/ SN:-/ Rev.:1.512/ FRU:false
      Type:00050012/ Manuf.:HPE/ Model:iLO 6/ SN:-/ Rev.:1.55/ FRU:false
      Type:00070002/ Manuf.:NOT SPECIFIED/ Model:MK000960GXAXB/ SN:[Redacted]/ Rev.:HPG1/ FRU:true
      Type:00070002/ Manuf.:NOT SPECIFIED/ Model:MK000960GXAXB/ SN:[Redacted]/ Rev.:HPG1/ FRU:true
      Type:0006001D/ Manuf.:Hynix/ Model:HMCG94AEBRA123N/ SN:[Redacted]/ Rev.:-/ FRU:true
      Type:0006001D/ Manuf.:Hynix/ Model:HMCG94AEBRA123N/ SN:[Redacted]/ Rev.:-/ FRU:true
      Type:00090002/ Manuf.:Intel Corporation/ Model:K53978-005/ SN:[Redacted]/ Rev.:1.3310.0/ FRU:true
      Type:000A0002/ Manuf.:CHCNY/ Model:P38995-B21/ SN:[Redacted]/ Rev.:2.01/ FRU:true
      Type:000A0002/ Manuf.:CHCNY/ Model:P38995-B21/ SN:[Redacted]/ Rev.:2.01/ FRU:true
      Type:00070002/ Manuf.:HPE/ Model:VO003840RZWUT/ SN:[Redacted]/ Rev.:HPD0/ FRU:true
      Type:00070002/ Manuf.:HPE/ Model:VO003840RZWUT/ SN:[Redacted] / Rev.:HPD0/ FRU:true
      Type:00020016/ Manuf.:HPE/ Model:ProLiant DL320 Gen11/ SN:[Redacted]/ Rev.:-/ FRU:false
      Type:00030003/ Manuf.:HPE/ Model:P48995-001/ SN:[Redacted]/ Rev.:-/ FRU:false
      Type:00050003/ Manuf.:HPE/ Model:P47785-B21/ SN:[Redacted]/ Rev.:52.22.3-4650/ FRU:true

      Verifying platform certificate...
      Platform Certificate Holder matches the TPM EK certificate.
      - root/PlatformCertificate signature and chain VERIFIED.
      - root/SystemIAKCertificate VERIFIED.
      - root/SystemIDevIDCertificate VERIFIED.

      Validating hardware manifest against certificate...
      No changes detected between Platform Certificate and hardware manifest.


