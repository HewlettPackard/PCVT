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

public class PLATFORM_CERTIFICATE_BIOS_INFO {
	
	public byte[] Vendor = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] BiosVersion = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] BiosReleaseDate = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];	
	public byte BiosMajorRelease;
	public byte BiosMinorRelease;
	
	@Override
	public String toString() {
		return "PLATFORM_CERTIFICATE_BIOS_INFO "+ 
				"\n  Vendor=" + Utils.bytesToString(Vendor).trim() + 
				"\n  BiosVersion=" + Utils.bytesToString(BiosVersion).trim() + 
				"\n  BiosReleaseDate=" + Utils.bytesToString(BiosReleaseDate).trim() + 
				"\n  BiosMajorRelease=" + BiosMajorRelease + 
				"\n  BiosMinorRelease=" + BiosMinorRelease;

	}
	
}