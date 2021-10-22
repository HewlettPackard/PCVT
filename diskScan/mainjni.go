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

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"os/exec"
	"path"
	"strings"

	dmidecode "github.com/dselans/dmidecode"
	udev "github.com/jochenvg/go-udev"
	dmidecode02 "github.com/yumaojun03/dmidecode"
)

func compSetup(comp *Component) (err error) {
	comp.COMPONENTCLASS.COMPONENTCLASSREGISTRY = "2.23.133.18.3.1"
	comp.COMPONENTCLASS.COMPONENTCLASSVALUE = "00070002"
	comp.FIELDREPLACEABLE = "true"

	return
}

func GetDisksInfo() (comps []Component, err error) {
	u := udev.Udev{}
	e := u.NewEnumerate()
	e.AddMatchSubsystem("block")
	e.AddMatchIsInitialized()

	var compsNvme []Component
	labelNotSpecified := "NOT SPECIFIED"

	devices, _ := e.Devices()
	useNvmeCmdLine := false
	for i := range devices {
		device := devices[i]

		if device.Devtype() != "partition" &&
			device.Sysname() != "loop" {
			var manuf string
			var model string

			// TODO: check if we want a criteria here for fullfilled items
			if len(device.PropertyValue("ID_VENDOR")) > 0 ||
				len(device.PropertyValue("ID_MODEL")) > 0 ||
				len(device.PropertyValue("ID_SERIAL_SHORT")) > 0 ||
				len(device.PropertyValue("ID_REVISION")) > 0 {

				prop := device.Properties()
				// log.Printf("** DEBUG: properties from block: \n%v\n", prop)
				manuf = labelNotSpecified
				if len(device.PropertyValue("ID_VENDOR")) > 0 {
					manuf = prop["ID_VENDOR"]
				}
				model = labelNotSpecified
				if len(device.PropertyValue("ID_MODEL")) > 0 {
					model = prop["ID_MODEL"]
				}

				// log.Printf("** DEBUG: MANUFACTURER: %v\n", manuf)
				// log.Printf("** DEBUG: MODEL: %v\n", model)
				tempComp := Component{
					MANUFACTURER: manuf,
					MODEL:        model,
					SERIAL:       prop["ID_SERIAL_SHORT"],
					REVISION:     prop["ID_REVISION"],
				}
				err := compSetup(&tempComp)
				if err != nil {
					fmt.Println("Error setting common fields at nvme disk component")
					return []Component{}, err
				}

				// Older format used to retrieve the nvme firmware version with the
				// nvme command line tool. Not being used at the moment
				// TODO: if we find any useful info at these properties, they can
				// be matched against the libudev nvme filtered data below at getNvmeDisksInfoV2()
				if strings.Contains(device.PropertyValue("DEVNAME"), "nvme") {
					if useNvmeCmdLine {
						// NVME case, add to compsNvme
						// fmt.Printf("\n %s \n", device.Syspath())
						err := getNvmeDiskFw(&tempComp)
						if err != nil {
							fmt.Println("Error setting firmware/revision field at nvme disk component")
							return []Component{}, err
						}
						compsNvme = append(compsNvme, tempComp)
					}

				} else {
					// SCSI/SATA case, add to comps
					// Debug only
					// fmt.Printf("\n %s \n", device.Syspath())
					comps = append(comps, tempComp)
				}
			}
		}
	}

	// Newer format used to to retrieve the nvme related info, filtering at libudev
	// for nvme devices and from there getting the content from the files model,
	// serial and firmware_rev under the device's syspath.
	err = getNvmeDisksInfoV2(&comps)
	if err != nil {
		fmt.Println("Error getting NVME disks info")
		return []Component{}, err
	}

	if useNvmeCmdLine {
		// Adding NVME drives to the bottom of the list (Intel TSC tool requirement)
		for _, nvmeDrive := range compsNvme {
			comps = append(comps, nvmeDrive)
		}
	}

	return comps, nil
}

