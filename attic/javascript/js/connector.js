/**
 * @fileOverview This file contains the firebase connector
 * @author <a href="http://www.cubeia.org">Peter Lundh</a>
 * @version 1.0-SNAPSHOT
 */

/**
 *  @namespace FIREBASE
 */
var FIREBASE = FIREBASE || {};
var FB_PROTOCOL = FB_PROTOCOL || {};

/**
 * Firebase Connector.
 * 
 * This constructor takes various callback functions that will be called on incoming messages or for status changes.
 * The callback functions should take one argument which is a Firebase packet if nothing else is specified.
 * 
 * When created call {@link #connect} to open a connection to Firebase.
 * 
 * @see <a href="http://cubeia.org/index.php/firebase/documentation">Firebase procotol specification</a>
 * @constructor
 * @param {function({Object})} packetCallback Packet callback function. Default handler for game specific packets and packets that are not login- or lobby related.
 * @param {function({Object})} lobbyCallback Lobby packet callback function. This handler will be called on the following packets: TableRemovedPacket, TableSnapshotListPacket, TableUpdateListPacket.
 * @param {function({Object})} loginCallback Login packet callback function. This handler will be called for the LoginResponsePacket
 * @param {function({Number})} statusCallback Status callback function. This handler will be called when the connection status changes. The status codes are defined in: {@link com.cubeia.firebase.io.ConnectionStatus}.
 */
