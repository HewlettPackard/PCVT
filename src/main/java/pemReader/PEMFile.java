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

package pemReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PEMFile {
	Pattern pemBeginPattern = Pattern.compile("-+BEGIN.*?CERTIFICATE-+");
	Pattern pemEndPattern = Pattern.compile("-+END.*?CERTIFICATE-+");
	String fileName;
	File pemFile;
	
	public PEMFile(File pemFile) {
		this.pemFile = pemFile;
	}
	
	/**
	 * Splits a CA bundle into individual PEM certificate blocks
	 */
	public ArrayList<String> getPEMBlocks() throws IOException {
		FileReader fr = new FileReader(pemFile);
		BufferedReader pemFileBR = new BufferedReader(fr);
		ArrayList<String> result = new ArrayList<String>();
		
		try {
			Boolean inBlock = false;
			String pemBlock = null;
			while (pemFileBR.ready()) {
				String line = pemFileBR.readLine();
					
				if (!inBlock) {
					// look for the block begin
					Matcher beginPatternMatcher = pemBeginPattern.matcher(line);
					if (beginPatternMatcher.find()) {
						inBlock = true;
						pemBlock = "";
					}				
				} else {
					Matcher endPatternMatcher = pemEndPattern.matcher(line);
					if (endPatternMatcher.find()) {
						result.add(pemBlock);
						inBlock = false;
					} else {
						pemBlock += line;
					}
				}
			}
		} finally {
			pemFileBR.close();
		}
		return result;
	}

}
