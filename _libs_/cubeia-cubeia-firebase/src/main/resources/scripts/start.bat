@ECHO off
set CLASS_P=conf

REM **************************************************************************
REM * Enable delayed variable expansion so we can build a proper classpath   *
REM **************************************************************************
SETLOCAL ENABLEDELAYEDEXPANSION


REM **************************************************************************
REM * Search bin directory for jar files and add them to classpath           *
REM **************************************************************************
for /R bin %%j in (*.jar) do SET CLASS_P=!CLASS_P!;%%j


REM **************************************************************************
REM * Search lib/common for jar files and add them to classpath
REM **************************************************************************
for /R lib\common %%j in (*.jar) do SET CLASS_P=!CLASS_P!;%%j


set JMX=-Djava.rmi.server.hostname=localhost -Dcom.sun.management.jmxremote.port=8999 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false
set MAXMEMORY=128M
set MAIN_CLASS=com.game.server.bootstrap.Server

java %JMX% %* -Djava.net.preferIPv4Stack=true -Xms%MAXMEMORY% -Xmx%MAXMEMORY% -XX:+UseParallelGC -classpath %CLASS_P% %MAIN_CLASS% -n singleton