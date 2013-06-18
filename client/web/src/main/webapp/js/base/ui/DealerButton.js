"use strict";
var Poker = Poker || {};
Poker.DealerButton = Class.extend({
    element : null,
    showing : false,
    animationManager : null,
    lastLeft : -1,
    lastTop : -1,
    init : function(element,animationManager) {
        this.animationManager = animationManager;
        this.element = element;
        this.hide();
    },
    show : function() {
        if(this.showing==false) {
            this.showing = true;
            this.element.show();
        }
    },
    hide : function() {
      this.showing = false;
      this.element.hide();
    },
    move : function(top,left) {
        this.show();
        new Poker.TransformAnimation(this.element).addTransition("transform",1,"ease-out")
            .addTranslate3d(left,top,0,"px").start(this.animationManager);
    },
    instantMove : function(top,left) {
        new Poker.TransformAnimation(this.element).addTransition("transform",0,"linear")
            .addTranslate3d(left,top,0,"px").start(this.animationManager);
    }
});