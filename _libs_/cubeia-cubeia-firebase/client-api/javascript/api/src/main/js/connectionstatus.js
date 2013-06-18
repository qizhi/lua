/**
 * @fileOverview Connection status
 * @author <a href="http://www.cubeia.org">Peter Lundh</a>
 * @version 1.0-SNAPSHOT
 */

/**
 *  @namespace FIREBASE
 */
var FIREBASE = FIREBASE || {};

/**
 * Connection status
 *  @class
 */
FIREBASE.ConnectionStatus = {
    /**
     * CONNECTING = 1
     *
     * @constant
     * @static
     * @type number
     */
    CONNECTING : 1,

    /**
     * CONNECT = 2
     * @constant
     * @static
     * @type number
     */
    CONNECTED : 2,

    /**
     * DISCONNECT = 3
     * @constant
     * @static
     * @type number
     */
    DISCONNECTED : 3,

    /**
     * RECONNECTING = 4
     * @constant
     * @static
     * @type number
     */
    RECONNECTING : 4,

    /**
     * RECONNECTED = 5
     * @constant
     * @static
     * @type number
     */
    RECONNECTED : 5,

    /**
     * FAIL = 6
     * @constant
     * @static
     * @type number
     */
    FAIL : 6,

    /**
     * CANCELLED = 7
     * @constant
     * @static
     * @type number
     */
    CANCELLED : 7,

    /**
     * Return string representation
     * @static
     * @param status value to get string representation for
     * @return {String} stringified value or "undefined"
     */
    toString : function(status) {
        var key;
        for (key in this) {
            if ( this[key] === status ) {
                return key;
            }
        }
    }
};