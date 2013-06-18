"user strict";
var Poker = Poker || {};
/**
 *
 * @type {Poker.ProtocolUtils}
 */
Poker.ProtocolUtils = Class.extend({
    init : function(){},

    readParam : function(key,params) {
        for (var i = 0; i < params.length; i++) {
            var object = params[i];

            if (object.key == key) {
                var p = null;
                var valueArray = FIREBASE.ByteArray.fromBase64String(object.value);
                var byteArray = new FIREBASE.ByteArray(valueArray);
                if (object.type == 1) {
                    p = byteArray.readInt();
                } else {
                    p = byteArray.readString();
                }
                return p;
            }
        }
        return null;
    },
    extractTournamentData : function(snapshot) {
        var params = snapshot.params;
        var self = this;
        var param = function(name) {
            var val = self.readParam(name,params);
            if(val == null) {
                val = null;
            }
            return val;
        };

        var data = {
            id : snapshot.mttid,
            name : param("NAME"),
            speed : param("SPEED"),
            capacity : param("CAPACITY"),
            registered : param("REGISTERED"),
            biggestStack : param("BIGGEST_STACK"),
            smallestStack : param("SMALLEST_STACK"),
            averageStack : param("AVERAGE_STACK"),
            playersLeft : param("PLAYERS_LEFT"),
            buyIn : param("BUY_IN"),
            fee : param("FEE"),
            status : param("STATUS"),
            registered : param("REGISTERED"),
            startTime : param("START_TIME"),
            identifier : param("IDENTIFIER"),
            operatorIds : param("OPERATOR_IDS"),
            buyInCurrencyCode : param("BUY_IN_CURRENCY_CODE")
        };

        return data;
    },
    extractTableData : function(snapshot) {
        var params = snapshot.params;
        var self = this;
        var param = function(name) {
            var val = self.readParam(name,params);
            if(typeof(val)=="undefined" || val == null) {
                val = null;
            }
            return val;
        };
        var val = function(val) {
            if(typeof(val)!="undefined") {
                return val;
            } else {
                return null;
            }
        }

        var data = {
            id: val(snapshot.tableid),
            name: val(snapshot.name),
            speed: param("SPEED"),
            capacity: val(snapshot.capacity),
            seated: val(snapshot.seated),
            blinds: this.getBlinds(param),
            type: this.getBettingModel(param("BETTING_GAME_BETTING_MODEL")),
            tableStatus: this.getTableStatus(snapshot.seated, snapshot.capacity),
            smallBlind: param("SMALL_BLIND"),
            showInLobby : param("VISIBLE_IN_LOBBY"),
            currencyCode : param("CURRENCY_CODE")
        };

        return data;
    },
    getBlinds : function(param) {
        var sb = param("SMALL_BLIND");
        if(sb!=null) {
            return (Poker.Utils.formatCurrency(sb) + "/" + Poker.Utils.formatCurrency(param("BIG_BLIND")))
        }
        return null;
    },
    getTableName : function(data) {
        return data.name  + " " + data.blinds + " " + data.type + " " + data.capacity;
    },
    getTableStatus:function (seated, capacity) {
        if(typeof(seated)=="undefined" || typeof(capacity)=="undefined") {
            return null;
        }
        if (seated == capacity) {
            return "full";
        }
        return "open";
    },
    getBettingModel:function (model) {
        if (model == "NO_LIMIT") {
            return "NL"
        } else if (model == "POT_LIMIT") {
            return "PL";
        } else if (model == "FIXED_LIMIT") {
            return "FL";
        }
        return null;
    }
});
Poker.ProtocolUtils = new Poker.ProtocolUtils();