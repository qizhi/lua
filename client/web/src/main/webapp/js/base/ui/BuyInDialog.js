"use strict";
var Poker = Poker || {};

Poker.BuyInDialog = Class.extend({
    dialogManager : null,
    dialog : null,
    templateManager : null,
    init : function() {
        this.dialogManager = Poker.AppCtx.getDialogManager();
        this.templateManager = Poker.AppCtx.getTemplateManager();
    },
    show : function(tableId,tableName, balanceInWallet, maxAmount, minAmount, currencyCode) {
        $.ga._trackEvent("user_navigation", "open_buy_in_dialogue");
        var data = {
            tableId : tableId,
            title : tableName,
            balance : balanceInWallet,
            maxAmount : maxAmount,
            minAmount : minAmount,
            currencyCode : currencyCode
        };
        var self = this;
        var table = Poker.AppCtx.getTableManager().getTable(tableId);
        var viewContainer = table.getLayoutManager().tableView;
        this.render(data,viewContainer, function(){
            var buyIn =  self.dialog.getElement().find(".buyin-amount").val();
            if (self.validateAmount(buyIn)) {
                new Poker.PokerRequestHandler(data.tableId).buyIn(buyIn)
            }
            return false; //don't close the dialog, need to wait for response
        });
        this.dialog.getElement().find(".buyin-amount").val(Poker.Utils.formatCurrency(data.maxAmount).replace(",",""));
        this.dialog.getElement().find(".buy-in-amount-errors span").hide();
        var biw = parseFloat(balanceInWallet);
        if(biw<parseFloat(minAmount) || biw == 0){
            this.dialog.getElement().find(".max-amount-container").hide();
            this.dialog.getElement().find(".insufficient-funds").show();
            this.hideInputAndBuyIn();
        } else if(parseFloat(maxAmount) <=0) {
            this.dialog.getElement().find(".too-much-funds").show();
            this.hideInputAndBuyIn();
        } else {
            this.dialog.getElement().find("max-amount-container").show();
            this.dialog.getElement().find(".input-container").show();
            this.dialog.getElement().find(".dialog-ok-button").show();
        }
    },
    hideInputAndBuyIn : function() {
        this.dialog.getElement().find(".input-container").hide();
        this.dialog.getElement().find(".dialog-ok-button").hide();
    },
    render : function(data, viewContainer,okFunction) {
        var self = this;
        var template = this.templateManager.getRenderTemplate(this.getTemplateId());
        $("#buyInDialog").html(template.render(data));
        var dialog = new Poker.Dialog(viewContainer, $("#buyInDialog"));
        this.dialogManager.displayDialog(
            dialog,
            function() {
                dialog.getElement().find(".buyin-amount").blur();
               return okFunction();
            },
            function() {
                dialog.getElement().find(".buyin-error").hide();

            });
        dialog.getElement().find(".buyin-amount").bind("keyup",function(e){
            if(e.keyCode == 13) {
                dialog.getElement().find(".dialog-ok-button").click();
            }
        }).val(data.minAmount).select();
        this.dialog = dialog;
    },
    onError : function(msg) {
        $(".buyin-error").html(msg).show();
    },
    validateAmount : function(amount) {
        return true;
    },
    close : function() {
        this.dialog.close();
    },
    getTemplateId : function() {
        return "cashGamesBuyInContent";
    }
});