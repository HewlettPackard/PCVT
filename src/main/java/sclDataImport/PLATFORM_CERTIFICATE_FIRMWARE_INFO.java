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

public class PLATFORM_CERTIFICATE_FIRMWARE_INFO {
	//
	// Platform Certificate pre-defined strings
	//
	public byte[]                               Manufacturer = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // HPE // // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[]                               Model = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[]                               SerialNumber = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[]                               Revision = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	// public int 									FwTypeIndicator; //UINT16
	public byte[]								FwTypeIndicator = new byte[2]; //UINT16
	public boolean                              Fru; //BOOLEAN
	
	@Override
	public String toString() {
		return "PLATFORM_CERTIFICATE_FIRMWARE_INFO " + 
				"\n  Manufacturer=" + Utils.bytesToString(Manufacturer).trim() + 
				"\n  Model=" + Utils.bytesToString(Model).trim() + 
				"\n  SerialNumber=" + Utils.bytesToString(SerialNumber).trim() + 
				"\n  Revision=" + Utils.bytesToString(Revision).trim() +
				"\n  FwTypeIndicator=" + Utils.bytesToHex_LE_32bits(FwTypeIndicator) + 
				"\n  Fru=" + Fru; 
	}
	
}