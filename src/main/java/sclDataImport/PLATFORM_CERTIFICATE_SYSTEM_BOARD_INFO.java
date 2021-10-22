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

//PLATFORM_CERTIFICATE_SYSTEM_BOARD_INFO
public class PLATFORM_CERTIFICATE_SYSTEM_BOARD_INFO {
//    PLATFORM_CERTIFICATE_ALOM_ATTRIBUTES

	public PLATFORM_CERTIFICATE_ALOM_ATTRIBUTES HpALom1Attributes = new PLATFORM_CERTIFICATE_ALOM_ATTRIBUTES();
	public PLATFORM_CERTIFICATE_ALOM_ATTRIBUTES HpALom2Attributes = new PLATFORM_CERTIFICATE_ALOM_ATTRIBUTES();
	public PLATFORM_CERTIFICATE_ALOM_ATTRIBUTES HpALom3Attributes = new PLATFORM_CERTIFICATE_ALOM_ATTRIBUTES();
	public PLATFORM_CERTIFICATE_ALOM_ATTRIBUTES HpALom4Attributes = new PLATFORM_CERTIFICATE_ALOM_ATTRIBUTES();
	public PLATFORM_CERTIFICATE_AROC_ATTRIBUTES HpARoc1Attributes = new PLATFORM_CERTIFICATE_AROC_ATTRIBUTES();
	public PLATFORM_CERTIFICATE_AROC_ATTRIBUTES HpARoc2Attributes = new PLATFORM_CERTIFICATE_AROC_ATTRIBUTES();		
	public long                                 BmcAsicId; // UINT64 
	public int                                  SystemCpldVersion; //UINT16
	public byte                                 BmcStepping; //UINT8
	public byte                                 HpPcaRevId; //UINT8
	public byte                                 GpioSpecVersion; //UINT8
	public byte                                 PchStepping; //UINT8
	public byte                                 HpChassisId; //UINT8
	public byte                                 HpPlatformId; //UINT8
	public byte                                 HpRiserId1; //UINT8
	public byte                                 HpRiserId2; //UINT8
	public byte                                 HpRiserId3; //UINT8
	public byte                                 HpRiserId4; //UINT8
	public boolean                              MLomPresent; //BOOLEAN
	public boolean                              TpmPresent; //BOOLEAN
	public boolean                              TpmHidden; //BOOLEAN
	public byte                                 TpmType; //UINT8
	public byte                                 HpMegaCellPresenceMask;
	public boolean                              HpNandPresent; //BOOLEAN
	public boolean                              SideCard1Present; //BOOLEAN
	public boolean                              SideCard2Present; //BOOLEAN
	public byte                                 HpHddBackplaneDetect; //UINT8
	public byte                                 HpSecondaryHddBackplaneDetect; //UINT8
	public byte                                 HpSystemStorageConfig; //UINT8
	public boolean                              HpExpansionBladePresent; //BOOLEAN
	public boolean                              Switch1Set; //BOOLEAN
	public boolean                              Switch2Set; //BOOLEAN
	public boolean                              Switch3Set; //BOOLEAN
	public boolean                              Switch4Set; //BOOLEAN
	public boolean                              Switch5Set; //BOOLEAN
		
	public byte[]                               Reserved0 = new byte[64]; //UINT8[64]
	public byte[]                               ProductId = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; //UINT8[SMBIOS_STRING_MAX_LENGTH]
	public byte[]                               SerialNumber = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; //CHAR[SMBIOS_STRING_MAX_LENGTH]
	public byte[]                               MotherBoardSerialNumber = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; //CHAR[SMBIOS_STRING_MAX_LENGTH]
	public byte[]                               Reserved1 = new byte[128]; //UINT8[128]

	//
	// Hash for BUS0 System Board PCIe devices.
	//
	public byte[]                               SystemBoardPciSha256Hash = new byte[HpeSclData.SHA256_DIGEST_SIZE]; //UINT8[SHA256_DIGEST_SIZE]

	//
	// Hash for Embedded PCIe devices (i.e. Embedded NIC, SAS, etc).
	//
	public byte[]                               EmbeddedPciSha256Hash = new byte[HpeSclData.SHA256_DIGEST_SIZE]; //UINT8[SHA256_DIGEST_SIZE]

