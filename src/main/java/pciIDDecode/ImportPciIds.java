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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImportPciIds {

	String pciIdFile = "d:\\temp\\scldata\\pci.ids";
	String pciIdURL = "https://pci-ids.ucw.cz/v2.2/pci.ids";
	String pciIdContent = null;
	String pciIdVendorsContent = null;
	
	public ImportPciIds (){
		// Empty constructor
	}
	
	public void getPciIdContent ( ) {
		try {
			URL url = new URL(pciIdURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");			
			
            conn.setDoOutput(true);
			if (conn.getResponseCode() < 200 && conn.getResponseCode() > 300) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
		
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		
			 String output;
			 BufferedWriter writer = new BufferedWriter(new FileWriter(pciIdFile));
			 while ((output = br.readLine()) != null) {
			 	System.out.println(output);		
			 	// writer.write(output);
			 }
			
			// Writing to file with the proper line breaks
//			PrintWriter writer = new PrintWriter(new FileWriter(outTarget.get(i)));
//			br.lines().forEach(line -> writer.println(line));
//			
			System.out.printf("Platform certificate successfully written to %s\n", pciIdFile);
			writer.close();
			conn.disconnect();
			
		} catch (MalformedURLException e) {
			// e.printStackTrace();
			System.out.println("Error on URL address. " + e.getCause());
		} catch (IOException e) {					
			// e.printStackTrace();
			System.out.printf("Error on acessing file/address.\n");					
		}
	}
	
	public void filterPciIdVendors() {
		// TODO
	}
	
	
}
