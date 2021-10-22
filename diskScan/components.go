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

package main

type ComponentClass struct {
	COMPONENTCLASSREGISTRY string `json:"COMPONENTCLASSREGISTRY"`
	COMPONENTCLASSVALUE    string `json:"COMPONENTCLASSVALUE"`
}

type Component struct {
	COMPONENTCLASS   ComponentClass `json:"COMPONENTCLASS"`
	MANUFACTURER     string         `json:"MANUFACTURER"`
	MODEL            string         `json:"MODEL"`
	SERIAL           string         `json:"SERIAL"`
	REVISION         string         `json:"REVISION"`
	FIELDREPLACEABLE string         `json:"FIELDREPLACEABLE"`
}

type NvmeList struct {
	Devices []NvmeDevice `json:"Devices"`
}

type NvmeDevice struct {
	DevicePath   string `json:"DevicePath"`
	Firmware     string `json:"Firmware"`
	Index        int    `json:"Index"`
	ModelNumber  string `json:"ModelNumber"`
	ProductName  string `json:"ProductName"`
	SerialNumber string `json:"SerialNumber"`
	UsedBytes    int64  `json:"UsedBytes"`
	MaximumLBA   int    `json:"MaximumLBA"`
	PhysicalSize int64  `json:"PhysicalSize"`
	SectorSize   int    `json:"SectorSize"`
}

type PciCard struct {
	SysPath                string `json:"SysPath"`
	PciClass               string `json:"PciClass"`
	PciID                  string `json:"PciID"`
	PciSubsysID            string `json:"PciSubsysID"`
	IDModelFromDatabase    string `json:"IDModelFromDatabase"`
	IDVendorFromDatabase   string `json:"IDVendorFromDatabase"`
	IDPciClassFromDatabase string `json:"IDPciClassFromDatabase"`
}

type Platform struct {
	MANUFACTURER string `json:"PLATFORMMANUFACTURERSTR"`
	MODEL        string `json:"PLATFORMMODEL"`
	REVISION     string `json:"PLATFORMREVISION"`
	SERIAL       string `json:"PLATFORMSERIAL"`
}
