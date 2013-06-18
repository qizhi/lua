"use strict";

var Poker = Poker || {};

Poker.Navigation = Class.extend({
    /**
     * @type {Poker.Map}
     */
    views : null,
    init : function() {
        this.views = new Poker.Map();
        this.mountHandler("tournament",this.handleTournament);
        this.mountHandler("table",this.handleTable);
    },
    mountHandler : function(id,handler) {
        this.views.put(id,handler);
    },
    onLoginSuccess : function() {
        this.navigate();
    },
    navigate : function() {
        var segments = purl().fsegment();
        if(segments.length==0) {
            return;
        }
        var viewName = segments[0];
        var args = segments.slice(1);
        if(viewName!=null) {
            var handler = this.views.get(viewName);
            if(handler!=null) {
                if(args.length>0) {
                    handler.call(this,args);
                } else {
                    handler.call(this);
                }
                document.location.hash="";
            }
        }
    },
    handleTournament : function(tournamentId) {
        if(typeof(tournamentId)=="undefined") {
            return;
        }
        tournamentId = parseInt(tournamentId);
        var tournamentManager = Poker.AppCtx.getTournamentManager();
        tournamentManager.createTournament(tournamentId,"tournament");
    },
    handleTable : function(tableId) {

        if(typeof(tableId)=="undefined") {
            return;
        }
        tableId = parseInt(tableId);
        //TODO: we need snapshot to get capacity
        new Poker.TableRequestHandler(tableId).openTable(10);

    }

});