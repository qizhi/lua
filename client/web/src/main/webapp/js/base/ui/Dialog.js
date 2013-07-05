var Poker = Poker || {};

Poker.DialogSequence = 0;
Poker.Dialog = Class.extend({
    /**
     * @type {Poker.TemplateManager}
     */
    templateManager : null,
    parentContainer : null,
    dialogContent : null,
    dialogElement : null,
    id : null,
    settings : {
        closeOnBackgroundClick : true
    },

    /**
     * @param parentContainer
     * @param dialogId
     * @param settings.closeOnBackgroundClick
     */
    init : function(parentContainer,dialogContent,settings) {
        this.settings = $(this.settings, settings || {});
        this.dialogContent = dialogContent;
        this.templateManager = Poker.AppCtx.getTemplateManager();
        this.parentContainer = parentContainer;
        this.id = "dialog-" + Poker.DialogSequence++;
        this.render();
    },
    render : function() {
        var self = this;
        var html = this.templateManager.render("overLayDialogTemplate",{ dialogId : this.id });
        this.parentContainer.append(html);
        this.dialogElement = $("#"+this.id);

        var content = this.dialogElement.find(".dialog-content");
        content.append(this.dialogContent.html());

        var top = Math.round(0.15 * Math.min($(window).height(),this.dialogElement.height()));

        content.css("top",top + "px");

        var height = this.parentContainer.height();
        if(height === 0) {
            height = $(window).height();
        }
        this.dialogElement.height();
        this.dialogElement.hide();
    },
    show : function() {
        this.dialogElement.show();
    },
    close : function() {
        $("#"+this.id).remove();
    },
    getElement : function() {
        return this.dialogElement;
    }

});