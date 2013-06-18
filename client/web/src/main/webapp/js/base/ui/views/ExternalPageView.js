"use strict";

var Poker = Poker || {};

Poker.ExternalPageView = Poker.TabView.extend({
    url : null,
    init : function(viewElementId,name,tabIndex,url,closeFunction) {
        this._super(viewElementId,name,tabIndex);
        this.url = url;
        var self = this;
        this.removeElementOnClose = false;
        this.getViewElement().find(".close-button").click(function(){
            closeFunction();
        });
    },
    activate : function() {
        this._super();
        this.getViewElement().find("iframe").attr("src",this.url);
    },
    deactivate : function() {
        this._super();
        this.getViewElement().find("iframe").attr("src","");
    },
    calculateSize : function(maxWidth,maxHeight, aspectRatio) {
        this.getViewElement().width(maxWidth).height(maxHeight);
    },
    calculateFontSize : function() {

    }


});