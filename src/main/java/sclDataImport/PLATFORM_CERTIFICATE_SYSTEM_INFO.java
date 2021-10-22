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

public class PLATFORM_CERTIFICATE_SYSTEM_INFO {
	
	public byte[] Manufacturer = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] SerialNumber = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] ProductName = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];	
	public byte[] Version = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] UUID = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] SKU = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] Family = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	
	@Override
	public String toString() {
		return "PLATFORM_CERTIFICATE_SYSTEM_INFO "+ 
				"\n  Manufacturer=" + Utils.bytesToString(Manufacturer).trim() + 
				"\n  SerialNumber=" + Utils.bytesToString(SerialNumber).trim() + 
				"\n  ProductName=" + Utils.bytesToString(ProductName).trim() + 
				"\n  Version=" + Utils.bytesToString(Version).trim() + 
				"\n  UUID=" + Utils.bytesToString(UUID).trim() + 				
				"\n  SKU=" + Utils.bytesToString(SKU).trim() +
				"\n  Family=" + Utils.bytesToString(Family).trim();

	}
	
}