=========================================================================
==  Welcome to Cubeia Firebase                                         ==
=========================================================================

This product contains code developed by Cubeia Ltd (http://www.cubeia.com).
Copyright (c) 2009 Cubeia Ltd, http://www.cubeia.com, and licensed under 
the Firebase Enterprise Edition License, version 1.

For more information about Cubeia Firebase please visit one of the 
following resources:

 - Cubeia Ltd: http://www.cubeia.com
 - Cubeia Cummunity Site: http://www.cubeia.org
 - Cubeia Firebase: http://www.cubeia.com/index.php/products/firebase
 - Community Forum: http://www.cubeia.org/index.php?option=com_kunena&Itemid=14
 - Community Wiki: http://www.cubeia.org/wiki/index.php/Main_Page

Cubeia Firebase is build with Maven 2. If you have a source distribution, 
you should simply have to enter the root directory with command window 
and execute:

 `mvn package assembly:assembly`

This will compile all sources and package Cubeia Firebase in a ZIP file
ready for deployment.


=========================================================================
==  Starting / Stopping on Linux / Unix Systems                        ==
=========================================================================

 - Change directory into the installation folder
 - Call "./start.sh" or "./stop.sh"

To change the script configuration, edit "./conf/config.sh".


=========================================================================
==  Starting / Stopping on Windows                                     ==
=========================================================================

 - Change directory into the installation folder
 - Start by callint "./start.bat"
 - Stop by closing CMD window

The windows script is provided as a convenience for development. It is n
ot intended for live systems.