func getNvmeDiskFw(comp *Component) (err error) {
	nvmePath, err := exec.LookPath("nvme")
	if err != nil {
		fmt.Printf("Error: nvme tool path not found. Exiting.\n")
		return
	}

	cmd := exec.Command(nvmePath, "list", "-o", "json")
	var out bytes.Buffer
	cmd.Stdout = &out
	err = cmd.Run()
	if err != nil {
		log.Fatal(err)
	}

	var nvmeDevs NvmeList
	err = json.Unmarshal(out.Bytes(), &nvmeDevs)
	if err != nil {
		fmt.Println(err)
	}

	for _, dev := range nvmeDevs.Devices {
		if dev.ModelNumber == comp.MODEL {
			comp.REVISION = dev.Firmware
		}
	}
	return
}

// Alternative way of retrieving the NVME firmware version not requiring
// the use of the command line tool "nvme", but checking the firmware_rev
// file content under the device syspath.
func getNvmeDiskFwV2(dev *udev.Device) (fwVersion string, err error) {

	// fmt.Printf("** DEBUG: syspath: %v\n", dev.Syspath())
	fpath := path.Join(dev.Syspath(), "firmware_rev")
	data, err := ioutil.ReadFile(fpath)
	if err != nil {
		fmt.Printf("Error reading: %v\n", fpath)
		return "", err
	}

	fwVersion = string(bytes.TrimSpace(data))
	// fmt.Printf("** DEBUG: fwVersion: %v\n", fwVersion)
	return fwVersion, nil
}

func getNvmeDisksInfo(comps []Component) (err error) {

	nvmePath, err := exec.LookPath("nvme")
	if err != nil {
		fmt.Printf("Error: nvme tool path not found. Exiting.\n")
		return
	}
	cmd := exec.Command(nvmePath, "list", "-o", "json")
	var out bytes.Buffer
	cmd.Stdout = &out
	err = cmd.Run()
	if err != nil {
		log.Fatal(err)
	}

	var nvmeDevs NvmeList
	err = json.Unmarshal(out.Bytes(), &nvmeDevs)
	if err != nil {
		fmt.Println(err)
	}
	for _, dev := range nvmeDevs.Devices {
		fmt.Printf("DevicePath: %v\n", dev.DevicePath)
		fmt.Printf("Firmware: %v\n", dev.Firmware)
	}
	return
}

func getNvmeDisksInfoV2(comps *[]Component) (err error) {

	var u udev.Udev
	e := u.NewEnumerate()
	e.AddMatchSubsystem("nvme")

	devs, err := e.Devices()
	if err != nil {
		log.Fatal(err)
	}

	for _, dev := range devs {
		frev, err := getFirmwareRevision(dev.Syspath())
		if err != nil {
			log.Fatal(err)
		}
		model, err := getModel(dev.Syspath())
		if err != nil {
			log.Fatal(err)
		}
		serial, err := getSerial(dev.Syspath())
		if err != nil {
			log.Fatal(err)
		}

		// log.Printf("** DEBUG: NVME syspath: \n%v\n", dev.Syspath())
		// log.Printf("** DEBUG: NVME properties: \n%v\n", dev.Properties())
		// log.Printf("** DEBUG: NVME model, serial, revision: \n%v\n%v\n%v\n", model, serial, frev)
		labelNotSpecified := "NOT SPECIFIED"
		manuf := labelNotSpecified
		if len(dev.PropertyValue("ID_VENDOR")) > 0 {
			manuf = dev.PropertyValue("ID_VENDOR")
		}
		if len(model) == 0 {
			model = labelNotSpecified
		}
		tempComp := Component{
			MANUFACTURER: manuf, // this info was not found under the syspath
			MODEL:        model,
			SERIAL:       serial,
			REVISION:     frev,
		}
		err = compSetup(&tempComp)
		if err != nil {
			fmt.Println("Error setting common fields at nvme disk component")
			return err
		}
		*comps = append(*comps, tempComp)

		log.Println(dev.Devnode(), frev, dev.Syspath())
		// fmt.Printf("DevicePath: %X\n", dev.Devpath)
	}

	return
}

