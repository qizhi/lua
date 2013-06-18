"use strict";

var Poker = Poker || {};
Poker.MockEventManager = Class.extend({
    events : null,
    currentPosition : 0,
    playing : false,
    defaultDelay : 500,
    delayOverride : -1,
    beforeFunc : null,
    cleanUpFunc : null,
    stopAt : -1,
    init : function(beforeFunc, cleanUpFunc) {
        this.beforeFunc = beforeFunc;
        this.cleanUpFunc = cleanUpFunc;
        this.events = [];
        var div = $("<div/>").hide().attr("id","mockEventPlayer");
        div.draggable({height:200});
        div.append(this.createToolsPanel());

        div.append($("<ul/>").attr("id","mockEvents"));
        $("body").append(div);
        $("#mockEventPlayer").show();
        this.beforeFunc();

    },
    setBeforeFunc : function(beforeFunc) {
        this.beforeFunc = beforeFunc;
    },
    setCleanUpFunc : function(cleanUpFunc) {
        this.cleanUpFunc = cleanUpFunc;
    },
    createToolsPanel : function() {
        var self = this;
        var toolbar = $("<div/>").addClass("player-panel");
        this.createToolBarButton(toolbar,"play-previous",function(){self.playPrevious();});
        this.createToolBarButton(toolbar,"play",function(){self.play();});
        this.createToolBarButton(toolbar,"pause",function(){self.pause();});
        this.createToolBarButton(toolbar,"play-next",function(){self.playNext();});
        this.createToolBarButton(toolbar,"maximize",function(){self.maximize();},true);
        this.createToolBarButton(toolbar,"minimize",function(){self.minimize();});

        return toolbar;
    },
    maximize : function() {
        $("#mockEventPlayer .minimize").show();
        $("#mockEventPlayer .maximize").hide();
        $("#mockEventPlayer").height(250+"px").find("ul").show();
    },
    minimize : function() {
        $("#mockEventPlayer .minimize").hide();
        $("#mockEventPlayer .maximize").show();
        $("#mockEventPlayer").height(20+"px").find("ul").hide();
    },
    createToolBarButton : function(container,clazz,func,hidden) {
        var b = $("<div/>").addClass(clazz).click(function(){
            container.find(".active").removeClass("active");
            $(this).addClass("active");
            func();
        });
        if(hidden) {
            b.hide();
        }
        container.append(b);
    },
    addEvent : function(event) {
        var self = this;
        var id = this.events.length;
        this.events.push(event);
        var ev = $("<li/>").attr("id","mockEvent-"+id)
        var cb = $("<input/>").attr("type","checkbox").attr("checked","checked");
        var play = $("<div/>").addClass("play").click(function(e){
            self.playTo(id+1);
        });
        cb.change(function(e){
            if($(this).is(":checked")) {
                event.enabled = true;
            } else {
                event.enabled = false;
            }
            ev.toggleClass("disabled");
        });
        ev.append(play).append(cb).append(event.name);

        $("#mockEvents").append(ev);

    },
    playTo : function(pos) {
        var self = this;
        var restart = false;
        if(pos<this.currentPosition) {
            restart = true;
        }
        this.stopAt = pos;

        if(restart==true) {
            this.currentPosition=0;
            this.cleanUpFunc();
            var self = this;

            setTimeout(function(){
                self.beforeFunc();
                self.play();

            },500)

        } else {
            self.play();
        }
        $(".player-panel").find(".active").removeClass("active");
        $(".player-panel").find(".play").addClass("active");

    },
    pause : function() {
        this.playing = false;
    },
    playPrevious : function() {
        if(this.currentPosition==0) {
            return;
        }
        this.playTo(this.currentPosition-1);


    },
    play : function() {
        console.log("Playing mock events");
        this.playing = true;
        this.nextEvent();
    },
    playNext : function() {
        if(this.events.length<=this.currentPosition) {
            return;
        }
        this.events[this.currentPosition].func();
        $("#mockEvents .current").removeClass("current");

        $("#mockEvent-"+this.currentPosition).addClass("current");
        this.currentPosition++;
    },
    nextEvent : function() {
        if(this.playing == false) {
            return;
        }
        if(this.events.length<=this.currentPosition) {
            this.playing = false;
            $(".player-panel").find(".active").removeClass("active");
            return;
        }
        if(this.stopAt == this.currentPosition) {
            $(".player-panel").find(".active").removeClass("active");
            $(".player-panel").find(".pause").addClass("active");
            this.stopAt = -1;
            this.playing = false;
            return;
        }

        var event = this.events[this.currentPosition];
        if(event.enabled==false) {
            this.currentPosition++;
            this.nextEvent();
            return;
        }
        console.log("next event");
        var delay = this.defaultDelay;
        if(this.stopAt!=-1) {
            delay = 0;
        }
        if(typeof(event.delay)!="undefined") {
            delay = event.delay;
        }
        this.playNext();
        var self = this;
        setTimeout(function(){
            self.nextEvent();
        },delay);
    }
});

Poker.MockEvent = Class.extend({
    name : null,
    func : null,
    delay : 0,
    enabled : true,
    init : function(name,func,delay) {
        this.name = name;
        this.func = func;
        this.delay = delay;
    }
});