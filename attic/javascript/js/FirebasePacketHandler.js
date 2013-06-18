
var FB_PROTOCOL = FB_PROTOCOL || {};


FB_PROTOCOL.FirebasePacketHandler = function(callbackObject) {

	this.callbackObject = callbackObject;
	
	this.handlePacket = function(protocolObject) {
		console.log("enter handlePacket classidX[" + protocolObject.classId + "]");
		switch (protocolObject.classId) {
			case FB_PROTOCOL.NotifyJoinPacket.CLASSID :
				this.callbackObject.handleNotifyJoin(protocolObject);
				break;
			
			case FB_PROTOCOL.NotifyLeavePacket.CLASSID :
				this.callbackObject.handleNotifyLeave(protocolObject);
				break;
			
			case FB_PROTOCOL.SeatInfoPacket.CLASSID :
				console.log("calling handleSeatInfo");
				this.callbackObject.handleSeatInfo(protocolObject);
				break;
		}
		console.log("leave handlePacket classidX[" + protocolObject.classId + "]");
	};
};