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

package sclDataImport;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class HpeSclData {
	
	public HpeSclData(String pciIds) {
		InitStructs();		
		pciIdsPath = pciIds;
	}
	
	public HpeSclData() {
		InitStructs();
	}
	
	public void InitStructs ( ) {
		for (int i=0; i<CPU_MAX_COUNT; i++) {
			CpuInfo[i] = new PLATFORM_CERTIFICATE_CPU_INFO();
		}		
		for (int i=0; i<DIMM_MAX_COUNT; i++) {
			DimmInfo[i] = new PLATFORM_CERTIFICATE_DIMM_INFO();
		}
		for (int i=0; i<PCI_SLOT_MAX_COUNT; i++) {
			PciSlotInfo[i] = new PLATFORM_CERTIFICATE_PCI_SLOT_INFO();
		}		
		for (int i=0; i<POWER_SUPPLY_MAX_COUNT; i++) {
			PowerSupplyInfo[i] = new PLATFORM_CERTIFICATE_POWER_SUPPLY_INFO();
		}		
	}
	
	public String pciIdsPath = null;

	static String HP_PLATFORM_CERTIFICATE_VARIABLE = "HpePlatformCertificate";
	//#define HP_PLATFORM_CERTIFICATE_VAR_GUID    { 0xB250B9D5, 0x40E6, 0xB2BB, { 0xAF, 0x7C, 0x4F, 0x9E, 0x95, 0xA1, 0x5B, 0x31 } }	

	static int CPU_MAX_COUNT = 4;
	static int DIMM_MAX_COUNT = 48;
	static int PCI_SLOT_MAX_COUNT = 24; 
	static int POWER_SUPPLY_MAX_COUNT = 8;

	//#define HP_PLATFORM_CERTIFICATE_REV1_GUID   { 0x82422266, 0x4B1E, 0xB9CF, { 0x5F, 0xB0, 0x8B, 0xB2, 0x7C, 0xC4, 0x3E, 0xC2 } }
	//#define HP_PCR6_EVENT_PLATFORM_CERTIFICATE_GUID   { 0x4D9F0003, 0x43BA, 0x2895, { 0xEA, 0x16, 0x17, 0x80, 0x8A, 0xDC, 0x02, 0xB8 } }

	static int SMBIOS_STRING_MAX_LENGTH = 64;
	static int SHA256_DIGEST_SIZE = 32;
	static int MAX_MANUFACTURER_ASCII_STRING = 16;
	
	// HP_PLATFORM_CERTIFICATE
	// EFI_GUID - TODO: review if the current understanding is correct.
	public byte[] CertificateRevisionGuid = new byte[16];
	public PLATFORM_CERTIFICATE_MOTHERBOARD_INFO 	MotherBoardInfo = new PLATFORM_CERTIFICATE_MOTHERBOARD_INFO();
	public PLATFORM_CERTIFICATE_FIRMWARE_INFO 		BackupBiosInfo = new PLATFORM_CERTIFICATE_FIRMWARE_INFO();
	public PLATFORM_CERTIFICATE_FIRMWARE_INFO 		ApmlInfo = new PLATFORM_CERTIFICATE_FIRMWARE_INFO();	
	public PLATFORM_CERTIFICATE_FIRMWARE_INFO 		MegaCellInfo = new PLATFORM_CERTIFICATE_FIRMWARE_INFO();	
	public PLATFORM_CERTIFICATE_FIRMWARE_INFO 		NvmeInfo = new PLATFORM_CERTIFICATE_FIRMWARE_INFO();	
	public PLATFORM_CERTIFICATE_FIRMWARE_INFO 		IntelligentProvisioningInfo = new PLATFORM_CERTIFICATE_FIRMWARE_INFO();	
	public PLATFORM_CERTIFICATE_FIRMWARE_INFO 		VgaInfo = new PLATFORM_CERTIFICATE_FIRMWARE_INFO();
	public PLATFORM_CERTIFICATE_FIRMWARE_INFO 		PowerPicInfo = new PLATFORM_CERTIFICATE_FIRMWARE_INFO();
	public PLATFORM_CERTIFICATE_FIRMWARE_INFO 		PowerPicBootLoaderInfo = new PLATFORM_CERTIFICATE_FIRMWARE_INFO();
	public PLATFORM_CERTIFICATE_FIRMWARE_INFO 		CpldInfo = new PLATFORM_CERTIFICATE_FIRMWARE_INFO();
	public PLATFORM_CERTIFICATE_FIRMWARE_INFO 		IntelMeInfo = new PLATFORM_CERTIFICATE_FIRMWARE_INFO();	
	public PLATFORM_CERTIFICATE_FIRMWARE_INFO 		IntelMeSpiInfo = new PLATFORM_CERTIFICATE_FIRMWARE_INFO();	
	public PLATFORM_CERTIFICATE_FIRMWARE_INFO 		IntelIeInfo = new PLATFORM_CERTIFICATE_FIRMWARE_INFO();	
	public PLATFORM_CERTIFICATE_SYSTEM_INFO	  		SystemInfo = new PLATFORM_CERTIFICATE_SYSTEM_INFO();
	public PLATFORM_CERTIFICATE_BIOS_INFO 	  		BiosInfo = new PLATFORM_CERTIFICATE_BIOS_INFO();
	public PLATFORM_CERTIFICATE_CHASSIS_INFO  		ChassisInfo = new PLATFORM_CERTIFICATE_CHASSIS_INFO();
	public PLATFORM_CERTIFICATE_CPU_INFO[]    		CpuInfo = new PLATFORM_CERTIFICATE_CPU_INFO[CPU_MAX_COUNT];
	public PLATFORM_CERTIFICATE_DIMM_INFO[]   		DimmInfo = new PLATFORM_CERTIFICATE_DIMM_INFO[DIMM_MAX_COUNT];
	public PLATFORM_CERTIFICATE_TPM_INFO      		TpmInfo = new PLATFORM_CERTIFICATE_TPM_INFO();
	public PLATFORM_CERTIFICATE_BMC_INFO      		BmcInfo = new PLATFORM_CERTIFICATE_BMC_INFO();
	public PLATFORM_CERTIFICATE_POWER_SUPPLY_INFO[] PowerSupplyInfo = new PLATFORM_CERTIFICATE_POWER_SUPPLY_INFO[POWER_SUPPLY_MAX_COUNT];
	public PLATFORM_CERTIFICATE_PCI_SLOT_INFO[]     PciSlotInfo = new PLATFORM_CERTIFICATE_PCI_SLOT_INFO[PCI_SLOT_MAX_COUNT];
	public PLATFORM_CERTIFICATE_BOOT_STATUS_INFO    BootStatusInfo = new PLATFORM_CERTIFICATE_BOOT_STATUS_INFO();
	
	
	public String parseSCLData (String inputFile, String outputFile, boolean fromEfiVars) throws IOException {
		BufferedWriter txtout= new BufferedWriter(new FileWriter(outputFile));
		// ObjectOutputStream binout = new ObjectOutputStream(new FileOutputStream(outputBin));
		
		String readableSCLData =  parseSCLData(inputFile, fromEfiVars);
		txtout.write(readableSCLData);
		txtout.close();
		
		return readableSCLData;
	}

	public void populateMotherBoardInfo (InputStream inputStream) {
		try {
			MotherBoardInfo.MotherBoardSha256Hash = inputStream.readNBytes(SHA256_DIGEST_SIZE);
			MotherBoardInfo.MotherBoardPciSha256Hash = inputStream.readNBytes(SHA256_DIGEST_SIZE);
			MotherBoardInfo.EmbeddedPciSha256Hash = inputStream.readNBytes(SHA256_DIGEST_SIZE);
			MotherBoardInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			MotherBoardInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			MotherBoardInfo.Version = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			MotherBoardInfo.Product = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			MotherBoardInfo.AssetTag = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			MotherBoardInfo.BoardType = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateBackupBiosInfo (InputStream inputStream) {
		try {
			BackupBiosInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			BackupBiosInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			BackupBiosInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			BackupBiosInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			BackupBiosInfo.FwTypeIndicator = inputStream.readNBytes(2);  // read 2 byte 16 bits
			BackupBiosInfo.Fru = ((byte) inputStream.read())!=0; // read 1 byte 8 bits;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateApmlInfo (InputStream inputStream) {
		try {
			ApmlInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			ApmlInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			ApmlInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			ApmlInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			ApmlInfo.FwTypeIndicator = inputStream.readNBytes(2);  // read 2 byte 16 bits
			ApmlInfo.Fru = ((byte) inputStream.read())!=0; // read 1 byte 8 bits;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateMegaCellInfo (InputStream inputStream) {
		try {
			MegaCellInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			MegaCellInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			MegaCellInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			MegaCellInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			MegaCellInfo.FwTypeIndicator = inputStream.readNBytes(2);  // read 2 byte 16 bits
			MegaCellInfo.Fru = ((byte) inputStream.read())!=0; // read 1 byte 8 bits;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateNvmeInfo (InputStream inputStream) {
		try {
			NvmeInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			NvmeInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			NvmeInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			NvmeInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			NvmeInfo.FwTypeIndicator = inputStream.readNBytes(2);  // read 2 byte 16 bits
			NvmeInfo.Fru = ((byte) inputStream.read())!=0; // read 1 byte 8 bits;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateIntelligentProvisioningInfo (InputStream inputStream) {
		try {
			IntelligentProvisioningInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			IntelligentProvisioningInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			IntelligentProvisioningInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			IntelligentProvisioningInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			IntelligentProvisioningInfo.FwTypeIndicator = inputStream.readNBytes(2);  // read 2 byte 16 bits
			IntelligentProvisioningInfo.Fru = ((byte) inputStream.read())!=0; // read 1 byte 8 bits;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateVgaInfo (InputStream inputStream) {
		try {
			VgaInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			VgaInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			VgaInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			VgaInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			VgaInfo.FwTypeIndicator = inputStream.readNBytes(2);  // read 2 byte 16 bits
			VgaInfo.Fru = ((byte) inputStream.read())!=0; // read 1 byte 8 bits;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populatePowerPicInfo (InputStream inputStream) {
		try {
			PowerPicInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			PowerPicInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			PowerPicInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			PowerPicInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			PowerPicInfo.FwTypeIndicator = inputStream.readNBytes(2);  // read 2 byte 16 bits
			PowerPicInfo.Fru = ((byte) inputStream.read())!=0; // read 1 byte 8 bits;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populatePowerPicBootLoaderInfo (InputStream inputStream) {
		try {
			PowerPicBootLoaderInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			PowerPicBootLoaderInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			PowerPicBootLoaderInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			PowerPicBootLoaderInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			PowerPicBootLoaderInfo.FwTypeIndicator = inputStream.readNBytes(2);  // read 2 byte 16 bits
			PowerPicBootLoaderInfo.Fru = ((byte) inputStream.read())!=0; // read 1 byte 8 bits;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateCpldInfo (InputStream inputStream) {
		try {
			CpldInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			CpldInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			CpldInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			CpldInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			CpldInfo.FwTypeIndicator = inputStream.readNBytes(2);  // read 2 byte 16 bits
			CpldInfo.Fru = ((byte) inputStream.read())!=0; // read 1 byte 8 bits;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateIntelMeInfo (InputStream inputStream) {
		try {
			IntelMeInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			IntelMeInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			IntelMeInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			IntelMeInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			IntelMeInfo.FwTypeIndicator = inputStream.readNBytes(2);  // read 2 byte 16 bits
			IntelMeInfo.Fru = ((byte) inputStream.read())!=0; // read 1 byte 8 bits;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateIntelMeSpiInfo (InputStream inputStream) {
		try {
			IntelMeSpiInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			IntelMeSpiInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			IntelMeSpiInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			IntelMeSpiInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			IntelMeSpiInfo.FwTypeIndicator = inputStream.readNBytes(2);  // read 2 byte 16 bits
			IntelMeSpiInfo.Fru = ((byte) inputStream.read())!=0; // read 1 byte 8 bits;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateIntelIeInfo (InputStream inputStream) {
		try {
			IntelIeInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			IntelIeInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			IntelIeInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			IntelIeInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH); 
			IntelIeInfo.FwTypeIndicator = inputStream.readNBytes(2);  // read 2 byte 16 bits
			IntelIeInfo.Fru = ((byte) inputStream.read())!=0; // read 1 byte 8 bits;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateSystemInfo (InputStream inputStream) {
		try {
			SystemInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			SystemInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			SystemInfo.ProductName = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			SystemInfo.Version = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			SystemInfo.UUID = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			SystemInfo.SKU = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			SystemInfo.Family = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateBiosInfo (InputStream inputStream) {
		try {
			BiosInfo.Vendor = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			BiosInfo.BiosVersion = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			BiosInfo.BiosReleaseDate = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			BiosInfo.BiosMajorRelease = (byte) inputStream.read();
			BiosInfo.BiosMinorRelease = (byte) inputStream.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateChassisInfo (InputStream inputStream) {
		try {
			ChassisInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			ChassisInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			ChassisInfo.Version = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			ChassisInfo.AssetTag = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			ChassisInfo.Type = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			ChassisInfo.BootupState = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateCpuInfo (InputStream inputStream) {
		try {
			for (int i=0; i<CPU_MAX_COUNT; i++) {
				CpuInfo[i].CpuInfoHash = inputStream.readNBytes(SHA256_DIGEST_SIZE);
				CpuInfo[i].Socket = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				CpuInfo[i].Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				CpuInfo[i].PartNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				CpuInfo[i].SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				CpuInfo[i].Version = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				CpuInfo[i].AssetTag = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				CpuInfo[i].ID = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				CpuInfo[i].Type = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				
				CpuInfo[i].parseID();				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateDimmInfo (InputStream inputStream) {
		try {
			for (int i=0; i<DIMM_MAX_COUNT; i++) {
				DimmInfo[i].DimmInfoHash = inputStream.readNBytes(SHA256_DIGEST_SIZE);
				DimmInfo[i].Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				DimmInfo[i].Type = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				DimmInfo[i].SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				DimmInfo[i].Device = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				DimmInfo[i].Bank = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				DimmInfo[i].AssetTag = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				DimmInfo[i].PartNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				//DimmInfo[i].Size = ByteBuffer.wrap(inputStream.readNBytes(2)).getShort();  // read 2 byte 16 bits
				DimmInfo[i].Size = inputStream.readNBytes(2);  // read 2 byte 16 bits
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateTpmInfo (InputStream inputStream) {
		try {
			TpmInfo.VendorId = inputStream.readNBytes(4);
			TpmInfo.SpecVersion = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			TpmInfo.FirmwareVersion = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			TpmInfo.Description = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			
			TpmInfo.parseFwVersion();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateBmcInfo (InputStream inputStream) {
		try {
			BmcInfo.Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			BmcInfo.Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			BmcInfo.SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
			BmcInfo.Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populatePowerSupplyInfo (InputStream inputStream) {
		try {
			for (int i=0; i<POWER_SUPPLY_MAX_COUNT; i++) {
				PowerSupplyInfo[i].Manufacturer = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PowerSupplyInfo[i].SerialNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PowerSupplyInfo[i].AssetTag = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PowerSupplyInfo[i].PartNumber = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PowerSupplyInfo[i].DeviceName = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PowerSupplyInfo[i].Location = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PowerSupplyInfo[i].Revision = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PowerSupplyInfo[i].Hotplug = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PowerSupplyInfo[i].Present = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PowerSupplyInfo[i].Unplugged = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PowerSupplyInfo[i].VoltRange = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PowerSupplyInfo[i].Status = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PowerSupplyInfo[i].Type = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				// PowerSupplyInfo[i].Watts = ByteBuffer.wrap(inputStream.readNBytes(2)).getShort();  // read 2 byte 16 bits
				PowerSupplyInfo[i].Watts = inputStream.readNBytes(2);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populatePciSlotInfo (InputStream inputStream) {
		try {
			//int count = 0;
			for (int i=0; i<PCI_SLOT_MAX_COUNT; i++) {	
				PciSlotInfo[i].PciSlotInfoHash = inputStream.readNBytes(SHA256_DIGEST_SIZE);
				PciSlotInfo[i].Model = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				PciSlotInfo[i].Serial = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
				//PciSlotInfo[i].DeviceVendorId = ByteBuffer.wrap(inputStream.readNBytes(4)).getInt(); // read 4 bytes 32 bits;
				PciSlotInfo[i].DeviceVendorId = inputStream.readNBytes(4); // read 4 bytes 32 bits;
				PciSlotInfo[i].SubDeviceVendorId = inputStream.readNBytes(4); // read 4 bytes 32 bits;
				
//				System.out.println("** DEBUG count: "+ count); count++;
//				System.out.println("** DEBUG DeviceVendorId: "+ Utils.bytesToHex(PciSlotInfo[i].DeviceVendorId));
//				System.out.println("** DEBUG SubDeviceVendorId: "+ Utils.bytesToHex(PciSlotInfo[i].DeviceVendorId));
//				if (Utils.bytesToHex_LE_32bits(
//						new byte[] {PciSlotInfo[i].DeviceVendorId[0], 
//									PciSlotInfo[i].DeviceVendorId[1]}).equals("0000") == false) {
//					PciSlotInfo[i].Manufacturer = PciSlotInfo[i].getVendorManufacturer(pciIdsPath);
////					System.out.println("** DEBUG vendor name: "+ PciSlotInfo[i].Manufacturer);
//				}
				PciSlotInfo[i].populateVendor();
				PciSlotInfo[i].populateDevice();
				PciSlotInfo[i].populateSubVendor();
				PciSlotInfo[i].populateSubDevice();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void populateBootStatusInfo (InputStream inputStream) {
		try {
			BootStatusInfo.BootStatus = inputStream.readNBytes(SMBIOS_STRING_MAX_LENGTH);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String parseSCLData (String inputFile, boolean fromEfiVars) throws IOException {
		
		InputStream inputStream = new FileInputStream(inputFile);

		try {
			if (fromEfiVars) {
				inputStream.readNBytes(4); // read 4 byte header from /sys/firmware/efi/efivars/HpePlatformCertificate* file
			}
				
			CertificateRevisionGuid = inputStream.readNBytes(16); // read 16 bytes			
			populateMotherBoardInfo(inputStream);
			populateBackupBiosInfo (inputStream);
			populateApmlInfo (inputStream);
			populateMegaCellInfo (inputStream);
			populateNvmeInfo (inputStream);
			populateIntelligentProvisioningInfo (inputStream);
			populateVgaInfo (inputStream);
			populatePowerPicInfo (inputStream);
			populatePowerPicBootLoaderInfo (inputStream);
			populateCpldInfo (inputStream);
			populateIntelMeInfo (inputStream);
			populateIntelMeSpiInfo (inputStream);
			populateIntelIeInfo (inputStream);
			populateSystemInfo (inputStream);
			populateBiosInfo (inputStream);
			populateChassisInfo (inputStream);
			populateCpuInfo(inputStream);
			populateDimmInfo(inputStream);
			populateTpmInfo (inputStream);
			populateBmcInfo (inputStream);
			populatePowerSupplyInfo (inputStream);
			populatePciSlotInfo (inputStream);
			populateBootStatusInfo (inputStream);

			// Temporary debug output
			// System.out.println(this.toString());
			inputStream.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	
		return this.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Platform Information details: " + 
				  "\n CertificateRevisionGuid="     + Utils.bytesToHex(CertificateRevisionGuid) + 
				  "\n MotherBoardInfo="             + MotherBoardInfo +  
				  "\n BackupBiosInfo="              + BackupBiosInfo + 
				  "\n ApmlInfo="                    + ApmlInfo + 
				  "\n MegaCellInfo="                + MegaCellInfo + 
				  "\n NvmeInfo="                    + NvmeInfo + 
				  "\n IntelligentProvisioningInfo=" + IntelligentProvisioningInfo + 
				  "\n VgaInfo="                     + VgaInfo + 
				  "\n PowerPicInfo="                + PowerPicInfo + 
				  "\n PowerPicBootLoaderInfo="      + PowerPicBootLoaderInfo + 
				  "\n CpldInfo="                    + CpldInfo + 
				  "\n IntelMeInfo="                 + IntelMeInfo +
				  "\n IntelMeSpiInfo="              + IntelMeSpiInfo +
				  "\n IntelIeInfo="                 + IntelIeInfo +
				  "\n SystemInfo="                  + SystemInfo +
				  "\n BiosInfo="                    + BiosInfo +
				  "\n ChassisInfo="                 + ChassisInfo 
				);
		
		for (int i=0; i<CPU_MAX_COUNT; i++) {
			if (!Utils.bytesToString(CpuInfo[i].Manufacturer).trim().isEmpty()) 
				sb.append(CpuInfo[i].toString());
		}		
		for (int i=0; i<DIMM_MAX_COUNT; i++) {
			if (!Utils.bytesToString(DimmInfo[i].Manufacturer).trim().isEmpty())
				sb.append(DimmInfo[i].toString());
		}
		
		sb.append("\n TpmInfo=" + TpmInfo + 
				  "\n BmcInfo=" + BmcInfo
				  );

		for (int i=0; i<POWER_SUPPLY_MAX_COUNT; i++) {
			if (!Utils.bytesToString(PowerSupplyInfo[i].Manufacturer).trim().isEmpty())
				sb.append(PowerSupplyInfo[i].toString());
		}
		for (int i=0; i<PCI_SLOT_MAX_COUNT; i++) {
			if (!PciSlotInfo[i].Manufacturer.trim().isEmpty())
				sb.append(PciSlotInfo[i].toString());
		}
		
		sb.append("\n BootStatus=" + BootStatusInfo);
		
		return sb.toString();
	}
	

}
