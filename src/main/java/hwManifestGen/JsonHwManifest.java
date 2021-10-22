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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
//import java.math.BigInteger;
//import java.nio.ByteBuffer;
import java.util.ArrayList;

//import org.apache.commons.lang3.SystemUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
//import org.bouncycastle.asn1.ASN1OctetString;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
//import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import devicesScan.NativeDiskScan;
import devicesScan.NativePciCardsScan;
import devicesScan.NativePlatformScan;
import devicesScan.NativeSmbiosScan;
//import factory.ComponentIdentifierFactory;
import factory.ComponentIdentifierV2FactoryRA2;
import factory.PlatformConfigurationV2Factory;
//import pciIDDecode.DecodePciIds;
//import factory.URIReferenceFactory;
import sclDataImport.HpeSclData;
import sclDataImport.PLATFORM_CERTIFICATE_CPU_INFO;
import sclDataImport.PLATFORM_CERTIFICATE_DIMM_INFO;
import sclDataImport.PLATFORM_CERTIFICATE_PCI_SLOT_INFO;
import sclDataImport.PLATFORM_CERTIFICATE_POWER_SUPPLY_INFO;
import sclDataImport.Utils;
//import tcg.credential.AttributeStatus;
//import tcg.credential.ComponentAddress;
import tcg.credential.ComponentClass;

public class JsonHwManifest {
	
	public HpeSclData platcert;
    public String pciIdsPath;
	public ArrayList<ComponentIdentifierV2Factory_simple_RevSerialFru> smbiosComponents;
	public boolean fromSmbios;
    
    public JsonHwManifest(String pciIds) {
		platcert = new HpeSclData(pciIds);
		pciIdsPath = pciIds;
	}
    public JsonHwManifest() {
		platcert = new HpeSclData();
		pciIdsPath = null; // TODO: threat this situation
	}
    	
    public void populateComponent(ComponentIdentifierV2FactoryRA2 c, 
    		ArrayList<ComponentIdentifierV2Factory_simple> comps_simple) {        
		String serial = c.getComponentSerial();
		String revision = c.getComponentRevision();

		if (serial.equals(Components.NOT_SPECIFIED) && revision.equals(Components.NOT_SPECIFIED) && 
		    !c.fieldReplaceable.isTrue()) {
        	comps_simple.add(new ComponentIdentifierV2Factory_simple(c));

		} else if (serial.equals(Components.NOT_SPECIFIED) && !c.fieldReplaceable.isTrue()) {
            comps_simple.add(new ComponentIdentifierV2Factory_simple_Rev(c));

		} else if (revision.equals(Components.NOT_SPECIFIED) && !c.fieldReplaceable.isTrue()) {
        	comps_simple.add(new ComponentIdentifierV2Factory_simple_Serial(c));

		} else if (!c.fieldReplaceable.isTrue()){ 
			// Valid revision and serial, no FRU
        	comps_simple.add(new ComponentIdentifierV2Factory_simple_RevSerial(c));

        	
		} else if (serial.equals(Components.NOT_SPECIFIED) && revision.equals(Components.NOT_SPECIFIED) && 
		    c.fieldReplaceable.isTrue()) {
        	comps_simple.add(new ComponentIdentifierV2Factory_simpleFru(c));

		} else if (serial.equals(Components.NOT_SPECIFIED) && c.fieldReplaceable.isTrue()) {
            comps_simple.add(new ComponentIdentifierV2Factory_simple_RevFru(c));

		} else if (revision.equals(Components.NOT_SPECIFIED) && c.fieldReplaceable.isTrue()) {
        	comps_simple.add(new ComponentIdentifierV2Factory_simple_SerialFru(c));

		} else { 
			// Valid revision and serial
        	comps_simple.add(new ComponentIdentifierV2Factory_simple_RevSerialFru(c));
		}

	}
    
