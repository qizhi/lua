var buffer = [];

var i;

for (i = 0; i < packet.length; i++) {
	buffer.push(packet[i] < 0 ? packet[i] + 256 : packet[i]);
}
var array = new FIREBASE.ByteArray(buffer);
var length = array.readInt();
var classId = array.readByte();

var obj = TEST_PROTOCOL.ProtocolObjectFactory.create(classId, array);
JSON.stringify(obj);
