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

public class PLATFORM_CERTIFICATE_POWER_SUPPLY_INFO {
	
	public byte[] Manufacturer = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] SerialNumber = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] AssetTag = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];	
	public byte[] PartNumber = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] DeviceName = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] Location = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] Revision = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] Hotplug = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] Present = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] Unplugged = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] VoltRange = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] Status = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	public byte[] Type = new byte[HpeSclData.SMBIOS_STRING_MAX_LENGTH];
	// public int 	  Watts; //UINT16
	public byte[] Watts = new byte[2];

	
	@Override
	public String toString() {
		return "\n PLATFORM_CERTIFICATE_POWER_SUPPLY_INFO "+ 
				"\n  Manufacturer=" + Utils.bytesToString(Manufacturer).trim() + 
				"\n  SerialNumber=" + Utils.bytesToString(SerialNumber).trim() + 
				"\n  AssetTag=" + Utils.bytesToString(AssetTag).trim() + 
				"\n  PartNumber=" + Utils.bytesToString(PartNumber).trim() + 
				"\n  DeviceName=" + Utils.bytesToString(DeviceName).trim() + 				
				"\n  Location=" + Utils.bytesToString(Location).trim() +
				"\n  Revision=" + Utils.bytesToString(Revision).trim() +
				"\n  Hotplug=" + Utils.bytesToString(Hotplug).trim() +
				"\n  Present=" + Utils.bytesToString(Present).trim() +
				"\n  Unplugged=" + Utils.bytesToString(Unplugged).trim() +
				"\n  VoltRange=" + Utils.bytesToString(VoltRange).trim() +
				"\n  Status=" + Utils.bytesToString(Status).trim() +
				"\n  Type=" + Utils.bytesToString(Type).trim() +
				"\n  Watts=" + Integer.parseInt(Utils.bytesToHex_LE_each16bits(Watts), 16);

	}
	
}