#!/bin/bash

# Dont allow firebase to run as root
# if [ $UID -eq 0 ]; then
#	echo "You cannot run Firebase as root!"
#	exit 1
# fi

# Demand that JAVA_HOME is set
if [ -z "${JAVA_HOME}" ]; then
	echo "System variable JAVA_HOME must be set!"
	exit 1
fi


##### STARTS VARIABLES BELOW
###########################

if [ "${SILENT}" != "YES" ]; then
	echo ">>>>>>>>>>>>>>> Start system variables >>>>>>>>>>>>>>>"
fi

# FIREBASE_HOME=${SERVER_HOME}
if [ -z "${FIREBASE_HOME}" ]; then
	if [ -n "${SERVER_HOME}" ]; then
		FIREBASE_HOME=${SERVER_HOME}
	else 
		FIREBASE_HOME=`pwd`
	fi 
fi
if [ "${SILENT}" != "YES" ]; then
	echo "Using FIREBASE_HOME: ${FIREBASE_HOME}"
fi
export FIREBASE_HOME
cd $FIREBASE_HOME

# Setup PATH to include java binaries
export PATH=$PATH:${JAVA_HOME}/bin

# Max and min memory in megs
if [ -z "${MAX_MEMORY}" ]; then
	MAX_MEMORY=512M
fi
if [ "${SILENT}" != "YES" ]; then
	echo "Using MAX_MEMORY: ${MAX_MEMORY}"
fi
export MEMORY=${MAX_MEMORY}

# Max startup/shutdown wait in seconds 
if [ -z "${MAX_WAIT}" ]; then
	MAX_WAIT=90
fi
if [ "${SILENT}" != "YES" ]; then
	echo "Using MAX_WAIT: ${MAX_WAIT}"
fi
export MAXWAIT=${MAX_WAIT}

# GC Args
if [ -z "${GC_ARGUMENTS}" ]; then
	GC_ARGUMENTS="-XX:+UseParallelGC"
fi
if [ "${SILENT}" != "YES" ]; then
	echo "Using GC_ARGUMENTS: ${GC_ARGUMENTS}"
fi
export GCARGS=${GC_ARGUMENTS}

# Java binary
if [ "${SILENT}" != "YES" ]; then
	echo "Using JAVA_HOME: ${JAVA_HOME}"
fi
export BINARY=${JAVA_HOME}/bin/java

# Command line args
CMDLINE=$@
if [ -z "${CMDLINE}" ]; then
	CMDLINE="-n singleton"
	if [ "${SILENT}" != "YES" ]; then
		echo "Defaulting to singleton mode"
	fi
else
	if [ "${SILENT}" != "YES" ]; then
		echo "Using command line arguments: ${CMDLINE}"
	fi
fi	

# Host IP
if [ -z "${HOST_IP}" ]; then
	MYIPS=(`hostname -i`)
	MYIP=${MYIPS[0]}
	HOST_IP=${MYIP}
fi
if [ "${SILENT}" != "YES" ]; then
	echo "Using HOST_IP: ${HOST_IP}"
fi
export MYIP=${HOST_IP}

# Host name
if [ -z "${HOST_NAME}" ]; then
	HOST_NAME=`hostname`
fi
if [ "${SILENT}" != "YES" ]; then
	echo "Using HOST_NAME: ${HOST_NAME}"
fi
export MYHOST=${HOST_NAME}

# Startup jar
export JAR=firebase-bootstrap.jar

# Name of game server instance, used to identify process
export SERVERINSTANCENAME="CubeiaFirebase"

# Display name used in script output
export SERVERDISPLAYNAME="Cubeia Firebase"

# Java sysargs
export SYSARGS="-D${SERVERINSTANCENAME} -Djava.net.preferIPv4Stack=true"

# JDK nio epoll provider 
if [ -z "${DISABLE_EPOLL}" ]; then
	export JDK_EPOLL="-Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.EPollSelectorProvider"
	if [ "${SILENT}" != "YES" ]; then
		echo "Using Java NIO EPoll"
	fi
else 
	if [ "${SILENT}" != "YES" ]; then
		echo "Java NIO EPoll disabled"
	fi
fi

#Java JMX args
JMXP=${JMX_PORT}
if [ -z "${JMXP}" ]; then
	JMXP=8999
fi
export JMXARGS="-Djava.rmi.server.hostname=$MYIP -Dcom.sun.management.jmxremote.port=$JMXP -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
if [ "${SILENT}" != "YES" ]; then
	echo "Using JMX arguments: $JMXARGS"
fi