FIREBASE.Connector = function(packetCallback, lobbyCallback, loginCallback, statusCallback) {
	    
	this._packetCallback = packetCallback;
	this._lobbyCallback = lobbyCallback;
	this._loginCallback = loginCallback;
	this._statusCallback = statusCallback;
	
	/**
	 * Connect to a Firebase instance.
	 * 
	 * @param {String} ioAdapterName the name of the actual IO adapter implementation.
	 * @param {String} hostname hostname (or ip number)
	 * @param {Number} port port
	 * @param {String} endpoint connection endpoint
	 * @param {Boolean} secure true for encrypted connection, false for plain text
	 */
	this.connect = function(ioAdapterName, hostname, port, endpoint, secure) {
		var instance = this;
		var i;
		secure = secure || false;
		
		var objectString = "new " + ioAdapterName + "(\"" + hostname + "\"," + port.toString() +",\"" + endpoint + "\"," + secure+ ");";
		this.ioAdapter = eval(objectString);
		
		this.ioAdapter.connect(instance._statusCallback, function(message) {
			var protocolObjects = JSON.parse(message.data);
			if ( protocolObjects  instanceof Array  ) {
				for (i = 0; i < protocolObjects.length; i ++ ) {
					instance._handlePacket(protocolObjects[i]);
				}
			} else {
				instance._handlePacket(protocolObjects);
			}
		});
	};
	
	/** 
	 * Handle packets.
	 * @private 
	 */
	this._handlePacket = function(protocolObject) {
		// first, parse the JSON packet payload
		
		// now, we have an Object, check the classid and call the appropriate handler
		// add handlers here for all your needs
		switch ( protocolObject.classId ) {
			case FB_PROTOCOL.LoginResponsePacket.CLASSID :   // LoginResponsePacket
				this._handleLoginResponse(protocolObject);
				break;
			/*
			 * LOBBY RELATED OBJECTS
			 */
			case FB_PROTOCOL.TableQueryResponsePacket.CLASSID :
			case FB_PROTOCOL.TableSnapshotPacket.CLASSID :
			case FB_PROTOCOL.TableUpdatePacket.CLASSID :				
			case FB_PROTOCOL.TableRemovedPacket.CLASSID :
			case FB_PROTOCOL.TableSnapshotListPacket.CLASSID :
			case FB_PROTOCOL.TableUpdateListPacket.CLASSID :
			case FB_PROTOCOL.TournamentRemovedPacket.CLASSID :
			case FB_PROTOCOL.TournamentSnapshotPacket.CLASSID :
			case FB_PROTOCOL.TournamentUpdatePacket.CLASSID :
			case FB_PROTOCOL.TournamentSnapshotListPacket.CLASSID :
			case FB_PROTOCOL.TournamentUpdateListPacket.CLASSID :
				// call lobby callback if available
				if ( this._lobbyCallback ) {
					this._lobbyCallback(protocolObject);
				// else fallback to generic packet handler 
				} else if ( this._packetCallback ) {
					this._packetCallback(protocolObject);
				}
				break;
			default:
				if ( this._packetCallback ) {
					this._packetCallback(protocolObject);
				}
				break;
		}
	};


	/**
	 * Send a packet to Firebase.
	 * @param {Object} packet the packet to send
	 */
	this.send = function(packet) {  
		if ( this.ioAdapter ) {
			this.ioAdapter.send(packet);
		}
	}; 
	
	/**
	 * Check login response 
	 * @private
	 */
	this._handleLoginResponse = function(loginResponse) {
		if ( this._loginCallback ) {
			this._loginCallback(loginResponse.status, loginResponse.pid, loginResponse.screenname);
		}
	};  
	
	/**
	 * Send login request to Firebase.
	 * @param {String} user user name
	 * @param {String} pwd password
	 * @param {Number} operatorid Operator id
	 * @param {Array} credentials byte array
	 */
	this.login = function(user, pwd, operatorid, credentials) {
		var loginRequest = new FB_PROTOCOL.LoginRequestPacket();
		loginRequest.user = user;
		loginRequest.password = pwd;
		loginRequest.operatorid = operatorid || 0;
		loginRequest.credentials = credentials || [];
		this.sendProtocolObject(loginRequest);	          
	};  
	
	/**
	 * Subscribe to a lobby path.
	 * @param gameid {Number} gameId game id
	 * @param address address lobby address (path)
	 */
	this.lobbySubscribe = function(gameId, address) {
		var subscribeRequest = new FB_PROTOCOL.LobbySubscribePacket();
		subscribeRequest.type = FB_PROTOCOL.LobbyTypeEnum.REGULAR;
		subscribeRequest.gameid = gameId;
		subscribeRequest.address = address;
		this.sendProtocolObject(subscribeRequest);
	};
	
	/**
	 * Watch the given table.
	 * @param {Number} tableId table id
	 */
	this.watchTable = function(tableId) {
		var watchRequest = new FB_PROTOCOL.WatchRequestPacket();
		watchRequest.tableid = tableId;
		this.sendProtocolObject(watchRequest);
	};
	
	/**
	 * Join the given table.
	 * @param {Number} tableId table id
	 * @param {Number} seatId seat id 
	 */
	this.joinTable = function(tableId, seatId) {
		var joinRequest = new FB_PROTOCOL.JoinRequestPacket();
		joinRequest.tableid = tableId;
		joinRequest.seat = seatId;
		this.sendProtocolObject(joinRequest);
	};
	
	/**
	 * Leave the given table.
	 * @param {Number} tableId table id
	 */
	this.leaveTable = function(tableId) {
		var leaveRequest = new FB_PROTOCOL.LeaveRequestPacket();
		leaveRequest.tableid = tableId;
		this.sendProtocolObject(leaveRequest);
	};
	
	/**
	 * Send a GameTransportPacket
	 * @param {Number} pid player id
	 * @param {Number} tableId table id
	 * @param {Array} bytearray game data 
	 */
	this.sendGameTransportPacket = function(pid, tableId, classId, byteArray) {
		var gameTransportPacket = new FB_PROTOCOL.GameTransportPacket();
		gameTransportPacket.tableid = tableId;
		gameTransportPacket.pid = pid;
		gameTransportPacket.gamedata = FIREBASE.ByteArray.toBase64String(byteArray.createGameDataArray(classId));
		this.sendProtocolObject(gameTransportPacket);
	};
	
	/**
	 * Send a protocol object to firebase
	 * @param protocolObject object to send
	 */
	this.sendProtocolObject = function(protocolObject) {
		var jsonString = FIREBASE.Styx.toJSON(protocolObject);
		this.send(jsonString);
	};
};  

