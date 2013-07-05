"use strict";
var Poker = Poker || {};
/**
 * Handles the lobby UI
 * @type {Poker.LobbyManager}
 */
Poker.LobbyManager = Class.extend({

    /**
     * @type {Poker.LobbyData}
     */
    cashGamesLobbyData : null,

    tournamentLobbyData : null,

    listItemTemplate:null,

    currentScroll:null,

    sitAndGoState : false,

    /**
     * @type Poker.LobbyLayoutManager
     */
    lobbyLayoutManager : null,

    /**
     * @constructor
     */
    init: function () {
        var self = this;
        this.lobbyLayoutManager = Poker.AppCtx.getLobbyLayoutManager();
        this.cashGamesLobbyData = new Poker.LobbyData(new Poker.TableLobbyDataValidator(),
            function(items) {
                self.lobbyLayoutManager.createTableList(items);
            },
            function(itemId) {
                self.lobbyLayoutManager.tableRemoved(itemId);
            });


        this.tournamentLobbyData = new Poker.LobbyData(new Poker.TournamentLobbyDataValidator(),
            function(items) {
                if(self.lobbyLayoutManager.state == Poker.LobbyLayoutManager.TOURNAMENT_STATE) {
                    self.lobbyLayoutManager.createTournamentList(items);
                } else {
                    self.lobbyLayoutManager.createSitAndGoList(items);
                }
            },
            function(itemId) {
                self.lobbyLayoutManager.tournamentRemoved(itemId);
            });

    },

    handleTableSnapshotList : function (tableSnapshotList) {
        var items = [];
        for (var i = 0; i < tableSnapshotList.length; i++) {
            items.push(Poker.ProtocolUtils.extractTableData(tableSnapshotList[i]));
        }
        this.cashGamesLobbyData.addOrUpdateItems(items);
    },
    handleTournamentSnapshotList : function (tournamentSnapshotList) {
        if(tournamentSnapshotList.length>0 && tournamentSnapshotList[0].address.indexOf("/sitandgo")!=-1) {
            this.lobbyLayoutManager.state = Poker.LobbyLayoutManager.SIT_AND_GO_STATE;
        } else {
            this.lobbyLayoutManager.state = Poker.LobbyLayoutManager.TOURNAMENT_STATE;
        }

        var items = [];
        for (var i = 0; i < tournamentSnapshotList.length; i++) {
            items.push(Poker.ProtocolUtils.extractTournamentData(tournamentSnapshotList[i]));
        }
        this.tournamentLobbyData.addOrUpdateItems(items);


    },

    /**
     *
     * @param {FB_PROTOCOL.TournamentUpdatePacket[]} tournamentUpdateList
     */
    handleTournamentUpdates : function (tournamentUpdateList) {
        var items = [];
        for (var i = 0; i < tournamentUpdateList.length; i++) {
            items.push(Poker.ProtocolUtils.extractTournamentData(tournamentUpdateList[i]));
        }
        this.tournamentLobbyData.addOrUpdateItems(items);
    },

    getTableStatus:function (seated, capacity) {
        if (seated == capacity) {
            return "full";
        }
        return "open";
    },
    getBettingModel : function (model) {
        if (model == "NO_LIMIT") {
            return "NL"
        } else if (model == "POT_LIMIT") {
            return "PL";
        } else if (model == "FIXED_LIMIT") {
            return "FL";
        }
        return model;
    },
    handleTableUpdateList : function (tableUpdateList) {
        var items = [];
        for (var i = 0; i < tableUpdateList.length; i++) {
            items.push(Poker.ProtocolUtils.extractTableData(tableUpdateList[i]));
        }
        this.cashGamesLobbyData.addOrUpdateItems(items);
    },
    handleTableRemoved : function(tableId) {
        this.cashGamesLobbyData.addOrUpdateItem({ id: tableId, showInLobby: 0});
    },
    handleTournamentRemoved : function(tournamentId) {
        this.tournamentLobbyData.addOrUpdateItem({ id : tournamentId, showInLobby : 0});
    },
    clearLobby : function () {
        this.cashGamesLobbyData.clear();
        this.tournamentLobbyData.clear();
        $("#tableListContainer").empty();
    },

    getCapacity:function (id) {
        var tableData = this.cashGamesLobbyData.getItem(id);
        return tableData.capacity;
    }

});

Poker.LobbyFilter = Class.extend({
    enabled:false,
    id:null,
    filterFunction:null,
    lobbyLayoutManager:null,
    init : function (id, enabled, filterFunction, lobbyLayoutManager) {
        this.enabled = Poker.Utils.loadBoolean(id, true);
        this.id = id;
        this.filterFunction = filterFunction;
        this.lobbyLayoutManager = lobbyLayoutManager;
        var self = this;

        $("#" + id).touchSafeClick(function () {
            self.enabled = !self.enabled;
            $(this).toggleClass("active");
            Poker.Utils.store(self.id, self.enabled);
            self.filterUpdated();

        });
        if (this.enabled == true) {
            $("#" + this.id).addClass("active");
        } else {
            $("#" + this.id).removeClass("active");
        }
    },
    filterUpdated : function () {
        this.lobbyLayoutManager.filterUpdated(Poker.AppCtx.getLobbyManager().cashGamesLobbyData.getFilteredItems());
    },
    /**
     * Returns true if it should be included in the lobby and
     * false if it shouldn't
     * @param lobbyData
     * @return {boolean} if it should be included
     */
    filter:function (lobbyData) {
        return this.filterFunction(this.enabled, lobbyData);
    }
});

Poker.PropertyMinMaxFilter = Poker.LobbyFilter.extend({
    min:-1,
    max:-1,
    property:null,
    init:function (id, enabled, lobbyLayoutManager, property, min, max) {
        this.min = min;
        this.max = max;
        this.property = property;
        var self = this;
        this._super(id, enabled, function (enabled, lobbyData) {
            return self.doFilter(enabled, lobbyData);
        }, lobbyLayoutManager);

    },
    doFilter:function (enabled, lobbyData) {
        var p = lobbyData[this.property];
        if (typeof(p) != "undefined" && !this.enabled) {
            p = parseFloat(p);
            if (this.max != -1 && this.min != -1) {
                return p > this.max || p < this.min;
            } else if (this.max != -1) {
                return p > this.max;
            } else if (this.min != -1) {
                return p < this.min;
            } else {
                console.log("PropertyFilter: neither min or max is defined");
                return true;
            }
        } else {
            return true;
        }
    }
});

Poker.PropertyStringFilter = Poker.LobbyFilter.extend({
    str:null,
    property:null,
    init:function (id, enabled, lobbyLayoutManager, property, str) {
        this.property = property;
        this.str = str;
        var self = this;
        this._super(id, enabled, function (enabled, lobbyData) {
            return self.doFilter(enabled, lobbyData);
        }, lobbyLayoutManager);

    },
    doFilter : function (enabled, lobbyData) {
        var p = lobbyData[this.property];
        if (typeof(p) != "undefined" && !this.enabled) {
            return (p !== this.str);
        } else {
            return true;
        }
    }
});

Poker.PrivateTournamentFilter = Class.extend({

    filter: function(lobbyData) {
        var operatorId = Poker.SkinConfiguration.operatorId;
        var stringList = lobbyData["operatorIds"];
        if (!stringList || stringList.length == 0) {
            return true; // this is a table, or no operators set
        } else if (!operatorId) {
            // this is a private tournament, but the player has no operator (?!), deny
            return false;
        } else {
            var stringArr = stringList.split(",");
            return $.inArray(operatorId.toString(), stringArr) != -1; // allow if in array
        }
    }
});





