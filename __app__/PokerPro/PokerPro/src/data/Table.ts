///<reference path="Player.ts"/>
///<reference path="GameConfig.ts"/>

///<reference path="../ui/TableLayout.ts"/>

module data {
    export class PotType {
        public static MAIN: number = 1;
        public static SIDE: number = 2;
    }
    /**
     * Representation of a pot, there are two types of pots MAIN and SIDE
     * @param id - the id of the pot
     * @param type - the Poker.PotType type of the pot
     * @param amount - the pot amount
     */
    export class Pot {
        constructor(public id: number, public type: number, public amount:number) {
        }
    }

    //Action that a player does, such as Call, Raise etc.
    export class Action {
        constructor(public type: string, public minAmount: number, public maxAmount:number) {
        }
    }

    //The poker hands that a player can get
    export class Hand {
        public static UNKNOWN: number = 0;
        public static HIGH_CARD: number = 1;
        public static PAIR: number = 2;
        public static TWO_PAIRS: number = 3;
        public static THREE_OF_A_KIND: number = 4;
        public static STRAIGHT: number = 5;
        public static FLUSH: number = 6;
        public static FULL_HOUSE: number = 7;
        public static FOUR_OF_A_KIND: number = 8;
        public static STRAIGHT_FLUSH: number = 9;
        public static ROYAL_STRAIGHT_FLUSH: number = 10;

    }

    export class ActionType {
        public static CALL: string = "action-call";
        public static CHECK: string = "action-check";
        public static FOLD: string = "action-fold";
        public static BET: string = "action-bet";
        public static RAISE: string = "action-raise";
        public static SMALL_BLIND: string = "action-small-blind";
        public static BIG_BLIND: string = "action-big-blind";
        public static JOIN: string = "action-join";
        public static LEAVE: string = "action-leave";
        public static SIT_OUT: string = "action-sit-out";
        public static SIT_IN: string = "action-sit-in";
        public static ENTRY_BET: string = "entry-bet";
        public static DECLINE_ENTRY_BET: string = "decline-entry-bet";
        public static WAIT_FOR_BIG_BLIND: string = "wait-for-big-blind";
        public static ANTE: string = "ante";
        public static BIG_BLIND_PLUS_DEAD_SMALL_BLIND: string = "big-and-small-blind";
        public static DEAD_SMALL_BLIND: string = "dead-small-blind";
        public static REBUY: string = "rebuy";
        public static DECLINE_REBUY: string = "decline-rebuy";
        public static ADD_ON: string = "add-on";
    }
    export class FutureActionType {
        public static CHECK: string = "check";
        public static CHECK_OR_FOLD: string = "check-or-fold";
        public static CALL_CURRENT_BET: string = "call-current-bet";
        public static CHECK_OR_CALL_ANY: string = "check-or-call-any";
        public static CALL_ANY: string = "call-any";
        public static FOLD: string = "fold";
        public static RAISE: string = "raise";
        public static RAISE_ANY: string = "raise-any";
    }
    export class Table {
        players: Map<number, IPlayer>;
        myPlayerSeat: any;
        handCount: number;
        dealerSeatId: number;
        totalPot: number;
        handId: number;
        betStrategy: any;
        currency: any;

        layout: ui.TableLayout;

        noMoreBlinds: boolean;

        //// True means that this table belongs to a tournament that is closed.
        tournamentClosed: boolean;

        constructor(public id: number, public capacity: number, public name: string) {
            this.players = new Map<number, IPlayer>();
        }

        public leave(): void {
            //this.layoutManager.onLeaveTableSuccess();
        }
        /**
         *
         * @param seat position at the table
         * @param player to add to the table
         */
        public addPlayer(seat: number, player: IPlayer): void {
            if (seat < 0 || seat >= this.capacity) {
                throw "Table : seat " + seat + " of player " + player.name + " is invalid, capacity=" + this.capacity;
            }
            this.players.put(seat, player);

        }
        public removePlayer(playerId: number): void {
            var kvp: any[] = this.players.keyValuePairs();
            for (var i: number = 0; i < kvp.length; i++) {
                if (kvp[i].value.id == playerId) {
                    this.players.remove(kvp[i].key);
                    return;
                }
            }
            console.log("player not found when trying to remove");
        }

        /**
         * Get a player by its player id
         * @param playerId to get
         * @return {Poker.Player} with the playerId or null if not found
         */
        public getPlayerById(playerId: number): IPlayer {
            var players: any[] = this.players.values();
            for (var i: number = 0; i < players.length; i++) {
                if (players[i].id == playerId) {
                    return players[i];
                }
            }
            return null;
        }
        /**
         * Returns the number of players at the table;
         * @return {int}
         */
        public getNumOfPlayers(): number {
            return this.players.size();

        }
    }
}