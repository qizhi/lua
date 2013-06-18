"use strict";

var Poker = Poker || {};

Poker.Sharing = Class.extend({
    init : function() {
    },
    bindShareTournament : function(targetElement,tournamentId, title){
        var url = Poker.OperatorConfig.getShareUrl();
        if(url==null) {
            $(targetElement).hide();
        } else {
            this.bindShareButton(targetElement,url,title,"tournament",tournamentId);
        }
    },
    bindShareTable : function(targetElement,tableId, title){
        var url = Poker.OperatorConfig.getShareUrl();
        if(url==null) {
            $(targetElement).hide();
        } else {
            this.bindShareButton(targetElement,url,title,"table",tableId);
        }
    },
    bindShareButton : function(targetElement,url,title,type,id) {
        url = this.setValues(url,type,id);
        var opts = {
            url : url,
            title : title
        };
        console.log("ADDTHIS:");
        console.log(addthis);
        console.log(addthis.button);
        addthis.button(targetElement,{},opts);
    },
    setValues : function(url, type,id) {
        return url.replace("{type}",type).replace("{id}",id);
    }
});
Poker.Sharing = new Poker.Sharing();