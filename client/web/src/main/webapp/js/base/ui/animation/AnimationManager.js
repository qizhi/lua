"use strict";
var Poker = Poker || {};

/**
 * Manages animations
 * @type {Poker.AnimationManager}
 */
Poker.AnimationManager = Class.extend({
    cssUtils : null,
    active : true,
    pendingAnimations : null,
    currentAnimations : null,
    currentId : 0,
    init : function() {
        this.pendingAnimations = [];
        this.currentAnimations = [];
        this.cssUtils = new Poker.CSSUtils();
    },
    activate : function() {
        this.active = true;
        for(var x in this.pendingAnimations) {
            this.animate(this.pendingAnimations[x]);
        }
        this.pendingAnimations = [];
    },
    removeAnimation : function(animation) {
        this.removeCurrentAnimation(animation);
        this.removePendingAnimation(animation);
    },
    removePendingAnimation : function(animation) {
        for(var i = 0; i<this.pendingAnimations.length; i++) {
            if(this.pendingAnimations[i].id == animation.id){
                this.pendingAnimations.splice(i,1);
                break;
            }
        }
    },
    removeCurrentAnimation : function(animation) {
        for(var i = 0; i<this.currentAnimations.length;i++) {
            if(this.currentAnimations[i].id == animation.id){
                this.currentAnimations.splice(i,1);
                break;
            }
        }
    },
    /**
     * Executes an animation
     * @param animation
     * @param {Number} [delay]
     */
    animate : function(animation,delay) {
        var self = this;
        if(animation.id == null) {
            animation.id = this.nextId();
        }
        // if it's a timed animation (time sensitive) and the animation manager isn't active
        // we need to store it for later activation
        if(this.active === false && animation.timed === true ) {
            this.pendingAnimations.push(animation);
            return;
        }

        //define callback that triggers the animation callback and starts the next chained animation
        var callback = function(){
            if(animation.timed==true) {
                self.removeCurrentAnimation(animation);
            }
            if(animation.callback!=null) {
                animation.callback();
            }
            if(animation.nextAnimation!=null)  {
                self.animate(animation.nextAnimation,0);
            }
        };

        //if it's a timed animation we're about to start we need to keep track of it
        //since it might get inactive when switching views
        var timeLeft = animation.getRemainingTime()>0;
        if(animation.timed === true && timeLeft) {
            this.currentAnimations.push(animation);
        } else if(animation.timed === true && !timeLeft) {
            //if the time for the animation has passed call the callback
            //since a chained animation might still be needed to run
            callback();
            return;
        }

        //build transition and transform strings and add
        animation.build();

        if(typeof(delay) == "undefined") {
            delay = 50;
        }
        //be sure it's no crap
        this.cssUtils.removeTransitionCallback(animation.element);

        animation.prepareElement();

        var animateFunc = function(){
            //add the transition properties to the element
            animation.prepare();

            //setup the animation callbacks
            self.cssUtils.addTransitionCallback(animation.element,callback);
            //if the animation manager is NOT active (view not showing)
            if(self.active==false) {
                animation.animate(); //add the transforms right away
                if(animation.callback!=null) {
                    animation.callback();  // since it's not active execute callback
                }
                if(animation.nextAnimation!=null) {
                    this.animate(animation.nextAnimation,0);
                }
            } else if(delay==0) {
                animation.animate();
            } else {
                setTimeout(function(){
                    animation.animate();
                },delay);
            }
        };
        if(animation.timed == true) {
            setTimeout(animateFunc,50);
        } else {
            animateFunc();
        }

    },
    setActive : function(active) {
        this.active = active;
        if(active===true) {
            this.activate();
        } else {
            this.deactivate();
        }
    },
    deactivate : function() {
        for(var x in this.currentAnimations) {
            this.cssUtils.removeTransitionCallback(this.currentAnimations[x].element);
            this.currentAnimations[x].cancel();
            this.pendingAnimations.push(this.currentAnimations[x]);
        }
        this.currentAnimations = [];
    },
    nextId : function() {
        return this.currentId++;
    }
});