	public String genHwManifest(String inputFile, String intermediateOutputFile, 
			boolean fromEviVars, boolean scanDisks, boolean smbios) throws IOException {
		
		smbiosComponents = new ArrayList<ComponentIdentifierV2Factory_simple_RevSerialFru>();
		fromSmbios = smbios;
		if( fromSmbios == false) {
			if (intermediateOutputFile != null) {
				platcert.parseSCLData(inputFile, intermediateOutputFile, fromEviVars);
			}	 
			else {			
				platcert.parseSCLData(inputFile, fromEviVars);
			}
		}
		else {
			getSmbiosComponents(smbiosComponents);
		}

        PlatformConfigurationV2Factory plat = PlatformConfigurationV2Factory.create();

        ///////////////////
        // Platform section
        ///////////////////
        PlatformIdentifier_simple platform = new PlatformIdentifier_simple();
		if(fromSmbios) {
                        platform = getSmbiosPlatformInfo();
                        platform.PLATFORMMANUFACTURERSTR = platform.PLATFORMMANUFACTURERSTR != null? platform.PLATFORMMANUFACTURERSTR : "";
                        platform.PLATFORMMODEL = platform.PLATFORMMODEL != null? platform.PLATFORMMODEL  : "";
                        platform.PLATFORMVERSION = platform.PLATFORMVERSION != null? platform.PLATFORMVERSION : "";
                        platform.PLATFORMSERIAL = platform.PLATFORMSERIAL != null? platform.PLATFORMSERIAL : "";

		} else {
        	platform.PLATFORMMANUFACTURERSTR = Utils.bytesToString(platcert.SystemInfo.Manufacturer).trim();
        	platform.PLATFORMMODEL = Utils.bytesToString(platcert.SystemInfo.ProductName).trim();
        	platform.PLATFORMVERSION = Utils.bytesToString(platcert.SystemInfo.Version).trim();
        	// platform.PLATFORMVERSION = Utils.bytesToString(platcert.SystemInfo.Family).trim() + " " +
        	// 							  Utils.bytesToString(platcert.SystemInfo.SKU).trim();
        	platform.PLATFORMSERIAL = Utils.bytesToString(platcert.SystemInfo.SerialNumber).trim();
		}

        //////////////////////
        // Components section
        //////////////////////
        ArrayList<ComponentIdentifierV2FactoryRA2> comps = new ArrayList<ComponentIdentifierV2FactoryRA2>();
        ArrayList<ComponentIdentifierV2Factory_simple> comps_simple = new ArrayList<ComponentIdentifierV2Factory_simple>();

        // The add*Info commented methods are removed from the Hw Manifest temporarily 
        // as it was agreed with Jacquim Ludovic by during May 2020. 

        addChassisInfo(comps, plat);
        addMotherboardInfo(comps, plat);
        addBiosInfo(comps, plat);
        // addBackupBiosInfo(comps, plat);
        // addApmlInfo(comps, plat);
        // addMegaCellInfo(comps, plat);
        // addNvmeInfo(comps, plat);
        // addIntelligentProvisioningInfo(comps, plat);
        // addVgaInfo(comps, plat);
        // addPowerPicInfo(comps, plat);
        // addPowerPicBootLoaderInfo(comps, plat);
        // addCpldInfo(comps, plat);
        // addIntelMeInfo(comps, plat);
        // addIntelMeSpiInfo(comps, plat);
        // addIntelIeInfo(comps, plat);
        addTpmInfo(comps, plat);        
        addBmcInfo(comps, plat);
        addCPUInfo(comps, plat);
        addDIMMInfo(comps, plat);
		
        // For debugging purposes
        boolean scanPciCards = true; 
        
        if (scanPciCards == true) {	
        	// By default for now choosing for scanning the PCI cards at the OS level.
        	// TODO: match the information we have from the OS level with the one 
        	//       retrieved from the SCL data.
        	boolean scanPCIFromSCL = false;
        	boolean scanPCIFromOS = !scanPCIFromSCL;
        	
        	if (scanPCIFromSCL == true) {
                addPCISlotInfo(comps, plat);        		
        	} else if (scanPCIFromOS == true) {
        		try {
        			addPciCards(comps, plat);
        		} catch (Error e) { // for dealing with UnsatisfiedLinkError
            		System.err.println(e.toString());
                	System.err.println("Error: unable to locate libdiskscan.so library. PCI Card information is not being retrieved");
            	} catch (Exception e) {
            		System.err.println(e.toString());
                	System.err.println("Exception: unable to retrieve PCI Cards information");        		
            	}
            } else {
            	System.err.println("Warning: PCI Cards information is not being retrieved");
            }
        }
        
        addPowerSupplyInfo(comps, plat);
        
        for (ComponentIdentifierV2FactoryRA2 c : comps) {
        	
        	// Here the correct class should be instantiated to handle each component
        	// according to its parameters (if having or not, revision, serial, fru, etc.
        	// This is being handled inside the populateComponent() call below.
        	// If we need any further special case, we can go for a specific check here. E.g.:
        	// 
        	// if (c.getComponentClass().getComponentClassValue().toString().substring(1).
        	// 		equals(Components.COMPCLASS_SERVER_CHASSIS)) {
        	//		populateComponent(c, comps_simple);        		
        	// }
        	
    		populateComponent(c, comps_simple);        		

        }        

        // Adding disks information
        if (scanDisks == true) {
        	try {
        		addDisks(comps_simple);
        		
        	} catch (Error e) { // for dealing with UnsatisfiedLinkError
        		System.err.println(e.toString());
            	System.err.println("Error: unable to locate libdiskscan.so library. Disks information is not being retrieved");
        	} catch (Exception e) {
        		System.err.println(e.toString());
            	System.err.println("Exception: unable to retrieve disks information");        		
        	}
        } else {
        	System.err.println("Warning: Disks information is not being retrieved");
        }
        
        //////////////////////
        // Properties section
	// only have this with SCL data
        //////////////////////
       	ArrayList<PlatformProperties_simple> prop = new  ArrayList<PlatformProperties_simple>();
		if( !fromSmbios ) {
	
        
        // TODO: add whatever properties are necessary here
	        prop.add(new PlatformProperties_simple("SKU", Utils.bytesToString(platcert.SystemInfo.SKU).trim()));
   		    prop.add(new PlatformProperties_simple("CertificateRevisionGuid", Utils.bytesToHex(platcert.CertificateRevisionGuid).trim()));
   	    	prop.add(new PlatformProperties_simple("MotherBoardSha256Hash", Utils.bytesToHex(platcert.MotherBoardInfo.MotherBoardSha256Hash).trim()));
   	     	prop.add(new PlatformProperties_simple("EmbeddedPciSha256Hash", Utils.bytesToHex(platcert.MotherBoardInfo.EmbeddedPciSha256Hash).trim()));
        
        	for (PLATFORM_CERTIFICATE_CPU_INFO cpu : platcert.CpuInfo) {
        		if (!Utils.bytesToString(cpu.Manufacturer).trim().isEmpty()) {
        			prop.add(new PlatformProperties_simple( String.format("%s,%s,%s", 
        					Utils.bytesToString(cpu.Manufacturer).trim(), Utils.bytesToString(cpu.Type).trim(),
        					Utils.bytesToString(cpu.SerialNumber).trim()), Utils.bytesToHex(cpu.CpuInfoHash).trim()));		            
        		}
        	}

        	for (PLATFORM_CERTIFICATE_DIMM_INFO dimm : platcert.DimmInfo) {
        		if (!Utils.bytesToString(dimm.Manufacturer).trim().isEmpty()) {
        			prop.add(new PlatformProperties_simple( String.format("%s,%s,%s", 
        					Utils.bytesToString(dimm.Manufacturer).trim(), Utils.bytesToString(dimm.Type).trim(),
        					Utils.bytesToString(dimm.SerialNumber).trim()), Utils.bytesToHex(dimm.DimmInfoHash).trim()));
        		}
        	}
		}  else {
                        // The HardwareManifestVersion 1.0 is the first versioned Hardware Manifest,
                        // in this case with SMBIOS data. Future PCVT versions will track this info.
                        // For a first time check, if no HardwareManifestVersion is found, the first SCL
                        // format will be checked, while 1.00 will point to the first SMBIOS format.
                        prop.add(new PlatformProperties_simple("HPE_HardwareManifestVersion", "1.00"));
                }

        // The PCI slots are being retrieved from the OS level through libdiskscan.so
        // This means no Serial number or PciSlotInfoHash fields are available. 
        // Next: we can match the OS info and the SCL data based on DeviceVendorID and SubDeviceVendorId later
        //       and add the properties related to it.
//        for (PLATFORM_CERTIFICATE_PCI_SLOT_INFO pcislot : platcert.PciSlotInfo) {
//        	if (!pcislot.Manufacturer.trim().isEmpty()) {
//        		prop.add(new PlatformProperties_simple( String.format("%s,%s,%s", 
//        				pcislot.Manufacturer.trim(), Utils.bytesToString(pcislot.Model).trim(),
//        				Utils.bytesToString(pcislot.Serial).trim()), Utils.bytesToHex(pcislot.PciSlotInfoHash).trim()));
//        	}
//        }
        
        
        PlatformHwManifest_simple hwManifest = new PlatformHwManifest_simple(platform, comps_simple, prop);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        String jsonHwManifest ;
        jsonHwManifest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(hwManifest);
        // jsonHwManifest = objectMapper.writeValueAsString(hwManifest);
        
        return jsonHwManifest ;
        
    	// For reference, the comments below show the json mount process of Paccor tool 
    	//
        // Gather baseboard details
        // Gather BIOS details
        // Gather platform details for the subject alternative name. If not available, set to $NOT_SPECIFIED"
        //  platformManufacturer=$(dmidecode -s system-manufacturer)
        //  platformModel=$(dmidecode -s system-product-name)
        //  platformVersion=$(dmidecode -s system-version)
        //  platformSerial=$(dmidecode -s system-serial-number)
        //  platform=$(jsonPlatformObject "$platformManufacturer" "$platformModel" "$platformVersion" "$platformSerial")

        // Gather component details
        // Gather baseboard details
        // Gather BIOS details
        // Collate the component details
        //  componentsCPU=$(parseCpuData)
        //  componentsRAM=$(parseRamData)
        //  componentsNIC=$(parseNicData)
        //  componentsHDD=$(parseHddData)
        //  componentArray=$(jsonComponentArray "$componentChassis" "$componentBaseboard" "$componentBiosUefi" "$componentsCPU" "$componentsRAM" "$componentsNIC" "$componentsHDD")
        //
        // Collate the property details
        //  propertyArray=$(jsonPropertyArray "$property1" "$property2")
        // Construct the final JSON object
        //  FINAL_JSON_OBJECT=$(jsonIntermediateFile "$platform" "$componentArray" "$propertyArray")
    }

