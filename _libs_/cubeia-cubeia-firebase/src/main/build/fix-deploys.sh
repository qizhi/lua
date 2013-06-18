#!/bin/bash

_HOME=`pwd`

_DIRS="client-api/flash/ client-api/javascript/ firebase-tools/maven/firebase-plugin/ firebase-tests/systest/blackbox/"

for i in $_DIRS
do
    _DIR=$_HOME/$i
    echo "Deploying $_DIR"
    cd $_DIR
    mvn deploy -Dmaven.test.skip=true
    cd $_HOME
done