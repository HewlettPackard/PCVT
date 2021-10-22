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

public class PLATFORM_CERTIFICATE_CPU_INFO {

	//
	// Hash for CPU info.
	//
	public byte[] CpuInfoHash = new byte[HpeSclData.SHA256_DIGEST_SIZE]; // UINT8[SHA256_DIGEST_SIZE]

	//
	// Platform Certificate pre-defined strings
	//
	public byte[] Socket = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] Manufacturer = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] PartNumber = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] SerialNumber = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] Version = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] AssetTag = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] ID = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] Type = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public String ID_parsed;
	
	@Override
	public String toString() {
		return "\n PLATFORM_CERTIFICATE_CPU_INFO " +
				"\n  Socket=" + Utils.bytesToString(Socket).trim() + 
				"\n  Manufacturer=" + Utils.bytesToString(Manufacturer).trim() + 
				"\n  PartNumber=" + Utils.bytesToString(PartNumber).trim() + 
				"\n  SerialNumber=" + Utils.bytesToString(SerialNumber).trim() + 
				"\n  Version=" + Utils.bytesToString(Version).trim() + 
				"\n  AssetTag=" + Utils.bytesToString(AssetTag).trim() + 
				"\n  ID=" + Utils.bytesToString(ID).trim() + 
				"\n  Type=" + Utils.bytesToString(Type).trim() + 
				"\n  CpuInfoHash=" + Utils.bytesToHex(CpuInfoHash);
	}
	
	// Parsing the ID to proper format each 32 bits hexa equivalent from LE. 
	public void parseID () {
		
		// System.out.println("1: " + Utils.bytesToString(ID).trim());		
		byte[] major = new byte[8];
		byte[] minor = new byte[8];
//		byte[] major_ = new byte[8];
//		byte[] minor_ = new byte[8];

		for (int i=0; i<8; i+=2) {
//			System.arraycopy(ID, i, major_, i, 2);
//			System.arraycopy(ID, 8+i, minor_, i, 2);			
			System.arraycopy(ID, 6-i, major, i, 2);
			System.arraycopy(ID, 8+6-i, minor, i, 2);
		}
		
		// Debugging prints
//		System.out.println("Major:     " + Utils.bytesToString(major_).trim());
//		System.out.println("Major:     " + Utils.bytesToString(major).trim());
//		System.out.println("Minor:     " + Utils.bytesToString(minor_).trim());
//		System.out.println("Minor:     " + Utils.bytesToString(minor).trim());
//		System.out.println("");
		
		ID_parsed = String.format("%s%s", 
				Utils.bytesToString(major).trim(),
				Utils.bytesToString(minor).trim());
		
	}
	
}
