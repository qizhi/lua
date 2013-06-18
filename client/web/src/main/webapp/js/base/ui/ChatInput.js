"use strict";

var Poker = Poker || {};

Poker.ChatInput = Class.extend({
    sendMessageCallback : null,

    init : function(input,sendMessageCallback) {
        this.sendMessageCallback = sendMessageCallback;
        var self = this;
        input.bind('keypress', function(e) {
            var code = (e.keyCode ? e.keyCode : e.which);
            if(code == 13) {
                self.onSendMessage($(this).val());
                $(this).val("");
            }
        }).describe();
    },
    onSendMessage : function(message) {
        this.sendMessageCallback(message);
    }
});