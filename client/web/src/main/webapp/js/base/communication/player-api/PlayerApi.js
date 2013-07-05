"use strict";

var Poker = Poker || {};

Poker.PlayerApi = Class.extend({
    baseUrl : null,

    init : function(baseUrl) {
        this.baseUrl = baseUrl;
    },
    /**
     * Retrieves the player profile for a specific player
     * @param {Number} playerId id of the player to get the profile for
     * @param {String} sessionToken authentication token to the player api
     * @param {Function} callback success callback
     * @param {Function} errorCallback error callback
     */
    requestPlayerProfile : function(playerId,sessionToken,callback,errorCallback) {
        var url = this.baseUrl + "/public/player/"+playerId+"/profile?session="+sessionToken;
        $.ajax(url, {
            method : "GET",
            contentType : "application/json",
            success : function(data) {
                callback(data);
            },
            error : function() {
                console.log("Error while fetching player profile " + url);
                if(typeof(errorCallback)!="undefined") {
                    errorCallback();
                }
            }

        });
    }

});