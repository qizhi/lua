var ODOBO_DEBUG = true;

describe('connector (firebase)', function() {

	describe('connect', function() {
		it('should instantiate io adapter on connect', function() {
			var connector = new FIREBASE.Connector(function() {}, function() {}, function() {}, function() {});
			
			DummyIOAdapter = function(host, port, endpoint, secure) {
				this.host = host;
				this.port = port;
				this.endpoint = endpoint;
				this.secure = secure;
				
				this.connect = function(statusCallback, dataCallback) {
					this.statusCallback = statusCallback;
					this.dataCallback = dataCallback;
				};
			};
			
			connector.connect("DummyIOAdapter", "example.org", 1234, "firebase", false);
			
			expect(connector.getIOAdapter().host).toBe("example.org");
			expect(connector.getIOAdapter().port).toBe(1234);
			expect(connector.getIOAdapter().endpoint).toBe("firebase");
			expect(connector.getIOAdapter().secure).toBe(false);
			expect(connector.getIOAdapter().statusCallback).toBeDefined();
			expect(connector.getIOAdapter().dataCallback).toBeDefined();
		});

        it('should callback with status FAIL for invalid ioAdapter', function() {

            var callbacks = {
                _status : 0,
                _errorCode : 0,
                _message : "",
                statusCallback: function(status, errorCode, message) {
                    callbacks._status = status;
                    callbacks._errorCode = errorCode;
                    callbacks._message = message;
                }
            };
            spyOn(callbacks, 'statusCallback').andCallThrough();

            var connector = new FIREBASE.Connector(function() {}, function() {}, function() {}, callbacks.statusCallback);

            connector.connect("ItDoesNotExist", "example.org", 1234, "firebase", false);
            expect(callbacks.statusCallback).toHaveBeenCalled();
            expect(callbacks._status).toBe(FIREBASE.ConnectionStatus.FAIL);
            expect(callbacks._errorCode).toBe(FIREBASE.ErrorCodes.INVALID_IO_ADAPTER);
            expect(callbacks._message).toBe("ItDoesNotExist");

        });


        it('should create message callback that handles incoming packets on connect', function() {
			var connector = new FIREBASE.Connector(function() {}, function() {}, function() {}, function() {});
			DummyIOAdapter = function(host, port, endpoint, secure) {
				this.connect = function(statusCallback, dataCallback) {
					this.dataCallback = dataCallback;
				};
			};
			connector.connect("DummyIOAdapter", "example.org", 1234, "firebase", false);
			var message = {
				data: '[{"str":"string","num":12345}]'
			};
			
			spyOn(JSON, 'parse').andReturn("packet");

			connector.getIOAdapter().dataCallback(message);
			
			expect(JSON.parse).toHaveBeenCalledWith(message.data);

		});
		
		it('should create status callback that forwards from io adapter on connect', function() {
			var callbacks = { statusCallback: function(status) {}};
			spyOn(callbacks, 'statusCallback');
			
			var connector = new FIREBASE.Connector(function() {}, function() {}, function() {}, callbacks.statusCallback);
			DummyIOAdapter = function(host, port, endpoint, secure) {
				this.connect = function(statusCallback, dataCallback) {
					this.statusCallback = statusCallback;
				};
			};
			connector.connect("DummyIOAdapter", "example.org", 1234, "firebase", false);
			
			connector.getIOAdapter().statusCallback(1337);
			
			expect(callbacks.statusCallback).toHaveBeenCalledWith(1337);
		});
		
	});
	
	
	describe('packet handling', function() {
		var callbacks = { 
			packetCallback: function(packet) {},
			loginCallback: function(packet) {},
			lobbyCallback: function(packet) {},
			statusCallback: function(packet) {}
		};
		
		var connector = undefined;
		
		beforeEach(function () {
			spyOn(callbacks, 'packetCallback');
			spyOn(callbacks, 'loginCallback');
			spyOn(callbacks, 'lobbyCallback');
			spyOn(callbacks, 'statusCallback');
			
			connector = new FIREBASE.Connector(
				callbacks.packetCallback, 
				callbacks.lobbyCallback, 
				callbacks.loginCallback, 
				callbacks.statusCallback);
		});
		
		it('should call packet callback handler for unknown packets', function() {
			var packet = { classId: 1000 };
			connector.getHandlePacketFunction(packet);
			expect(callbacks.packetCallback).toHaveBeenCalledWith(packet);
			expect(callbacks.lobbyCallback).not.toHaveBeenCalled();
			expect(callbacks.loginCallback).not.toHaveBeenCalled();
			expect(callbacks.statusCallback).not.toHaveBeenCalled();
		});
	
		it('should call login callback handler for login response packet', function() {
			var packet = { 
				classId: 11,
				status: "status",
				pid: 123,
				screenname: "gubbe"
			};
			connector.getHandlePacketFunction(packet);
			expect(callbacks.packetCallback).not.toHaveBeenCalled();
			expect(callbacks.lobbyCallback).not.toHaveBeenCalled();
			expect(callbacks.loginCallback).toHaveBeenCalledWith(packet.status, packet.pid, packet.screenname, null);
			expect(callbacks.statusCallback).not.toHaveBeenCalled();
		});
		
		it('should call lobby callback handler for lobby packets', function() {
			var packet147 = { classId: 147 };
			var packet153 = { classId: 153 };
			var packet154 = { classId: 154 };

            connector.getHandlePacketFunction(packet147);
            connector.getHandlePacketFunction(packet153);
            connector.getHandlePacketFunction(packet154);
			
			expect(callbacks.packetCallback).not.toHaveBeenCalled;
			expect(callbacks.loginCallback).not.toHaveBeenCalled();
			expect(callbacks.statusCallback).not.toHaveBeenCalled();
			expect(callbacks.lobbyCallback).toHaveBeenCalledWith(packet147);
			expect(callbacks.lobbyCallback).toHaveBeenCalledWith(packet153);
			expect(callbacks.lobbyCallback).toHaveBeenCalledWith(packet154);
		});
	});

	
	describe('login and lobby', function() {
		it('send login request packet on login call', function() {
			var connector = new FIREBASE.Connector(function() {}, function() {}, function() {}, function() {});
			
			spyOn(connector, 'send');
			connector.login("snubbe", "hemlo");
			expect(connector.send).toHaveBeenCalledWith('{"classId":10,"user":"snubbe","password":"hemlo","operatorid":1,"credentials":[]}');
		});
	
		it('send lobby subscribe packet on subscribe call', function() {
			var connector = new FIREBASE.Connector(function() {}, function() {}, function() {}, function() {});
			spyOn(connector, 'send');
			connector.lobbySubscribe(4433, "/path");
			expect(connector.send).toHaveBeenCalledWith('{"classId":145,"type":0,"gameid":4433,"address":"/path"}');
		});
		
		it('send watch table packet on watch table call', function() {
			var connector = new FIREBASE.Connector(function() {}, function() {}, function() {}, function() {});
			spyOn(connector, 'send');
			connector.watchTable(1337);
			expect(connector.send).toHaveBeenCalledWith('{"classId":32,"tableid":1337}');
		});

        it('send join table packet on join table call', function() {
            var connector = new FIREBASE.Connector(function() {}, function() {}, function() {}, function() {});
            spyOn(connector, 'send');
            connector.joinTable(624, 2);
            expect(connector.send).toHaveBeenCalledWith('{"classId":30,"tableid":624,"seat":2,"params":[]}');
        });

        it('send join leave packet on leave table call', function() {
            var connector = new FIREBASE.Connector(function() {}, function() {}, function() {}, function() {});
            spyOn(connector, 'send');
            connector.leaveTable(123);
            expect(connector.send).toHaveBeenCalledWith('{"classId":36,"tableid":123}');
        });

        it('should send a game transport packet on sendStyxGameData call', function() {
            var connector = new FIREBASE.Connector(function() {}, function() {}, function() {}, function() {});
            spyOn(connector, 'send');
            var classId = 100;
            var tableId = 33;
            var pid = 52;
            
            var betPacket = new TESTGAME.Bet();
            betPacket.betAmount = 1000;
            betPacket.betNumber = 2;
            
            // var byteArray = betPacket.save();
            // connector.sendGameTransportPacket(pid, tableId, betPacket.classId(), byteArray);

            connector.sendStyxGameData(pid, tableId, betPacket);
            
            expect(connector.send).toHaveBeenCalledWith('{"classId":100,"tableid":33,"pid":52,"gamedata":"AAAADQAAAAPoAAAAAgAAAAAA","attributes":[]}');
        });
        
        it('should send a game transport packet on sendStringGameData call', function() {
            var connector = new FIREBASE.Connector(function() {}, function() {}, function() {}, function() {});
            spyOn(connector, 'send');
            var classId = 100;
            var tableId = 33;
            var pid = 52;
            
            var str = "kalle";
            
            // var byteArray = betPacket.save();
            // connector.sendGameTransportPacket(pid, tableId, betPacket.classId(), byteArray);

            connector.sendStringGameData(pid, tableId, str);
            
            expect(connector.send).toHaveBeenCalledWith('{"classId":100,"tableid":33,"pid":52,"gamedata":"a2FsbGU=","attributes":[]}');
        });

        it('should send a game transport packet on sendBinaryGameData call', function() {
            var connector = new FIREBASE.Connector(function() {}, function() {}, function() {}, function() {});
            spyOn(connector, 'send');
            var classId = 100;
            var tableId = 33;
            var pid = 52;
            
            var bytes = utf8.toByteArray("kalle");
            
            // var byteArray = betPacket.save();
            // connector.sendGameTransportPacket(pid, tableId, betPacket.classId(), byteArray);

            connector.sendBinaryGameData(pid, tableId, bytes);
            
            expect(connector.send).toHaveBeenCalledWith('{"classId":100,"tableid":33,"pid":52,"gamedata":"a2FsbGU=","attributes":[]}');
        });
	});

});