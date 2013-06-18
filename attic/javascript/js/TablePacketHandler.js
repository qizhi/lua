var FB_PROTOCOL = FB_PROTOCOL || {};


FB_PROTOCOL.TablePacketHandler = function(firebaseProtocolHandler, pokerProtocolHandler) {
	
	this.firebaseProtocolHandler = firebaseProtocolHandler;
	this.pokerProtocolHandler = pokerProtocolHandler;

	this.handlePacket = function(protocolObject) {
		if ( protocolObject.classId === FB_PROTOCOL.GameTransportPacket.CLASSID ) {
			pokerProtocolHandler.handleGameTransportPacket(protocolObject);
		} else {
			firebaseProtocolHandler.handlePacket(protocolObject);
		}
	};
};