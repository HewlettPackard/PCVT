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

public class PLATFORM_CERTIFICATE_TPM_INFO {

	public byte[] VendorId = new byte[4]; // UINT32
	public byte[] SpecVersion = new byte[HpeSclData.SHA256_DIGEST_SIZE];
	public byte[] FirmwareVersion = new byte[HpeSclData.SHA256_DIGEST_SIZE];
	public byte[] Description = new byte[HpeSclData.SHA256_DIGEST_SIZE];
	public String ParsedFirmwareVersion;
		
	@Override
	public String toString() {
		return "PLATFORM_CERTIFICATE_TPM_INFO "+ 
				"\n  VendorId=" + Utils.bytesToString(VendorId).trim() + 
				"\n  SpecVersion=" + Utils.bytesToString(SpecVersion).trim() + 
				"\n  FirmwareVersion=" + Utils.bytesToString(FirmwareVersion).trim() + 
				"\n  Description=" + Utils.bytesToString(Description).trim();

	}
	
	public void parseFwVersion() {
		String[] fwVersion = Utils.bytesToString(FirmwareVersion).split("[.]");

		// where the first part of the FirmwareVersion, e.g.: 490008
		// the 4900 represents the major version in LE => 0x0049 
		// and the 08 represents the minor version 0x08 
		byte[] major = new byte[4];
		byte[] minor = new byte[2];
		System.arraycopy(FirmwareVersion, 0, major, 2, 2);
		System.arraycopy(FirmwareVersion, 2, major, 0, 2);
		System.arraycopy(FirmwareVersion, 4, minor, 0, 2);

//		// Previous logic, to be commented for now after sync with Ludo by May 26th 2020.
//		byte[] major = new byte[4];
//		byte[] minor = new byte[4];
//		// where the first part of the FirmwareVersion, e.g.: 490008
//		// the 49 represents the major version 0x0049 and 0x0008 the minor
//		if (fwVersion[0].length() == 6) {
//			System.arraycopy(new byte[] {'0', '0'}, 0, major, 0, 2);
//			System.arraycopy(FirmwareVersion, 0, major, 2, 2);
//			System.arraycopy(FirmwareVersion, 2, minor, 0, 4);
//			// System.out.println(Integer.parseInt(Utils.bytesToString(major),16));
//			// System.out.println(Integer.parseInt(Utils.bytesToString(minor),16));			
//		}
//		// where the first part of the FirmwareVersion, e.g.: 00490008
//		// the 0049 represents the major version 0x0049 and 0x0008 the minor
//		if (fwVersion[0].length() == 8) {
//			System.arraycopy(FirmwareVersion, 0, major, 0, 4);
//			System.arraycopy(FirmwareVersion, 2, minor, 0, 4);
//			// System.out.println(Integer.parseInt(Utils.bytesToString(major),16));
//			// System.out.println(Integer.parseInt(Utils.bytesToString(minor),16));
//		}
		
		ParsedFirmwareVersion = String.format("%s.%s.%s", 
				Integer.parseInt(Utils.bytesToString(major),16),
				Integer.parseInt(Utils.bytesToString(minor),16),
				fwVersion[1].trim());
		
	}
	
}