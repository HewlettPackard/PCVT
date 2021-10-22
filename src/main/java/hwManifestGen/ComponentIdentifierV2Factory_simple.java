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

public class  ComponentIdentifierV2Factory_simple {
	public ComponentIdentifierV2Factory_simple(ComponentIdentifierV2FactoryRA2 comp) {
		COMPONENTCLASS = new componentClass_(comp);
		MANUFACTURER = comp.componentManufacturer.toString();
		MODEL = comp.componentModel.toString();
	}
	
	public ComponentIdentifierV2Factory_simple() {
		// TODO Auto-generated constructor stub
	}

	public class componentClass_ {
		public String COMPONENTCLASSREGISTRY;
		public String COMPONENTCLASSVALUE;

		public componentClass_(ComponentIdentifierV2FactoryRA2 comp) {
			COMPONENTCLASSREGISTRY = comp.getComponentClass().getComponentClassRegistry().toString();
			COMPONENTCLASSVALUE = comp.getComponentClass().getComponentClassValue().toString().substring(1);
		}
	}
	
	public componentClass_ COMPONENTCLASS; 
	public String MANUFACTURER;
	public String MODEL;
	
}
