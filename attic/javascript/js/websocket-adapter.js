/**
 * @fileOverview This file contains the implementation of firebase WebSocket io adapter
 * @author <a href="http://www.cubeia.org">Peter Lundh</a>
 * @version 1.0-SNAPSHOT
 */

var WebSocket = WebSocket || {};

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
 *  @returns a new WebSocketAdapter Connector
 */
FIREBASE.WebSocketAdapter = function(hostname, port, endpoint, secure) {

	var _hostname = hostname;
	var _secure = secure !== undefined ? secure : false;
	var _endpoint = endpoint; 
	var _port = port;
	
	this.protocol = _secure ? "wss://" : "ws://";
	
	this.url = this.protocol + hostname;
	if ( port ) {
		this.url += ":" + port.toString();
	}
	if ( endpoint ) {
		this.url += "/" + endpoint;
	}
		
	/**
	 * Connect to a Firebase server
	 * @param {function({Number})} statusCallback callback function for connection status defined in: {@link FIREBASE.ConnectionStatus}.
	 * @param {function({Object})} dataCallback callback function for data
	 */
	this.connect = function(statusCallback, dataCallback) {
		
		statusCallback(FIREBASE.ConnectionStatus.CONNECTING);
		
		this.socket = new WebSocket(this.url);  
		
		this.socket.onopen = function() {  
			 statusCallback(FIREBASE.ConnectionStatus.CONNECTED);  
		}; 
 
		/** callback function when there is socket data available */
		this.socket.onmessage = function(msg) {  
			dataCallback(msg);
		};  
 
		this.socket.onclose = function(){  
			statusCallback(FIREBASE.ConnectionStatus.DISCONNECTED);
		};             
	};
	
	/**
	 * Send a message on the socket.
	 * @param {Object} message message to send
	 */
	this.send = function(message) {  
		this.socket.send(message);  
	}; 
	
	
};