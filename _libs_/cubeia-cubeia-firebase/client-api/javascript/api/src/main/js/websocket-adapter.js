/**
 * @fileOverview This file contains the implementation of firebase WebSocket io adapter
 * @author <a href="http://www.cubeia.org">Peter Lundh</a>
 * @version 1.0-SNAPSHOT
 */
//DEFINED_FOR_JASMINE_TESTING_START
// Everything between the DEFINED_FOR_JASMINE_TESTING_START and DEFINED_FOR_JASMINE_TESTING_END comments will be filtered out for production
var WebSocket = WebSocket || {};
//DEFINED_FOR_JASMINE_TESTING_END

/**
 *  @namespace FIREBASE
 */
var FIREBASE = FIREBASE || {};

/**
 * WebSocketAdapter Connector
 *  @constructor
 *  @param {String} hostname hostname/ip
 *  @param {Number} port port
 *  @param {String} endpoint endpoint 
 *  @param {Boolean} secure true for encryption, false for plain text
 *  @param {Object} config extra configuration, not used in this adapter
 *  @returns a new WebSocketAdapter Connector
 */
FIREBASE.WebSocketAdapter = function(hostname, port, endpoint, secure, config) {

	var _hostname = hostname;
	var _secure = secure !== undefined ? secure : false;
	var _endpoint = endpoint; 
	var _port = port;
	var _statusCallback;
    var _dataCallback;
    var _instance = this;
    var _socket = {};

	this.protocol = _secure ? "wss://" : "ws://";
	
	this.url = this.protocol + hostname;
	if ( port ) {
		this.url += ":" + port.toString();
	}

	if (endpoint) {
		if(endpoint.charAt(0) === "/") {
			this.url +=  endpoint;
		} else {
			this.url += "/" + endpoint;
		}
	}

    this.getSocket = function() {
        return _socket;
    };

    var _connect = function() {
        _statusCallback(FIREBASE.ConnectionStatus.CONNECTING);

        _socket = new WebSocket(_instance.url);

        _socket.onopen = function() {
            _statusCallback(FIREBASE.ConnectionStatus.CONNECTED);
        };

        /** callback function when there is socket data available */
        _socket.onmessage = function(msg) {
            _dataCallback(msg);
        };

        _socket.onclose = function(){
            _statusCallback(FIREBASE.ConnectionStatus.DISCONNECTED);
        };
    };

	/**
	 * Connect to a Firebase server
	 * @param {function({Number})} statusCallback callback function for connection status defined in: {@link FIREBASE.ConnectionStatus}.
	 * @param {function({Object})} dataCallback callback function for data
	 */
	this.connect = function(statusCallback, dataCallback) {

        _statusCallback = statusCallback;
        _dataCallback = dataCallback;

        _connect();
    };

    this.reconnect = function() {
        _connect();
    };

	/**
	 * Send a message on the socket.
	 * @param {Object} message message to send
	 */
	this.send = function(message) {
        _socket.send(message);
	}; 
	
	
};