# Classpath
CLASSPATH="${FIREBASE_HOME}/conf/:${FIREBASE_HOME}/bin/:${FIREBASE_HOME}/bin/${JAR}:${JAVA_HOME}/lib/tools.jar"
# Append libs to CLASSPATH
for jarfile in ${FIREBASE_HOME}/lib/common/*.jar; do
	CLASSPATH=$CLASSPATH:$jarfile
done
if [ "${SILENT}" != "YES" ]; then
	echo "Using classpath: ${CLASSPATH}"
fi
export CLASSPATH

# Debug options
DEBUG=${DEBUG_OPTS}
if [ "${DEBUG}" != "" ]; then
	if [ "${SILENT}" != "YES" ]; then
		echo "Using debug options: ${DEBUG}"
	fi
fi

##### ENDS VARIABLES BELOW
##########################

if [ "${SILENT}" != "YES" ]; then
	echo ">>>>>>>>>>>>>>> End system variables >>>>>>>>>>>>>>>"
fi

# Server started log string
export STARTEDINDICATOR="Server started"

# Server stdout log file
export LOGFILE="logs/system.log"

# Server log archive directory
export LOGARCHIVE="logs/archive"

# Number of stdout/stderr logfile generations kept in archive
export LOGGENERATIONS=10

# Final return value
RETVAL=0

# Interval for jstat sampling
export JSTATINTERVAL=2000

# Log file for jstat output
export JSTATLOGFILE=${FIREBASE_HOME}/logs/jstat.log

startjstat()
{
	if [ "$USEJSTAT" == "true" ]; then
		jstat -gcutil $1 $JSTATINTERVAL > $JSTATLOGFILE 2>&1 &
	fi
}

rotate_archive()
{
	LIST=$(ls -r $1*)
	COUNT="$2"
	for i in $LIST; do
        	TMP=$(ls $i | cut -d "." -f 2)
        	if [ $TMP = $1 ]; then
                	NEW=$TMP.0
                	mv $i $NEW
        	elif [ $TMP -gt $COUNT ]; then
                	rm $i
        	else
                	BASE=$(ls $i | cut -d "." -f 1)
                	NEW=$BASE.$(($TMP+1))
                	mv $i $NEW
        	fi
        	touch $1
	done
}

# move stdout/err logs
move_log() {

	if [ ! -d $LOGARCHIVE ]; then
		mkdir -p $LOGARCHIVE
	fi
	
	if [ -e ${FIREBASE_HOME}/logs/stdout.txt ] ; then
		rotate_archive "$LOGARCHIVE/stdout" $LOGGENERATIONS
		DESTINATION="$LOGARCHIVE/stdout"
		if [ "${SILENT}" != "YES" ]; then
			echo "Moving stdout log file to log archive: $DESTINATION"
		fi

		mv ${FIREBASE_HOME}/logs/stdout.txt $DESTINATION
	fi


	if [ -e ${FIREBASE_HOME}/logs/stderr.txt ] ; then
		rotate_archive "$LOGARCHIVE/stderr" $LOGGENERATIONS
		DESTINATION="$LOGARCHIVE/stderr"
		if [ "${SILENT}" != "YES" ]; then
			echo "Moving stderr log file to log archive: $DESTINATION"
		fi
		mv ${FIREBASE_HOME}/logs/stderr.txt $DESTINATION
	fi
}

get_role() {
	ROLE=`ps aux|grep [j]ava|grep $SERVERINSTANCENAME|tail -1|grep com.game.server.bootstrap.Server|awk '{print substr($0, index($0, "com.game.server.bootstrap.Server")+length("com.game.server.bootstrap.Server")+1)}'`
	echo $ROLE
}

# Find process by name
findserverinstance()
{
	INSTANCEPID=`ps ax|grep [j]ava|grep $SERVERINSTANCENAME|tail -1|awk '{print $1}'`
	if [ -z $INSTANCEPID ]; then
		echo "0"
	else
		echo $INSTANCEPID
	fi
}

# Find process by PID
findpid()
{
	SERVERPID=`ps ax|awk '{print $1}'|grep -x $1`

	if [ -z $SERVERPID ]; then
		echo "0"
	else	
		echo $SERVERPID
	fi
}


# Check if server is already running
isrunning()
{
	cd $FIREBASE_HOME
	# do we have a PID file?
	if [ -e bin/server.pid ]; then
		# yes, check if process is still running
		CURRENTPID=`cat bin/server.pid`
		PIDFOUND=`findpid $CURRENTPID`
		if [ $PIDFOUND == "true" ]; then
			echo $PIDFOUND
			return
		fi
	fi

	# PID file not found, so far so good, now let's check by name
	INSTANCEPID=`findserverinstance`
	echo $INSTANCEPID

}


# Stop the server
killserver()
{
	if [ "${SILENT}" != "YES" ]; then
		echo -n "Waiting for $SERVERDISPLAYNAME to stop"
	fi
	kill $1
	
	COUNT=$MAXWAIT

  	until [ $COUNT == 0 ]
  	do
		PIDFOUND=`findpid $1`

		if [ $PIDFOUND == "0" ];  then
			if [ "$SILENT" != "YES" ]; then
				echo
			fi
			return 
		fi


		sleep 1
    		COUNT=`expr $COUNT - 1`
    		if [ "$SILENT" != "YES" ]; then
			echo -n .
		fi
  	done
	if [ "${SILENT}" != "YES" ]; then
		echo
		echo "Timeout killing $SERVERDISPLAYNAME"
		echo "Trying hard kill"
	fi
	kill -9 $1
	COUNT=$MAXWAIT

  	until [ $COUNT == 0 ]
  	do
		PIDFOUND=`findpid $1`

		if [ $PIDFOUND == "0" ];  then
			if [ "$SILENT" != "YES" ]; then
				echo
			fi
			return
		fi


		sleep 1
    		COUNT=`expr $COUNT - 1`
		if [ "${SILENT}" != "YES" ]; then
    			echo -n .
		fi
  	done
	if [ "${SILENT}" != "YES" ]; then
		echo
		echo " *** Error: Failed to stop $SERVERDISPLAYNAME"
	fi
	exit 0
}	

waitforstartup()
{
	# Wait for server to create log file
	sleep 3
	if [ "${SILENT}" != "YES" ]; then
		echo -n "Waiting for $SERVERDISPLAYNAME to start"
	fi
	COUNT=$MAXWAIT

  	until [ $COUNT == 0 ]
  	do
		PIDFOUND=`findpid $1`

		if [ $PIDFOUND == "0" ];  then
			if [ "${SILENT}" != "YES" ]; then
				echo " *** Error starting $SERVERDISPLAYNAME"
				echo " *** Please check logs for error messages"
			fi
			return 
		fi

		STARTED=`cat $LOGFILE|grep "Server started"|tail -1|awk '{print $1}'`

		if [ ! -z $STARTED ]; then
			if [ "${SILENT}" != "YES" ]; then
				echo
			fi
			return
		fi

		sleep 1
    		COUNT=`expr $COUNT - 1`
		if [ "${SILENT}" != "YES" ]; then
    			echo -n .
		fi
  	done
	if [ "${SILENT}" != "YES" ]; then
		echo
		echo " *** $SERVERDISPLAYNAME seems to take a long time to start"
		echo " *** Please check server status manually"
	fi
	exit 0
}	


stamplog()
{
	TODAY=`date "+%Y-%m-%d %T"`
	echo "=====================================================">>$2
	echo "$TODAY - $SERVERDISPLAYNAME $1">>$2
	echo "=====================================================">>$2
}

startserver()
{
	# CMDLINE=$@

	# export CMDLINE
	cd $FIREBASE_HOME
	
	ISRUNNING=`isrunning`
	if [ $ISRUNNING != "0" ]; then
		if [ "${SILENT}" != "YES" ]; then
			echo "Error: A server instance is already running with PID: $ISRUNNING"
			echo "Unable to start another instance"
		fi
		exit 1
	fi

	move_log
	stamplog "Starting" "logs/stdout.txt"
	stamplog "Starting" "logs/stderr.txt"
	if [ "${SILENT}" != "YES" ]; then	
		echo "Starting ${SERVERDISPLAYNAME}"
	fi
	${BINARY} -server ${DEBUG} ${GCARGS} ${SYSARGS} ${JDK_EPOLL} ${JMXARGS} -Xmx${MEMORY} -Xms${MEMORY} -classpath ${CLASSPATH} com.game.server.bootstrap.Server -i ${MYHOST} ${CMDLINE} >> "logs/stdout.txt" 2>> "logs/stderr.txt" &
	SERVERPID=$!
	echo $SERVERPID>bin/server.pid
	waitforstartup $SERVERPID
	if [ "${SILENT}" != "YES" ]; then
		echo "$SERVERDISPLAYNAME started"
	fi
	startjstat $SERVERPID
}

runserver()
{
	cd $FIREBASE_HOME
	
	ISRUNNING=`isrunning`
	if [ $ISRUNNING != "0" ]; then
		echo "Error: A server instance is already running with PID: $ISRUNNING"
		echo "Unable to start another instance"
		exit 1
	fi

	echo "Running ${SERVERDISPLAYNAME}"
	${BINARY} -server ${DEBUG} ${GCARGS} ${SYSARGS} ${JDK_EPOLL} ${JMXARGS} -Xmx${MEMORY} -Xms${MEMORY} -classpath ${CLASSPATH} com.game.server.bootstrap.Server -i ${MYHOST} ${CMDLINE} 
}

stopserver()
{
	cd $FIREBASE_HOME
	ISRUNNING=`isrunning`
	if [ $ISRUNNING == "0" ]; then
		if [ "${SILENT}" != "YES" ]; then
			echo "$SERVERDISPLAYNAME is not running"
		fi
		exit 1
	fi

	killserver $ISRUNNING
	stamplog "Stopped" "logs/stdout.txt"
	if [ -e bin/server.pid ]; then
		rm -f bin/server.pid
	fi
	if [ "${SILENT}" != "YES" ]; then	
		echo "$SERVERDISPLAYNAME stopped"
	fi
}

case "$1" in

	start)
	# SILENT="NO"
	startserver $CMDLINE
	exit $RETVAL
	;;

	stop)
	# SILENT="NO"
	stopserver
	exit 0
	;;

	restart)
	# SILENT="NO"
	stopserver
	startserver
	exit $RETVAL

esac
