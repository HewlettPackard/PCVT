MakefilePath := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

LIB_DIR     := $(MakefilePath)lib
TARGET_DIR  := $(MakefilePath)target
TPM_MODULE := tpm_module-1.1.1-1574364941.0c2005.x86_64.rpm
all: clean library build 

build:
	mvn package


library:
	mkdir $(LIB_DIR)
	wget https://github.com/nsacyber/HIRS/releases/download/v1.1.1/$(TPM_MODULE) -P $(LIB_DIR)/
	cd $(LIB_DIR)
	tar xvf $(TPM_MODULE)

clean:
	@rm -rf $(TARGET_DIR)
	@rm -rf $(LIB_DIR)



