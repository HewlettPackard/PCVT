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

import pciIDDecode.DecodePciIds;

public class PLATFORM_CERTIFICATE_PCI_SLOT_INFO {
	//
	// Hash for DIMM info.
	//
	public byte[] PciSlotInfoHash = new byte[HpeSclData.SHA256_DIGEST_SIZE]; // UINT8[SHA256_DIGEST_SIZE]

	//
	// PCI Slot info from SMBIOS Type 9
	//
	public byte[] Model = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	public byte[] Serial = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH]; // CHAR8[SMBIOS_STRING_MAX_LENGTH]
	
	//
	// PCI Slot info from PCI Configuration Space.
	//	
	public byte[] DeviceVendorId = new byte[4]; // UINT32
	public byte[] SubDeviceVendorId = new byte[4]; // UINT32

	public String Manufacturer = ""; // It does not exist at SCL data, obtained from DeviceVendorId Vendor
	public String Vendor = ""; // obtained from DeviceVendorId 
	public String Device = ""; // obtained from DeviceVendorId 
	public String SubVendor = ""; // obtained from SubDeviceVendorId
	public String SubDevice = ""; // obtained from SubDeviceVendorId
	public String pciIdsPath;
	
	public String getVendorName (String vendorId, String pciids) {
		// Data from https://pci-ids.ucw.cz/ at the pciIDDecode package
		DecodePciIds decpci = new DecodePciIds(pciids);
		return decpci.getVendorName(vendorId);
	}
	
	public String getVendorName (String vendorId) {
		// Data from https://pci-ids.ucw.cz/ at the pciIDDecode package
		DecodePciIds decpci = new DecodePciIds();
		return decpci.getVendorName(vendorId);
	}
	
	public String getVendorManufacturer(String pciids) {
		// System.out.println("** DEBUG : " + Utils.bytesToHex_LE_32bits(new byte[]{DeviceVendorId[0],DeviceVendorId[1]}));		
		String vendorID = Utils.bytesToHex_LE_32bits(new byte[]{DeviceVendorId[0],DeviceVendorId[1]});
		if (pciids != null) {
			return getVendorName(vendorID, pciids);
		} else {
			return getVendorName(vendorID);
		}
	}
	
	public void populateVendor() {
		try {
			Vendor = Utils.bytesToHex_LE_32bits(new byte[]{DeviceVendorId[0], DeviceVendorId[1]});
		} catch (Exception e) {
			System.out.println("Error: Unable to populate PCI Vendor from DeviceVendorID");
		}
	}
	public void populateDevice() {
		try {
			Device = Utils.bytesToHex_LE_32bits(new byte[]{DeviceVendorId[2], DeviceVendorId[3]});
		} catch (Exception e) {
			System.out.println("Error: Unable to populate PCI Device from DeviceVendorID");
		}		
	}
	public void populateSubVendor() {
		try {
			SubVendor = Utils.bytesToHex_LE_32bits(new byte[]{SubDeviceVendorId[0], SubDeviceVendorId[1]});
		} catch (Exception e) {
			System.out.println("Error: Unable to populate PCI SubVendor from SubDeviceVendorID");
		}				
	}
	public void populateSubDevice() {
		try {
			SubDevice = Utils.bytesToHex_LE_32bits(new byte[]{SubDeviceVendorId[2], SubDeviceVendorId[3]});
		} catch (Exception e) {
			System.out.println("Error: Unable to populate PCI SubDevice from SubDeviceVendorID");
		}				
		
	}
	
	public String getNumericManufacturer() {
		String numManufacturer = SubVendor;
		if (numManufacturer.isEmpty()) {
			return Vendor;
		} 
		return numManufacturer;
	}
	
	public String getNumericModel() {
		String numModel = SubDevice;
		if (numModel.isEmpty()) {
			return Device;
		} 
		return numModel;
	}

	@Override
	public String toString() {
		return "\n PLATFORM_CERTIFICATE_PCI_SLOT_INFO" +
				"\n  Manufacturer=" + Manufacturer.trim() + 
				"\n  Model=" + Utils.bytesToString(Model).trim() + 
				"\n  Serial=" + Utils.bytesToString(Serial).trim() + 
				"\n  DeviceVendorId=" + 
					Utils.bytesToHex_LE_32bits(new byte[]{DeviceVendorId[0],DeviceVendorId[1]}) + ":" +
					Utils.bytesToHex_LE_32bits(new byte[]{DeviceVendorId[2],DeviceVendorId[3]}) + 
				"\n  SubDeviceVendorId=" + 
					Utils.bytesToHex_LE_32bits(new byte[]{SubDeviceVendorId[0],SubDeviceVendorId[1]}) + ":" +
					Utils.bytesToHex_LE_32bits(new byte[]{SubDeviceVendorId[2],SubDeviceVendorId[3]}) +
				"\n  Vendor=" + Vendor +
				"\n  Device=" + Device +
				"\n  SubVendor=" + SubVendor +
				"\n  SubDevice=" + SubDevice +
				"\n  PciSlotInfoHash=" + Utils.bytesToHex(PciSlotInfoHash);
	}
	
}
