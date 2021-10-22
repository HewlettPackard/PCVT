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

public class Components {

	// ComponentClass values
	static String COMPCLASS_REGISTRY_TCG="2.23.133.18.3.1";
//	static String COMPCLASS_BASEBOARD="00030003"; // these values are meant to be an example.  check the component class registry.
	static String COMPCLASS_BIOS="00130003";
	static String COMPCLASS_UEFI="00130002";
	static String COMPCLASS_CHASSIS="00020001"; // TODO:  chassis type is included in SMBIOS
	static String COMPCLASS_SERVER_CHASSIS="00020016";	// Rack Mount Chassis	 
	static String COMPCLASS_CPU="00010002";
	static String COMPCLASS_HDD="00070002";
	static String COMPCLASS_NIC="00090002";
	static String COMPCLASS_RAM="00060001";  // TODO: memory type is included in SMBIOS
	static String COMPCLASS_RAM_SD="00060010"; // SDRAM Memory	Synchronous DRAM memory component
	static String COMPCLASS_RAM_DDR4="00060016"; // DDR4 Memory- Double Data Rate RAM memory component (successor to DDR3)
	static String COMPCLASS_POWER_SUPPLY = "000A0002";
	static String COMPCLASS_TPM = "00040009"; // A discrete Trusted Platform Module
	static String COMPCLASS_BMC = "00060003"; // Baseboard Management Controller


	static String COMPCLASS_UNKNOWN_COMPONENT="00030001";	//	Unknown	Component is an IC board but identity is unknown to the attesting party
	static String COMPCLASS_DAUGHTER_BOARD="00030002";	//	Daughter board	A board that extends the circuitry of another board
	static String COMPCLASS_MOTHERBOARD="00030003";	//	Motherboard (includes processor, memory, and I/O)	A board containing the principal components of a computer or other device

	// PCI Class related mappings: 
	static String COMPCLASS_SAS_CONTROLLER  = "00050009";       // PCI Class starting with 0x0107
	static String COMPCLASS_SCSI_CONTROLLER = "00050003";      // PCI Class starting with 0x0100
	static String COMPCLASS_RAID_CONTROLLER = "0005000B";      // PCI Class starting with 0x0104
	static String COMPCLASS_PATA_CONTROLLER = "00050007";      // PCI Class starting with 0x0105
	static String COMPCLASS_SATA_CONTROLLER = "00050008";      // PCI Class starting with 0x0106
	static String COMPCLASS_STORAGE_CONTROLLER = "00070000";   // others starting with 0x01

	static String COMPCLASS_ETHERNET_LAN_ADAPTER = "00090002"; // starting with 0x0200
	static String COMPCLASS_OTHER="00030000";	               // others starting with 0x02. Other	Component is an IC board but identity does not match any of the supported values
	
	static String COMPCLASS_VIDEO_CONTROLLER = "00050002"; // others starting with 0x03

	static String COMPCLASS_RISER_CARD="00030004";	// PCI Class starting with other than 0x01, 0x02 or 0x03. Riser Card	A board that plugs into the system board and provides additional slots	

	
	// JSON Structure Keywords;
	static String JSON_COMPONENTS="COMPONENTS";
	static String JSON_COMPONENTSURI="COMPONENTSURI";
	static String JSON_PROPERTIES="PROPERTIES";
	static String JSON_PROPERTIESURI="PROPERTIESURI";
	static String JSON_PLATFORM="PLATFORM";

	// JSON Component Keywords;
	static String JSON_COMPONENTCLASS="COMPONENTCLASS";
	static String JSON_COMPONENTCLASSREGISTRY="COMPONENTCLASSREGISTRY";
	static String JSON_COMPONENTCLASSVALUE="COMPONENTCLASSVALUE";
	static String JSON_MANUFACTURER="MANUFACTURER";
	static String JSON_MODEL="MODEL";
	static String JSON_SERIAL="SERIAL";
	static String JSON_REVISION="REVISION";
	static String JSON_MANUFACTURERID="MANUFACTURERID";
	static String JSON_FIELDREPLACEABLE="FIELDREPLACEABLE";
	static String JSON_ADDRESSES="ADDRESSES";
	static String JSON_ETHERNETMAC="ETHERNETMAC";
	static String JSON_WLANMAC="WLANMAC";
	static String JSON_BLUETOOTHMAC="BLUETOOTHMAC";
	static String JSON_COMPONENTPLATFORMCERT="PLATFORMCERT";
	static String JSON_ATTRIBUTECERTIDENTIFIER="ATTRIBUTECERTIDENTIFIER";
	static String JSON_GENERICCERTIDENTIFIER="GENERICCERTIDENTIFIER";
	static String JSON_ISSUER="ISSUER";
	static String JSON_COMPONENTPLATFORMCERTURI="PLATFORMCERTURI";
	static String JSON_STATUS="STATUS";

	// JSON Platform Keywords (Subject Alternative Name);
	static String JSON_PLATFORMMODEL="PLATFORMMODEL";
	static String JSON_PLATFORMMANUFACTURERSTR="PLATFORMMANUFACTURERSTR";
	static String JSON_PLATFORMVERSION="PLATFORMVERSION";
	static String JSON_PLATFORMSERIAL="PLATFORMSERIAL";
	static String JSON_PLATFORMMANUFACTURERID="PLATFORMMANUFACTURERID";

	// JSON Platform URI Keywords;
	static String JSON_URI="UNIFORMRESOURCEIDENTIFIER";
	static String JSON_HASHALG="HASHALGORITHM";
	static String JSON_HASHVALUE="HASHVALUE";

	// JSON Properties Keywords;
	static String JSON_NAME="NAME";
	static String JSON_VALUE="VALUE";
	public static String NOT_SPECIFIED="Not Specified";
		
}
