This module contains the actual implementation of the poker game logic. The idea was to have no
dependencies to Firebase, so that one could use this module for poker logic in any type of system. There is a callback
interface (ServerAdapter), which the surrounding code must implement, which will handle things like sending messages
to players.