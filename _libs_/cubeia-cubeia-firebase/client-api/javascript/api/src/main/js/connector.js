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
var utf8 = utf8 || {};

/**
 * Firebase Connector.
 *
 * This constructor takes various callback functions that will be called on incoming messages or for status changes.
 * The callback functions should take one argument which is a Firebase packet if nothing else is specified.
 *
 * When created call {@link #connect} to open a connection to Firebase.
 *
 * @see <a href="http://cubeia.org/index.php/firebase/documentation">Firebase protocol specification</a>
 * @constructor
 * @param {function({Object})} packetCallback Packet callback function. Default handler for game specific packets and packets that are not login- or lobby related.
 * @param {function({Object})} lobbyCallback Lobby packet callback function. This handler will be called on the following packets: TableRemovedPacket, TableSnapshotListPacket, TableUpdateListPacket.
 * @param {function({String} status,{Number} pid,{String} screen name)} loginCallback Login packet callback function. This handler will be called for the LoginResponsePacket
 * @param {function({Number},{Number},{String})} statusCallback Status callback function. This handler will be called when the connection status changes. The status codes are defined in: {@link com.cubeia.firebase.io.ConnectionStatus}.
 */
FIREBASE.Connector = function(packetCallback, lobbyCallback, loginCallback, statusCallback) {

    var _packetCallback = packetCallback;
    var _lobbyCallback = lobbyCallback;
    var _loginCallback = loginCallback;
    var _statusCallback = statusCallback;

    var _ioAdapter;
    var _reconnecting = false;
    var _reconnectAttempts = 0;
    var _reconnectInterval = FIREBASE.ReconnectStrategy.RECONNECT_START_INTERVAL;
    var _reconnectTimer = {};
    var _instance = this;
    var _cancelled = false;
    /**
     * Reconnect to firebase
     * Report to back if this is the first attemp (_reconnectAttempts === 0)
     * @private
     */
    var _reconnect = function() {
        clearTimeout(_reconnectTimer);
        _ioAdapter.reconnect();
    };

    /**
     * Get an object constructor traversing the window object (global scope)
     * @param className
     * @private
     */
    var getClass = function(className) {
        var nameParts = className.split(".");
        var classConstructor = window;
        var i;
        for (i = 0; i < nameParts.length; i++) {
            if (classConstructor[nameParts[i]] !== undefined) {
                classConstructor = classConstructor[nameParts[i]];
            } else {
                return undefined;
            }
        }
        return classConstructor;
    };

    /**
     * Check login response
     * @private
     */
    var _handleLoginResponse = function(loginResponse) {
        if (_loginCallback) {
            var sessionToken = null;

            _loginCallback(loginResponse.status, loginResponse.pid, loginResponse.screenname, loginResponse.credentials || null);
        }
    };

    /**
     * Handle disconnect from Firebase
     * @private
     */
    var _handleDisconnect = function() {

        if (_cancelled) {
            return;
        }

        if (!_reconnecting) {
            if (FIREBASE.ReconnectStrategy.MAX_ATTEMPTS === 0) {
                _statusCallback(FIREBASE.ConnectionStatus.DISCONNECTED, 0, "Disconnected");
                console.log("SKIPPED RECONNECTING");
                return;
            } else {
                _statusCallback(FIREBASE.ConnectionStatus.DISCONNECTED, 0, "Disconnected");
                _reconnecting = true;
                _reconnectAttempts = 1;
                _reconnectInterval = FIREBASE.ReconnectStrategy.RECONNECT_START_INTERVAL;
                console.log("START RECONNECTING");
            }
        } else {
            _reconnectAttempts++;
        }
        if (_reconnectAttempts > FIREBASE.ReconnectStrategy.MAX_ATTEMPTS) {
            _statusCallback(FIREBASE.ConnectionStatus.FAIL, _reconnectAttempts, "Too many reconnect attempts");
            _reconnecting = false;
            _cancelled = true;
            console.log("STOP RECONNECTING");
        } else {
            if (_reconnectAttempts >= FIREBASE.ReconnectStrategy.INCREASE_THRESHOLD_COUNT) {
                _reconnectInterval += FIREBASE.ReconnectStrategy.INTERVAL_INCREMENT_STEP;
            }
            console.log("Reconnect attempt " + _reconnectAttempts);
            _statusCallback(FIREBASE.ConnectionStatus.RECONNECTING, _reconnectAttempts, "Reconnecting");
            _reconnectTimer = setTimeout(_reconnect, _reconnectInterval);
        }
    };

    /**
     * Handle successfull connect to firebase
     * @private
     */
    var _handleConnect = function() {
        if (_reconnecting) {
            // report that we have reconnected
            _statusCallback(FIREBASE.ConnectionStatus.RECONNECTED, 0, "Reconnected");
            _reconnecting = false;
        }
        // TODO: should CONNECTED be reported after reconnect?
        // report that we have connected
        _statusCallback(FIREBASE.ConnectionStatus.CONNECTED, 0, "Connected");
    };


    /**
     * Handle packets.
     * @private
     */
    var _handlePacket = function(protocolObject) {
        // Check the classid and call the appropriate handler
        // add handlers here for all your needs
        switch (protocolObject.classId) {

            // always return a ping from firebase
            case FB_PROTOCOL.PingPacket.CLASSID :
                console.log("received ping from firebase");
                _ioAdapter.send(protocolObject);
                break;

            // LoginResponsePacket
            case FB_PROTOCOL.LoginResponsePacket.CLASSID :
                _handleLoginResponse(protocolObject);
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
                if (_lobbyCallback) {
                    _lobbyCallback(protocolObject);
                    // else fallback to generic packet handler
                } else if (_packetCallback) {
                    _packetCallback(protocolObject);
                }
                break;
            default:
                if (_packetCallback) {
                    _packetCallback(protocolObject);
                }
                break;
        }
    };

//DEFINED_FOR_JASMINE_TESTING_START
// Everything between the DEFINED_FOR_JASMINE_TESTING_START and DEFINED_FOR_JASMINE_TESTING_END comments will be filtered out for production
    /** @private */
    this.getHandlePacketFunction = function(packet) {
        _handlePacket(packet);
    };
//DEFINED_FOR_JASMINE_TESTING_END

    /**
     * return the io adapter object
     * (needed for tests)
     */
    this.getIOAdapter = function() {
        return _ioAdapter;
    };

    /**
     * Close the underlying connection to Firebase.
     */
    this.close = function() {
        try {
            this.cancel();
            _ioAdapter.close();
        } catch (e) {
            console.log("exception thrown when closing connection");
        }
    };

    /**
     * Cancel all pending re-connections.
     */
    this.cancel = function() {
        if (_reconnecting) {
            clearTimeout(_reconnectTimer);
        }
        _statusCallback(FIREBASE.ConnectionStatus.CANCELLED, 0, "Cancelled");
        _cancelled = true;
    };

    /**
     * Connect to a Firebase instance.
     *
     * @param {String} ioAdapterName the name of the actual IO adapter implementation.
     * @param {String} hostname hostname (or ip number)
     * @param {Number} port port
     * @param {String} endpoint connection endpoint
     * @param {Boolean} secure true for encrypted connection, false for plain text
     * @param {Object} extraConfig extra configuration, used by cometd for access to bound instance
     */
    this.connect = function(ioAdapterName, hostname, port, endpoint, secure, extraConfig) {

        var i;
        var IoAdapterClass;
        _cancelled = false;

        secure = secure || false;

        try {
            // retrieve the ioAdapter object
            if (window) {
                IoAdapterClass = getClass(ioAdapterName);
                if (IoAdapterClass === undefined) {
                    _statusCallback(FIREBASE.ConnectionStatus.FAIL, FIREBASE.ErrorCodes.INVALID_IO_ADAPTER, ioAdapterName);
                    return;
                }
                _ioAdapter = new IoAdapterClass(hostname, port, endpoint, secure, extraConfig);
            }

            // connect to firebase
            _ioAdapter.connect(function(status) {
                // status handler
                if (status === FIREBASE.ConnectionStatus.DISCONNECTED) {
                    _handleDisconnect();
                } else if (status === FIREBASE.ConnectionStatus.CONNECTED) {
                    _handleConnect();
                } else {
                    // report back if we're not in reconnecting state
                    if (!_reconnecting) {
                        _statusCallback(status);
                    }
                }
            }, function(message) {
                // message handler
                var protocolObjects = JSON.parse(message.data);

                if (typeof(protocolObjects) === Array) {
                    // handle an array of packets
                    for (i = 0; i < protocolObjects.length; i++) {
                        _handlePacket(protocolObjects[i]);
                    }
                } else {
                    // handle single packet
                    _handlePacket(protocolObjects);
                }
            });
        } catch (error) {
            _statusCallback(FIREBASE.ConnectionStatus.FAIL, FIREBASE.ErrorCodes.IO_ADAPTER_ERROR, error.message);
        }
    };


    /**
     * Send a packet to Firebase.
     * @param {Object} packet the packet to send
     */
    this.send = function(packet) {
        if (_ioAdapter) {
            _ioAdapter.send(packet);
        }
    };


    /**
     * Send login request to Firebase.
     * @param {String} user user name
     * @param {String} pwd password
     * @param {Number} operatorid Operator id, defaults to 1 if undefined.
     * @param {Array} credentials byte array
     */
    this.login = function(user, pwd, operatorid, credentials) {
        var loginRequest = new FB_PROTOCOL.LoginRequestPacket();
        loginRequest.user = user;
        loginRequest.password = pwd;
        loginRequest.operatorid = operatorid === undefined ? 1 : operatorid;
        if (credentials) {
            if (credentials instanceof FIREBASE.ByteArray) {
                loginRequest.credentials = FIREBASE.ByteArray.toBase64String(credentials.createDataArray());
            } else if (typeof(credentials.classId) === "function") {
                var byteArray = credentials.save();
                loginRequest.credentials = FIREBASE.ByteArray.toBase64String(byteArray.createGameDataArray(credentials.classId()));
            } else {
                loginRequest.credentials = credentials;
            }
        } else {
            loginRequest.credentials = [];
        }

        this.sendProtocolObject(loginRequest);
    };

    /**
     * Send logout request to Firebase.
     * @param {Boolean} leaveTables leave all joined tables
     */
    this.logout = function(leaveTables) {
        this.cancel();
        var logoutRequest = new FB_PROTOCOL.LogoutPacket();
        logoutRequest.leaveTables = leaveTables;
        this.sendProtocolObject(logoutRequest);
    };


    /**
     * Subscribe to a lobby path.
     * @param gameId {Number} gameId game id
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
     * Send a Styx protocol object to a table. This protocol
     * object send to the game using a GameTransportPacket.
     *
     * @param {Number} pid player id
     * @param {Number} tableid table id
     * @param {Object} protocolObject Styx protocol object
     */
    this.sendStyxGameData = function(pid, tableid, protocolObject) {
        var transportPacket = FIREBASE.Styx.wrapInGameTransportPacket(pid, tableid, protocolObject);
        this.sendProtocolObject(transportPacket);
    };

    /**
     * Send a string to a table as a UTF8 byte array. The string
     * will be converted to byte according to UTF8 and sent using
     * a GameTransportPacket.
     *
     * @param {Number} pid player id
     * @param {Number} tableid table id
     * @param {Object} string string to send
     */
    this.sendStringGameData = function(pid, tableid, string) {
        var bytes = utf8.toByteArray(string);
        this.sendBinaryGameData(pid, tableid, bytes);
    };

    /**
     * Send binary data to a table. The data will be sent using a
     * GameTransportPacket.
     *
     * @param {Number} pid player id
     * @param {Number} tableId table id
     * @param {Array} bytearray game data
     */
    this.sendBinaryGameData = function(pid, tableId, bytearray) {
        var gameTransportPacket = new FB_PROTOCOL.GameTransportPacket();
        gameTransportPacket.tableid = tableId;
        gameTransportPacket.pid = pid;
        gameTransportPacket.gamedata = FIREBASE.ByteArray.toBase64String(bytearray);
        this.sendProtocolObject(gameTransportPacket);
    };

    /**
     * Send a ServiceTransportPacket
     * @param {Number} pid player id
     * @param {Number} gameId gamed id
     * @param {Number} classId class id
     * @param {String} serviceContract name of service contract
     * @param {Array} byteArray game data
     */
    this.sendServiceTransportPacket = function(pid, gameId, classId, serviceContract, byteArray) {
        var serviceTransportPacket = new FB_PROTOCOL.ServiceTransportPacket();
        serviceTransportPacket.gameid = gameId;
        serviceTransportPacket.seq = -1;
        serviceTransportPacket.pid = pid;
        serviceTransportPacket.service = serviceContract;
        serviceTransportPacket.idtype = FB_PROTOCOL.ServiceIdentifierEnum.CONTRACT;
        serviceTransportPacket.servicedata = FIREBASE.ByteArray.toBase64String(byteArray.createServiceDataArray(classId));
        this.sendProtocolObject(serviceTransportPacket);
    };

    /**
     * Send a Styx protocol object to Firebase.
     *
     * @param protocolObject object to send
     */
    this.sendProtocolObject = function(protocolObject) {
        var jsonString = FIREBASE.Styx.toJSON(protocolObject);
        this.send(jsonString);
    };
};  
