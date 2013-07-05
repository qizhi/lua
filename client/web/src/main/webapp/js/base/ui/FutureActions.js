"use strict";
var Poker = Poker || {};

Poker.FutureActions = Class.extend({

    selectedFutureActionType : null,
    currentCallAmount : 0,
    currentRaiseAmount : 0,
    currentActions : null,
    container : null,
    allActions : null,
    init : function(container) {
        var self = this;
        this.container = container;
        this.allActions = new Poker.Map();

        $.each(this.getFutureActionTypes(),function(i,futureActionType){

            var fa = new Poker.CheckboxAction(container,$("."+futureActionType.id,container),false);
            fa.onChange(function(enabled){
                if(enabled===true) {
                    self.clear();
                    self.setSelectedFutureAction(futureActionType);
                } else {
                    self.clear();
                }
            });
            self.allActions.put(futureActionType.id,fa);
        });
    },

    setSelectedFutureAction : function(futureActionType) {
        if(futureActionType!=null) {
            this.allActions.get(futureActionType.id).setEnabled(true);
        }
        this.selectedFutureActionType = futureActionType;
    },
    /**
     * Updates the available future actions handles transitions between future actions that
     * has disappeared to one related or clears it if no related exist
     * @param futureActionTypes
     * @param callAmount
     * @param raiseAmount
     */
    setFutureActions : function(futureActionTypes, callAmount, raiseAmount) {

        if(this.selectedFutureActionType != null) {
            if(this.selectedFutureActionType.id == Poker.FutureActionType.CHECK_OR_FOLD.id && callAmount>0) {
                this.setSelectedFutureAction(Poker.FutureActionType.FOLD);
            } else if(this.selectedFutureActionType.id == Poker.FutureActionType.CALL_CURRENT_BET.id && callAmount!=this.currentCallAmount) {
                this.clear();
            } else if(this.selectedFutureActionType.id == Poker.FutureActionType.RAISE.id && raiseAmount!=this.currentRaiseAmount) {
                this.clear();
            } else if(this.selectedFutureActionType.id == Poker.FutureActionType.CHECK_OR_CALL_ANY.id && callAmount>0) {
                this.setSelectedFutureAction(Poker.FutureActionType.CALL_ANY);
            }
        }
        this.displayFutureActions(futureActionTypes,callAmount,raiseAmount);
        this.currentRaiseAmount = raiseAmount;
        this.currentCallAmount = callAmount;
    },
    displayFutureActions : function(actions,callAmount,raiseAmount) {
        this.container.show();
        this.container.find(".future-action").hide();
        for(var i = 0; i<actions.length; i++) {
            var actionContainer = this.container.find("."+actions[i].id).show();
            if(actions[i].id === Poker.FutureActionType.CALL_CURRENT_BET.id) {
                actionContainer.find(".amount").html(Poker.Utils.formatCurrency(callAmount));
            } else if(actions[i].id === Poker.FutureActionType.RAISE.id) {
                actionContainer.find(".amount").html(Poker.Utils.formatCurrency(raiseAmount));
            }
        }
    },
    getFutureActionTypes : function() {
        var types = [];
        for(var x in Poker.FutureActionType) {
            types.push(Poker.FutureActionType[x]);
        }
        return types;
    },
    /**
     * Get the action to send to the server by using the players
     * selected future action
     * returns null if the future action don't match any of the available actions
     * @param {Poker.Action[]} actions - available player actions
     */
    getAction : function(actions) {
        if(this.selectedFutureActionType == null) {
            return null;
        }
        var selectedId = this.selectedFutureActionType.id;
        switch(selectedId) {
            case Poker.FutureActionType.FOLD.id:
                return this.findAction(Poker.ActionType.FOLD,actions);

            case Poker.FutureActionType.CHECK.id:
                return this.findAction(Poker.ActionType.CHECK,actions);

            case Poker.FutureActionType.CHECK_OR_FOLD.id:
                var check = this.findAction(Poker.ActionType.CHECK,actions)
                if(check==null) {
                    return this.findAction(Poker.ActionType.FOLD,actions);
                } else {
                    return check;
                }
            case Poker.FutureActionType.CALL_CURRENT_BET.id:
                var call = this.findAction(Poker.ActionType.CALL,actions);
                if(call!=null && call.minAmount == this.currentCallAmount) {
                    return call;
                } else {
                    return null;
                }
            case Poker.FutureActionType.CHECK_OR_CALL_ANY.id:
                var check = this.findAction(Poker.ActionType.CHECK,actions);
                if(check==null) {
                    var call = this.findAction(Poker.ActionType.CALL,actions);
                    if(call!=null) {
                        return call;
                    }
                }
                return check;
            case Poker.FutureActionType.CALL_ANY.id:
                return this.findAction(Poker.ActionType.CALL,actions);

            case Poker.FutureActionType.RAISE.id:

                var raise = this.findAction(Poker.ActionType.RAISE,actions);
                if(raise==null) {
                    //if he has selected raise and only the bet action is available
                    //we do the bet without comparing the amount since it only can be one
                    //amount otherwise it would be a raise
                    return this.findAction(Poker.ActionType.BET,actions);
                }

                if(raise!=null && raise.minAmount == this.currentRaiseAmount) {
                    return raise;
                } else {
                    return null;
                }

            case Poker.FutureActionType.RAISE_ANY.id:
                var raise = this.findAction(Poker.ActionType.RAISE,actions);
                if(raise!=null) {
                    return raise;
                } else {
                    return this.findAction(Poker.ActionType.BET,actions);
                }
        }

    },
    /**
     *
     * @param  type
     * @param {Poker.Action[]} actions
     * @return {Poker.Action}
     */
    findAction : function(type,actions) {
        for(var i = 0; i<actions.length; i++) {
            if(actions[i].type.id === type.id) {
                return actions[i];
            }
        }
        return null;
    },
    clear : function() {
        this.setSelectedFutureAction(null);
        var actions = this.allActions.values();
        for(var i = 0; i<actions.length; i++) {
            actions[i].setEnabled(false);
        }

    },
    hide : function() {
        this.container.hide();
    }

});
