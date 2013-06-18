The shutdown service is responsible for handling a clean shutdown of the system. It will first receive a prepare shutdown message via JMX and then tell the system to prepare a shutdown.

Once the preparation is finished, a shutdown message should be sent via JMX and then the system will shut down completely.

Here's a list of things that should be done:

Prepare:
- Broadcast a message to all players saying "the system will shut down soon, please finish your sessions".
- Tell game activators to prepare a shutdown. The game activator will then:
  - Remove all empty tables.
  - Tell existing 
- Tell tournament activators to stop spawning new tournaments.
- Tell tables to stop accepting new players.
- Tell tournaments to stop accepting new registrations (at least sit&go tournaments, maybe in v2?)

Shutdown:
- Broadcast a message to all players saying "the system is now shutting down, goodbye".
