"use strict";
var Poker = Poker || {};
/**
 * Util class for adding css3 transforms and transition
 * attributes to DOM elements with browser specific prefixes
 * @type {*}
 */
Poker.CSSUtils = Class.extend({
    prefix : ["Moz","Webkit","O", ""],
    cssPrefix : ["-moz-","-webkit-","-o-",""],
    addTransition : function(el,transition) {
        if(typeof(el)=="undefined") {
            return;
        }
        el = this.getElement(el);
        for(var i = 0; i<this.prefix.length;i++){
            var property = this.getProperty(this.prefix[i],"Transition");

            if(typeof(el.style[property])!="undefined"){
                el.style[property] = this.cssPrefix[i] + transition;
            }
        }
    },
    getProperty : function(prefix,prop) {
        var propertyName = prefix + prop;
        if(prefix=="") {
            propertyName =  prop.charAt(0).toLowerCase() + prop.slice(1);
        }
        return propertyName;
    },
    clear : function(el) {
        this.clearTransition(el);
        this.clearTransform(el);
    },
    clearTransform : function(el) {
        if(el == null) {
            return;
        }
        el = this.getElement(el);
        for(var i = 0; i<this.prefix.length; i++) {
            var property = this.getProperty(this.prefix[i],"Transform");
            if(typeof(el.style[property])!="undefined") {
                el.style[property] = "";
            }
            property = this.getProperty(this.prefix[i],"TransformOrigin");
            if(typeof(el.style[property])!="undefined") {
                el.style[property]="";
            }

        }
    },
    clearTransition : function(el){
        if(el == null) {
            return;
        }
        el = this.getElement(el);
        for(var i = 0; i<this.prefix.length; i++) {
            var property = this.getProperty(this.prefix[i],"Transition");
            if(typeof(el.style[property])!="undefined"){
                el.style[property] = "";
            }
        }
    },
    toScale3dString : function(x,y,z) {
        return "scale3d("+x+","+y+","+z+")";
    },
    toTranslate3dString : function(x,y,z,unit) {
        return "translate3d("+this.withUnit(x,unit)+","+this.withUnit(y,unit)+","+this.withUnit(z,unit)+")";
    },
    withUnit : function(val,unit)  {
        if(val!=0) {
            if(unit == "px") {
                val = Math.round(val);
            }
            return val+unit;
        } else {
            return 0;
        }

    },
    toRotateString : function(angle) {
        return "rotate("+Math.round(angle)+"deg)";
    },
    setScale3d : function(el,x,y,z,orig) {
        if(typeof(el)=="undefined") {
            return;
        }
        if(typeof(orig)=="undefined") {
            orig = "center";
        }

        this.addTransform(el,this.toScale3dString(x,y,z),orig);
    },
    setRotate : function(el,angle) {
        if(typeof(el)=="undefined") {
            return;
        }
        this.addTransform(el,this.toRotateString(angle));
    },
    setTranslate3d : function(el,x,y,z,unit,orig) {
        if(typeof(el)=="undefined") {
            return;
        }
        if(typeof(orig)=="undefined") {
            orig = "center";
        }

        this.addTransform(el,this.toTranslate3dString(x,y,z,unit),orig);
    },
    addTransform : function(el,transform,origin)  {
        if(!el || !transform) {
            throw "Poker.CSSUtils: Illegal argument, element and transforms must be set";
        }
        el = this.getElement(el);
        for(var i = 0; i<this.prefix.length; i++) {
            var property = this.getProperty(this.prefix[i],"Transform");
            if(typeof(el.style[property])!="undefined") {
                el.style[property]=transform;
            }
            property = this.getProperty(this.prefix[i],"TransformOrigin");
            if(origin!=null && typeof(el.style[property])!="undefined") {
                el.style[property]=origin;
            }
        }
    },
    getElement : function(el) {
        if(typeof(el.length)!="undefined") {
            return el.get(0);
        }  else {
            return el;
        }

    },
    addTransitionCallback : function(element,func) {
        if(!element || !func) {
           throw "Poker.CSSUtils: Illegal argument, element and callback function must be set";
        }
        this.removeTransitionCallback(element);
        $(element).bind('webkitTransitionEnd',func);
        $(element).bind('transitionend',func);
        $(element).bind('transitionend',func);
        $(element).bind('msTransitionEnd',func);
    },
    removeTransitionCallback : function(element) {
        $(element).unbind("webkitTransitionEnd");
        $(element).unbind("transitionend");
        $(element).unbind("oanimationend");
        $(element).unbind("msTransitionEnd");
    }
});
