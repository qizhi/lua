/**
 * @namespace FIREBASE
 */
var FIREBASE = FIREBASE || {};

var org = org || {};
org.cometd = org.cometd || {};

/**
 * CometdAdapter Connector. This connector uses a callback function to access to 
 * current CometD binding since the Firebase API is independent of Dojo and JQuery.
 * 
 * @constructor
 * @param {String} hostname hostname or ip
 * @param {Number} port port
 * @param {String} endpoint endpoint, 
 * @param {Boolean} secure true for encryption, false for plain text
 * @param {Function} cometdAccess function which returns a bound CometD instance
 * @returns a new CometdAdapter Connector
 */
FIREBASE.CometdAdapter = function(hostname, port, endpoint, secure, cometdAccess) {

	// --- PRIVATE VARIABLES --- //
	
	var _hostname = hostname;
	var _secure = secure !== undefined ? secure : false;
	var _endpoint = endpoint;
	var _port = port;
	var _statusCallback;
	var _dataCallback;
	var _connected = false;
	var _firstConnect = false;
	
	
	// ---- PUBLIC VARIABLES --- //
	
	this.cometd = cometdAccess();
	this.protocol = _secure ? "https://" : "http://";
	this.firebaseUrl = this.protocol + _hostname;
	
	var _instance = this.cometd;
	
	
	// --- CONFIGURATION --- //
	
	/*
	 * Add port to URL if necesarry
	 */
	if (_port) {
		this.firebaseUrl += ":" + _port.toString();
	}
	
	/*
	 * Check and add end-point to URL
	 */
	if (_endpoint) {
		if (_endpoint.charAt(0) === "/") {
			this.firebaseUrl += _endpoint;
		} else {
			this.firebaseUrl += "/" + _endpoint;
		}
	}

	/*
	 * Unregister web socket as we will not use it, and configure
	 * comet with the Firebase URL
	 */
	_instance.unregisterTransport("websocket");
	_instance.configure({
		url : this.firebaseUrl
	});

	
	
	// --- PRIVATE FUNCTIONS --- //
	
	var _reportConnected = function() {
		_statusCallback(FIREBASE.ConnectionStatus.CONNECTED);
		_firstConnect = false;
	};

	var _reportDisconnected = function() {
		_statusCallback(FIREBASE.ConnectionStatus.DISCONNECTED);
		_firstConnect = true;
	};

	var _subscribe = function() {
		_instance.subscribe("/service/client", function(message) {
			_dataCallback({
				data : org.cometd.JSON.toJSON(message.data)
			});
		});
	};
	
	var _connect = function() {
		_statusCallback(FIREBASE.ConnectionStatus.CONNECTING);
		_instance.handshake();
	};
	
	
	// --- INITIALIZATION --- //

	_instance.addListener("/meta/handshake", function(message) {
		if (message.failure) {
			_reportDisconnected();
		}
	});

	_instance.addListener("/meta/connect", function(message) {
		if (!_instance.isDisconnected()) {
			var wasConnected = _connected;
			_connected = message.successful;
			if (!wasConnected && _connected) {
				// reconnect
				_reportConnected();
				_subscribe();
			} else if (wasConnected && !_connected) {
				// disconnect
				_reportDisconnected();
			} else if (_connected) {
				// connection succeeded
				if (_firstConnect) {
					_reportConnected();
					_subscribe();
				}
			} else {
				// connection failed
				_reportDisconnected();
			}
		}
	});


	
	
	// --- PUBLIC FUNCTIONS --- //

	/**
	 * This function is equivalent to "connect" but reuses the status and 
	 * data callbacks.
	 */
	this.reconnect = function() {
		_connect();
	};

	/**
	 * Connect to a Firebase server. This will connect cometd and setup subscriptions 
	 * on the connection meta channel and the Firebase client service. 
	 * 
	 * @param {function({Number})} statusCallback callback function for connection status defined in: {@link FIREBASE.ConnectionStatus}, mandatory
	 * @param {function({Object})} dataCallback callback function for protocol objects, mandatory
	 */
	this.connect = function(statusCallback, dataCallback) {
		_statusCallback = statusCallback;
		_dataCallback = dataCallback;
		_connect();
	};

	this.send = function(message) {
		_instance.publish("/service/client", org.cometd.JSON.fromJSON(message));
	};
};