func getSlotsV1() (err error) {
	dmi, err := dmidecode02.New()
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return err
	}

	slots, err := dmi.Slot()
	if err != nil {
		fmt.Println("Error getting slots with demidecode")
		return err
	}

	for _, slot := range slots {
		fmt.Println(slot)
	}
	return
}

func getSlotsV2() (slots []string, err error) {
	dmi := dmidecode.New()
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return nil, err
	}

	if err := dmi.Run(); err != nil {
		fmt.Printf("Unable to get dmidecode information. Error: %v\n", err)
	}

	// Example of search by record name
	// byNameData, byNameErr := dmi.SearchByName("System Information")

	// Looking by record type 9 (slots)
	slotsData, err := dmi.SearchByType(9)
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return nil, err
	}
	// for slotnum, slotinfo := range slotsData {
	for _, slotinfo := range slotsData {
		// fmt.Printf("---> %v\n", slotnum)
		for infoType, infoValue := range slotinfo {
			// fmt.Printf("  %v - %v\n", infoType, infoValue)
			if infoType == "Bus Address" {
				// fmt.Printf("%v - %v\n", slotnum, infoValue)
				slots = append(slots, infoValue)
			}
		}
	}

	return
}

func printPciCardInfo(dev *udev.Device) (err error) {
	fmt.Printf("--> Syspath:    %v\n", dev.Syspath())
	fmt.Printf("    Sysname:    %v\n", dev.Sysname())
	fmt.Printf("    Driver:     %v\n", dev.Driver())
	// fmt.Printf("    Properties: %v\n", dev.Properties())
	fmt.Printf("    Properties: \n")
	// for propName, propValue := range dev.Properties() {
	// 	fmt.Printf("                - %v: %v\n", propName, propValue)
	// }
	fmt.Printf("                - PCI_CLASS: %v\n", dev.PropertyValue("PCI_CLASS"))
	fmt.Printf("                - PCI_ID: %v\n", dev.PropertyValue("PCI_ID"))
	fmt.Printf("                - PCI_SUBSYS_ID: %v\n", dev.PropertyValue("PCI_SUBSYS_ID"))
	fmt.Printf("                - ID_MODEL_FROM_DATABASE: %v\n", dev.PropertyValue("ID_MODEL_FROM_DATABASE"))
	fmt.Printf("                - ID_VENDOR_FROM_DATABASE: %v\n", dev.PropertyValue("ID_VENDOR_FROM_DATABASE"))
	fmt.Printf("                - ID_PCI_CLASS_FROM_DATABASE: %v\n", dev.PropertyValue("ID_PCI_CLASS_FROM_DATABASE"))

	return
}

