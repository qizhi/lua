///<reference path="Player.ts"/>
///<reference path="GameConfig.ts"/>
///<reference path="../ui/TableLayout.ts"/>
var data;
(function (data) {
    var PotType = (function () {
        function PotType() {
        }
        PotType.MAIN = 1;
        PotType.SIDE = 2;
        return PotType;
    })();
    data.PotType = PotType;

    /**
    * Representation of a pot, there are two types of pots MAIN and SIDE
    * @param id - the id of the pot
    * @param type - the Poker.PotType type of the pot
    * @param amount - the pot amount
    */
    var Pot = (function () {
        function Pot(id, type, amount) {
            this.id = id;
            this.type = type;
            this.amount = amount;
        }
        return Pot;
    })();
    data.Pot = Pot;

    //Action that a player does, such as Call, Raise etc.
    var Action = (function () {
        function Action(type, minAmount, maxAmount) {
            this.type = type;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }
        return Action;
    })();
    data.Action = Action;

    //The poker hands that a player can get
    var Hand = (function () {
        function Hand() {
        }
        Hand.UNKNOWN = 0;
        Hand.HIGH_CARD = 1;
        Hand.PAIR = 2;
        Hand.TWO_PAIRS = 3;
        Hand.THREE_OF_A_KIND = 4;
        Hand.STRAIGHT = 5;
        Hand.FLUSH = 6;
        Hand.FULL_HOUSE = 7;
        Hand.FOUR_OF_A_KIND = 8;
        Hand.STRAIGHT_FLUSH = 9;
        Hand.ROYAL_STRAIGHT_FLUSH = 10;
        return Hand;
    })();
    data.Hand = Hand;

    var ActionType = (function () {
        function ActionType() {
        }
        ActionType.CALL = "action-call";
        ActionType.CHECK = "action-check";
        ActionType.FOLD = "action-fold";
        ActionType.BET = "action-bet";
        ActionType.RAISE = "action-raise";
        ActionType.SMALL_BLIND = "action-small-blind";
        ActionType.BIG_BLIND = "action-big-blind";
        ActionType.JOIN = "action-join";
        ActionType.LEAVE = "action-leave";
        ActionType.SIT_OUT = "action-sit-out";
        ActionType.SIT_IN = "action-sit-in";
        ActionType.ENTRY_BET = "entry-bet";
        ActionType.DECLINE_ENTRY_BET = "decline-entry-bet";
        ActionType.WAIT_FOR_BIG_BLIND = "wait-for-big-blind";
        ActionType.ANTE = "ante";
        ActionType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND = "big-and-small-blind";
        ActionType.DEAD_SMALL_BLIND = "dead-small-blind";
        ActionType.REBUY = "rebuy";
        ActionType.DECLINE_REBUY = "decline-rebuy";
        ActionType.ADD_ON = "add-on";
        return ActionType;
    })();
    data.ActionType = ActionType;
    var FutureActionType = (function () {
        function FutureActionType() {
        }
        FutureActionType.CHECK = "check";
        FutureActionType.CHECK_OR_FOLD = "check-or-fold";
        FutureActionType.CALL_CURRENT_BET = "call-current-bet";
        FutureActionType.CHECK_OR_CALL_ANY = "check-or-call-any";
        FutureActionType.CALL_ANY = "call-any";
        FutureActionType.FOLD = "fold";
        FutureActionType.RAISE = "raise";
        FutureActionType.RAISE_ANY = "raise-any";
        return FutureActionType;
    })();
    data.FutureActionType = FutureActionType;
    var Table = (function () {
        function Table(id, capacity, name) {
            this.id = id;
            this.capacity = capacity;
            this.name = name;
            this.players = new data.Map();
        }
        Table.prototype.leave = function () {
        };

        /**
        *
        * @param seat position at the table
        * @param player to add to the table
        */
        Table.prototype.addPlayer = function (seat, player) {
            if (seat < 0 || seat >= this.capacity) {
                throw "Table : seat " + seat + " of player " + player.name + " is invalid, capacity=" + this.capacity;
            }
            this.players.put(seat, player);
        };
        Table.prototype.removePlayer = function (playerId) {
            var kvp = this.players.keyValuePairs();
            for (var i = 0; i < kvp.length; i++) {
                if (kvp[i].value.id == playerId) {
                    this.players.remove(kvp[i].key);
                    return;
                }
            }
            console.log("player not found when trying to remove");
        };

        /**
        * Get a player by its player id
        * @param playerId to get
        * @return {Poker.Player} with the playerId or null if not found
        */
        Table.prototype.getPlayerById = function (playerId) {
            var players = this.players.values();
            for (var i = 0; i < players.length; i++) {
                if (players[i].id == playerId) {
                    return players[i];
                }
            }
            return null;
        };

        /**
        * Returns the number of players at the table;
        * @return {int}
        */
        Table.prototype.getNumOfPlayers = function () {
            return this.players.size();
        };
        return Table;
    })();
    data.Table = Table;
})(data || (data = {}));
//@ sourceMappingURL=Table.js.map
