"use strict";
var Poker = Poker || {};

/**
 * Wrapper class for css transform strings
 * @type {*}
 */
Poker.Transform = Class.extend({
    scaleVal : null,
    rotateVal : null,
    translateVal : null,

    init : function(){

    },
    getScale : function() {
        return this.scaleVal;
    },
    scale : function(x,y,z) {
        this.scaleVal = {x:x,y:y,z:z};
    },
    getRotate : function( ){
        return this.rotateVal;
    },
    rotate : function(angle) {
        this.rotateVal = angle;
    },
    translate : function(x,y,z,unit) {
        if(unit==null) {
            unit = "%";
        }
        this.translateVal = {x:x,y:y,z:z,unit:unit};
    },
    getTranslate : function() {
        return this.translateVal;
    }
});