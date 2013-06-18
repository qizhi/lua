function createValueInt8(value) {
	var packet = new TEST_PROTOCOL.ValueInt8();
	packet.value = value;
	return createJson(packet);
}

function createValueUInt16(value) {
	var packet = new TEST_PROTOCOL.ValueUint16();
	packet.value = value;
	return createJson(packet);
}

function createValueInt16(value) {
	var packet = new TEST_PROTOCOL.ValueInt16();
	packet.value = value;
	return createJson(packet);
}

function createValueUInt32(value) {
	var packet = new TEST_PROTOCOL.ValueUint32();
	packet.value = value;
	return createJson(packet);
}

function createValueInt32(value) {
	var packet = new TEST_PROTOCOL.ValueInt32();
	packet.value = value;
	return createJson(packet);
}

function createValueInt64(value) {
	var packet = new TEST_PROTOCOL.ValueInt64();
	packet.value = value;
	return createJson(packet);
}

function createBetRequest(handId, amount, tieAmount) {
	var bet = new TEST_PROTOCOL.BetRequest();
	bet.handId = handId;
	bet.amount = amount;
	bet.tieAmount = tieAmount;
	return createJson(bet);
}

function createListOfInt8(v0, v1, v2, v3) {
	var packet = new TEST_PROTOCOL.ListOfInt8();
	packet.l.push(v0);
	packet.l.push(v1);
	packet.l.push(v2);
	packet.l.push(v3);
	return createJson(packet);
}

function createListOfInt16(v0, v1, v2, v3) {
	var packet = new TEST_PROTOCOL.ListOfInt16();
	packet.l.push(v0);
	packet.l.push(v1);
	packet.l.push(v2);
	packet.l.push(v3);
	return createJson(packet);
}

function createListOfUInt16(v0, v1, v2, v3) {
	var packet = new TEST_PROTOCOL.ListOfUint16();
	packet.l.push(v0);
	packet.l.push(v1);
	packet.l.push(v2);
	packet.l.push(v3);
	return createJson(packet);
}

function createListOfInt32(v0, v1, v2, v3) {
	var packet = new TEST_PROTOCOL.ListOfInts();
	packet.l.push(v0);
	packet.l.push(v1);
	packet.l.push(v2);
	packet.l.push(v3);
	return createJson(packet);
}

function createListOfUInt32(v0, v1, v2, v3) {
	var packet = new TEST_PROTOCOL.ListOfUint32();
	packet.l.push(v0);
	packet.l.push(v1);
	packet.l.push(v2);
	packet.l.push(v3);
	return createJson(packet);
}

function createListOfInt64(v0, v1, v2, v3) {
	var packet = new TEST_PROTOCOL.ListOfInts64();
	packet.l.push(v0);
	packet.l.push(v1);
	packet.l.push(v2);
	packet.l.push(v3);
	return createJson(packet);
}

function createListOfString(v0, v1, v2, v3) {
	var packet = new TEST_PROTOCOL.ListOfString();
	packet.l.push(v0);
	packet.l.push(v1);
	packet.l.push(v2);
	packet.l.push(v3);
	return createJson(packet);
}

function createListOfBool(v0, v1, v2, v3) {
	var packet = new TEST_PROTOCOL.ListOfBool();
	packet.l.push(v0);
	packet.l.push(v1);
	packet.l.push(v2);
	packet.l.push(v3);
	return createJson(packet);
}

function createListOfEnum(v0, v1, v2, v3) {
	var packet = new TEST_PROTOCOL.ListOfEnums();
	packet.l.push(v0.ordinal());
	packet.l.push(v1.ordinal());
	packet.l.push(v2.ordinal());
	packet.l.push(v3.ordinal());
	return createJson(packet);
}

function createDealAction(handId, amount, tieAmount, handId2, amount2, tieAmount2) {
	var dealAction = new TEST_PROTOCOL.ActionDeal();
	
	var bet1 = new TEST_PROTOCOL.BetRequest();
	bet1.handId = handId;
	bet1.amount = amount;
	bet1.tieAmount = tieAmount;
	dealAction.bets.push(bet1);
	var bet2 = new TEST_PROTOCOL.BetRequest();
	bet2.handId = handId2;
	bet2.amount = amount2;
	bet2.tieAmount = tieAmount2;
	
	dealAction.bets = [bet1, bet2];
	
	return createJson(dealAction);
}


function createJson(packet) {
	var buffer = FIREBASE.Styx.wrapInGameTransportPacket(1, 2, packet);
//	var buffer = packet.save();
//	for (var i = 0; i < buffer.getBuffer().length; i++) {
//		println("b = " + buffer.getBuffer()[i]);
//	}
//	
	return FIREBASE.Styx.toJSON(buffer);
}

//function createJson(packet) {
//	 var gameTransportPacket = new FB_PROTOCOL.GameTransportPacket();
//	 gameTransportPacket.tableid = 1;
//	 gameTransportPacket.pid = 2;
//	 var byteArray = packet.save();
//	 gameTransportPacket.gamedata = byteArray.createGameDataArray(packet.classId());
//	 
//	for (var i = 0; i < gameTransportPacket.gamedata.length; i++) {
//		println("b = " + gameTransportPacket.gamedata[i]);
//	}	
// 
////	println("str: " + btoa(String.fromCharCode.apply(null, gameTransportPacket.gamedata));
//	 
//	 var json =FIREBASE.Styx.toJSON(gameTransportPacket);
//	 println("json: " + json);
//	 return json;
//}








