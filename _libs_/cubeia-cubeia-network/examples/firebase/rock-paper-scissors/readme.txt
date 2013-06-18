INTRODUCTION

This project contains a simple example game (logic and client) demonstrating 
the use  of the Cubeia Network wallet- and user services.

Most error handling and special cases of the game has been omitted to make
the wallet code easier to follow. 

Setting up and running this game is somewhat complicated but will give you some 
insight in the Firebase game server and the Cubeia Network services. 


THE GAME

The game logic is based on Rock Paper Scissors (see reference 1 for rules). 

The basic game play is:
1. login
2. join a table
3. play your hand: ROCK, PAPER or SCISSORS.
4. winner is decided when another player has done the same
5. game over, the table is closed


DETAILS

Authentication is done using the Firebase login service. To use the Cubeia 
Network User Service the game depends on the fb-user-service module which
plugs into Firebase and then communicates with the remote user service.

When joining a table (step 2 in the list above) the wallet is used to open 
a new session account and then credit the session with the bet amount. 
Look at the code in GameImpl.playerJoined() for details.

The game starts when two users have joined the table and played their hand.
See Processor#handle() and Processor#handlePlayCommand().

When the game winner is decided a single transaction is made where the loser's 
session account is debited the bet amount, the winner is credited
the bet minus fee and the system fee account is credited the fee. If the game is 
a draw this transaction is omitted.

The last step is to transfer the funds back from the session accounts to
the remote wallets, kick the players from the table and close it.


PREREQUISITES

A running user service, wallet service and backoffice web application.
The Cubeia Network Bundle (see reference 2) probably the fastest way to get up 
and running.



RUNNING AND BUILDING

1. Build both projects by running "mvn clean package" in the parent project.
2. To start the game server run "mvn firebase:run" in the game project.
3. The client is started by running:
   "java -jar target/rps-client.jar localhost 4123".


PLAYING

Before playing the game you must use the backoffice web application to setup:
a. two users with passwords and operator id 0
b. the created users must each have an account of type "STATIC_ACCOUNT" with 
   currency EUR
c. an "OPERATOR_ACCOUNT" for operator 0 and currency EUR

If you are using the Cubeia Network Bundle make sure that the properties
in the game project "src/test/resources/firebase/conf/cluster.props"-file 
point the the correct URL:s.

When the accounts and users have been set up start the game server as in step 2 
above.

Play the game by first starting the client as in step 3 above. Then writing the 
following in the client:
  login <user1> <password1>
  join 1 -1
  play ROCK

Start another client and write:
  login <user2> <password2>
  join 1 -1
  play PAPER
  
If both players were successfully authenticated and no accounts were missing 
player two should have won and his static account should have been credited with
the winnings. The operator account should have
been credited the fee amount and player two should have lost his bet.


REFERENCES

1. http://en.wikipedia.org/wiki/Rock-paper-scissors
2. http://www.cubeia.org/wiki/index.php/Network/tutorials/quick-start-bundle
