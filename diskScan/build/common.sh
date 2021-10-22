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

# Copy a file to a destination
# @param: Source file
# @param: Destination
# @param: Log file
function copy_files() {
    SOURCE_FILES="$1"
    DEST="$2"
    LOG_FILE=$3

    if [ $# -ne 3 ]; then
        echo "Invalid arguments to function copy_files"
        echo "Log available at ${LOG_FILE}."
        exit 1
    fi

    cp ${SOURCE_FILES} ${DEST} &>> ${LOG_FILE}

    if [ $? -ne 0 ]; then
        echo "Copying "${SOURCE_FILES}" to ${DEST} failed"
        echo "Log available at ${LOG_FILE}."
        exit 1
    fi

    return 0
}

# Check environment variables
function check_envvars() {
    if [ "x${GOPATH}" == "x" ]; then
        echo "GOPATH not set"
        exit 1
    fi

    return 0
}

# Check Linux distro
function check_os() {
    if [ -f /etc/os-release ]; then
        # freedesktop.org and systemd
        . /etc/os-release
        OS=$NAME
        VER=$VERSION_ID
    elif type lsb_release >/dev/null 2>&1; then
        # linuxbase.org
        OS=$(lsb_release -si)
        VER=$(lsb_release -sr)
    elif [ -f /etc/lsb-release ]; then
        # For some versions of Debian/Ubuntu without lsb_release command
        . /etc/lsb-release
        OS=$DISTRIB_ID
        VER=$DISTRIB_RELEASE
    elif [ -f /etc/debian_version ]; then
        # Older Debian/Ubuntu/etc.
        OS="Debian"
        VER=$(cat /etc/debian_version)
    elif [ -f /etc/SuSe-release ]; then
        # Older SuSE/etc.
        OS="SuSe Linux"
    elif [ -f /etc/redhat-release ]; then
        # Older Red Hat, CentOS, etc.
        OS="RedHat Linux"
    else
        # Fall back to uname, e.g. "Linux <version>", also works for BSD, etc.
        OS="Unsupported"
        VER=$(uname -a)
    fi

    echo ${OS} | egrep -i "centos" &> /dev/null
    if [ $? -ne 0 ]; then
        echo "Unsupported Linux distribution"
        exit 1
    fi

    if [ "${VER}" != "7" ]; then
        echo "Unsupported CentOS version"
        exit 1
    fi

    return 0
}

# Install build dependencies
# @param: Package list
# @param: Log file
# Assumes the host OS is RPM based
function install_build_deps() {
    PACKAGES="$1"
    LOG_FILE=$2

    if [ $# -ne 2 ]; then
        echo "Invalid arguments to function install_build_deps"
        echo "Log available at ${LOG_FILE}."
        exit 1
    fi

    # yum update should be call just once
    sudo yum update -y &>> ${LOG_FILE}
    sudo yum install -y ${PACKAGES} &>> ${LOG_FILE}
    if [ $? -ne 0 ]; then
        echo "Dependencies installation failed."
        echo "Log available at ${LOG_FILE}."
        exit 1
    fi

    return 0
}

# Get source code of a given repository list
# @param: Repository list
# @param: Log file
function get_source_code() {
    REPO_LIST="$1"
    LOG_FILE=$2

    if [ $# -ne 2 ]; then
        echo "Invalid arguments to function get_source_code"
        echo "Log available at ${LOG_FILE}."
        exit 1
    fi

    for i in ${REPO_LIST}; do
        go get -u github.hpe.com/pivs/$i/... &>> ${LOG_FILE}
        if [ $? -ne 0 ]; then
            echo "Downloading/updating source code failed for $i"
            echo "Log available at ${LOG_FILE}."
            exit 1
        fi
    done

    return 0
}

# Check source code of a given repository list
# @param: Repository list
# @param: Log file
function check_source_code() {
    REPO_LIST="$1"
    LOG_FILE=$2

    if [ $# -ne 2 ]; then
        echo "Invalid arguments to function check_source_code"
        echo "Log available at ${LOG_FILE}."
        exit 1
    fi

    for i in ${REPO_LIST}; do
        if [ ! -d ${GOPATH}/src/github.hpe.com/pivs/$i/ ]; then
            echo "Source code for $i was not found in the GOPATH"
            echo "Log available at ${LOG_FILE}."
            exit 1
        fi
    done

    return 0
}
