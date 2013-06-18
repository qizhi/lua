#!/bin/bash

# Run Firebase in the foreground.

# game server home directory: current dir
export SERVER_HOME=`pwd`

# import local server configuration
source ${SERVER_HOME}/conf/config.sh

# import function from start script
source ${SERVER_HOME}/bin/gameserver.sh 

# do start
runserver $@ 
