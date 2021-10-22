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

public class PLATFORM_CERTIFICATE_DIMM_INFO {
	//
	// Hash for DIMM info.
	//
	public byte[] DimmInfoHash = new byte[HpeSclData.SHA256_DIGEST_SIZE]; // UINT8[SHA256_DIGEST_SIZE]

	//
	// Platform Certificate pre-defined strings
	//
	public byte[] Manufacturer = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] Type = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] SerialNumber = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] Device = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] Bank = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] AssetTag = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] PartNumber = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	//public int    Size;
	public byte[] Size = new byte[2];

	@Override
	public String toString() {
		return "\n PLATFORM_CERTIFICATE_DIMM_INFO " +
				"\n  Manufacturer=" + Utils.bytesToString(Manufacturer).trim() + 
				"\n  Type=" + Utils.bytesToString(Type).trim() + 
				"\n  SerialNumber=" + Utils.bytesToString(SerialNumber).trim() + 
				"\n  Device=" + Utils.bytesToString(Device).trim() + 
				"\n  Bank=" + Utils.bytesToString(Bank).trim() + 
				"\n  AssetTag=" + Utils.bytesToString(AssetTag).trim() + 
				"\n  PartNumber=" + Utils.bytesToString(PartNumber).trim() + 
				"\n  Size=" + Integer.parseInt(Utils.bytesToHex_LE_each16bits(Size), 16) + 
				"\n  DimmInfoHash=" + Utils.bytesToHex(DimmInfoHash);
	}
	
}