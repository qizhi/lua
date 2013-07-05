"use strict";

var Poker = Poker || {};

Poker.ContextMenu = Class.extend({
    container : null,
    init : function(event,items) {
        var container = $("#contextMenuContainer");
        if(container.length==0){
            container = $("<div/>").attr("id","contextMenuContainer").addClass("context-menu");
            $("body").append(container);
            container = $("#contextMenuContainer");
        }
        this.container = container;

        var left = Math.max(0,event.pageX - container.outerWidth());

        container.css("top",event.pageY).css("left",left);

        this.addItems(items);
        event.stopPropagation();
        $("body").click(function(e){
            container.remove();
        });
    },
    addItems : function(items) {

        var self = this;
        this.container.empty();
        var menuList = $("<ul/>")

        $.each(items,function(i,item){
            var li = $("<li/>").append("&raquo; ").append(item.title);
            li.click(function(evt){
                if(item.items) {
                    self.addItems(item.items);
                    evt.stopPropagation();
                } else {
                    item.callback(evt);
                }
            });
            menuList.append(li);
        });
        this.container.append(menuList);
    }

});