"use strict";
var Poker = Poker || {};

/**
 * A simple map built on associative arrays,
 * numeric keys are converted to strings
 * @type {Poker.Map}
 */
Poker.Map = Class.extend({
   holder : null,
   length : 0,
   init : function() {
        this.holder = new Object();
        this.length = 0;
   },
    /**
     * Retrieves the number of elements the Map
     * @return {Number}
     */
   size : function() {
     return this.length;
   },
   contains : function(key) {
       var key = this._key(key);
       return this.get(key)!=null;
   },
    /**
     *
     * @param key
     * @return {*}
     * @private
     */
   _key : function(key) {
        if(key===undefined) {
             throw "Key must not be undefined";
        } else if(typeof(key)=="number")  {
             return ""+key;
        } else {
            return key;
        }
   },
    /**
     * Puts/replaces a value in the map with the specific key
     * @param key
     * @param val
     * @return {Object} if there already was an object associated to the key
     * it will be returned otherwise null
     */
   put: function(key,val) {
       key = this._key(key);
       var existing = null;
       if(this.holder[key]!==undefined) {
           existing = this.holder[key];
       }
       this.holder[key] = val;

        if(existing==null) {
            this.length++;
        }


       return existing;
   },
    /**
     * Get a values by its key, null if no values are associated with the specified key
     * @param key - key of the value to return
     * @return {Object}
     */
   get : function(key) {
       key = this._key(key);
       if(this.holder[key]!==undefined) {
           return this.holder[key];
       } else {
           return null;
       }
   },
    /**
     * Removes and returns a values associated to the supplied key,
     * null if there was no value with
     * @param key
     * @return {Object}
     */
   remove : function(key) {
       key = this._key(key);
        if(key!==undefined) {
            if(this.holder[key]!==undefined) {
                var val = this.holder[key];
                delete this.holder[key];
                this.length--;
                return val;
            }
        }
        return null;

   },
    /**
     * Get an array of the values in this map
     * @return {Array}
     */
   values : function() {
       var values = new Array();
       for(var v in this.holder) {
            values.push(this.holder[v]);
       }
       return values;
   },
    /**
     * Gets the keys of the values as an array
     * @return {Array}
     */
   keys : function() {
       var keys = new Array();
       for(var v in this.holder) {
           keys.push(v);
       }
       return keys;
   },
    /**
     * Gets a key value pair { key : k, value : v } array with the key-value pairs
     * @return {Array}
     */
   keyValuePairs : function() {
       var valuePairs = new Array();
       for(var k in this.holder) {
           valuePairs.push({key : k, value : this.holder[k]});
       }
       return valuePairs;
   }
});