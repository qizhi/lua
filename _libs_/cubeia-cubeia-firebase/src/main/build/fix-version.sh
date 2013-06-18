#/bin/bash

_VERSION=$1

_NS="http://maven.apache.org/POM/4.0.0"

if [ -z "$_VERSION" ]
then
    echo "No version specified, will use version from /pom.xml"
    _VERSION=`xmlstarlet sel -N n="$_NS" -t -v "/n:project/n:version" pom.xml`
fi

echo "New version: $_VERSION"


# VERSIONED POM's BELOW
#######################

echo "Fixing parent version on un-connected POM's"

_FILES="client-api/flash/pom.xml client-api/flash/default-crypto/pom.xml client-api/flash/crypto-api/pom.xml client-api/flash/client-api/pom.xml firebase-tools/maven/firebase-plugin/pom.xml firebase-tests/systest/blackbox/pom.xml"

for i in $_FILES
do 
     _BACKUP="$i.tmp"
     _OLD_VERSION=`xmlstarlet sel -N n="$_NS" -t -v "/n:project/n:parent/n:version" $i`
     echo "Switching old version $_OLD_VERSION to $_VERSION in $i"
     xmlstarlet ed -P -N n="$_NS" -t -u "/n:project/n:parent/n:version" -v "$_VERSION" $i > $_BACKUP
     mv $_BACKUP $i
done


# SYSTEST BUILD FILE
####################

echo "Fixing systest ANT test version"

_BACKUP="tmp.xml"
_FILE="firebase-tests/systest/blackbox/build.xml"
echo "Switching systest version to $_VERSION in $_FILE"
xmlstarlet ed -P -u "//property[@name = 'version']/@value" -v "$_VERSION" $_FILE > $_BACKUP
mv $_BACKUP $_FILE
