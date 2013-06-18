"use strict";
var Poker = Poker || {};
Poker.DialogManager = Class.extend({
    templateManager: null,
    container: null,
    currentCloseCallback: null,
    open: false,
    dialogQueue: null,
    currentDialog : null,
    init: function() {
        this.dialogQueue = [];
        this.templateManager = new Poker.TemplateManager();
        var c = $("<div/>").attr("id", "genericDialogContainer");
        $("body").append(c);
        this.container = $("#genericDialogContainer");

        var self = this;
    },
    openQueuedDialog: function() {
        if (this.dialogQueue.length > 0) {
            var d = this.dialogQueue[0];
            this.dialogQueue.splice(0, 1);
            this.displayDialog(d.dialog, d.okCallback, d.closeCallback);
        }
    },
    queueDialog: function(dialog, okCallback, closeCallback) {
        this.dialogQueue.push({
            dialog : dialog,
            okCallback: okCallback,
            closeCallback: closeCallback
        });
    },
    /**
     * Displays a generic dialog with a header, message and a continue button
     * example
     * displayManager.displayGenericDialog({header : "header" , message:"message", okButtonText : "reload"});
     * @param {Object} content - the content of the dialog, see above for format
     * @param {String} content.header - header of the dialog
     * @param {String} content.message - the message to display
     * @param {Number} content.tableId - the id of the table to use as context
     * @param {Number} content.tournamentId - the id of the tournament to use as context
     * @param {String} [content.translationKey] - optional translation key to use
     * @param {Boolean} [content.displayCancelButton] - if you should display a cancel
     * @param {Function} [okCallback] callback to execute when ok button is clicked
     */
    displayGenericDialog: function(content, okCallback) {

        if(typeof(content.translationKey)!="undefined") {
            content = $.extend(content,{ header : i18n.t("dialogs." + content.translationKey + ".header"),
                message : i18n.t("dialogs." + content.translationKey + ".message")});
        }

        var container = this.getContainer(content);

        var genericDialog = new Poker.Dialog(container,$("#genericDialog"));
        var element = genericDialog.getElement();
        if (content.header) {
            element.find("h1").html(content.header);
        } else {
            element.find("h1").hide();
        }

        if (content.message) {
            element.find(".message").html(content.message);
        } else {
            element.find(".message").hide();
        }

        if(content.displayCancelButton === true) {
            element.find(".dialog-cancel-button").show();
        } else {
            element.find(".dialog-cancel-button").hide();
        }
        var self = this;
        if (typeof(content.okButtonText) != "undefined") {
            element.find(".dialog-ok-button").html(content.okButtonText);
        }
        if (typeof(okCallback) == "undefined") {
            this.displayDialog(genericDialog, function() {
                genericDialog.close();
            }, null);
        } else {
            this.displayDialog(genericDialog, function() {
                return okCallback();
            }, null);
        }
    },
    getContainer : function(content) {

        var container = null;

        if(typeof(content.tableId)!="undefined") {
            container = this.getTableContainer(content.tableId);
        }

        if(container==null && typeof(content.tournamentId)!="undefined") {
            container = this.getTournamentLobbyContainer(content.tournamentId);
        }

        if(container==null && typeof(content.container)!="undefined") {
            container = content.container;
        }

        if(container==null) {
            container = $("body");
        }
        return container;
    },
    getTableContainer : function(tableId) {
        var table = Poker.AppCtx.getTableManager().getTable(tableId);
        if(table!=null) {
            return table.getLayoutManager().tableView;
        }
        return null;
    },
    getTournamentLobbyContainer : function(tournamentId) {
        var tournament = Poker.AppCtx.getTournamentManager().getTournamentById(tournamentId);
        console.log("table container for tournament = " + tournamentId);

        if(tournament!=null) {
            console.log(tournament.tournamentLayoutManager.viewElement);
            return tournament.tournamentLayoutManager.viewElement;
        }
        return null;
    },

    /**
     * Display a dialog by passing a DOM element id you want to be placed in
     * the dialog, if a dialog is open it will be queued and showed when
     * previous dialog is closed
     * @param {Poker.Dialog} dialog
     * @param okCallback
     * @param closeCallback
     */
    displayDialog: function(dialog, okCallback, closeCallback) {
        var self = this;
        if (closeCallback) {
            this.currentCloseCallback = closeCallback;
        }
        if(dialog.parentContainer==$("body")) {
            var targetFontSize =  Math.round(90* $(window).width()/1024);
            if (targetFontSize > 125) {
                targetFontSize = 125;
            }
        }
        var dialogElement = dialog.getElement();
        dialogElement.css({fontSize : targetFontSize + "%"});
        dialogElement.find(".dialog-cancel-button").touchSafeClick(function(){
            dialog.close();
        });
        dialogElement.find(".dialog-ok-button").touchSafeClick(function() {
            if (okCallback() || !okCallback) {
                dialog.close();
            }
        });
        dialog.show();

        this.currentDialog = dialog;

    }
});