func getPciCards(slots []string) (pciCards []PciCard, err error) {

	var u udev.Udev
	e := u.NewEnumerate()

	// fmt.Println("Getting devices... ")
	e.AddMatchSubsystem("pci")
	// e.AddMatchIsInitialized()

	devs, err := e.Devices()
	if err != nil {
		log.Fatal(err)
	}

	for _, dev := range devs {
		for _, slot := range slots {
			if dev.Sysname() == slot {
				// printPciCardInfo(dev)
				tempCard := PciCard{
					SysPath:                dev.Syspath(),
					PciClass:               dev.PropertyValue("PCI_CLASS"),
					PciID:                  dev.PropertyValue("PCI_ID"),
					PciSubsysID:            dev.PropertyValue("PCI_SUBSYS_ID"),
					IDModelFromDatabase:    dev.PropertyValue("ID_MODEL_FROM_DATABASE"),
					IDVendorFromDatabase:   dev.PropertyValue("ID_VENDOR_FROM_DATABASE"),
					IDPciClassFromDatabase: dev.PropertyValue("ID_PCI_CLASS_FROM_DATABASE"),
				}
				pciCards = append(pciCards, tempCard)
			}
		}

		// frev, err := getFirmwareRevision(dev.Syspath())
		// if err != nil {
		// 	log.Fatal(err)
		// }
		// model, err := getModel(dev.Syspath())
		// if err != nil {
		// 	log.Fatal(err)
		// }
		// serial, err := getSerial(dev.Syspath())
		// if err != nil {
		// 	log.Fatal(err)
		// }
		// // log.Printf("** DEBUG: properties from NVME \n%v\n", dev.Properties())
		// tempComp := Component{
		// 	MANUFACTURER: dev.PropertyValue("ID_VENDOR"), // this info was not found under the syspath
		// 	MODEL:        model,
		// 	SERIAL:       serial,
		// 	REVISION:     frev,
		// }
		// err = compSetup(&tempComp)
		// if err != nil {
		// 	fmt.Println("Error setting common fields at nvme disk component")
		// 	return err
		// }
		// *comps = append(*comps, tempComp)

		// TODO: check better how to parse VPD data, includes SN
		// vpd, err := getVPD(dev.Syspath())
		// if err != nil {
		// 	log.Fatal(err)
		// }
		// if vpd == "no vpd" {
		// 	continue
		// }

		// fmt.Printf("%v, %v\n", dev.Syspath(), dev.Properties())
		// fmt.Printf("--> Syspath:       %v\n", dev.Syspath())
		// fmt.Printf("    Sysname:       %v\n", dev.Sysname())
		// fmt.Printf("    Driver:        %v\n", dev.Driver())
		// fmt.Printf("    Devpath:       %v\n", dev.Devpath())
		// fmt.Printf("    Subsystem:     %v\n", dev.Subsystem())
		// fmt.Printf("    Devtype:       %v\n", dev.Devtype())
		// fmt.Printf("    Sysnum:        %v\n", dev.Sysnum())
		// fmt.Printf("    Devnode:       %v\n", dev.Devnode())
		// fmt.Printf("    IsInitialized: %v\n", dev.IsInitialized())
		// fmt.Printf("    Devnum:        %v\n", dev.Devnum())

	}

	return
}

func getFirmwareRevision(syspath string) (rev string, err error) {
	fpath := path.Join(syspath, "firmware_rev")
	data, err := ioutil.ReadFile(fpath)
	if err != nil {
		return "", err
	}

	rev = string(bytes.TrimSpace(data))
	return rev, nil
}

func getModel(syspath string) (model string, err error) {
	fpath := path.Join(syspath, "model")
	data, err := ioutil.ReadFile(fpath)
	if err != nil {
		return "", err
	}

	model = string(bytes.TrimSpace(data))
	return model, nil
}

func getSerial(syspath string) (serial string, err error) {
	fpath := path.Join(syspath, "serial")
	data, err := ioutil.ReadFile(fpath)
	if err != nil {
		return "", err
	}

	serial = string(bytes.TrimSpace(data))
	return serial, nil
}

func getVPD(syspath string) (vpd string, err error) {
	fpath := path.Join(syspath, "vpd")
	_, err = os.Stat(fpath)
	if os.IsNotExist(err) {
		return "no vpd", nil
	}

	data, err := ioutil.ReadFile(fpath)
	if err != nil {
		return "", err
	}

	vpd = string(bytes.TrimSpace(data))
	return vpd, nil
}

