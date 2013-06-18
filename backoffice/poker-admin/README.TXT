The Poker Admin Webapplication, used to create Tables and Tournaments

To build: 
mvn clean install

To run:
mvn jetty:run

Access at localhost:8088/poker-admin/

At this point you can see the functionality, but most of it is not usable. To
make the Poker Admin fully functional, go on with...

DATABASES
=========

Mandatory: create a mysql database.
the default configuration is: db: poker, user: poker, pass: poker
to change: src/test/resources/jetty-env.xml

Optionally: for hand histories and tournament histories, create a mongodb
the default configuration is: db: poker, user: poker, pass: poker
to change: src/test/resources/admin.properties


CUBEIA NETWORK
==============

The Poker Admin depends on Cubeia Network. 

Install Cubeia Network, as described in the ../README (./backoffice/README.TXT)

This will start the Backoffice and the services at port 8080.

CONFIG FILES
============

As Cubeia Nework is started outside the development environment, configuration
starts.

within ./backoffice/poker-admin/src/test/resources/admin.properties, change the
ports of user/wallet/operator services to 8080.

Build the Poker Admin again with "mvn clean install"

INTEGRATIONS
============

Τhe default build of the poker-server results in a system with mock-services
(or mock-integrations). This means that the poker-admin does not influence the
actual tables/tournaments, nor there is any real user and management.

In order to build & start a poker system which integrates fully:

a) build the poker-server with "-P use-integrations" option.

b) server\game-modules\poker-uar\src\test\resources\firebase\conf\cluster.props
   - set use-mock-integrations=false"
   - change the ports of user/wallet/operator services to 8080
     (http://localhost:8080)

c) Start: Cubeia Network then the Poker Server (firebase) then the Poker Client

You now login with the user credentials created within Cubeia Network.

ADMIN DATA
==========

At this point, you create all the data needed within the Poker Admin:

First create Timings and Rake Configurations (good name is "Standard")

At this point you can create Table Templates, which should result in tables
showing up in the lobby.

Then Create Payout and Blinds Structures (again choose "Standard" as name).

After this you can create a simple Sit&Go, which should show up in the lobby.

Last, setup a Scheduled Tournament template.