"use strict";
var Poker = Poker || {};

Poker.ViewSwiper = Class.extend({
    startXPos : 0,
    startYPos : 0,
    centerElement : null,
    leftElement : null,
    rightElement : null,
    cssUtils : null,
    nextCallback : null,
    previousCallback : null,
    completeRight : false,
    completeLeft: false,
    running : false,
    swiped : false,
    animationManager : null,
    transitioning : false,
    minSwipe : 6,
    lockSwipe : false,


    init : function(swipeElement,nextCallback,previousCallback) {
        var self = this;
        this.cssUtils = new Poker.CSSUtils();
        this.nextCallback = nextCallback;
        this.previousCallback = previousCallback;

        swipeElement.bind("touchstart",function(e){
            if(!Poker.Settings.isEnabled(Poker.Settings.Param.SWIPE_ENABLED)){
                return;
            }
            if(e.originalEvent.touches.length>0){
                var startTouch = e.originalEvent.touches[0];
                self.swiped = false;
                if(!self.isLocked()) {
                    self.startXPos = startTouch.pageX;
                    self.startYPos = startTouch.pageY;

                    $(this).bind("touchmove",function(e){
                        var touch = e.originalEvent.touches[0];
                        var movedY = touch.pageY - self.startYPos;
                        var movedX = touch.pageX - self.startXPos;

                        if(movedX<10) {
                            e.preventDefault();
                            self.swiped = true;
                            self.moveLeft(-movedX);
                        } else if(movedX>10) {
                            e.preventDefault();
                            self.swiped = true;
                            self.moveRight(movedX);
                        }
                    });

                    $(this).bind("touchend",function(){
                        if(self.swiped==true) {
                            self.lock();
                            self.end();
                        }
                        $(this).unbind('touchmove');
                        $(this).unbind('touchend');


                    });
                }
            }
        });


    },
    unlock : function() {
        this.lockSwipe = false;
    },
    isLocked : function() {
        return this.lockSwipe;
    },
    lock : function() {
       if(this.lockSwipe==false) {
           this.lockSwipe = true;
           this.switchNext=false;
           this.switchPrevious=false;
           return true;
       } else {
           return false;
       }

    },
    setElements : function(left,center,right) {
        var width = $(".view-port").width();
        this.animationManager = new Poker.AnimationManager();
        this.leftElement = left!=null ? left.getViewElement() : null;
        this.centerElement = center!=null ? center.getViewElement() : null;
        this.rightElement = right!=null ? right.getViewElement() : null;

        if(this.leftElement!=null) {
            this.cssUtils.clearTransition(this.leftElement);
            this.cssUtils.setTranslate3d(this.leftElement,-width,0,0,"px");
        }

        if(this.rightElement!=null) {
            this.cssUtils.clearTransition(this.rightElement);
            this.cssUtils.setTranslate3d(this.rightElement,width,0,0,"px");
        }

        if(this.centerElement!=null) {
            this.cssUtils.clearTransition(this.centerElement);
            this.cssUtils.setTranslate3d(this.centerElement,0,0,0,"px");
        }

    },
    end : function() {
        this.startXPos = 0;
        if(this.completeRight==true) {
            this.completeRight=false;
            this.finishRight();
        } else if(this.completeLeft==true) {
            this.completeLeft=false;
            this.finishLeft();
        } else {
            //when there was no swipe, transition views back to 0
            this.moveToOriginalPositions();
        }
    },
    moveToOriginalPositions : function() {
        var self = this;
        var width = $(".view-port").width();
        new Poker.TransformAnimation(this.centerElement).
            addTransition("transform",0.2,"ease-out").
            addCallback(function(){self.unlock();}).
            addTranslate3d(0,0,0,"px").
            start(this.animationManager);


        if(this.rightElement!=null) {
            new Poker.TransformAnimation(this.rightElement).
                addTransition("transform",0.2,"ease-out").
                addTranslate3d(width,0,0,"px").
                start(this.animationManager);
        }

        if(this.leftElement!=null) {
            new Poker.TransformAnimation(this.leftElement).
                addTransition("transform",0.2,"ease-out").
                addTranslate3d(-width,0,0,"px").
                start(this.animationManager);
        }
    },
    reset : function() {
        this.cssUtils.clear(this.leftElement);
        this.cssUtils.clear(this.rightElement);
        this.cssUtils.clear(this.centerElement);
    },
    finishRight : function() {
        var self = this;
        var width = $(".view-port").width();
        new Poker.TransformAnimation(this.leftElement).
            addTransition("transform",0.5,"ease-out").
            addTranslate3d(0,0,0,"px").
            start(this.animationManager);

        new Poker.TransformAnimation(this.centerElement).
            addCallback(function(){self.rightCallBack();}).
            addTransition("transform",0.5,"ease-out").
            addTranslate3d(width,0,0,"px").
            start(this.animationManager);


    },
    rightCallBack : function() {
        this.previousCallback();
        this.unlock();
    },
    leftCallBack : function() {
        this.nextCallback();
        this.unlock();

    },
    finishLeft : function() {
        var self = this;
        this.transitioning = true;

        new Poker.TransformAnimation(this.centerElement).
            addTransition("transform",0.5,"ease-out").
            addTranslate3d(-this.centerElement.width(),0,0,"px").
            addCallback(function(){self.leftCallBack()}).
            start(this.animationManager);

        new Poker.TransformAnimation(this.rightElement).
            addTransition("transform",0.5,"ease-out").
            addTranslate3d(0,0,0,"px").
            start(this.animationManager);
    },
    moveLeft : function(distance) {
        if(this.rightElement==null ) {
            distance = Math.min(distance,this.centerElement.width()/this.minSwipe);
        }
        this.cssUtils.setTranslate3d(this.centerElement,-distance,0,0,"px");
        if(this.rightElement==null) {
            return;
        }
        if(distance>(this.centerElement.width()/this.minSwipe)) {
            this.completeLeft = true;
            this.completeRight = false;
        } else {
            this.completeLeft  = false;
        }

        var pos = this.rightElement.width() - distance;
        this.rightElement.show();
        this.cssUtils.setTranslate3d(this.rightElement,pos,0,0,"px");
    },
    moveRight : function(distance) {
        if(this.leftElement==null ) {
            distance = Math.min(distance,this.centerElement.width()/this.minSwipe);
        }
        this.cssUtils.setTranslate3d(this.centerElement,distance,0,0,"px");

        if(this.leftElement==null) {
            return;
        }
        if(distance>(this.centerElement.width()/this.minSwipe)) {
            this.completeRight = true;
            this.completeLeft=false;
        } else {
            this.completeLeft= false;
        }

        var pos = -this.leftElement.width()+distance;
        this.leftElement.show();
        this.cssUtils.setTranslate3d(this.leftElement,pos,0,0,"px");

    }

});