	public void addChassisInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
		if( fromSmbios ) {
			for ( ComponentIdentifierV2Factory_simple_RevSerialFru comp : smbiosComponents) {
				if(comp.COMPONENTCLASS.COMPONENTCLASSVALUE.equals(Components.COMPCLASS_SERVER_CHASSIS)) {
					if(!comp.MANUFACTURER.isEmpty()){
						comps.add(ComponentIdentifierV2FactoryRA2.create());        
	    				comps.get(comps.size()-1)
	    					.componentClass(new ComponentClass(new ASN1ObjectIdentifier(
	    							Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_SERVER_CHASSIS)) 
		        			.componentManufacturer(comp.MANUFACTURER.trim())
		        			.componentModel(comp.MODEL.trim()) 
		        			.componentSerial(comp.SERIAL.trim().isEmpty() != true? 
		        					comp.SERIAL.trim() : null)
		        			.componentRevision(null);
	    				plat.addComponent(comps.get(comps.size()-1).build());
					}
				}
			}

		} else {
			if (!Utils.bytesToString(platcert.ChassisInfo.Manufacturer).trim().isEmpty()) {
	    		comps.add(ComponentIdentifierV2FactoryRA2.create());        
	    		comps.get(comps.size()-1)
	    			.componentClass(new ComponentClass(new ASN1ObjectIdentifier(
	    					Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_SERVER_CHASSIS)) 
		        	.componentManufacturer(Utils.bytesToString(platcert.ChassisInfo.Manufacturer).trim())
		        	.componentModel(Utils.bytesToString(platcert.ChassisInfo.Type).trim()) 
		        	.componentSerial(Utils.bytesToString(platcert.ChassisInfo.SerialNumber).trim().isEmpty() != true? 
		        			Utils.bytesToString(platcert.ChassisInfo.SerialNumber).trim() : null)
		        	.componentRevision(null);
	    		plat.addComponent(comps.get(comps.size()-1).build());
			}
     	}
    }

    public void addMotherboardInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
     	if( fromSmbios ) {
			for ( ComponentIdentifierV2Factory_simple_RevSerialFru comp : smbiosComponents) {
				if(comp.COMPONENTCLASS.COMPONENTCLASSVALUE.equals(Components.COMPCLASS_MOTHERBOARD)) {
					comps.add(ComponentIdentifierV2FactoryRA2.create());        
					comps.get(comps.size()-1)
						.componentClass(new ComponentClass(new ASN1ObjectIdentifier(
							Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_MOTHERBOARD)) 
						.componentManufacturer(comp.MANUFACTURER.trim())
						.componentModel(comp.MODEL.trim()) 
						.componentSerial(comp.SERIAL.trim())
						.componentRevision(comp.REVISION.trim());
					plat.addComponent(comps.get(comps.size()-1).build());
				}
			}
			
		} else { 
			if (!Utils.bytesToString(platcert.MotherBoardInfo.Manufacturer).trim().isEmpty()) {
	        	comps.add(ComponentIdentifierV2FactoryRA2.create());
	        	comps.get(comps.size()-1)
		        	.componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        			Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_MOTHERBOARD)) 
		        	.componentManufacturer(Utils.bytesToString(platcert.MotherBoardInfo.Manufacturer).trim())
		        	.componentModel(Utils.bytesToString(platcert.MotherBoardInfo.Product).trim())
		        	.componentSerial(Utils.bytesToString(platcert.MotherBoardInfo.SerialNumber).trim())
	        		.componentRevision(Utils.bytesToString(platcert.MotherBoardInfo.Version).trim());
	        	plat.addComponent(comps.get(comps.size()-1).build());
			}
     	}
    }

    public void addBackupBiosInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
    	if (!Utils.bytesToString(platcert.BackupBiosInfo.Manufacturer).trim().isEmpty()) { 
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_UEFI)) //TODO: map this better
		        .componentManufacturer(Utils.bytesToString(platcert.BackupBiosInfo.Manufacturer).trim())
		        .componentModel(Utils.bytesToString(platcert.BackupBiosInfo.Model).trim())
		        .componentRevision(Utils.bytesToString(platcert.BackupBiosInfo.Revision).trim());
	        plat.addComponent(comps.get(comps.size()-1).build());
    	}
    }

    public void addBmcInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        // BmcInfo
    	if( fromSmbios ) {
			for ( ComponentIdentifierV2Factory_simple_RevSerialFru comp : smbiosComponents) {
				if(comp.COMPONENTCLASS.COMPONENTCLASSVALUE.equals(Components.COMPCLASS_BMC)) {
					comps.add(ComponentIdentifierV2FactoryRA2.create());        
	    			comps.get(comps.size()-1)
	    				.componentClass(new ComponentClass(new ASN1ObjectIdentifier(
	    						Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_BMC)) 
		        		.componentManufacturer(comp.MANUFACTURER.trim())
		        		.componentModel(comp.MODEL.trim()) 
		        		.componentRevision(comp.REVISION.trim());
	    			plat.addComponent(comps.get(comps.size()-1).build());
				}
			}
		}
		else {
			if (!Utils.bytesToString(platcert.BmcInfo.Manufacturer).trim().isEmpty()) { 
        		comps.add(ComponentIdentifierV2FactoryRA2.create());
        		comps.get(comps.size()-1)
	    	    	.componentClass(new ComponentClass(new ASN1ObjectIdentifier(
	    		    		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_BMC)) 
	    		    .componentManufacturer(Utils.bytesToString(platcert.BmcInfo.Manufacturer).trim())
	    		    .componentModel(Utils.bytesToString(platcert.BmcInfo.Model).trim())
	    		    .componentRevision(Utils.bytesToString(platcert.BmcInfo.Revision).trim());
        		plat.addComponent(comps.get(comps.size()-1).build());  
			}  	
    	}
    }
    
    public void addApmlInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        // ApmlInfo 
    	if (!Utils.bytesToString(platcert.ApmlInfo.Manufacturer).trim().isEmpty()) { 
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_UEFI)) //TODO: map this better
		        .componentManufacturer(Utils.bytesToString(platcert.ApmlInfo.Manufacturer).trim())
		        .componentModel(Utils.bytesToString(platcert.ApmlInfo.Model).trim())
		        .componentRevision(Utils.bytesToString(platcert.ApmlInfo.Revision).trim());
	        plat.addComponent(comps.get(comps.size()-1).build());
    	}
    }
    
    public void addMegaCellInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        // MegaCellInfo
    	if (!Utils.bytesToString(platcert.MegaCellInfo.Manufacturer).trim().isEmpty()) { 
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_UEFI)) //TODO: map this better
		        .componentManufacturer(Utils.bytesToString(platcert.MegaCellInfo.Manufacturer).trim())
		        .componentModel(Utils.bytesToString(platcert.MegaCellInfo.Model).trim())
		        .componentRevision(Utils.bytesToString(platcert.MegaCellInfo.Revision).trim());
	        plat.addComponent(comps.get(comps.size()-1).build());
    	}
    }
    
    public void addNvmeInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        // NvmeInfo 
    	if (!Utils.bytesToString(platcert.NvmeInfo.Manufacturer).trim().isEmpty()) { 
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_BIOS)) //TODO: map this better
		        .componentManufacturer(Utils.bytesToString(platcert.NvmeInfo.Manufacturer).trim())
		        .componentModel(Utils.bytesToString(platcert.NvmeInfo.Model).trim())
		        .componentRevision(Utils.bytesToString(platcert.NvmeInfo.Revision).trim());
	        plat.addComponent(comps.get(comps.size()-1).build());
    	}
    }

    public void addIntelligentProvisioningInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        // IntelligentProvisioningInfo = ComponentIdentifierV2Factory.create();
	    if (!Utils.bytesToString(platcert.IntelligentProvisioningInfo.Manufacturer).trim().isEmpty()) { 
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_BIOS)) //TODO: map this better
		        .componentManufacturer(Utils.bytesToString(platcert.IntelligentProvisioningInfo.Manufacturer).trim())
		        .componentModel(Utils.bytesToString(platcert.IntelligentProvisioningInfo.Model).trim())
		        .componentRevision(Utils.bytesToString(platcert.IntelligentProvisioningInfo.Revision).trim());
	        plat.addComponent(comps.get(comps.size()-1).build());
    	}
    }
    
    public void addVgaInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        // VgaInfo = ComponentIdentifierV2Factory.create();
	    if (!Utils.bytesToString(platcert.VgaInfo.Manufacturer).trim().isEmpty()) { 
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_BIOS)) //TODO: map this better
		        .componentManufacturer(Utils.bytesToString(platcert.VgaInfo.Manufacturer).trim())
		        .componentModel(Utils.bytesToString(platcert.VgaInfo.Model).trim())
		        .componentRevision(Utils.bytesToString(platcert.VgaInfo.Revision).trim());
	        plat.addComponent(comps.get(comps.size()-1).build());
    	}
    }

    public void addPowerPicInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
    	if (!Utils.bytesToString(platcert.PowerPicInfo.Manufacturer).trim().isEmpty()) { 
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_BIOS)) //TODO: map this better
		        .componentManufacturer(Utils.bytesToString(platcert.PowerPicInfo.Manufacturer).trim())
		        .componentModel(Utils.bytesToString(platcert.PowerPicInfo.Model).trim())
		        .componentRevision(Utils.bytesToString(platcert.PowerPicInfo.Revision).trim());
	        plat.addComponent(comps.get(comps.size()-1).build());
    	}
    }
    
    public void addPowerPicBootLoaderInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
    	if (!Utils.bytesToString(platcert.PowerPicBootLoaderInfo.Manufacturer).trim().isEmpty()) { 
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_BIOS)) //TODO: map this better
		        .componentManufacturer(Utils.bytesToString(platcert.PowerPicBootLoaderInfo.Manufacturer).trim())
		        .componentModel(Utils.bytesToString(platcert.PowerPicBootLoaderInfo.Model).trim())
		        .componentRevision(Utils.bytesToString(platcert.PowerPicBootLoaderInfo.Revision).trim());
	        plat.addComponent(comps.get(comps.size()-1).build());
    	}
    }
    
    public void addCpldInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
    	if (!Utils.bytesToString(platcert.CpldInfo.Manufacturer).trim().isEmpty()) { 
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_UEFI)) //TODO: map this better
		        .componentManufacturer(Utils.bytesToString(platcert.CpldInfo.Manufacturer).trim())
		        .componentModel(Utils.bytesToString(platcert.CpldInfo.Model).trim())
		        .componentRevision(Utils.bytesToString(platcert.CpldInfo.Revision).trim());
	        plat.addComponent(comps.get(comps.size()-1).build());
    	}
    }
    
    public void addTpmInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
    	// TODO: check for translating VendorId as done at the 
    	if( fromSmbios ) {
			for ( ComponentIdentifierV2Factory_simple_RevSerialFru comp : smbiosComponents) {
				if(comp.COMPONENTCLASS.COMPONENTCLASSVALUE.equals(Components.COMPCLASS_TPM)) {
					comps.add(ComponentIdentifierV2FactoryRA2.create());        
	    			comps.get(comps.size()-1)
	    				.componentClass(new ComponentClass(new ASN1ObjectIdentifier(
	    						Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_TPM)) 
		        		.componentManufacturer(comp.MANUFACTURER)
		        		.componentModel(comp.MODEL.trim()) 
		        		.componentRevision(comp.REVISION.trim());
	    			plat.addComponent(comps.get(comps.size()-1).build());
				}
			}
			
		} else {
			if (!Utils.bytesToString(platcert.TpmInfo.VendorId).trim().isEmpty()) { 
	    	    comps.add(ComponentIdentifierV2FactoryRA2.create());
	    	    comps.get(comps.size()-1)
			        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
			        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_TPM))
			        .componentManufacturer(Utils.bytesToString(platcert.TpmInfo.VendorId))
			        .componentModel(Utils.bytesToString(platcert.TpmInfo.Description).trim())
			        .componentRevision(platcert.TpmInfo.ParsedFirmwareVersion);
	    	    plat.addComponent(comps.get(comps.size()-1).build());
			}
    	}
    }    
    
    public void addIntelMeInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        // IntelMeInfo = ComponentIdentifierV2Factory.create();
    	if (!Utils.bytesToString(platcert.IntelMeInfo.Manufacturer).trim().isEmpty()) { 
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_UEFI)) //TODO: map this better
		        .componentManufacturer(Utils.bytesToString(platcert.IntelMeInfo.Manufacturer).trim())
		        .componentModel(Utils.bytesToString(platcert.IntelMeInfo.Model).trim())
		        .componentRevision(Utils.bytesToString(platcert.IntelMeInfo.Revision).trim());
	        plat.addComponent(comps.get(comps.size()-1).build());
    	}
    }
    
    public void addIntelMeSpiInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        // IntelMeSPiInfo = ComponentIdentifierV2Factory.create();
    	if (!Utils.bytesToString(platcert.IntelMeSpiInfo.Manufacturer).trim().isEmpty()) { 
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_UEFI)) //TODO: map this better
		        .componentManufacturer(Utils.bytesToString(platcert.IntelMeSpiInfo.Manufacturer).trim())
		        .componentModel(Utils.bytesToString(platcert.IntelMeSpiInfo.Model).trim())
		        .componentRevision(Utils.bytesToString(platcert.IntelMeSpiInfo.Revision).trim());
	        plat.addComponent(comps.get(comps.size()-1).build());
    	}
    }
    
    public void addBiosInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
     	if( fromSmbios ) {
			for ( ComponentIdentifierV2Factory_simple_RevSerialFru comp : smbiosComponents) {
				if(comp.COMPONENTCLASS.COMPONENTCLASSVALUE.equals(Components.COMPCLASS_UEFI)) {
					comps.add(ComponentIdentifierV2FactoryRA2.create());        
	    			comps.get(comps.size()-1)
	    				.componentClass(new ComponentClass(new ASN1ObjectIdentifier(
	    						Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_UEFI)) 
		        		.componentManufacturer(comp.MANUFACTURER.trim())
		        		.componentModel(comp.MODEL.trim()) 
		        		.componentRevision(comp.REVISION.trim());
	    			plat.addComponent(comps.get(comps.size()-1).build());
				}
			}
		}
		else{
			if (!Utils.bytesToString(platcert.BiosInfo.Vendor).trim().isEmpty()) {
	    	    comps.add(ComponentIdentifierV2FactoryRA2.create());
	    	    comps.get(comps.size()-1)
			        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
			        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_UEFI)) 
			        .componentManufacturer(Utils.bytesToString(platcert.BiosInfo.Vendor).trim())
			        .componentModel(Utils.bytesToString(platcert.BiosInfo.BiosVersion).trim())
			        .componentRevision(platcert.BiosInfo.BiosMajorRelease + "." + 
			        				   platcert.BiosInfo.BiosMinorRelease);
	    	    plat.addComponent(comps.get(comps.size()-1).build());
			}
     	}
    }


    public void addIntelIeInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        // IntelIeInfo = ComponentIdentifierV2Factory.create();
    	if (!Utils.bytesToString(platcert.IntelIeInfo.Manufacturer).trim().isEmpty()) { 
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_UEFI)) //TODO: map this better
		        .componentManufacturer(Utils.bytesToString(platcert.IntelIeInfo.Manufacturer).trim())
		        .componentModel(Utils.bytesToString(platcert.IntelIeInfo.Model).trim())
		        .componentRevision(Utils.bytesToString(platcert.IntelIeInfo.Revision).trim());
	        plat.addComponent(comps.get(comps.size()-1).build());
    	}
    }
    
    public void addCPUInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        //         
     	if( fromSmbios ) {
			for ( ComponentIdentifierV2Factory_simple_RevSerialFru comp : smbiosComponents) {
				if(comp.COMPONENTCLASS.COMPONENTCLASSVALUE.equals(Components.COMPCLASS_CPU)) {
					comps.add(ComponentIdentifierV2FactoryRA2.create());        
	    			comps.get(comps.size()-1)
	    				.componentClass(new ComponentClass(new ASN1ObjectIdentifier(
	    						Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_CPU)) 
		        		.componentManufacturer(comp.MANUFACTURER.trim())
		        		.componentModel(comp.MODEL.trim()) 
		        		.componentRevision(comp.REVISION.trim())
		        		.componentSerial(comp.SERIAL.trim())
						.fieldReplaceable(true);
	    			plat.addComponent(comps.get(comps.size()-1).build());
				}
			}

		} else {
			for (PLATFORM_CERTIFICATE_CPU_INFO cpu : platcert.CpuInfo) {
        		if (!Utils.bytesToString(cpu.Manufacturer).trim().isEmpty()) { 
        			comps.add(ComponentIdentifierV2FactoryRA2.create());
        			comps.get(comps.size()-1)
	    	    		.componentClass(new ComponentClass(
	    	    				new ASN1ObjectIdentifier(Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_CPU))
	    	    		.componentManufacturer(Utils.bytesToString(cpu.Manufacturer).trim())
	    	    		.componentModel(cpu.ID_parsed)	        		
	    	    		.componentSerial(Utils.bytesToString(cpu.SerialNumber).trim())
	    	    		.componentRevision(Utils.bytesToString(cpu.Version).trim())
	    	    		.fieldReplaceable(true);
        			// TODO: cpuinfohash was added at the properties field. It would be better
			        //       to have a proper field in the component
        			plat.addComponent(comps.get(comps.size()-1).build());
				}
        	}
        }

    }

    public void addDIMMInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        // DIMMInfo
     	if( fromSmbios ) {
			for ( ComponentIdentifierV2Factory_simple_RevSerialFru comp : smbiosComponents) {
				if(comp.COMPONENTCLASS.COMPONENTCLASSVALUE.equals(Components.COMPCLASS_RAM_DDR4) || 
				   comp.COMPONENTCLASS.COMPONENTCLASSVALUE.equals(Components.COMPCLASS_RAM_SD) ) {
	    	       	
					comps.add(ComponentIdentifierV2FactoryRA2.create());        
	    			comps.get(comps.size()-1)
	    				.componentClass(new ComponentClass(new ASN1ObjectIdentifier(
	    						Components.COMPCLASS_REGISTRY_TCG), comp.COMPONENTCLASS.COMPONENTCLASSVALUE)) 
		        		.componentManufacturer(comp.MANUFACTURER.trim())
		        		.componentModel(comp.MODEL.trim()) 
		        		.componentSerial(comp.SERIAL.trim())
						.fieldReplaceable(true);
	    			plat.addComponent(comps.get(comps.size()-1).build());
				}
			}

		} else {
			for (PLATFORM_CERTIFICATE_DIMM_INFO dimm : platcert.DimmInfo) {
        	   	if (!Utils.bytesToString(dimm.Manufacturer).trim().isEmpty()) { 
	    	       	comps.add(ComponentIdentifierV2FactoryRA2.create());

	    	       	String compClassValue;
	    	       	if (Utils.bytesToString(dimm.Type).trim().equalsIgnoreCase("DDR4")) {
	    	       		compClassValue = Components.COMPCLASS_RAM_DDR4;
	    	       	} else {
	    	       		compClassValue = Components.COMPCLASS_RAM_SD;
	        	   	}

	        		comps.get(comps.size()-1)
		    	    	.componentClass(new ComponentClass(
		    	    			new ASN1ObjectIdentifier(Components.COMPCLASS_REGISTRY_TCG), compClassValue)) 
		    	    	.componentManufacturer(Utils.bytesToString(dimm.Manufacturer).trim())
		    	    	.componentModel(Utils.bytesToString(dimm.PartNumber).trim())
		    	    	.componentSerial(Utils.bytesToString(dimm.SerialNumber).trim())
		       	 	.fieldReplaceable(true);
	        	
	        		// TODO: dimminfohash was added at the properties field. It would be better
		    	    //       to have a proper field in the component
	        		plat.addComponent(comps.get(comps.size()-1).build());
			   }
           	}
        }
    }

    public void addPCISlotInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        // ****** TODO: How can we identify the kind of PCI card here between NICs and others? 
        // PCISlotInfo
        for (PLATFORM_CERTIFICATE_PCI_SLOT_INFO pcislot : platcert.PciSlotInfo) {
        	if (pcislot.getNumericManufacturer().equals("0000") == false) {
		        comps.add(ComponentIdentifierV2FactoryRA2.create());
		        comps.get(comps.size()-1)
		        	// TODO FIX: map the COMPCLASS_NIC better through the vendor id and subvendor id (its not all NICs)
			        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
			        		Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_RISER_CARD))
			        .componentManufacturer(pcislot.getNumericManufacturer())
			        .componentModel(pcislot.getNumericModel())
			        .componentSerial(Utils.bytesToString(pcislot.Serial).trim())
			        .fieldReplaceable(true);
	        	// TODO: pcislotinfohash was added at the properties field. It would be better
		        //       to have a proper field in the component
		        plat.addComponent(comps.get(comps.size()-1).build());
        	}
        }

    }    

    public void addPowerSupplyInfo(ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) {
        // PowerSupplyInfo
		if (fromSmbios) {
			for ( ComponentIdentifierV2Factory_simple_RevSerialFru comp : smbiosComponents) {
				comp.COMPONENTCLASS.COMPONENTCLASSVALUE = comp.COMPONENTCLASS.COMPONENTCLASSVALUE.toUpperCase();
				if(comp.COMPONENTCLASS.COMPONENTCLASSVALUE.equals(Components.COMPCLASS_POWER_SUPPLY.toUpperCase())) {

					comps.add(ComponentIdentifierV2FactoryRA2.create());
					comps.get(comps.size()-1)
						.componentClass(new ComponentClass(new ASN1ObjectIdentifier(
								Components.COMPCLASS_REGISTRY_TCG), comp.COMPONENTCLASS.COMPONENTCLASSVALUE))
						.componentManufacturer(comp.MANUFACTURER.trim())
						.componentModel(comp.MODEL.trim())
						.componentSerial(comp.SERIAL.trim())
						.componentRevision(comp.REVISION.trim())
						.fieldReplaceable(true);
					plat.addComponent(comps.get(comps.size()-1).build());
				}
			}

		} else {
			for (PLATFORM_CERTIFICATE_POWER_SUPPLY_INFO powersupply : platcert.PowerSupplyInfo) {
				if (!Utils.bytesToString(powersupply.Manufacturer).trim().isEmpty() && 
					 Utils.bytesToString(powersupply.Present).trim().equalsIgnoreCase("yes")) {

					comps.add(ComponentIdentifierV2FactoryRA2.create());
					boolean fru = false;
					if (Utils.bytesToString(powersupply.Hotplug).trim().equalsIgnoreCase("yes") == true) { 
						fru=true; 
					}
					comps.get(comps.size()-1)
						.componentClass(new ComponentClass(new ASN1ObjectIdentifier(
								Components.COMPCLASS_REGISTRY_TCG), Components.COMPCLASS_POWER_SUPPLY))
						.componentManufacturer(Utils.bytesToString(powersupply.Manufacturer).trim())
						.componentModel(Utils.bytesToString(powersupply.PartNumber).trim())
						.componentSerial(Utils.bytesToString(powersupply.SerialNumber).trim())
						.fieldReplaceable(fru);
					plat.addComponent(comps.get(comps.size()-1).build());
				}
			}
		}
    }

    public void getSmbiosComponents(ArrayList<ComponentIdentifierV2Factory_simple_RevSerialFru> comps) throws 
    JsonIOException, JsonSyntaxException, UnsupportedEncodingException, FileNotFoundException {

	    System.loadLibrary("diskscan");

	    String jsoncomps = NativeSmbiosScan.smbiosFetch();
	    JsonReader reader = new JsonReader(new StringReader(jsoncomps));

    	Gson gson = new Gson();
    	ComponentIdentifierV2Factory_simple_RevSerialFru []sysComponents = 
    			gson.fromJson(reader, ComponentIdentifierV2Factory_simple_RevSerialFru[].class);

    	for (ComponentIdentifierV2Factory_simple_RevSerialFru comp : sysComponents) {
//        	System.out.println("** DEBUG Disk Info");
//    		System.out.println(disk);
    		comps.add(comp);
    	}
    }

    public PlatformIdentifier_simple getSmbiosPlatformInfo() throws 
    JsonIOException, JsonSyntaxException, UnsupportedEncodingException, FileNotFoundException {

	    System.loadLibrary("diskscan");
	    // System.err.println(">>: " + NativeDiskScan.nativeFetch());

	    String jsoncomps = NativePlatformScan.platformFetch();
	    JsonReader reader = new JsonReader(new StringReader(jsoncomps));
		
    	Gson gson = new Gson();
    	PlatformIdentifier_simple platform = 
    			gson.fromJson(reader, PlatformIdentifier_simple.class);
    	
		return platform;

	}

    public void addDisks(ArrayList<ComponentIdentifierV2Factory_simple> comps_simple) throws 
    JsonIOException, JsonSyntaxException, UnsupportedEncodingException, FileNotFoundException {

	    System.loadLibrary("diskscan");
	    // System.err.println(">>: " + NativeDiskScan.nativeFetch());

	    String jsoncomps = NativeDiskScan.disksFetch();
	    JsonReader reader = new JsonReader(new StringReader(jsoncomps));
		
    	Gson gson = new Gson();
    	ComponentIdentifierV2Factory_simple_RevSerialFru []diskComponents = 
    			gson.fromJson(reader, ComponentIdentifierV2Factory_simple_RevSerialFru[].class);
    	
    	for (ComponentIdentifierV2Factory_simple_RevSerialFru disk : diskComponents) {
//        	System.out.println("** DEBUG Disk Info");
//    		System.out.println(disk);
    		comps_simple.add(disk);
    	}
    }

    public String getComponentClass(String []pclass) {
		// TODO: this mapping should grow in the future and
		//       would be better to put it at it's own class.
		String compClass;
		if (String.join("", pclass[0], pclass[1]).equals("0107")) {
			compClass = Components.COMPCLASS_SAS_CONTROLLER;
		} else if (String.join("", pclass[0], pclass[1]).equals("0100")) {
			compClass = Components.COMPCLASS_SCSI_CONTROLLER;
		} else if (String.join("", pclass[0], pclass[1]).equals("0104")) {
			compClass = Components.COMPCLASS_RAID_CONTROLLER;
		} else if (String.join("", pclass[0], pclass[1]).equals("0105")) {
			compClass = Components.COMPCLASS_PATA_CONTROLLER;
		} else if (String.join("", pclass[0], pclass[1]).equals("0106")) {
			compClass = Components.COMPCLASS_SATA_CONTROLLER;
		} else if (pclass[0].equals("01")) {
			compClass = Components.COMPCLASS_STORAGE_CONTROLLER;
		} else if (String.join("", pclass[0], pclass[1]).equals("0200")) {
			compClass = Components.COMPCLASS_ETHERNET_LAN_ADAPTER;
		} else if (pclass[0].equals("02")) {
			compClass = Components.COMPCLASS_OTHER;
		} else if (pclass[0].equals("03")) {
			compClass = Components.COMPCLASS_VIDEO_CONTROLLER;
		} else  {
			compClass = Components.COMPCLASS_RISER_CARD;
		}
		return compClass;
    	
    }
    
    public void addPciCards (ArrayList<ComponentIdentifierV2FactoryRA2> comps, PlatformConfigurationV2Factory plat) throws 
    JsonIOException, JsonSyntaxException, UnsupportedEncodingException, FileNotFoundException {

	    System.loadLibrary("diskscan");
	    String jsoncomps = NativePciCardsScan.pciCardsFetch();
	    JsonReader reader = new JsonReader(new StringReader(jsoncomps));
    	Gson gson = new Gson();
    	PciCard []cards = gson.fromJson(reader, PciCard[].class);

    	// In case we opt for not using the PCI numerical IDs, this library decodes the info.
//    	DecodePciIds decpci;
//    	if (pciIdsPath == null) {
//    		decpci = new DecodePciIds();
//    	} else {
//    		decpci = new DecodePciIds(pciIdsPath);
//    	}
//      
//      Example of output data from the libdiskscan.so 
//    	SysPath =/sys/devices/pci0000:85/0000:85:02.0/0000:86:00.0
//    			   PciClass =10700
//    			   PciID =9005:028F
//    			   PciSubsysID =103C:0602
//    			   IDModelFromDatabase =Smart Storage PQI 12G SAS/PCIe 3 (Smart Array P408i-a SR Gen10)
//    			   IDVendorFromDatabase =Adaptec
//    			   IDPciClassFromDatabase =Mass storage controller
        
    	for (PciCard card: cards) {
    		String []pclass; 
    		String []pciID;
    		String []pciSubsysID;
    		try {
//	        	System.out.println("\n** DEBUG PCI Cards Info");
//	    		System.out.println(card);

	    		pciID = card.PciID.split(":");
	        	pciSubsysID = card.PciSubsysID.split(":");
	        	// System.out.printf("    PciID       1st part: %s (%s)\n", pciID[0], decpci.getVendorName(pciID[0]));
	        	// System.out.printf("    PciSubsysID 1st part: %s (%s)\n", PciSubsysID[0], decpci.getVendorName(PciSubsysID[0]));
	        	
	        	if (card.PciClass.length() %2 == 1) {
	        		StringBuilder sb = new StringBuilder(card.PciClass);
	        		sb.insert(0, "0");
	        		card.PciClass = sb.toString();
	        	}
	        	// String []pclass = card.PciClass.split("(?<=\\G.{2})");
	        	pclass = card.PciClass.replaceAll("..(?!$)", "$0 ").split(" ");
//	        	System.out.println("pclass:  "+ java.util.Arrays.toString(pclass));
	        	
    		} catch (Exception e) {
    			e.printStackTrace();
    			continue;
    		}
    		
    		// Map the pciClass to the TCG Component Class
    		String compClass = getComponentClass(pclass);
//        	System.out.println("component class:  "+ compClass);

        	// In case the pciSubsysID is not available, rely on pciID information
        	String manufacturer, model;
        	if (pciSubsysID.length > 0) {
        		manufacturer = pciSubsysID[0];
        		model = pciSubsysID[1];        		
        	} else {
        		manufacturer = pciID[0];
        		model = pciID[1];
        	}
        	
	        comps.add(ComponentIdentifierV2FactoryRA2.create());
	        comps.get(comps.size()-1)
		        .componentClass(new ComponentClass(new ASN1ObjectIdentifier(
		        		Components.COMPCLASS_REGISTRY_TCG), compClass))
		        .componentManufacturer(manufacturer)
		        .componentModel(model)
		        //.componentSerial(Utils.bytesToString(pcislot.Serial).trim())
		        .fieldReplaceable(true);
	        plat.addComponent(comps.get(comps.size()-1).build());
    			
    	}
    }
}
