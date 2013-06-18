"use strict";
var Poker = Poker || {};

Poker.View = Class.extend({
    viewElement : null,
    fixedSizeView : false,
    id : null,
    active : false,
    baseWidth : 1024,
    removeElementOnClose : true,
    init : function(viewElementId,name) {
        if(viewElementId!=null &&  viewElementId.charAt(0)!="#") {
            viewElementId= "#" + viewElementId;
        }
        if(viewElementId!=null) {
            this.viewElement = $(viewElementId);
        }
    },
    activate : function() {
        this.active = true;
        this.getViewElement().show();
    },
    isActive : function() {
        return this.active;
    },
    deactivate : function() {
        this.active = false;
        this.getViewElement().hide();
    },
    updateInfo : function(data) {

    },
    close : function() {
        if(this.removeElementOnClose==true) {
            this.getViewElement().remove();
        }
        this.setViewElement(null);
        this.onDeactivateView();
    },
    isClosed : function() {
        return this.getViewElement()==null;
    },
    getViewElement : function() {
        return this.viewElement;
    },
    setViewElement : function(element) {
        this.viewElement = element;
    },
    setDimensions : function(css) {
        this.getViewElement().css(css);
    },
    calculateSize : function(maxWidth, maxHeight, aspectRatio) {
        //implemented by subclasses
    },
    calculateFontSize : function() {
        var targetFontSize =  Math.round(90* this.getViewElement().width()/this.baseWidth);
        if(targetFontSize>125) {
            targetFontSize=125;
        }
        this.getViewElement().css({fontSize : targetFontSize+"%"});
    }

});




