"use strict";
var Poker = Poker || {};
/**
 * Util class for installing mocks within the Poker namespace.
 *
 * Useful when you are testing a class that creates objects
 * that you wan to mock
 *
 * @type {Poker.MockUtils}
 */
Poker.MockUtils = Class.extend({

    orig : {},
    prefix : "Poker",
    init : function() {

    },
    /**
     * installs a mock and stores the original object to be able
     * to reset it to its original state
     *
     * @param name - the name of the class / object inside the Poker name-space
     * @param clazz  - the class to install
     */
    mock : function(name, clazz) {
        if(typeof(window[this.prefix][name])=="undefined") {
            this.orig[name] = null;
        } else {
            this.orig[name] =  window[this.prefix][name];
        }
        window[this.prefix][name] = clazz;

    },
    /**
     * Resets the mocks to it's original state since it might
     * be used by other tests
     */
    resetMocks : function() {
        for(var x in this.orig) {
            if(this.orig[x]!=null) {
                window[this.prefix][x] = this.orig[x];
            } else {
                window[this.prefix][x] = null;
            }
            delete this.orig[x];
        }
    }
});
Poker.MockUtils = new Poker.MockUtils();