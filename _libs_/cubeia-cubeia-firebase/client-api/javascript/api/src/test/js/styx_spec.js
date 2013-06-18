describe('the styx object', function() {
	
	describe('Test wrapInGameTransportPacket' , function() {
		var x = new FB_PROTOCOL.CreateTableRequestPacket();
		var packet = FIREBASE.Styx.wrapInGameTransportPacket(1, 2, x);
		expect(packet.pid).toBe(1);
		expect(packet.tableid).toBe(2);
	});
	
	describe('Test isByteArray', function() {
	 	var testArray = [1,2,3,4];
	 	var testArray2 = [1,2,3,300];
	 	var testArray3 = [1,2,3,"3"];
	 	
	 	it('[1,2,3,4] should be true', function() {
            expect(FIREBASE.Styx.isByteArray(testArray)).toBe(true);
        });
        
        it('[1,2,3,300] should be false', function() {
            expect(FIREBASE.Styx.isByteArray(testArray2)).toBe(false);
        });
        
        it('[1,2,3,"3"] should be false', function() {
            expect(FIREBASE.Styx.isByteArray(testArray3)).toBe(false);
        });

    });
	
	describe('Test cloneObject', function() {
		var createTableRequest = new FB_PROTOCOL.CreateTableRequestPacket();
		var param1 = new FB_PROTOCOL.Param();
		var param2 = new FB_PROTOCOL.Param();
		var objectClone;
		var byteArray = [];
		
		FIREBASE.Styx.writeParam(param1, "string", "12345");
		FIREBASE.Styx.writeParam(param2, "int", 12345);
		
		it('FIREBASE.Styx.readParam(param1) should return "12345"', function() {
			var result = FIREBASE.Styx.readParam(param1); 
            expect(result).toBe("12345");
            expect(typeof(result)).toBe("string");
        });

		it('FIREBASE.Styx.readParam(param2) should return 12345', function() {
			var result = FIREBASE.Styx.readParam(param2); 
            expect(result).toBe(12345);
            expect(typeof(result)).toBe("number");
        });

		createTableRequest.seq = 1; 
		createTableRequest.gameid = 1337;
		createTableRequest.seats = 1;
		createTableRequest.params.push(param1);
		createTableRequest.params.push(param2);

		objectClone = FIREBASE.Styx.cloneObject(createTableRequest);
		
		it('classId should be ' + FB_PROTOCOL.CreateTableRequestPacket.CLASSID, function() {
            expect(objectClone.classId).toBe(FB_PROTOCOL.CreateTableRequestPacket.CLASSID);
        });

		it('seq should be 1', function() {
            expect(objectClone.seq).toBe(1);
        });

		it('gameid should be 1337', function() {
            expect(objectClone.gameid).toBe(1337);
        });

		it('seats should be 1', function() {
            expect(objectClone.seats).toBe(1);
        });
		
		it('params length should be 2', function() {
            expect(objectClone.params.length).toBe(2);
        });
		
		it('params[0].key should be "string"', function() {
            expect(objectClone.params[0].key).toBe("string");
        });
		
		it('params[0].type should be ' + FB_PROTOCOL.ParameterTypeEnum.STRING, function() {
            expect(objectClone.params[0].type).toBe(FB_PROTOCOL.ParameterTypeEnum.STRING);
        });
		
		it('params[0].value should be "AAUxMjM0NQ=="', function() {
            expect(objectClone.params[0].value).toBe("AAUxMjM0NQ==");
        });

		it('decoded params[0].value length should be 7', function() {
			byteArray = FIREBASE.ByteArray.fromBase64String(objectClone.params[0].value);
            expect(byteArray.length).toBe(7);
        });
		
		it('decoded byteArray.readString() should return "12345"', function() {
			var styxArray = new FIREBASE.ByteArray(byteArray);
			var stringResult = styxArray.readString();
            expect(stringResult).toBe("12345");
        });
		
		it('params[1].key should be "int"', function() {
            expect(objectClone.params[1].key).toBe("int");
        });
		
		it('params[1].type should be ' + FB_PROTOCOL.ParameterTypeEnum.INT, function() {
            expect(objectClone.params[1].type).toBe(FB_PROTOCOL.ParameterTypeEnum.INT);
        });
		
		it('params[1].value should be "AAAwOQ=="', function() {
            expect(objectClone.params[1].value).toBe("AAAwOQ==");
        });

		it('decoded params[1].value length should be 4', function() {
			byteArray = FIREBASE.ByteArray.fromBase64String(objectClone.params[1].value);
            expect(byteArray.length).toBe(4);
        });
		
		it('decoded byteArray.readInt() should return 12345', function() {
			var styxArray = new FIREBASE.ByteArray(byteArray);
			var intResult = styxArray.readInt();
            expect(intResult).toBe(12345);
        });

	});

    describe('Test getParam()', function() {
        it('getParam() should return a correct object', function() {
            var param = new FB_PROTOCOL.Param();
            var paramObject;
            FIREBASE.Styx.writeParam(param, "string", "12345");
            paramObject = FIREBASE.Styx.getParam(param);
            expect(paramObject.name).toBe("string");
            expect(paramObject.value).toBe("12345");
        });
    });

	describe('Test toJSON()', function() {
		var createTableRequest = new FB_PROTOCOL.CreateTableRequestPacket();
		var param1 = new FB_PROTOCOL.Param();
		var param2 = new FB_PROTOCOL.Param();
		var jsonString = "";
		
		FIREBASE.Styx.writeParam(param1, "string", "12345");
		FIREBASE.Styx.writeParam(param2, "int", 12345);
		
		createTableRequest.seq = 1; 
		createTableRequest.gameid = 1337;
		createTableRequest.seats = 1;
		createTableRequest.params.push(param1);
		createTableRequest.params.push(param2);
		
		jsonString = FIREBASE.Styx.toJSON(createTableRequest);
		
		it('jsonString should be "{"classId":40,"seq":1,"gameid":1337,"seats":1,"params":[{"classId":5,"key":"string","type":0,"value":"AAUxMjM0NQ=="},{"classId":5,"key":"int","type":1,"value":"AAAwOQ=="}],"invitees":[]}"', function() {
            expect(jsonString).toBe('{"classId":40,"seq":1,"gameid":1337,"seats":1,"params":[{"classId":5,"key":"string","type":0,"value":"AAUxMjM0NQ=="},{"classId":5,"key":"int","type":1,"value":"AAAwOQ=="}],"invitees":[]}');
        });
	        
	});
});