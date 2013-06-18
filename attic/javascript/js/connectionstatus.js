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
 *  @returns a new ConnectionStatus object
 */
FIREBASE.ConnectionStatus = function() {};

/**
 * CONNECTING = 1
 * 
 * @constant
 */
FIREBASE.ConnectionStatus.CONNECTING = 1;

/**
 * CONNECT = 2
 * @constant
 */
FIREBASE.ConnectionStatus.CONNECTED = 2;

/**
 * DISCONNECT = 3
 * @constant
 */
FIREBASE.ConnectionStatus.DISCONNECTED = 3;

/**
 * RECONNECTING = 4
 * @constant
 */
FIREBASE.ConnectionStatus.RECONNECTING = 4;

/**
 * RECONNECTED = 5
 * @constant
 */
FIREBASE.ConnectionStatus.RECONNECTED = 5;
