"use strict";
var Poker = Poker || {};
Poker.LobbyLayoutManager = Class.extend({

    /**
     * @type Poker.TemplateManager
     */
    templateManager : null,
    filters:null,
    requiredFilters : null,
    filtersEnabled : true,
    state : null,

    tournamentListSettings : {
        prefix : "tournamentItem",
        listTemplateId : "tournamentLobbyListTemplate",
        listItemTemplateId : "tournamentListItemTemplate"

    },
    sitAndGoListSettings : {
        prefix : "sitAndGoItem",
        listTemplateId : "sitAndGoLobbyListTemplate",
        listItemTemplateId : "sitAndGoListItemTemplate"
    },
    tableListSettings : {
        prefix : "tableItem",
        listTemplateId : "tableLobbyListTemplate",
        listItemTemplateId : "tableListItemTemplate"
    },

    init : function() {
        this.templateManager = Poker.AppCtx.getTemplateManager();
        this.filters = [];
        this.requiredFilters = [];
        var self = this;
        $("#cashGameMenu").click(function (e) {
            self.goToList();
            $(".main-menu a.selected").removeClass("selected");
            $(this).addClass("selected");
            new Poker.LobbyRequestHandler().subscribeToCashGames();

            $.ga._trackEvent("user_navigation", "click_cashGameMenu");
        });
        $("#sitAndGoMenu").click(function (e) {
            $(".main-menu .selected").removeClass("selected");
            $(this).addClass("selected");
            this.state = Poker.LobbyLayoutManager.SIT_AND_GO_STATE;
            new Poker.LobbyRequestHandler().subscribeToSitAndGos();
            self.goToList();
            $.ga._trackEvent("user_navigation", "click_sitAndGoMenu");
        });
        $("#tournamentMenu").click(function (e) {
            $(".main-menu .selected").removeClass("selected");
            $(this).addClass("selected");
            this.state = Poker.LobbyLayoutManager.TOURNAMENT_STATE;
            new Poker.LobbyRequestHandler().subscribeToTournaments();
            self.goToList();
            $.ga._trackEvent("user_navigation", "click_tournamentMenu");
        });

        $(".show-filters").touchSafeClick(function () {
            $(this).toggleClass("selected");
            $(".table-filter").toggleClass("hidden");
        });
        this.initFilters();
    },
    goToList : function() {
        if(Poker.AppCtx.getViewManager().mobileDevice==true) {
            $('html, body').scrollTop($("#tableListAnchor").offset().top + 50);
        }
    },
    filterUpdated : function(lobbyListData) {
        if(this.state == Poker.LobbyLayoutManager.SIT_AND_GO_STATE) {
            this.createSitAndGoList(lobbyListData);
        } else if(this.state == Poker.LobbyLayoutManager.TOURNAMENT_STATE) {
            this.createTournamentList(lobbyListData);
        } else {
            this.createTableList(lobbyListData);
        }
    },
    initFilters:function () {
        var fullTablesFilter = new Poker.LobbyFilter("fullTables", true,
            function (enabled, lobbyData) {
                if (!enabled) {
                    return lobbyData.seated < lobbyData.capacity;
                } else {
                    return true;
                }
            }, this);
        this.filters.push(fullTablesFilter);
        var emptyTablesFilter = new Poker.LobbyFilter("emptyTables", true,
            function (enabled, lobbyData) {
                if (!enabled) {
                    return lobbyData.seated > 0;
                } else {
                    return true;
                }

            }, this);

        this.filters.push(emptyTablesFilter);

        var noLimit = new Poker.PropertyStringFilter("noLimit", true, this, "type", "NL");
        this.filters.push(noLimit);

        var potLimit = new Poker.PropertyStringFilter("potLimit", true, this, "type", "PL");
        this.filters.push(potLimit);

        var fixedLimit = new Poker.PropertyStringFilter("fixedLimit", true, this, "type", "FL");
        this.filters.push(fixedLimit);

        var highStakes = new Poker.PropertyMinMaxFilter("highStakes", true, this, "smallBlind", 10, -1);

        this.filters.push(highStakes);

        var mediumStakes = new Poker.PropertyMinMaxFilter("mediumStakes", true, this, "smallBlind", 5, 9.99);
        this.filters.push(mediumStakes);

        var lowStakes = new Poker.PropertyMinMaxFilter("lowStakes", true, this, "smallBlind", -1, 4.9);
        this.filters.push(lowStakes);
        
        this.requiredFilters.push(new Poker.PrivateTournamentFilter());
    },
    includeData : function (tableData) {
        for (var i = 0; i < this.filters.length; i++) {
            var filter = this.filters[i];
            if (this.filtersEnabled == true && filter.filter(tableData) == false) {
                return false;
            }
        }
        for (var i = 0; i < this.requiredFilters.length; i++) {
            var filter = this.requiredFilters[i];
            if (filter.filter(tableData) == false) {
                return false;
            }
        }

        return true;
    },
    createTableList : function(tables) {
        this.state = Poker.LobbyLayoutManager.CASH_STATE;
        this.filtersEnabled = true;
        $(".table-filter").addClass("cash-games");
        $(".show-filters").removeClass("hidden");
        if($(".table-filter").is(":visible")) {
            $(".show-filters").addClass("selected");
        } else {
            $(".show-filters").removeClass("selected");
        }
        this.createLobbyList(tables,this.tableListSettings, this.getTableItemCallback());
    },
    createTournamentList : function(tournaments) {
        this.state = Poker.LobbyLayoutManager.TOURNAMENT_STATE;
        $(".table-filter").removeClass("cash-games");
        $(".table-filter").addClass("hidden");
        $(".show-filters").addClass("hidden");
        this.filtersEnabled = false;
        this.createLobbyList(tournaments,this.tournamentListSettings, this.getTournamentItemCallback());
    },
    createSitAndGoList : function(sitAndGos) {
        this.state = Poker.LobbyLayoutManager.SIT_AND_GO_STATE;
        this.filtersEnabled = false;
        $(".table-filter").removeClass("cash-games");
        $(".table-filter").addClass("hidden");
        $(".show-filters").addClass("hidden");
        this.createLobbyList(sitAndGos,this.sitAndGoListSettings, this.getTournamentItemCallback());
    },
    getTableItemCallback : function() {
        var self = this;
        return function(listItem){
            new Poker.TableRequestHandler(listItem.id).openTableWithName(
                listItem.capacity,self.getTableDescription(listItem));
        };
    },
    getTournamentItemCallback  : function() {
        return function(listItem){
            var tournamentManager = Poker.AppCtx.getTournamentManager();
            tournamentManager.createTournament(listItem.id,listItem.name);
        };
    },
    getTableDescription : function(data) {
        return Poker.ProtocolUtils.getTableName(data);
    },
    tableRemoved : function(tableId) {
       this.removeListItem("tableItem",tableId);
    },
    tournamentRemoved : function(tournamentId) {
        this.removeListItem("tournamentItem",tournamentId);
    },
    removeListItem : function(prefix,id) {
        console.log("REMOVING LIST ITEM WITH ID " + id);
        $("#" + prefix + id).remove();
    },
    updateListItem : function(settings, listItem, callbackFunction) {
        var self = this;
        var item = $("#" + settings.prefix + listItem.id);
        console.log(item);
        console.log("UPDATING ITEM : ");
        console.log(listItem);
        if (item.length > 0) {

            item.unbind().replaceWith(this.getTableItemHtml(settings.listItemTemplateId,listItem));
            var item = $("#" + settings.prefix + listItem.id);  //need to pick it up again to be able to bind to it
            item.touchSafeClick(function(){
                callbackFunction(listItem);
            });
        }
        console.log("update complete");
    },
    updateTableItem : function(listItem) {
        this.updateListItem(this.tableListSettings,listItem,this.getTableItemCallback());
    },

    updateTournamentItem : function(listItem) {
        this.updateListItem(this.tournamentListSettings,listItem,this.getTournamentItemCallback());
    },
    updateSitAndGoItem : function(listItem) {
        this.updateListItem(this.sitAndGoListSettings,listItem,this.getTournamentItemCallback());
    },
    createLobbyList : function(listItems, settings, listItemCallback) {
        $('#lobby').show();

        var container = $("#tableListContainer");
        container.empty();

        var template = this.templateManager.getRenderTemplate(settings.listTemplateId);

        $("#tableListContainer").html(template.render({}));

        var listContainer =  container.find(".table-list-item-container");

        var self = this;
        var count = 0;
        $.each(listItems, function (i, item) {
            if(self.includeData(item)) {
                count++;
                if(typeof(item.tableStatus)!="undefined") {
                    item.tableStatus = Poker.ProtocolUtils.getTableStatus(item.seated,item.capacity);
                }
                var html = self.getTableItemHtml(settings.listItemTemplateId,item);
                listContainer.append(html);
                $("#" + settings.prefix + item.id).touchSafeClick(function(){
                    listItemCallback(item);
                });
            }


        });
        if (count == 0) {
            $("#tableListItemContainer").append($("<div/>").addClass("no-tables").html("Currently no tables matching your criteria"));
        }
    },
    getTableItemHtml : function (templateId, data) {
        var item = this.templateManager.render(templateId, data);
        return item;
    }
});
Poker.LobbyLayoutManager.CASH_STATE = 1;
Poker.LobbyLayoutManager.TOURNAMENT_STATE = 2;
Poker.LobbyLayoutManager.SIT_AND_GO_STATE = 3;