"use strict";
var Poker = Poker || {};

Poker.AbstractTableButtons = Class.extend({
    view : null,
    /**
     * @type {Poker.Map}
     */
    buttons : null,

    init : function(view,actionCallback) {
        this.view = view;
        this.buttons = new Poker.Map();
    },
    show : function(actionType) {
        var button = this.buttons.get(actionType.id);
        if (button!=null) {
            button.el.show();
        } else {
            console.log("Trying to show table button for " + actionType.id + " that doesn't exist");
        }
    },
    hide : function(actionType) {
        var button = this.buttons.get(actionType.id);
        if(button!=null) {
            button.el.hide();
        } else {
            console.log("Trying to show table button for " + actionType.id + " that doesn't exist");
        }
    },
    contains : function(actionType) {
        return this.buttons.contains(actionType.id);
    },
    showAll : function() {
        var buttons = this.buttons.values();
        for(var a in buttons) {
            buttons[a].el.show();
        }
    },
    hideAll : function() {
        var buttons = this.buttons.values();
        for(var a in buttons) {
            buttons[a].el.hide();
        }
    },
    clear : function() {
        var buttons = this.buttons.values();
        for(var a in buttons) {
            buttons[a].clear();
        }
    },
    getButton : function(actionType) {
        return this.buttons.get(actionType.id);
    }

});