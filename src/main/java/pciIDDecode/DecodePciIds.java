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

package pciIDDecode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

//import com.github.marandus.pciid.model.Device;
import com.github.marandus.pciid.service.PciIdsDatabase;

public class DecodePciIds {

	static String pciIdFile = "d:\\temp\\scldata\\pci.ids.txt";
	String pciIdURL = "https://pci-ids.ucw.cz/v2.2/pci.ids";
	PciIdsDatabase db = null;

	public DecodePciIds() {
		LoadPciIds(pciIdFile);
	}
	
	public DecodePciIds (String pciIdPath) {
		LoadPciIds(pciIdPath);
	}
	
	public void LoadPciIds (String pciIdPath) {
		db = new PciIdsDatabase();
		try {		
			// TODO: check the file path from arguments
			//       and not this fixed path
			File f = new File(pciIdPath);
			if(f.exists() && !f.isDirectory()) { 
				FileInputStream is;
				is = new FileInputStream(pciIdPath);
				db.loadStream(is);
				// System.out.println("Loading pci ids locally");
			} else {
				db.loadRemote();
				// System.out.println("Loading pci ids remotelly");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	public String getVendorName(String vendorID) {
//		System.out.println("** DEBUG: vendorID: "+ vendorID.toLowerCase());
		return db.findVendor(vendorID.toLowerCase()).getName();
	}

	// TODO: test properly 
	public String getDevice(String part1, String part2) {
		return db.findDevice(part1.toLowerCase(), part2.toLowerCase()).toString();
	}

	// TODO: test properly 
	public String getDeviceClass(String part1) {
		return db.findDeviceClass(part1.toLowerCase()).toString();
	}

	public void test() {
		System.out.println("1: " + db.findVendor("8086").toString());
		System.out.println("2: " + db.findVendor("8086").getName());		
		System.out.println("4: " + db.findDevice("103c", "339e"));
	}

	public static void main(String[] args) {
		//PciIdsDatabase db = new PciIdsDatabase();
//		try {
//			db.loadRemote();
//			List<Device> devs = db.findAllDevices("8086");
//			for (Iterator iterator = devs.iterator(); iterator.hasNext();) {
//				Device device = (Device) iterator.next();
//				System.out.println(device.toString());
//			}
//			
//			System.out.println("Vendor 0b0b = " + db.findVendor("0b0b").getName());
//			System.out.println("Vendor 0b0b = " + db.findVendor("0b0b").getComment());
//			System.out.println("Vendor 0b0b = " + db.findVendor("0b0b").getClass());
//			System.out.println("Vendor 0b0b = " + db.findVendor("0b0b").getDevices());
//			System.out.println("Vendor 0b0b = " + db.findVendor("0b0b").);
			
//			// Load pci.ids from custom URI
//			PciIdsDatabase db = new PciIdsDatabase();
//			URI customUri = URI.create("http://example.com/pci.ids");
//			db.loadRemote(customUri);

//			// Load pci.ids from local file
//			PciIdsDatabase db = new PciIdsDatabase();
//			InputStream is = this.getClass().getClassLoader().getResourceAsStream("pci.ids");
//			db.loadStream(is);

			// Load pci.ids from local file
			
//			FileInputStream is = new FileInputStream(pciIdFile);
//			db.loadStream(is);
//			System.out.println("Vendor 0b0b = " + db.findVendor("0b0b").getName());
//			is.close();
			
			DecodePciIds decpci = new DecodePciIds();
			System.out.println("TEST: "+ decpci.getVendorName("0b0b"));
			//decpci.test();
			
			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
}
