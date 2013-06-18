"use strict";

var Poker = Poker || {};

/**
 * Handles lobby data (for tables/tournaments)
 * Automatically merges existing items with snapshot updates
 * @type {Poker.LobbyData}
 */
Poker.LobbyData = Class.extend({
    items : null,
    validator : null,
    notifyUpdate : false,
    onUpdate : null,
    onItemRemoved : null,
    /**
     *
     * @param {Poker.LobbyDataValidator} validator
     * @param {Function} onUpdate
     * @param {Function} onItemRemoved
     * @constructor
     */
    init : function(validator,onUpdate,onItemRemoved){
        this.items = new Poker.Map();
        this.validator = validator;
        this.onUpdate = onUpdate;
        this.onItemRemoved = onItemRemoved;
    },
    addOrUpdateItems : function(items) {
        for(var i = 0; i<items.length; i++) {
            this.addOrUpdateItem(items[i]);
        }
        if(this.notifyUpdate==true) {
            this.notifyUpdate==false;
            this.onUpdate(this.getFilteredItems());
        }
    },
    remove : function(id) {
        this.items.remove(id);
        this.onItemRemoved(id);
    },
    clear : function() {
        this.items = new Poker.Map();
        this.notifyUpdate = false;
    },
    /**
     * @param item.id
     * @param [item.showInLobby]
     */
    addOrUpdateItem : function(item) {
        if(typeof(item.id)=="undefined") {
            console.log("No id in item, don't know what to do");
            return;
        }
        if(this.validator.shouldRemoveItem(item)) {
            this.remove(item.id);
        } else {
            var current = this.items.get(item.id);
            if(current!=null) {
                current = this._update(current,item);
                this.items.put(item.id,current);
            } else {
                current = item;
                this.items.put(item.id,current);
            }
            if(this.validator.validate(current)) {
                this.notifyUpdate = true;
            }
        }

    },
    _update : function(current,update) {
        for(var x in current) {
            if(typeof(update[x])!="undefined" && update[x]!=null) {
                current[x] = update[x];
            }
        }

        return current;
    },
    /**
     * Returns items that passes the Poker.LobbyDataValidator
     * validation step
     * @return {Array}
     */
    getFilteredItems : function() {
        var items = this.items.values();
        var filtered = [];
        for(var i = 0; i<items.length; i++) {
            if(this.validator.validate(items[i])==true) {
                filtered.push(items[i]);
            }
        }
        return filtered;
    },
    getItem : function(id) {
        return this.items.get(id);
    }
});

/**
 * @type {Poker.LobbyDataValidator}
 */
Poker.LobbyDataValidator = Class.extend({
    /**
     * @param item
     * @return {Boolean}
     */
    validate : function(item) {
        return false;
    },
    shouldRemoveItem : function(item) {
        return item.showInLobby!=null && item.showInLobby == 0;
    }
});

/**
 * @type {Poker.TableLobbyDataValidator}
 * @extends {Poker.LobbyDataValidator}
 */
Poker.TableLobbyDataValidator = Poker.LobbyDataValidator.extend({
    init : function() {

    },
    validate : function(item) {
        return item.name!=null && item.capacity!=null;
    }

});

/**
 * @type {Poker.TournamentLobbyDataValidator}
 * @extends {Poker.LobbyDataValidator}
 */
Poker.TournamentLobbyDataValidator = Poker.LobbyDataValidator.extend({
    init : function() {

    },
    validate : function(item) {
        return item.name!=null && item.capacity!=null && item.status!=null && item.buyIn!=null ;
    },
    shouldRemoveItem : function(item) {
        return this._super(item) || (item.status!=null && item.status == "CLOSED");
    }

});