This directory contains not the firebase server itself, but all the poker code
which is executed in context of the firebase server.

You *don't* need to install firebase separately, it is fetched by the build.

== QUICK-START ==

mvn install # or mvn clean install
cd  ../uar
mvn firebase:run

You should be able to access the game at:

http://localhost:19999

There is no user registration at this point. Use any name, and the password
should be an integer > 1000.

In order to get some opponents, go to the bots at:

http://localhost:19090

The default values will create 100 bots, just press OK button.

You should see now within the lobby that tables are getting players. 

Join one or more tables and check the game.

=== PRODUCTION ===

(TODO)