	//
	// Platform Certificate pre-defined strings
	//
	public byte[]                               Manufacturer = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; //CHAR[SMBIOS_STRING_MAX_LENGTH]
	public byte[]                               Model = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];   // ProLiant DL380 GEN10 //CHAR[SMBIOS_STRING_MAX_LENGTH]
	public byte[]                               Version = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // U30 //CHAR[SMBIOS_STRING_MAX_LENGTH]
	public byte[]                               Serial = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];  // Motherboard serial# //CHAR[SMBIOS_STRING_MAX_LENGTH]
	public boolean                              Fru; //BOOLEAN
	
	@Override
	public String toString() {
		return "PLATFORM_CERTIFICATE_SYSTEM_BOARD_INFO "+ 
				"\n  HpALom1Attributes=" + HpALom1Attributes + 
				"\n  HpALom2Attributes=" + HpALom2Attributes + 
				"\n  HpALom3Attributes=" + HpALom3Attributes + 
				"\n  HpALom4Attributes=" + HpALom4Attributes + 
				"\n  HpARoc1Attributes=" + HpARoc1Attributes + 
				"\n  HpARoc2Attributes=" + HpARoc2Attributes + 
				"\n  BmcAsicId=" + Long.toHexString(BmcAsicId) + // or Long.toUnsigned* 
				"\n  SystemCpldVersion=" + Integer.toHexString(SystemCpldVersion) + 
				"\n  BmcStepping=" + Integer.toHexString(BmcStepping) +
				"\n  HpPcaRevId=" + Integer.toHexString(HpPcaRevId) +
				"\n  GpioSpecVersion=" + Integer.toHexString(GpioSpecVersion) + 
				"\n  PchStepping=" + Integer.toHexString(PchStepping) + 
				"\n  HpChassisId=" + Integer.toHexString(HpChassisId) + 
				"\n  HpPlatformId=" + Integer.toHexString(HpPlatformId) + 
				"\n  HpRiserId1=" + Integer.toHexString(HpRiserId1) + 
				"\n  HpRiserId2=" + Integer.toHexString(HpRiserId2) + 
				"\n  HpRiserId3=" + Integer.toHexString(HpRiserId3) + 
				"\n  HpRiserId4=" + Integer.toHexString(HpRiserId4) + 
				"\n  MLomPresent=" + MLomPresent + 
				"\n  TpmPresent=" + TpmPresent + 
				"\n  TpmHidden=" + TpmHidden + 
				"\n  TpmType=" + Integer.toHexString(TpmType) + 
				"\n  HpMegaCellPresenceMask=" + Integer.toHexString(HpMegaCellPresenceMask) + 
				"\n  HpNandPresent=" + HpNandPresent + 
				"\n  SideCard1Present=" + SideCard1Present + 
				"\n  SideCard2Present=" + SideCard2Present + 
				"\n  HpHddBackplaneDetect=" + Integer.toHexString(HpHddBackplaneDetect) + 
				"\n  HpSecondaryHddBackplaneDetect=" + Integer.toHexString(HpSecondaryHddBackplaneDetect) + 
				"\n  HpSystemStorageConfig=" + Integer.toHexString(HpSystemStorageConfig) + 
				"\n  HpExpansionBladePresent=" + HpExpansionBladePresent + 
				"\n  Switch1Set=" + Switch1Set + 
				"\n  Switch2Set=" + Switch2Set + 
				"\n  Switch3Set=" + Switch3Set + 
				"\n  Switch4Set=" + Switch4Set + 
				"\n  Switch5Set=" + Switch5Set + 
				"\n  Reserved0=" + Utils.bytesToHex(Reserved0) + 
				"\n  ProductId=" + Utils.bytesToString(ProductId).trim() + //hpplatCert.bytesToHex(ProductId) + 
				"\n  SerialNumber=" + Utils.bytesToString(SerialNumber).trim() + 
				"\n  MotherBoardSerialNumber=" + Utils.bytesToString(MotherBoardSerialNumber).trim() + 
				"\n  Reserved1=" + Utils.bytesToHex(Reserved1) + 
				"\n  SystemBoardPciSha256Hash=" + Utils.bytesToHex(SystemBoardPciSha256Hash) + 
				"\n  EmbeddedPciSha256Hash=" + Utils.bytesToHex(EmbeddedPciSha256Hash) + 
				"\n  Manufacturer=" + Utils.bytesToString(Manufacturer).trim() + 
				"\n  Model=" + Utils.bytesToString(Model).trim() + 
				"\n  Version=" + Utils.bytesToString(Version).trim() + 
				"\n  Serial=" + Utils.bytesToString(Serial).trim() + 
				"\n  Fru=" + Fru;
	}
	
}