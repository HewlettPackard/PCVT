#!/bin/sh

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
echo $DIR

mkdir $DIR/lib
mkdir $DIR/tmp
cd $DIR/tmp

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
echo $DIR

wget https://github.com/nsacyber/HIRS/releases/download/v1.1.1/HIRS_AttestationCA-1.1.1-1574364941.0c2005.el6.noarch.rpm
wget https://github.com/nsacyber/paccor/releases/download/v1.1.3r2/paccor-1.1.3-2.noarch.rpm

rpm2cpio HIRS_AttestationCA-1.1.1-1574364941.0c2005.el6.noarch.rpm | cpio -idmv
rpm2cpio paccor-1.1.3-2.noarch.rpm | cpio -idmv

jar xvf $DIR/usr/share/tomcat6/webapps/HIRS_AttestationCA.war

cp WEB-INF/lib/HIRS_Structs-1.1.1-SNAPSHOT.jar ../lib/HIRS_Structs-1.1.1.jar
cp WEB-INF/lib/HIRS_Utils-1.1.1-SNAPSHOT.jar ../lib/HIRS_Utils-1.1.1.jar
#cp WEB-INF/lib/TPM_Utils-1.1.1-SNAPSHOT.jar ../lib/TPM_Utils-1.1.1.jar
cp opt/paccor/lib/paccor.jar ../lib/paccor-1.1.3-2.jar

#TODO: copy the lib dir content to each depency directory under ~/.m2/repository

cd ..
#rm -rf tmp

