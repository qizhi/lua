This is a Java based text client, for testing the poker.

Usage:
java PokerTextClient localhost [4123]
login 1 1 (username password)
list 1 (lists all tables)

Available Poker Commands:
	 help             	 : print help
	 buyin TID amount 	 : Buy in at table
	 small TID        	 : post small blind
	 big TID          	 : post big blind
	 check TID        	 : Check
	 call TID         	 : Call
	 bet TID <amount> 	 : Bet
	 fold TID         	 : Fold

