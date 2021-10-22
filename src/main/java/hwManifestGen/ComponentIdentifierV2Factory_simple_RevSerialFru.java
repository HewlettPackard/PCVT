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

package hwManifestGen;

import factory.ComponentIdentifierV2FactoryRA2;

public class  ComponentIdentifierV2Factory_simple_RevSerialFru extends ComponentIdentifierV2Factory_simple_RevSerial {
	
	public ComponentIdentifierV2Factory_simple_RevSerialFru(ComponentIdentifierV2FactoryRA2 comp) {
		super(comp);
		FIELDREPLACEABLE = comp.fieldReplaceable.isTrue()? "true" : "false";
    	
	}
	public ComponentIdentifierV2Factory_simple_RevSerialFru() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String FIELDREPLACEABLE;

    @Override
	public String toString() {
		return "ComponentIdentifierV2Factory_simple_RevSerialFru ["
				+ "\n  componentClassRegistry=" + COMPONENTCLASS.COMPONENTCLASSREGISTRY
				+ "\n  componentClassValue=" + COMPONENTCLASS.COMPONENTCLASSVALUE
				+ "\n  componentManufacturer=" + MANUFACTURER
				+ "\n  componentModel=" + MODEL
				+ "\n  componentSerial=" + SERIAL
				+ "\n  componentRevision=" + REVISION
				+ "\n  fieldReplaceable=" + FIELDREPLACEABLE + "]";
	}
}