func getPlatformInfo() (plat Platform, err error) {
	dmi := dmidecode.New()
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return Platform{}, err
	}

	if err := dmi.Run(); err != nil {
		fmt.Printf("Unable to get dmidecode information. Error: %v\n", err)
	}

	// Looking by record type 1
	dmiData, err := dmi.SearchByType(1)
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return Platform{}, err
	}

	plat.MANUFACTURER = ""
	plat.MODEL = ""
	plat.REVISION = ""
	plat.SERIAL = ""
	for _, record := range dmiData {
		for infoType, infoValue := range record {
			// fmt.Printf("---> %v\n", slotnum)
			if infoType == "Manufacturer" {
				// fmt.Printf("%v - %v\n", slotnum, infoValue)
				tempInfoValue := strings.TrimSpace(infoValue)
				if len(tempInfoValue) > 0 {
					plat.MANUFACTURER = infoValue
				}
			}
			if infoType == "Product Name" {
				// fmt.Printf("%v - %v\n", slotnum, infoValue)
				tempInfoValue := strings.TrimSpace(infoValue)
				if len(tempInfoValue) > 0 {
					plat.MODEL = infoValue
				}
			}
			if infoType == "Version" {
				// fmt.Printf("%v - %v\n", slotnum, infoValue)
				if infoValue != "Not Specified" {
					tempInfoValue := strings.TrimSpace(infoValue)
					if len(tempInfoValue) > 0 {
						plat.REVISION = infoValue
					}
				}
			}
			if infoType == "Serial Number" {
				// fmt.Printf("%v - %v\n", slotnum, infoValue)
				tempInfoValue := strings.TrimSpace(infoValue)
				if len(tempInfoValue) > 0 {
					plat.SERIAL = infoValue
				}
			}
		}
	}
	return
}

