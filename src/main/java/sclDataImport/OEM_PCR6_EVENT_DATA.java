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

public class OEM_PCR6_EVENT_DATA {
	public byte[]	PCR6EventManufacturerString = new byte[HpeSclData.MAX_MANUFACTURER_ASCII_STRING]; // CHAR8[MAX_MANUFACTURER_ASCII_STRING]
	public byte[]	PCR6EventGuid = new byte[16]; // EFI_GUID == 16bytes, 128 bits
	
	@Override
	public String toString() {
		return "OEM_PCR6_EVENT_DATA " + 
			   "\n PCR6EventManufacturerString=" + Utils.bytesToString(PCR6EventManufacturerString) +
			   "\n PCR6EventGuid=" + Utils.bytesToHex(PCR6EventGuid);
	}		
}