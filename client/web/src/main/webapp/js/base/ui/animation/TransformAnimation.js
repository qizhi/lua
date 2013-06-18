"use strict";
var Poker = Poker || {};

/**
 * Handles an animation based on css transforms and transitions
 *
 * usage:
 *
 *   new Poker.TransformAnimation(domElement).
 *      addTransition("scale",1,"ease-out).
 *      addRotate(200).
 *      addCallback(function(){ //do when transition done; }).
 *      start(new Poker.AnimationManger());
 *
 *
 * @type {Poker.TransformAnimation}
 */
Poker.TransformAnimation = Poker.Animation.extend({
    transform : null,
    startTransform : null,
    transitionProperty : null,
    transitionEasing : null,
    origin : null,
    cssUtils : null,
    init : function(element) {
        this._super(element);
        this.cssUtils = new Poker.CSSUtils();
        this.transform = new Poker.Transform();
        this.startTransform = new Poker.Transform();
        this.startTransform.scale(1,1,1);
        this.startTransform.rotate(0);
        this.startTransform.translate(0,0,0);
    },
    /**
     * adds  default transform values if they exist
     * and the transition string to the associated dom element(s)
     */
    prepare : function() {
        if(this.transitionProperty!=null) {
            this.cssUtils.addTransition(this.element,this.getCalculatedTransition());
        }
    },
    prepareElement : function() {
        if(this.timed==true) {
            this.cssUtils.clear(this.element);
            this.setTimedStartTransform();
        }
    },
    setTimedStartTransform : function() {
        var currentTransitionTime = this.getRemainingTime();
        var originalTransitionTime = this.transitionTime;
        var remaining = currentTransitionTime / originalTransitionTime;
        var transform = "";


        if(this.transform.getScale()!=null) {
            var s = this.transform.getScale();
            var start = this.startTransform.getScale();
            var x = (start.x - s.x) * remaining + s.x;
            var y = (start.y - s.y) * remaining + s.y;
            var z = (start.z - s.z) * remaining + s.z;
            this.cssUtils.setScale3d(this.element,x,y,z,this.origin);

        } else if(this.transform.getTranslate()!=null) {
            //throw " currentTT = " + currentTransitionTime + ", originalTT="+originalTransitionTime + ", remaining="+remaining;

            var t = this.transform.getTranslate();
            var start = this.startTransform.getTranslate();
            var x = (start.x - t.x) * remaining + t.x;
            var y = (start.y - t.y) * remaining + t.y;
            var z = (start.z - t.z) * remaining + t.z;

            this.cssUtils.setTranslate3d(this.element,x,y,z,start.unit,this.origin);

        } else if(this.transform.getRotate()!=null) {
            var r = this.transform.getRotate();
            var start = this.startTransform.getRotate();
            var angle = (start - r) * remaining + r;

            this.cssUtils.setRotate(this.element,angle);
        }

    },
    /**
     * Adds the transform to use for its transition
     */
    animate : function() {
        if(this.getTransform()!=null) {
            this.cssUtils.addTransform(this.element,this.getTransform(),this.origin);
        }
    },
    addTransform : function(transform) {
        this.transform = transform;
        return this;
    },

    getTransform : function() {
        var transform = "";
        if(this.transform.getScale()!=null) {
            var s = this.transform.getScale();
            transform+= this.cssUtils.toScale3dString(s.x, s.y, s.z);
        }
        if(this.transform.getTranslate()!=null) {
            var t = this.transform.getTranslate();
            transform+=this.cssUtils.toTranslate3dString(t.x, t.y, t.z, t.unit);
        }
        if(this.transform.getRotate()!=null){
            transform+=this.cssUtils.toRotateString(this.transform.getRotate());
        }

        if(transform=="") {
            return null;
        } else {
            return transform;
        }
    },
    addStartScale : function(x,y,z) {
        this.startTransform.scale(x,y,x);
        return this;
    },
    addStartRotate : function(angle) {
        this.startTransform.rotate(angle);
        return this;
    },
    addStartTranslate : function(x,y,z,unit) {
        this.startTransform.translate(x,y,z,unit);
        return this;
    },
    addScale3d : function(x,y,z) {
        this.transform.scale(x,y,z);
        return this;
    },
    addTranslate3d : function(x,y,z,unit){
        this.transform.translate(x,y,z,unit);
        return this;
    },
    addRotate : function(angle) {
        this.transform.rotate(angle);
        return this;
    },
    addTransition : function(property, time, easing) {
        this.transitionProperty = property;
        this.transitionTime = time;
        this.transitionEasing = easing;
        return this;
    },
    next : function(el) {
        this.nextAnimation = new Poker.TransformAnimation(el || this.element);
        this.nextAnimation.startTime = this.startTime + this.transitionTime*1000;
        return this.nextAnimation;
    },
    addOrigin : function(origin) {
        this.origin = origin;
        return this;
    },
    /*
     * Returns a transition string based on the transition time and how much
     * time has passed since it was created.
     * @return {String}
     */
    getCalculatedTransition : function() {
        return this.transitionProperty + " " + this.getRemainingTime() + "s " + this.transitionEasing;
    },

    build : function() {
        if(this.nextAnimation!=null) {
            this.nextAnimation.build();
        }
    }
});