func getSmbiosInfo() (comps []Component, err error) {
	dmi := dmidecode.New()
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return nil, err
	}

	if err := dmi.Run(); err != nil {
		fmt.Printf("Unable to get dmidecode information. Error: %v\n", err)
	}

	// Looking by record type 3 (Chassis)
	dmiData, err := dmi.SearchByType(3)
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return nil, err
	}

	var type3 Component
	type3.COMPONENTCLASS.COMPONENTCLASSREGISTRY = "2.23.133.18.3.1"
	type3.COMPONENTCLASS.COMPONENTCLASSVALUE = "00020016"
	for _, record := range dmiData {

		for infoType, infoValue := range record {
			// fmt.Printf("---> %v\n", slotnum)
			if infoType == "Manufacturer" {
				// fmt.Printf("%v - %v\n", slotnum, infoValue)
				type3.MANUFACTURER = infoValue
			}
			if infoType == "Type" {
				// fmt.Printf("%v - %v\n", slotnum, infoValue)
				type3.MODEL = infoValue
			}
			if infoType == "Serial Number" {
				// fmt.Printf("%v - %v\n", slotnum, infoValue)
				type3.SERIAL = infoValue
			}
		}
	}
	comps = append(comps, type3)

	// Looking by record type 2 (MotherBoard)
	dmiData2, err := dmi.SearchByType(2)
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return nil, err
	}

	var type2 Component
	type2.COMPONENTCLASS.COMPONENTCLASSREGISTRY = "2.23.133.18.3.1"
	type2.COMPONENTCLASS.COMPONENTCLASSVALUE = "00030003"
	// fmt.Println("\n**DEBUG type2 entries: ", dmiData2)
	for _, record := range dmiData2 {
		// fmt.Println("\n  **DEBUG type2 new entry: ", record)

		for infoType, infoValue := range record {
			// fmt.Printf("    **DEBUG type2 entry: %s:%s\n", infoType, infoValue)
			if infoType == "Manufacturer" {
				type2.MANUFACTURER = infoValue
			}
			if infoType == "Product Name" {
				type2.MODEL = infoValue
			}
			if infoType == "Serial Number" {
				type2.SERIAL = infoValue
			}
		}
	}
	comps = append(comps, type2)

	// Looking by record type 0 (BIOS/UEFI)
	dmiData0, err := dmi.SearchByType(0)
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return nil, err
	}

	var type0 Component
	typeilo := Component{
		MANUFACTURER: "HPE",
		MODEL:        "iLO",
	}
	for _, record0 := range dmiData0 {

		for infoType, infoValue := range record0 {
			if infoType == "Vendor" {
				type0.MANUFACTURER = infoValue
			}
			if infoType == "Version" {
				type0.MODEL = infoValue
			}
			if infoType == "BIOS Revision" {
				type0.REVISION = infoValue
			}
			if infoType == "Firmware Revision" {
				typeilo.REVISION = infoValue
			}
		}
	}

	type0.COMPONENTCLASS.COMPONENTCLASSREGISTRY = "2.23.133.18.3.1"
	type0.COMPONENTCLASS.COMPONENTCLASSVALUE = "00130002"
	typeilo.COMPONENTCLASS.COMPONENTCLASSREGISTRY = "2.23.133.18.3.1"
	typeilo.COMPONENTCLASS.COMPONENTCLASSVALUE = "00060003"
	comps = append(comps, type0)
	comps = append(comps, typeilo)

	// Looking by record type 43 (TPM)
	dmiData43, err := dmi.SearchByType(43)
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return nil, err
	}

	var type43 Component
	type43.COMPONENTCLASS.COMPONENTCLASSREGISTRY = "2.23.133.18.3.1"
	type43.COMPONENTCLASS.COMPONENTCLASSVALUE = "00040009"
	for _, record43 := range dmiData43 {
		// fmt.Println("\n  **DEBUG type43 new entry: ", record43)
		for infoType, infoValue := range record43 {
			// fmt.Printf("---> %v\n", slotnum)
			if infoType == "Vendor ID" {
				// fmt.Printf("%v - %v\n", slotnum, infoValue)
				type43.MANUFACTURER = infoValue
			}
			if infoType == "Description" {
				// fmt.Printf("%v - %v\n", slotnum, infoValue)
				type43.MODEL = infoValue
			}
			if infoType == "Firmware Revision" {
				// fmt.Printf("%v - %v\n", slotnum, infoValue)
				type43.REVISION = infoValue
			}
		}
	}
	comps = append(comps, type43)

	// Looking by record type 4 (CPUs)
	dmiData4, err := dmi.SearchByType(4)
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return nil, err
	}

	var type4 Component
	type4.COMPONENTCLASS.COMPONENTCLASSREGISTRY = "2.23.133.18.3.1"
	type4.COMPONENTCLASS.COMPONENTCLASSVALUE = "00010002"
	type4.FIELDREPLACEABLE = "true"

	// fmt.Println("\n**DEBUG type4 entries: ")
	for _, record4 := range dmiData4 {
		// fmt.Println("\n  **DEBUG type4 new entry ")
		validComp := false

		for infoType, infoValue := range record4 {
			infoType = strings.TrimSpace(infoType)
			infoValue = strings.TrimSpace(infoValue)
			// fmt.Printf("    **DEBUG type4 entry: %s:%s\n", infoType, infoValue)

			if (infoType == "Status") && (infoValue != "Unpopulated") {
				validComp = true
			}
			if infoType == "Manufacturer" {
				type4.MANUFACTURER = infoValue
			}
			if infoType == "ID" {
				type4.MODEL = infoValue
				type4.MODEL = strings.Replace(type4.MODEL, " ", "", -1)
			}
			if infoType == "Version" {
				type4.REVISION = infoValue
			}
		}
		if validComp {
			comps = append(comps, type4)
		}
	}

	// Looking by record type 17 (DIMMs)
	dmiData17, err := dmi.SearchByType(17)
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return nil, err
	}

	var type17 Component
	type17.COMPONENTCLASS.COMPONENTCLASSREGISTRY = "2.23.133.18.3.1"
	type17.FIELDREPLACEABLE = "true"
	// fmt.Println("\n**DEBUG type17 entries: ")
	for _, record17 := range dmiData17 {
		validComp := false

		for infoType, infoValue := range record17 {
			infoType = strings.TrimSpace(infoType)
			infoValue = strings.TrimSpace(infoValue)
			// fmt.Println("**DEBUG type17 entry: ", infoType, ":", infoValue)

			if (infoType == "Size") && (infoValue != "No Module Installed") {
				validComp = true
			}
			if infoType == "Manufacturer" {
				type17.MANUFACTURER = infoValue
			}
			if infoType == "Part Number" {
				type17.MODEL = infoValue
			}
			if infoType == "Serial Number" {
				type17.SERIAL = infoValue
			}
			if infoType == "Type" {
				if infoValue == "DDR4" {
					type17.COMPONENTCLASS.COMPONENTCLASSVALUE = "00060016"
				} else {
					type17.COMPONENTCLASS.COMPONENTCLASSVALUE = "00060010"
				}
			}
		}
		if validComp {
			comps = append(comps, type17)
		}
	}

	// Looking by record type 39 (PowerSupply)
	dmiData39, err := dmi.SearchByType(39)
	if err != nil {
		fmt.Println("Error on demidecode component")
		fmt.Println(err)
		return nil, err
	}

	var type39 Component
	type39.COMPONENTCLASS.COMPONENTCLASSREGISTRY = "2.23.133.18.3.1"
	type39.COMPONENTCLASS.COMPONENTCLASSVALUE = "000a0002"
	type39.FIELDREPLACEABLE = "true"

	// fmt.Println("\n**DEBUG type39 entries: ")
	for _, record39 := range dmiData39 {
		validComp := false
		for infoType, infoValue := range record39 {
			infoType = strings.TrimSpace(infoType)
			infoValue = strings.TrimSpace(infoValue)
			// fmt.Println("**DEBUG type39 entry: ", infoType, ":", infoValue)

			if (infoType == "Status") && (infoValue != "Not Present") {
				validComp = true
			}
			if infoType == "Manufacturer" {
				type39.MANUFACTURER = infoValue
			}
			if infoType == "Model Part Number" {
				type39.MODEL = infoValue
			}
			if infoType == "Serial Number" {
				type39.SERIAL = infoValue
			}
		}
		if validComp {
			comps = append(comps, type39)
		}
	}

	return
}
func main() {
	// printComps worked fines
	_, _ = GetDisksInfo()
	comps, _ := GetDisksInfo()
	fmt.Println("\n** DEBUG: Printing Disks info ***")
	for _, comp := range comps {
		fmt.Printf(" ** DEBUG comps: ClassReg:%v, ClassVal:%v, \nManufac:%v, Model:%v, Rev:%v, Serial:%v, FRU:%v\n",
			comp.COMPONENTCLASS.COMPONENTCLASSREGISTRY, comp.COMPONENTCLASS.COMPONENTCLASSVALUE,
			comp.MANUFACTURER, comp.MODEL, comp.REVISION, comp.SERIAL, comp.FIELDREPLACEABLE)
	}

	// fmt.Println("\n** DEBUG: Printing PCI Cards info ***")
	slots, err := getSlotsV2()
	if err != nil {
		fmt.Println(err)
		return
	}
	// for _, slot := range slots {
	// 	fmt.Println(slot)
	// }

	_, err = getPciCards(slots)
	// cards, err := getPciCards(slots)
	if err != nil {
		fmt.Println(err)
		return
	}
	// for _, card := range cards {
	// fmt.Println(card)
	// }

	fmt.Println("\n** DEBUG: Printing SMBIOS info ***")
	smbios1, err := getSmbiosInfo()
	if err != nil {
		fmt.Println(err)
		return
	}
	for _, smb1 := range smbios1 {
		fmt.Println(smb1)
	}

	fmt.Println("\n** DEBUG: Printing SMBIOS Platform info ***")
	smbios2, err := getPlatformInfo()
	if err != nil {
		fmt.Println(err)
		return
	}
	fmt.Println("Manufacturer: ", smbios2.MANUFACTURER)
	fmt.Println("Model: ", smbios2.MODEL)
	fmt.Println("Revision: ", smbios2.REVISION, ". Len: ", len(smbios2.REVISION))
	fmt.Println("Serial: ", smbios2.SERIAL)

	// // fmt.Printf("  ** Debug comps: %v\n", comps)
	// jsonComps, err := json.Marshal(comps)
	// if err != nil {
	// 	fmt.Println(err)
	// }
	// err = ioutil.WriteFile("comps.json", jsonComps, 0644)
	// if err != nil {
	// 	fmt.Println(err)
	// }
	// fmt.Printf("%s\n", string(jsonComps))

	return
}
