"use strict";
var Poker = Poker || {};
Poker.TabView = Poker.View.extend({
    selectable : true,
    tabElement : null,
    cardTemplate : null,
    templateManager : null,
    init : function(viewElement,name,tabIndex) {

        this._super(viewElement,name);
        this.templateManager = Poker.AppCtx.getTemplateManager();
        var t = "tabTemplate";
        this.cardTemplate = "miniCardTemplate";

        var item = $(this.templateManager.render(t,{name:name}));
        this.tabElement = item;
        this.tabElement.find(".mini-cards").hide();
        this.tabElement.find(".tab-index").html(tabIndex)
    },
    updateTabIndex : function(index) {
        this.tabElement.find(".tab-index").html(index);
    },
    updateName : function(name){
        this.name = name;
        this.tabElement.find(".name").html(name);
    },
    setSelectable : function (selectable) {
        if(selectable == false) {
            this.tabElement.hide();
            this.getViewElement().hide();
        } else {
            this.tabElement.show();
        }
    },
    requestFocus : function() {
        if(!this.tabElement.hasClass("active")) {
            this.tabElement.addClass("focus");
        }
    },
    hideTab : function() {
        this.tabElement.hide();
    },
    showTab : function() {
        this.tabElement.show();
    },
    updateInfo : function(data) {
        var c = data.card;
        if(c!=null) {
            var html = this.templateManager.render(this.cardTemplate,{domId:c.id + "-" + c.tableId, cardString:c.cardString});
            this.tabElement.find(".mini-cards").attr("style","").append(html);
        } else {
            this.tabElement.find(".mini-cards").empty().hide();
        }

    },
    activateTab : function() {
        this.tabElement.addClass("active");
        this.tabElement.removeClass("focus");
    },
    deactivateTab : function() {
        this.tabElement.removeClass("active");
    },
    activate : function() {
        this._super();
        this.activateTab();
    },
    deactivate : function() {
        this._super();
        this.deactivateTab();
    },
    removeTab : function() {
        this.tabElement.remove();
    },
    close : function(){
        this.removeTab();
        if(this.removeElementOnClose==true) {
            this.getViewElement().remove();
        } else {
            this.getViewElement().hide();
        }
        this.setViewElement(null);
    }

});