"use strict";
var Poker = Poker || {};

Poker.PotTransferAnimator = Class.extend({

    transfers : null,
    tableViewContainer : null,
    potContainer : null,
    potTransferTemplate : null,
    animationManager : null,
    tableId : null,
    init : function(tableId, animationManager, tableContainer,potContainer) {
        this.animationManager = animationManager;
        this.tableViewContainer = tableContainer;
        this.potContainer = potContainer;
        this.tableId = tableId;
        this.transfers = [];
    },
    addTransfer : function(seat, potId, amount) {
        this.transfers.push({seat : seat, potId : potId, amount : amount});
    },
    createAnimation : function(targetElement,amount,seatId,potId) {
        var transferId = "pt" + potId  + "-" + seatId + "-" + this.tableId;

        var html = Poker.AppCtx.getTemplateManager().
            render("potTransferTemplate",{ ptId : transferId, amount: Poker.Utils.formatCurrency(amount)});
        var potElement = this.potContainer.find(".pot-container-" + potId);
        var offset = potElement.relativeOffset(this.tableViewContainer);

        this.tableViewContainer.append($(html).css({ left: offset.left, top: offset.top}));
        var div = $("#"+transferId);

        offset =  Poker.Utils.calculateDistance(div,targetElement,true,true);
        div.css("visibility","visible");

        var animation = new Poker.TransformAnimation(div).
            addTranslate3d(offset.left,offset.top,0,"%").
            addCallback(
            function(){
                setTimeout(function(){div.remove();},1200);
            }
        );
        return animation;
    },
    start : function() {
        var self = this;
        var animations = [];
        for(var i = 0; i<this.transfers.length; i++) {
            var t = this.transfers[i];
            var a = self.createAnimation(t.seat.actionAmount, t.amount, t.seat.seatId, t.potId);
            animations.push(a);
        }
        for(var i = 0; i<this.transfers.length; i++) {
            this.potContainer.find(".pot-container-" + this.transfers[i].potId).hide();
        }
        for(var i = 0; i<animations.length; i++) {
            var delay = i*300;
            this.animationManager.animate(animations[i], delay)
        }
    }
});

