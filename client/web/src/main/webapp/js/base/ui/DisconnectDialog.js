"use strict";
var Poker = Poker || {};

/**
 *
 * @type {Poker.DisconnectDialog}
 */
Poker.DisconnectDialog = Class.extend({
    dialogManager : null,
    templateManager : null,
    open : false,
    dialog : null,
    init : function() {
        this.dialogManager = Poker.AppCtx.getDialogManager();
        this.templateManager = Poker.AppCtx.getTemplateManager();

    },
    show : function(count) {
        var self = this;
        $(".reconnectAttempt").html(count);
        if(this.open==false) {
            this.dialog = new Poker.Dialog($("body"),$("#disconnectDialog"));
            this.dialogManager.displayDialog(
                this.dialog,
                function() {
                    document.location.reload();
                },
                function() {
                    self.open = false;
                });
        }
        this.open  = true;

    },
    stoppedReconnecting : function() {
        $(".disconnect-reconnecting").hide();
        $(".stopped-reconnecting").show();
    },
    close : function() {
        if(this.dialog!=null) {
            this.dialog.close();
        }
        this.open = false;
    },
    getTemplateId : function() {
        return "cashGamesBuyInContent";
    }
});
