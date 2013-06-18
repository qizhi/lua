This service provides configuration for the poker installation. It is implemented as a service for
easier testing and mocking as well as code reuse. 

The configuration is split up into several interfaces located in the API package. Default
values can be modified by changing the "Property" annotation on the getter methods.

Real configuration should take place in the cluster configuration. For example, the defaults 
in PokerActivatorConfig can be overridden by changing (or adding) the following properties in
"conf/cluster.props" (with default values below):

com.cubeia.game.poker.activator.use-mock-integrations=false
com.cubeia.game.poker.activator.default-table-ttl=60000
com.cubeia.game.poker.activator.activator-interval=15000
