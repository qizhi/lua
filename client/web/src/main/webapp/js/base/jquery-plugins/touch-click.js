"use strict";
var InstantClickListener = Class.extend({
    callFunc : null,
    moved : false,
    element : null,
    locked : false,
    id : null,

    init : function(jqEl,callFunc) {
        this.id = Math.random();
        var self = this;
        this.callFunc = callFunc;
        this.moved = false;
        jqEl.off();
        this.element = jqEl[0];
        if("ontouchstart" in window) {
            this.element.addEventListener('touchstart', this, false);
            jqEl.click(function(e){
                if(!self.isLocked()) {
                    self.lock();
                    $.proxy(callFunc, this, e)();
                    setTimeout(function(){
                        self.unlock();
                    },500);
                }
            });
        } else {
            jqEl.click(callFunc);
        }

    },
    lock : function(){
        this.locked = true;
    },
    unlock : function() {
        this.locked = false;
    },
    isLocked : function() {
        return this.locked;
    },
    handleEvent : function(e) {
        switch(e.type) {
            case 'touchstart': this.onTouchStart(e); break;
            case 'touchmove': this.onTouchMove(e); break;
            case 'touchend': this.onTouchEnd(e); break;
        }
    },

    onTouchStart : function(e) {
        this.moved = false;
        this.element.addEventListener('touchmove', this, false);
        this.element.addEventListener('touchend', this, false);
    },

    onTouchMove : function(e) {
        this.moved = true;
    },

    onTouchEnd : function(e) {
        this.element.removeEventListener('touchmove', this, false);
        this.element.removeEventListener('touchend', this, false);

        if(this.moved == false) {
            var theTarget = document.elementFromPoint(e.changedTouches[0].clientX, e.changedTouches[0].clientY);
            if(theTarget.nodeType == 3) theTarget = theTarget.parentNode;
            var theEvent = document.createEvent('MouseEvents');
            theEvent.initEvent('click', true, true);
            e.target.dispatchEvent(theEvent);
            e.stopPropagation();
            e.preventDefault();

        }
    }
});


(function( $ ) {
    $.fn.touchSafeClick = function(func) {
        return this.each(function(){
            new InstantClickListener($(this),func);
        });
    };
})( jQuery );