#!/bin/bash

#**********************************************************************
# (C) Copyright 2020-2021 Hewlett Packard Enterprise Development LP
#
# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this software and associated documentation files (the "Software"),
# to deal in the Software without restriction, including without limitation
# the rights to use, copy, modify, merge, publish, distribute, sublicense,
# and/or sell copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included
# in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
# THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
# OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
# ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
# OTHER DEALINGS IN THE SOFTWARE.
#**********************************************************************

source common.sh

LOG_FILE=/tmp/create_diskScan_bundle.log
SCRIPT_CWD=$(dirname $0)

#################### MAIN #####################

echo "=================== $(date) ==================" >> ${LOG_FILE}

if [ "x${GOPATH}" == "x" ]; then
    echo "GOPATH environment variable is missing trying to set automatically"
        GOPATH=`go env GOPATH`
        if [ "x${GOPATH}" == "x" ]; then
            echo "GOPATH environment variable is still missing"
            exit 1
        fi
fi

if [ ! -d "zulu11jdk" ]; then
    JDKADDR="https://cdn.azul.com/zulu/bin"
    JDKFILE="zulu11.35.15-ca-jdk11.0.5-linux_musl_x64"
    wget --no-check-certificate ${JDKADDR}/${JDKFILE}.tar.gz
    tar xvf ${JDKFILE}.tar.gz 
    mv ${JDKFILE} zulu11jdk
    rm ${JDKFILE}.tar.gz
fi

# create or clean up working bundle folder
#BUNDLE_TAR=${GOPATH}
BUNDLE_TAR=.
BUNDLE_WD=${BUNDLE_TAR}/diskscan-lib
rm -rf ${BUNDLE_WD}
mkdir -p ${BUNDLE_WD} &>> ${LOG_FILE}
if [ $? -ne 0 ]; then
    echo "Creating working directory failed"
    echo "Log available at ${LOG_FILE}."
    exit 1
fi

# setup go dependencies
go get github.com/jochenvg/go-udev
go get github.com/yumaojun03/dmidecode
go get github.com/dselans/dmidecode

go build -buildmode=c-shared -o ${BUNDLE_WD}/libdiskscan.so ../mainjni.go ../components.go ../jnibridge.go

if [ $? -ne 0 ]; then
    echo "Installation bundle creation failed"
    echo "Log available at ${LOG_FILE}."
    exit 1
fi

# create the tarball bundle with packages
echo "Creating installation bundle"
BUNDLE_FILE=diskScan-lib-$(date +"%m-%d-%y").tgz
pushd ${BUNDLE_TAR} &>> ${LOG_FILE}
tar czf ${BUNDLE_FILE} diskscan-lib/
popd &>> ${LOG_FILE}

if [ $? -ne 0 ]; then
    echo "Installation bundle creation failed"
    echo "Log available at ${LOG_FILE}."
    exit 1
fi

rm -rf ${BUNDLE_WD}

echo "Installation bundle created at ${BUNDLE_TAR}/${BUNDLE_FILE}"
echo "Log available at ${LOG_FILE}."

exit ${result}
