"use strict";
var Animator = function() { this.init(); };
Animator.prototype = {
   animations : [],
   init : function() {

   },
   addAnimation : function(animation,delayFromLast) {
       if(typeof delayFromLast!="undefined" && this.animations.length>0) {
           var start = this.animations[this.animations.length-1].startTime;
           animation.reschedule((start+delayFromLast*1000));
       }
       this.animations.push(animation);
   },
   tick : function(currentTime) {
       if(this.animations.length==0) {
           return;
       }
       for(var i = this.animations.length-1; i>=0; i--) {

           try {
               var anim = this.animations[i];
               if(!anim.progress(currentTime)) {
                   this.animations.splice(i,1);
                   anim.ensureTargetPositions();
               }
           } catch(e) {
               this.animations.splice(i,1);
               console.log("error = " + e.message);
           }
       }
   }
};

var Animation = function(element,duration, properties, delay) {
    if(!delay) {
        delay = 0;
    }
    this.init(element,duration,properties,delay);
};
Animation.prototype = {
    element : null,
    properties : null,
    startValues : [],
    startTime : 0,
    duration  : 0,
    delay : 0,
    init : function(element,duration,properties,delay) {
        this.element = element;
        this.properties = properties;
        this.duration = duration*1000;
        this.delay = delay*1000;
        this.startTime = new Date().getTime() + this.delay;
        for(var name in properties) {
            this.startValues[name] =  this.getCurrentValue(name);
        }
    },
    reschedule : function(startTime) {
      this.startTime = startTime;
    },
    getCurrentValue : function(name) {
        var currentValue =   $(this.element).css(name);
        if(currentValue=="") {
            currentValue = "0";
        }
        currentValue = parseFloat(currentValue.replace(this.getCssUnit(name),""));
        return currentValue;
    },
    progress : function(currentTime) {
        if(this.startTime>currentTime) {
            return true;
        }
        for(var x in this.properties) {
            var remainingTime = (this.duration-this.getProgress(currentTime));
            var remainingPercentage = remainingTime/this.duration;
            this.progressProperty(x,this.properties[x],remainingPercentage)
        }


        return remainingTime>0;
    },
    ensureTargetPositions : function() {
        for(var name in this.properties) {
            this.element.style[name] = this.getCssValue(name,this.properties[name]);
        }
    },
    progressProperty : function(name, targetValue, remaining) {
        var start = this.startValues[name];
        var val = start + (targetValue-start) * (1-remaining);
        this.element.style[name] = this.getCssValue(name,val);
    },
    getCssValue : function(name, value) {
      var unit = this.getCssUnit(name);
      if(unit=="px") {
          value = Math.round(value);
      }
      return value + unit;
    },
    getCssUnit : function(name) {
      if(name == "opacity"){
          return "";
      } else {
         return "px";
      }
    },
    getProgress : function(currentTime) {
        return (currentTime-this.startTime);
    }
};