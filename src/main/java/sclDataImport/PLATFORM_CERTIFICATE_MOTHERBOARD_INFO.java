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

public class PLATFORM_CERTIFICATE_MOTHERBOARD_INFO {

	public byte[] MotherBoardSha256Hash = new byte[HpeSclData.SHA256_DIGEST_SIZE];
	public byte[] MotherBoardPciSha256Hash = new byte[HpeSclData.SHA256_DIGEST_SIZE];
	public byte[] EmbeddedPciSha256Hash = new byte[HpeSclData.SHA256_DIGEST_SIZE];
	
	public byte[] Manufacturer = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] SerialNumber = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] Version = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] Product = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] AssetTag = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] BoardType = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	
//	public byte[]                               MotherBoardSerialNumber = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; //CHAR[SMBIOS_STRING_MAX_LENGTH]
//	public byte[]                               Manufacturer = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; //CHAR[SMBIOS_STRING_MAX_LENGTH]
//	public byte[]                               Model = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];   // ProLiant DL380 GEN10 //CHAR[SMBIOS_STRING_MAX_LENGTH]
//	public byte[]                               Serial = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];  // Motherboard serial# //CHAR[SMBIOS_STRING_MAX_LENGTH]
//	public boolean                              Fru; //BOOLEAN
	
	@Override
	public String toString() {
		return "PLATFORM_CERTIFICATE_MOTHERBOARD_INFO "+ 
				"\n  Manufacturer=" + Utils.bytesToString(Manufacturer).trim() + 
				"\n  SerialNumber=" + Utils.bytesToString(SerialNumber).trim() + 
				"\n  Version=" + Utils.bytesToString(Version).trim() + 
				"\n  Product=" + Utils.bytesToString(Product).trim() + 
				"\n  AssetTag=" + Utils.bytesToString(AssetTag).trim() + 
				"\n  BoardType=" + Utils.bytesToString(BoardType).trim() + 				
				"\n  MotherBoardSha256Hash=" + Utils.bytesToHex(MotherBoardSha256Hash) +
				"\n  MotherBoardPciSha256Hash=" + Utils.bytesToHex(MotherBoardSha256Hash) +
				"\n  EmbeddedPciSha256Hash=" + Utils.bytesToHex(MotherBoardSha256Hash);

	}
	
}