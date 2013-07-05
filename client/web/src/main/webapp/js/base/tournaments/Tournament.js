"use strict";
var Poker = Poker || {};
Poker.Tournament = Class.extend({
    /**
     * @type Number
     */
    id : -1,
    /**
     * @type String
     */
    name : null,
    /**
     * @type Poker.TournamentLayoutManager
     */
    tournamentLayoutManager : null,

    /**
     * @type Boolean
     */
    updating : false,


    /**
     * @type Boolean
     */
    finished : false,

    /**
     *
     * @param {Number} id
     * @param {String} name
     * @param {Poker.TournamentLayoutManager} tournamentLayoutManager
     */
    init : function(id, name, tournamentLayoutManager) {
        this.id = id;
        this.name = name;
        this.tournamentLayoutManager = tournamentLayoutManager;
    }
});