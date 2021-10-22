
Scanning library for detection of the following parts, to be used along with PCVT tool.
  1) SCSI/SATA + NVME devices; 
  2) PCI cards within the physical slots.

Compilation instructions:

  Check the #cgo CFLAGS entries at jnibridge.go to point to valid JDK include 
  directories. Originally ZULU11 JDK was used

  Build command for generating the library to be used along with the PCVT jar file:
    Inside the build directory please run create_install_bundle.sh
    
    In case there is any need to compile the library without the build script, 
    the command below can be trigered.
      go build -buildmode=c-shared -o libdiskscan.so mainjni.go components.go jnibridge.go

Usage instructions:

  Please make sure to include the directory containing the libdiskscan.so at the 
  LD_LIBRARY_PATH before running the PCVT tool.

  Example: export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$PWD


