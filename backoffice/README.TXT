The full backoffice functionality, like User, Wallet and Accounting Service, is
provided by Cubeia Network.

Retrieve the code and follow the contained instructions to setup:

https://bitbucket.org/cubeia/cubeia-network

Once you have access to the Cubeia Network Webapplication, you have to create
some basic data, thus the Poker Admin and the Poker Game works.

OPERATOR
========

Create an Operator with the ID 1

CURRENCY
========

Add one currency, e.g. EUR or USD. Avoid creating multiple currencies at this
point.


ACCOUNTS
========

You need to create 3 different wallet accounts for each supported currency in
the system. The accounts uses dummy user ID's:

 * Dummy User -1000 is the system rake account
 * Dummy User -2000 is for promotions (such as free-rolls etc)
 * Dummy User -3000 is a general bank for internal transfers

The type is SYSTEM_ACCOUNT. So for example, if you only support American dollars,
you'd go to the Cubeia Network and create 3 wallet accounts (you do not
have to create the corresponding users): 

 * SYSTEM_ACCOUNT | USD | OPEN | UserID -1000 | Rake
 * SYSTEM_ACCOUNT | USD | OPEN | UserID -2000 | Promotions
 * SYSTEM_ACCOUNT | USD | OPEN | UserID -3000 | General Bank

USER
====

Create some users. Username, password and Operator ID (1) is enough data.

Alternatively, you can use the user-creator tool (see next step).

BOTS
====

Bots are like other users, so if you want to use the bots, you have to create
accounts for them, too. The user-creator tool simplifies this:

./poker/server/test-modules/user-creator/README