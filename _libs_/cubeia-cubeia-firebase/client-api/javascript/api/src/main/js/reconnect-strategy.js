/**
 * @fileOverview This file contains reconnect strategy parameters
 * @author <a href="http://www.cubeia.org">Peter Lundh</a>
 * @version 1.0-SNAPSHOT
 */

/**
 *  @namespace FIREBASE
 */
var FIREBASE = FIREBASE || {};

/**
 * Reconnect strategy
 *  @class
 */
FIREBASE.ReconnectStrategy = {
    /**
     * Max number of reconnect retries
     * @default 0
     *
     * @constant
     * @static
     * @type number
     */
    MAX_ATTEMPTS : 0,

    /**
     * Reconnect start interval in millliseconds
     * @default  Infinity
     *
     * @constant
     * @static
     * @type number
     */
    RECONNECT_START_INTERVAL : 1000,

    /**
     * Interval count when strategy starts to increase reconnect retry interval
     * @default Infinity
     *
     * @constant
     * @static
     * @type number
     */
    INCREASE_THRESHOLD_COUNT : Infinity,

    /**
     * Interval increment step in milliseconds (used when INCREASE_THRESHOLD_COUNT has been reached)
     * @default 500;
     *
     * @constant
     * @static
     * @type number
     */
    INTERVAL_INCREMENT_STEP : 200
};
