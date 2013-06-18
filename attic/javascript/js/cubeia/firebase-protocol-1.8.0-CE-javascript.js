// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

var FB_PROTOCOL = FB_PROTOCOL || {};


FB_PROTOCOL.Attribute=function(){this.classId=function(){return FB_PROTOCOL.Attribute.CLASSID
};
this.name={};
this.value={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeString(this.name);
a.writeString(this.value);
return a
};
this.load=function(a){this.name=a.readString();
this.value=a.readString()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.Attribute";
a.details={};
a.details.name=this.name;
a.details.value=this.value;
return a
}
};
FB_PROTOCOL.Attribute.CLASSID=8;FB_PROTOCOL.BadPacket=function(){this.classId=function(){return FB_PROTOCOL.BadPacket.CLASSID
};
this.cmd={};
this.error={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeByte(this.cmd);
a.writeByte(this.error);
return a
};
this.load=function(a){this.cmd=a.readByte();
this.error=a.readByte()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.BadPacket";
a.details={};
a.details.cmd=this.cmd;
a.details.error=this.error;
return a
}
};
FB_PROTOCOL.BadPacket.CLASSID=3;FB_PROTOCOL.ChannelChatPacket=function(){this.classId=function(){return FB_PROTOCOL.ChannelChatPacket.CLASSID
};
this.channelid={};
this.targetid={};
this.message={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.channelid);
a.writeInt(this.targetid);
a.writeString(this.message);
return a
};
this.load=function(a){this.channelid=a.readInt();
this.targetid=a.readInt();
this.message=a.readString()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.ChannelChatPacket";
a.details={};
a.details.channelid=this.channelid;
a.details.targetid=this.targetid;
a.details.message=this.message;
return a
}
};
FB_PROTOCOL.ChannelChatPacket.CLASSID=124;FB_PROTOCOL.CreateTableRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.CreateTableRequestPacket.CLASSID
};
this.seq={};
this.gameid={};
this.seats={};
this.params=[];
this.invitees=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.seq);
a.writeInt(this.gameid);
a.writeByte(this.seats);
a.writeInt(this.params.length);
var b;
for(b=0;
b<this.params.length;
b++){a.writeArray(this.params[b].save())
}a.writeInt(this.invitees.length);
for(b=0;
b<this.invitees.length;
b++){a.writeInt(this.invitees[b])
}return a
};
this.load=function(b){this.seq=b.readInt();
this.gameid=b.readInt();
this.seats=b.readByte();
var c;
var e=b.readInt();
var d;
this.params=[];
for(c=0;
c<e;
c++){d=new FB_PROTOCOL.Param();
d.load(b);
this.params.push(d)
}var a=b.readInt();
this.invitees=[];
for(c=0;
c<a;
c++){this.invitees.push(b.readInt())
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.CreateTableRequestPacket";
a.details={};
a.details.seq=this.seq;
a.details.gameid=this.gameid;
a.details.seats=this.seats;
a.details.params=[];
for(b=0;
b<this.params.length;
b++){a.details.params.push(FIREBASE.Styx.getParam(this.params[b]))
}a.details.invitees=[];
for(b=0;
b<this.invitees.length;
b++){a.details.invitees.push(this.invitees[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.CreateTableRequestPacket.CLASSID=40;FB_PROTOCOL.CreateTableResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.CreateTableResponsePacket.CLASSID
};
this.seq={};
this.tableid={};
this.seat={};
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(0);
this.code={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.seq);
a.writeInt(this.tableid);
a.writeByte(this.seat);
a.writeUnsignedByte(this.status);
a.writeInt(this.code);
return a
};
this.load=function(a){this.seq=a.readInt();
this.tableid=a.readInt();
this.seat=a.readByte();
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(a.readUnsignedByte());
this.code=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.CreateTableResponsePacket";
a.details={};
a.details.seq=this.seq;
a.details.tableid=this.tableid;
a.details.seat=this.seat;
a.details.status=FB_PROTOCOL.ResponseStatusEnum.toString(this.status);
a.details.code=this.code;
return a
}
};
FB_PROTOCOL.CreateTableResponsePacket.CLASSID=41;FB_PROTOCOL.EncryptedTransportPacket=function(){this.classId=function(){return FB_PROTOCOL.EncryptedTransportPacket.CLASSID
};
this.func={};
this.payload=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeByte(this.func);
a.writeInt(this.payload.length);
a.writeArray(this.payload);
return a
};
this.load=function(a){this.func=a.readByte();
var b=a.readInt();
this.payload=a.readArray(b)
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.EncryptedTransportPacket";
a.details={};
a.details.func=this.func;
a.details.payload=[];
for(b=0;
b<this.payload.length;
b++){a.details.payload.push(this.payload[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.EncryptedTransportPacket.CLASSID=105;FB_PROTOCOL.FilteredJoinCancelRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.FilteredJoinCancelRequestPacket.CLASSID
};
this.seq={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.seq);
return a
};
this.load=function(a){this.seq=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.FilteredJoinCancelRequestPacket";
a.details={};
a.details.seq=this.seq;
return a
}
};
FB_PROTOCOL.FilteredJoinCancelRequestPacket.CLASSID=172;FB_PROTOCOL.FilteredJoinCancelResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.FilteredJoinCancelResponsePacket.CLASSID
};
this.seq={};
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(0);
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.seq);
a.writeUnsignedByte(this.status);
return a
};
this.load=function(a){this.seq=a.readInt();
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.FilteredJoinCancelResponsePacket";
a.details={};
a.details.seq=this.seq;
a.details.status=FB_PROTOCOL.ResponseStatusEnum.toString(this.status);
return a
}
};
FB_PROTOCOL.FilteredJoinCancelResponsePacket.CLASSID=173;FB_PROTOCOL.FilteredJoinResponseStatusEnum=function(){};
FB_PROTOCOL.FilteredJoinResponseStatusEnum.OK=0;
FB_PROTOCOL.FilteredJoinResponseStatusEnum.FAILED=1;
FB_PROTOCOL.FilteredJoinResponseStatusEnum.DENIED=2;
FB_PROTOCOL.FilteredJoinResponseStatusEnum.SEATING=3;
FB_PROTOCOL.FilteredJoinResponseStatusEnum.WAIT_LIST=4;
FB_PROTOCOL.FilteredJoinResponseStatusEnum.makeFilteredJoinResponseStatusEnum=function(a){switch(a){case 0:return FB_PROTOCOL.FilteredJoinResponseStatusEnum.OK;
case 1:return FB_PROTOCOL.FilteredJoinResponseStatusEnum.FAILED;
case 2:return FB_PROTOCOL.FilteredJoinResponseStatusEnum.DENIED;
case 3:return FB_PROTOCOL.FilteredJoinResponseStatusEnum.SEATING;
case 4:return FB_PROTOCOL.FilteredJoinResponseStatusEnum.WAIT_LIST
}return -1
};
FB_PROTOCOL.FilteredJoinResponseStatusEnum.toString=function(a){switch(a){case 0:return"OK";
case 1:return"FAILED";
case 2:return"DENIED";
case 3:return"SEATING";
case 4:return"WAIT_LIST"
}return"INVALID_ENUM_VALUE"
};FB_PROTOCOL.FilteredJoinTableAvailablePacket=function(){this.classId=function(){return FB_PROTOCOL.FilteredJoinTableAvailablePacket.CLASSID
};
this.seq={};
this.tableid={};
this.seat={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.seq);
a.writeInt(this.tableid);
a.writeByte(this.seat);
return a
};
this.load=function(a){this.seq=a.readInt();
this.tableid=a.readInt();
this.seat=a.readByte()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.FilteredJoinTableAvailablePacket";
a.details={};
a.details.seq=this.seq;
a.details.tableid=this.tableid;
a.details.seat=this.seat;
return a
}
};
FB_PROTOCOL.FilteredJoinTableAvailablePacket.CLASSID=174;FB_PROTOCOL.FilteredJoinTableRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.FilteredJoinTableRequestPacket.CLASSID
};
this.seq={};
this.gameid={};
this.address={};
this.params=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.seq);
a.writeInt(this.gameid);
a.writeString(this.address);
a.writeInt(this.params.length);
var b;
for(b=0;
b<this.params.length;
b++){a.writeArray(this.params[b].save())
}return a
};
this.load=function(a){this.seq=a.readInt();
this.gameid=a.readInt();
this.address=a.readString();
var b;
var c=a.readInt();
var d;
this.params=[];
for(b=0;
b<c;
b++){d=new FB_PROTOCOL.ParamFilter();
d.load(a);
this.params.push(d)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.FilteredJoinTableRequestPacket";
a.details={};
a.details.seq=this.seq;
a.details.gameid=this.gameid;
a.details.address=this.address;
a.details.params=[];
for(b=0;
b<this.params.length;
b++){a.details.params.push(this.params[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.FilteredJoinTableRequestPacket.CLASSID=170;FB_PROTOCOL.FilteredJoinTableResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.FilteredJoinTableResponsePacket.CLASSID
};
this.seq={};
this.gameid={};
this.address={};
this.status=FB_PROTOCOL.FilteredJoinResponseStatusEnum.makeFilteredJoinResponseStatusEnum(0);
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.seq);
a.writeInt(this.gameid);
a.writeString(this.address);
a.writeUnsignedByte(this.status);
return a
};
this.load=function(a){this.seq=a.readInt();
this.gameid=a.readInt();
this.address=a.readString();
this.status=FB_PROTOCOL.FilteredJoinResponseStatusEnum.makeFilteredJoinResponseStatusEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.FilteredJoinTableResponsePacket";
a.details={};
a.details.seq=this.seq;
a.details.gameid=this.gameid;
a.details.address=this.address;
a.details.status=FB_PROTOCOL.FilteredJoinResponseStatusEnum.toString(this.status);
return a
}
};
FB_PROTOCOL.FilteredJoinTableResponsePacket.CLASSID=171;FB_PROTOCOL.ForcedLogoutPacket=function(){this.classId=function(){return FB_PROTOCOL.ForcedLogoutPacket.CLASSID
};
this.code={};
this.message={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.code);
a.writeString(this.message);
return a
};
this.load=function(a){this.code=a.readInt();
this.message=a.readString()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.ForcedLogoutPacket";
a.details={};
a.details.code=this.code;
a.details.message=this.message;
return a
}
};
FB_PROTOCOL.ForcedLogoutPacket.CLASSID=14;FB_PROTOCOL.GameTransportPacket=function(){this.classId=function(){return FB_PROTOCOL.GameTransportPacket.CLASSID
};
this.tableid={};
this.pid={};
this.gamedata=[];
this.attributes=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeInt(this.pid);
a.writeInt(this.gamedata.length);
a.writeArray(this.gamedata);
a.writeInt(this.attributes.length);
var b;
for(b=0;
b<this.attributes.length;
b++){a.writeArray(this.attributes[b].save())
}return a
};
this.load=function(a){this.tableid=a.readInt();
this.pid=a.readInt();
var d=a.readInt();
this.gamedata=a.readArray(d);
var b;
var c=a.readInt();
var e;
this.attributes=[];
for(b=0;
b<c;
b++){e=new FB_PROTOCOL.Attribute();
e.load(a);
this.attributes.push(e)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.GameTransportPacket";
a.details={};
a.details.tableid=this.tableid;
a.details.pid=this.pid;
a.details.gamedata=[];
for(b=0;
b<this.gamedata.length;
b++){a.details.gamedata.push(this.gamedata[b].getNormalizedObject())
}a.details.attributes=[];
for(b=0;
b<this.attributes.length;
b++){a.details.attributes.push(this.attributes[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.GameTransportPacket.CLASSID=100;FB_PROTOCOL.GameVersionPacket=function(){this.classId=function(){return FB_PROTOCOL.GameVersionPacket.CLASSID
};
this.game={};
this.operatorid={};
this.version={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.game);
a.writeInt(this.operatorid);
a.writeString(this.version);
return a
};
this.load=function(a){this.game=a.readInt();
this.operatorid=a.readInt();
this.version=a.readString()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.GameVersionPacket";
a.details={};
a.details.game=this.game;
a.details.operatorid=this.operatorid;
a.details.version=this.version;
return a
}
};
FB_PROTOCOL.GameVersionPacket.CLASSID=1;FB_PROTOCOL.GoodPacket=function(){this.classId=function(){return FB_PROTOCOL.GoodPacket.CLASSID
};
this.cmd={};
this.extra={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeByte(this.cmd);
a.writeInt(this.extra);
return a
};
this.load=function(a){this.cmd=a.readByte();
this.extra=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.GoodPacket";
a.details={};
a.details.cmd=this.cmd;
a.details.extra=this.extra;
return a
}
};
FB_PROTOCOL.GoodPacket.CLASSID=2;FB_PROTOCOL.InvitePlayersRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.InvitePlayersRequestPacket.CLASSID
};
this.tableid={};
this.invitees=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeInt(this.invitees.length);
var b;
for(b=0;
b<this.invitees.length;
b++){a.writeInt(this.invitees[b])
}return a
};
this.load=function(b){this.tableid=b.readInt();
var c;
var a=b.readInt();
this.invitees=[];
for(c=0;
c<a;
c++){this.invitees.push(b.readInt())
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.InvitePlayersRequestPacket";
a.details={};
a.details.tableid=this.tableid;
a.details.invitees=[];
for(b=0;
b<this.invitees.length;
b++){a.details.invitees.push(this.invitees[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.InvitePlayersRequestPacket.CLASSID=42;FB_PROTOCOL.JoinChatChannelRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.JoinChatChannelRequestPacket.CLASSID
};
this.channelid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.channelid);
return a
};
this.load=function(a){this.channelid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.JoinChatChannelRequestPacket";
a.details={};
a.details.channelid=this.channelid;
return a
}
};
FB_PROTOCOL.JoinChatChannelRequestPacket.CLASSID=120;FB_PROTOCOL.JoinChatChannelResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.JoinChatChannelResponsePacket.CLASSID
};
this.channelid={};
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(0);
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.channelid);
a.writeUnsignedByte(this.status);
return a
};
this.load=function(a){this.channelid=a.readInt();
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.JoinChatChannelResponsePacket";
a.details={};
a.details.channelid=this.channelid;
a.details.status=FB_PROTOCOL.ResponseStatusEnum.toString(this.status);
return a
}
};
FB_PROTOCOL.JoinChatChannelResponsePacket.CLASSID=121;FB_PROTOCOL.JoinRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.JoinRequestPacket.CLASSID
};
this.tableid={};
this.seat={};
this.params=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeByte(this.seat);
a.writeInt(this.params.length);
var b;
for(b=0;
b<this.params.length;
b++){a.writeArray(this.params[b].save())
}return a
};
this.load=function(a){this.tableid=a.readInt();
this.seat=a.readByte();
var b;
var d=a.readInt();
var c;
this.params=[];
for(b=0;
b<d;
b++){c=new FB_PROTOCOL.Param();
c.load(a);
this.params.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.JoinRequestPacket";
a.details={};
a.details.tableid=this.tableid;
a.details.seat=this.seat;
a.details.params=[];
for(b=0;
b<this.params.length;
b++){a.details.params.push(FIREBASE.Styx.getParam(this.params[b]))
}return a
}
};
FB_PROTOCOL.JoinRequestPacket.CLASSID=30;FB_PROTOCOL.JoinResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.JoinResponsePacket.CLASSID
};
this.tableid={};
this.seat={};
this.status=FB_PROTOCOL.JoinResponseStatusEnum.makeJoinResponseStatusEnum(0);
this.code={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeByte(this.seat);
a.writeUnsignedByte(this.status);
a.writeInt(this.code);
return a
};
this.load=function(a){this.tableid=a.readInt();
this.seat=a.readByte();
this.status=FB_PROTOCOL.JoinResponseStatusEnum.makeJoinResponseStatusEnum(a.readUnsignedByte());
this.code=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.JoinResponsePacket";
a.details={};
a.details.tableid=this.tableid;
a.details.seat=this.seat;
a.details.status=FB_PROTOCOL.JoinResponseStatusEnum.toString(this.status);
a.details.code=this.code;
return a
}
};
FB_PROTOCOL.JoinResponsePacket.CLASSID=31;FB_PROTOCOL.JoinResponseStatusEnum=function(){};
FB_PROTOCOL.JoinResponseStatusEnum.OK=0;
FB_PROTOCOL.JoinResponseStatusEnum.FAILED=1;
FB_PROTOCOL.JoinResponseStatusEnum.DENIED=2;
FB_PROTOCOL.JoinResponseStatusEnum.makeJoinResponseStatusEnum=function(a){switch(a){case 0:return FB_PROTOCOL.JoinResponseStatusEnum.OK;
case 1:return FB_PROTOCOL.JoinResponseStatusEnum.FAILED;
case 2:return FB_PROTOCOL.JoinResponseStatusEnum.DENIED
}return -1
};
FB_PROTOCOL.JoinResponseStatusEnum.toString=function(a){switch(a){case 0:return"OK";
case 1:return"FAILED";
case 2:return"DENIED"
}return"INVALID_ENUM_VALUE"
};FB_PROTOCOL.KickPlayerPacket=function(){this.classId=function(){return FB_PROTOCOL.KickPlayerPacket.CLASSID
};
this.tableid={};
this.reasonCode={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeShort(this.reasonCode);
return a
};
this.load=function(a){this.tableid=a.readInt();
this.reasonCode=a.readShort()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.KickPlayerPacket";
a.details={};
a.details.tableid=this.tableid;
a.details.reasonCode=this.reasonCode;
return a
}
};
FB_PROTOCOL.KickPlayerPacket.CLASSID=64;FB_PROTOCOL.LeaveChatChannelPacket=function(){this.classId=function(){return FB_PROTOCOL.LeaveChatChannelPacket.CLASSID
};
this.channelid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.channelid);
return a
};
this.load=function(a){this.channelid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.LeaveChatChannelPacket";
a.details={};
a.details.channelid=this.channelid;
return a
}
};
FB_PROTOCOL.LeaveChatChannelPacket.CLASSID=122;FB_PROTOCOL.LeaveRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.LeaveRequestPacket.CLASSID
};
this.tableid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
return a
};
this.load=function(a){this.tableid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.LeaveRequestPacket";
a.details={};
a.details.tableid=this.tableid;
return a
}
};
FB_PROTOCOL.LeaveRequestPacket.CLASSID=36;FB_PROTOCOL.LeaveResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.LeaveResponsePacket.CLASSID
};
this.tableid={};
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(0);
this.code={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeUnsignedByte(this.status);
a.writeInt(this.code);
return a
};
this.load=function(a){this.tableid=a.readInt();
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(a.readUnsignedByte());
this.code=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.LeaveResponsePacket";
a.details={};
a.details.tableid=this.tableid;
a.details.status=FB_PROTOCOL.ResponseStatusEnum.toString(this.status);
a.details.code=this.code;
return a
}
};
FB_PROTOCOL.LeaveResponsePacket.CLASSID=37;FB_PROTOCOL.LobbyObjectSubscribePacket=function(){this.classId=function(){return FB_PROTOCOL.LobbyObjectSubscribePacket.CLASSID
};
this.type=FB_PROTOCOL.LobbyTypeEnum.makeLobbyTypeEnum(0);
this.gameid={};
this.address={};
this.objectid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeUnsignedByte(this.type);
a.writeInt(this.gameid);
a.writeString(this.address);
a.writeInt(this.objectid);
return a
};
this.load=function(a){this.type=FB_PROTOCOL.LobbyTypeEnum.makeLobbyTypeEnum(a.readUnsignedByte());
this.gameid=a.readInt();
this.address=a.readString();
this.objectid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.LobbyObjectSubscribePacket";
a.details={};
a.details.type=FB_PROTOCOL.LobbyTypeEnum.toString(this.type);
a.details.gameid=this.gameid;
a.details.address=this.address;
a.details.objectid=this.objectid;
return a
}
};
FB_PROTOCOL.LobbyObjectSubscribePacket.CLASSID=151;FB_PROTOCOL.LobbyObjectUnsubscribePacket=function(){this.classId=function(){return FB_PROTOCOL.LobbyObjectUnsubscribePacket.CLASSID
};
this.type=FB_PROTOCOL.LobbyTypeEnum.makeLobbyTypeEnum(0);
this.gameid={};
this.address={};
this.objectid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeUnsignedByte(this.type);
a.writeInt(this.gameid);
a.writeString(this.address);
a.writeInt(this.objectid);
return a
};
this.load=function(a){this.type=FB_PROTOCOL.LobbyTypeEnum.makeLobbyTypeEnum(a.readUnsignedByte());
this.gameid=a.readInt();
this.address=a.readString();
this.objectid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.LobbyObjectUnsubscribePacket";
a.details={};
a.details.type=FB_PROTOCOL.LobbyTypeEnum.toString(this.type);
a.details.gameid=this.gameid;
a.details.address=this.address;
a.details.objectid=this.objectid;
return a
}
};
FB_PROTOCOL.LobbyObjectUnsubscribePacket.CLASSID=152;FB_PROTOCOL.LobbyQueryPacket=function(){this.classId=function(){return FB_PROTOCOL.LobbyQueryPacket.CLASSID
};
this.gameid={};
this.address={};
this.type=FB_PROTOCOL.LobbyTypeEnum.makeLobbyTypeEnum(0);
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.gameid);
a.writeString(this.address);
a.writeUnsignedByte(this.type);
return a
};
this.load=function(a){this.gameid=a.readInt();
this.address=a.readString();
this.type=FB_PROTOCOL.LobbyTypeEnum.makeLobbyTypeEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.LobbyQueryPacket";
a.details={};
a.details.gameid=this.gameid;
a.details.address=this.address;
a.details.type=FB_PROTOCOL.LobbyTypeEnum.toString(this.type);
return a
}
};
FB_PROTOCOL.LobbyQueryPacket.CLASSID=142;FB_PROTOCOL.LobbySubscribePacket=function(){this.classId=function(){return FB_PROTOCOL.LobbySubscribePacket.CLASSID
};
this.type=FB_PROTOCOL.LobbyTypeEnum.makeLobbyTypeEnum(0);
this.gameid={};
this.address={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeUnsignedByte(this.type);
a.writeInt(this.gameid);
a.writeString(this.address);
return a
};
this.load=function(a){this.type=FB_PROTOCOL.LobbyTypeEnum.makeLobbyTypeEnum(a.readUnsignedByte());
this.gameid=a.readInt();
this.address=a.readString()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.LobbySubscribePacket";
a.details={};
a.details.type=FB_PROTOCOL.LobbyTypeEnum.toString(this.type);
a.details.gameid=this.gameid;
a.details.address=this.address;
return a
}
};
FB_PROTOCOL.LobbySubscribePacket.CLASSID=145;FB_PROTOCOL.LobbyTypeEnum=function(){};
FB_PROTOCOL.LobbyTypeEnum.REGULAR=0;
FB_PROTOCOL.LobbyTypeEnum.MTT=1;
FB_PROTOCOL.LobbyTypeEnum.makeLobbyTypeEnum=function(a){switch(a){case 0:return FB_PROTOCOL.LobbyTypeEnum.REGULAR;
case 1:return FB_PROTOCOL.LobbyTypeEnum.MTT
}return -1
};
FB_PROTOCOL.LobbyTypeEnum.toString=function(a){switch(a){case 0:return"REGULAR";
case 1:return"MTT"
}return"INVALID_ENUM_VALUE"
};FB_PROTOCOL.LobbyUnsubscribePacket=function(){this.classId=function(){return FB_PROTOCOL.LobbyUnsubscribePacket.CLASSID
};
this.type=FB_PROTOCOL.LobbyTypeEnum.makeLobbyTypeEnum(0);
this.gameid={};
this.address={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeUnsignedByte(this.type);
a.writeInt(this.gameid);
a.writeString(this.address);
return a
};
this.load=function(a){this.type=FB_PROTOCOL.LobbyTypeEnum.makeLobbyTypeEnum(a.readUnsignedByte());
this.gameid=a.readInt();
this.address=a.readString()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.LobbyUnsubscribePacket";
a.details={};
a.details.type=FB_PROTOCOL.LobbyTypeEnum.toString(this.type);
a.details.gameid=this.gameid;
a.details.address=this.address;
return a
}
};
FB_PROTOCOL.LobbyUnsubscribePacket.CLASSID=146;FB_PROTOCOL.LocalServiceTransportPacket=function(){this.classId=function(){return FB_PROTOCOL.LocalServiceTransportPacket.CLASSID
};
this.seq={};
this.servicedata=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.seq);
a.writeInt(this.servicedata.length);
a.writeArray(this.servicedata);
return a
};
this.load=function(a){this.seq=a.readInt();
var b=a.readInt();
this.servicedata=a.readArray(b)
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.LocalServiceTransportPacket";
a.details={};
a.details.seq=this.seq;
a.details.servicedata=[];
for(b=0;
b<this.servicedata.length;
b++){a.details.servicedata.push(this.servicedata[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.LocalServiceTransportPacket.CLASSID=103;FB_PROTOCOL.LoginRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.LoginRequestPacket.CLASSID
};
this.user={};
this.password={};
this.operatorid={};
this.credentials=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeString(this.user);
a.writeString(this.password);
a.writeInt(this.operatorid);
a.writeInt(this.credentials.length);
a.writeArray(this.credentials);
return a
};
this.load=function(a){this.user=a.readString();
this.password=a.readString();
this.operatorid=a.readInt();
var b=a.readInt();
this.credentials=a.readArray(b)
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.LoginRequestPacket";
a.details={};
a.details.user=this.user;
a.details.password=this.password;
a.details.operatorid=this.operatorid;
a.details.credentials=[];
for(b=0;
b<this.credentials.length;
b++){a.details.credentials.push(this.credentials[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.LoginRequestPacket.CLASSID=10;FB_PROTOCOL.LoginResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.LoginResponsePacket.CLASSID
};
this.screenname={};
this.pid={};
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(0);
this.code={};
this.message={};
this.credentials=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeString(this.screenname);
a.writeInt(this.pid);
a.writeUnsignedByte(this.status);
a.writeInt(this.code);
a.writeString(this.message);
a.writeInt(this.credentials.length);
a.writeArray(this.credentials);
return a
};
this.load=function(a){this.screenname=a.readString();
this.pid=a.readInt();
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(a.readUnsignedByte());
this.code=a.readInt();
this.message=a.readString();
var b=a.readInt();
this.credentials=a.readArray(b)
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.LoginResponsePacket";
a.details={};
a.details.screenname=this.screenname;
a.details.pid=this.pid;
a.details.status=FB_PROTOCOL.ResponseStatusEnum.toString(this.status);
a.details.code=this.code;
a.details.message=this.message;
a.details.credentials=[];
for(b=0;
b<this.credentials.length;
b++){a.details.credentials.push(this.credentials[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.LoginResponsePacket.CLASSID=11;FB_PROTOCOL.LogoutPacket=function(){this.classId=function(){return FB_PROTOCOL.LogoutPacket.CLASSID
};
this.leaveTables={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeBoolean(this.leaveTables);
return a
};
this.load=function(a){this.leaveTables=a.readBoolean()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.LogoutPacket";
a.details={};
a.details.leaveTables=this.leaveTables;
return a
}
};
FB_PROTOCOL.LogoutPacket.CLASSID=12;FB_PROTOCOL.MttPickedUpPacket=function(){this.classId=function(){return FB_PROTOCOL.MttPickedUpPacket.CLASSID
};
this.mttid={};
this.tableid={};
this.keepWatching={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.mttid);
a.writeInt(this.tableid);
a.writeBoolean(this.keepWatching);
return a
};
this.load=function(a){this.mttid=a.readInt();
this.tableid=a.readInt();
this.keepWatching=a.readBoolean()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.MttPickedUpPacket";
a.details={};
a.details.mttid=this.mttid;
a.details.tableid=this.tableid;
a.details.keepWatching=this.keepWatching;
return a
}
};
FB_PROTOCOL.MttPickedUpPacket.CLASSID=210;FB_PROTOCOL.MttRegisterRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.MttRegisterRequestPacket.CLASSID
};
this.mttid={};
this.params=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.mttid);
a.writeInt(this.params.length);
var b;
for(b=0;
b<this.params.length;
b++){a.writeArray(this.params[b].save())
}return a
};
this.load=function(a){this.mttid=a.readInt();
var b;
var d=a.readInt();
var c;
this.params=[];
for(b=0;
b<d;
b++){c=new FB_PROTOCOL.Param();
c.load(a);
this.params.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.MttRegisterRequestPacket";
a.details={};
a.details.mttid=this.mttid;
a.details.params=[];
for(b=0;
b<this.params.length;
b++){a.details.params.push(FIREBASE.Styx.getParam(this.params[b]))
}return a
}
};
FB_PROTOCOL.MttRegisterRequestPacket.CLASSID=205;FB_PROTOCOL.MttRegisterResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.MttRegisterResponsePacket.CLASSID
};
this.mttid={};
this.status=FB_PROTOCOL.TournamentRegisterResponseStatusEnum.makeTournamentRegisterResponseStatusEnum(0);
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.mttid);
a.writeUnsignedByte(this.status);
return a
};
this.load=function(a){this.mttid=a.readInt();
this.status=FB_PROTOCOL.TournamentRegisterResponseStatusEnum.makeTournamentRegisterResponseStatusEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.MttRegisterResponsePacket";
a.details={};
a.details.mttid=this.mttid;
a.details.status=FB_PROTOCOL.TournamentRegisterResponseStatusEnum.toString(this.status);
return a
}
};
FB_PROTOCOL.MttRegisterResponsePacket.CLASSID=206;FB_PROTOCOL.MttSeatedPacket=function(){this.classId=function(){return FB_PROTOCOL.MttSeatedPacket.CLASSID
};
this.mttid={};
this.tableid={};
this.seat={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.mttid);
a.writeInt(this.tableid);
a.writeByte(this.seat);
return a
};
this.load=function(a){this.mttid=a.readInt();
this.tableid=a.readInt();
this.seat=a.readByte()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.MttSeatedPacket";
a.details={};
a.details.mttid=this.mttid;
a.details.tableid=this.tableid;
a.details.seat=this.seat;
return a
}
};
FB_PROTOCOL.MttSeatedPacket.CLASSID=209;FB_PROTOCOL.MttTransportPacket=function(){this.classId=function(){return FB_PROTOCOL.MttTransportPacket.CLASSID
};
this.mttid={};
this.pid={};
this.mttdata=[];
this.attributes=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.mttid);
a.writeInt(this.pid);
a.writeInt(this.mttdata.length);
a.writeArray(this.mttdata);
a.writeInt(this.attributes.length);
var b;
for(b=0;
b<this.attributes.length;
b++){a.writeArray(this.attributes[b].save())
}return a
};
this.load=function(a){this.mttid=a.readInt();
this.pid=a.readInt();
var b=a.readInt();
this.mttdata=a.readArray(b);
var c;
var d=a.readInt();
var e;
this.attributes=[];
for(c=0;
c<d;
c++){e=new FB_PROTOCOL.Attribute();
e.load(a);
this.attributes.push(e)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.MttTransportPacket";
a.details={};
a.details.mttid=this.mttid;
a.details.pid=this.pid;
a.details.mttdata=[];
for(b=0;
b<this.mttdata.length;
b++){a.details.mttdata.push(this.mttdata[b].getNormalizedObject())
}a.details.attributes=[];
for(b=0;
b<this.attributes.length;
b++){a.details.attributes.push(this.attributes[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.MttTransportPacket.CLASSID=104;FB_PROTOCOL.MttUnregisterRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.MttUnregisterRequestPacket.CLASSID
};
this.mttid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.mttid);
return a
};
this.load=function(a){this.mttid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.MttUnregisterRequestPacket";
a.details={};
a.details.mttid=this.mttid;
return a
}
};
FB_PROTOCOL.MttUnregisterRequestPacket.CLASSID=207;FB_PROTOCOL.MttUnregisterResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.MttUnregisterResponsePacket.CLASSID
};
this.mttid={};
this.status=FB_PROTOCOL.TournamentRegisterResponseStatusEnum.makeTournamentRegisterResponseStatusEnum(0);
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.mttid);
a.writeUnsignedByte(this.status);
return a
};
this.load=function(a){this.mttid=a.readInt();
this.status=FB_PROTOCOL.TournamentRegisterResponseStatusEnum.makeTournamentRegisterResponseStatusEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.MttUnregisterResponsePacket";
a.details={};
a.details.mttid=this.mttid;
a.details.status=FB_PROTOCOL.TournamentRegisterResponseStatusEnum.toString(this.status);
return a
}
};
FB_PROTOCOL.MttUnregisterResponsePacket.CLASSID=208;FB_PROTOCOL.NotifyChannelChatPacket=function(){this.classId=function(){return FB_PROTOCOL.NotifyChannelChatPacket.CLASSID
};
this.pid={};
this.channelid={};
this.targetid={};
this.nick={};
this.message={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.pid);
a.writeInt(this.channelid);
a.writeInt(this.targetid);
a.writeString(this.nick);
a.writeString(this.message);
return a
};
this.load=function(a){this.pid=a.readInt();
this.channelid=a.readInt();
this.targetid=a.readInt();
this.nick=a.readString();
this.message=a.readString()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.NotifyChannelChatPacket";
a.details={};
a.details.pid=this.pid;
a.details.channelid=this.channelid;
a.details.targetid=this.targetid;
a.details.nick=this.nick;
a.details.message=this.message;
return a
}
};
FB_PROTOCOL.NotifyChannelChatPacket.CLASSID=123;FB_PROTOCOL.NotifyInvitedPacket=function(){this.classId=function(){return FB_PROTOCOL.NotifyInvitedPacket.CLASSID
};
this.inviter={};
this.screenname={};
this.tableid={};
this.seat={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.inviter);
a.writeString(this.screenname);
a.writeInt(this.tableid);
a.writeByte(this.seat);
return a
};
this.load=function(a){this.inviter=a.readInt();
this.screenname=a.readString();
this.tableid=a.readInt();
this.seat=a.readByte()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.NotifyInvitedPacket";
a.details={};
a.details.inviter=this.inviter;
a.details.screenname=this.screenname;
a.details.tableid=this.tableid;
a.details.seat=this.seat;
return a
}
};
FB_PROTOCOL.NotifyInvitedPacket.CLASSID=43;FB_PROTOCOL.NotifyJoinPacket=function(){this.classId=function(){return FB_PROTOCOL.NotifyJoinPacket.CLASSID
};
this.tableid={};
this.pid={};
this.nick={};
this.seat={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeInt(this.pid);
a.writeString(this.nick);
a.writeByte(this.seat);
return a
};
this.load=function(a){this.tableid=a.readInt();
this.pid=a.readInt();
this.nick=a.readString();
this.seat=a.readByte()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.NotifyJoinPacket";
a.details={};
a.details.tableid=this.tableid;
a.details.pid=this.pid;
a.details.nick=this.nick;
a.details.seat=this.seat;
return a
}
};
FB_PROTOCOL.NotifyJoinPacket.CLASSID=60;FB_PROTOCOL.NotifyLeavePacket=function(){this.classId=function(){return FB_PROTOCOL.NotifyLeavePacket.CLASSID
};
this.tableid={};
this.pid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeInt(this.pid);
return a
};
this.load=function(a){this.tableid=a.readInt();
this.pid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.NotifyLeavePacket";
a.details={};
a.details.tableid=this.tableid;
a.details.pid=this.pid;
return a
}
};
FB_PROTOCOL.NotifyLeavePacket.CLASSID=61;FB_PROTOCOL.NotifyRegisteredPacket=function(){this.classId=function(){return FB_PROTOCOL.NotifyRegisteredPacket.CLASSID
};
this.tournaments=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tournaments.length);
var b;
for(b=0;
b<this.tournaments.length;
b++){a.writeInt(this.tournaments[b])
}return a
};
this.load=function(b){var c;
var a=b.readInt();
this.tournaments=[];
for(c=0;
c<a;
c++){this.tournaments.push(b.readInt())
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.NotifyRegisteredPacket";
a.details={};
a.details.tournaments=[];
for(b=0;
b<this.tournaments.length;
b++){a.details.tournaments.push(this.tournaments[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.NotifyRegisteredPacket.CLASSID=211;FB_PROTOCOL.NotifySeatedPacket=function(){this.classId=function(){return FB_PROTOCOL.NotifySeatedPacket.CLASSID
};
this.tableid={};
this.seat={};
this.mttid={};
this.snapshot={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeByte(this.seat);
a.writeInt(this.mttid);
a.writeArray(this.snapshot.save());
return a
};
this.load=function(a){this.tableid=a.readInt();
this.seat=a.readByte();
this.mttid=a.readInt();
this.snapshot=new FB_PROTOCOL.TableSnapshotPacket();
this.snapshot.load(a)
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.NotifySeatedPacket";
a.details={};
a.details.tableid=this.tableid;
a.details.seat=this.seat;
a.details.mttid=this.mttid;
a.details.snapshot=this.snapshot.getNormalizedObject();
return a
}
};
FB_PROTOCOL.NotifySeatedPacket.CLASSID=62;FB_PROTOCOL.NotifyWatchingPacket=function(){this.classId=function(){return FB_PROTOCOL.NotifyWatchingPacket.CLASSID
};
this.tableid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
return a
};
this.load=function(a){this.tableid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.NotifyWatchingPacket";
a.details={};
a.details.tableid=this.tableid;
return a
}
};
FB_PROTOCOL.NotifyWatchingPacket.CLASSID=63;FB_PROTOCOL.Param=function(){this.classId=function(){return FB_PROTOCOL.Param.CLASSID
};
this.key={};
this.type={};
this.value=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeString(this.key);
a.writeByte(this.type);
a.writeInt(this.value.length);
a.writeArray(this.value);
return a
};
this.load=function(b){this.key=b.readString();
this.type=b.readByte();
var a=b.readInt();
this.value=b.readArray(a)
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.Param";
a.details={};
a.details.key=this.key;
a.details.type=this.type;
a.details.value=[];
for(b=0;
b<this.value.length;
b++){a.details.value.push(this.value[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.Param.CLASSID=5;FB_PROTOCOL.ParamFilter=function(){this.classId=function(){return FB_PROTOCOL.ParamFilter.CLASSID
};
this.param={};
this.op={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeArray(this.param.save());
a.writeByte(this.op);
return a
};
this.load=function(a){this.param=new FB_PROTOCOL.Param();
this.param.load(a);
this.op=a.readByte()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.ParamFilter";
a.details={};
a.details.param=this.param.getNormalizedObject();
a.details.op=this.op;
return a
}
};
FB_PROTOCOL.ParamFilter.CLASSID=6;FB_PROTOCOL.ParameterFilterEnum=function(){};
FB_PROTOCOL.ParameterFilterEnum.EQUALS=0;
FB_PROTOCOL.ParameterFilterEnum.GREATER_THAN=1;
FB_PROTOCOL.ParameterFilterEnum.SMALLER_THAN=2;
FB_PROTOCOL.ParameterFilterEnum.EQUALS_OR_GREATER_THAN=3;
FB_PROTOCOL.ParameterFilterEnum.EQUALS_OR_SMALLER_THAN=4;
FB_PROTOCOL.ParameterFilterEnum.makeParameterFilterEnum=function(a){switch(a){case 0:return FB_PROTOCOL.ParameterFilterEnum.EQUALS;
case 1:return FB_PROTOCOL.ParameterFilterEnum.GREATER_THAN;
case 2:return FB_PROTOCOL.ParameterFilterEnum.SMALLER_THAN;
case 3:return FB_PROTOCOL.ParameterFilterEnum.EQUALS_OR_GREATER_THAN;
case 4:return FB_PROTOCOL.ParameterFilterEnum.EQUALS_OR_SMALLER_THAN
}return -1
};
FB_PROTOCOL.ParameterFilterEnum.toString=function(a){switch(a){case 0:return"EQUALS";
case 1:return"GREATER_THAN";
case 2:return"SMALLER_THAN";
case 3:return"EQUALS_OR_GREATER_THAN";
case 4:return"EQUALS_OR_SMALLER_THAN"
}return"INVALID_ENUM_VALUE"
};FB_PROTOCOL.ParameterTypeEnum=function(){};
FB_PROTOCOL.ParameterTypeEnum.STRING=0;
FB_PROTOCOL.ParameterTypeEnum.INT=1;
FB_PROTOCOL.ParameterTypeEnum.DATE=2;
FB_PROTOCOL.ParameterTypeEnum.makeParameterTypeEnum=function(a){switch(a){case 0:return FB_PROTOCOL.ParameterTypeEnum.STRING;
case 1:return FB_PROTOCOL.ParameterTypeEnum.INT;
case 2:return FB_PROTOCOL.ParameterTypeEnum.DATE
}return -1
};
FB_PROTOCOL.ParameterTypeEnum.toString=function(a){switch(a){case 0:return"STRING";
case 1:return"INT";
case 2:return"DATE"
}return"INVALID_ENUM_VALUE"
};FB_PROTOCOL.PingPacket=function(){this.classId=function(){return FB_PROTOCOL.PingPacket.CLASSID
};
this.id={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.id);
return a
};
this.load=function(a){this.id=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.PingPacket";
a.details={};
a.details.id=this.id;
return a
}
};
FB_PROTOCOL.PingPacket.CLASSID=7;FB_PROTOCOL.PlayerInfoPacket=function(){this.classId=function(){return FB_PROTOCOL.PlayerInfoPacket.CLASSID
};
this.pid={};
this.nick={};
this.details=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.pid);
a.writeString(this.nick);
a.writeInt(this.details.length);
var b;
for(b=0;
b<this.details.length;
b++){a.writeArray(this.details[b].save())
}return a
};
this.load=function(a){this.pid=a.readInt();
this.nick=a.readString();
var b;
var d=a.readInt();
var c;
this.details=[];
for(b=0;
b<d;
b++){c=new FB_PROTOCOL.Param();
c.load(a);
this.details.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.PlayerInfoPacket";
a.details={};
a.details.pid=this.pid;
a.details.nick=this.nick;
a.details.details=[];
for(b=0;
b<this.details.length;
b++){a.details.details.push(FIREBASE.Styx.getParam(this.details[b]))
}return a
}
};
FB_PROTOCOL.PlayerInfoPacket.CLASSID=13;FB_PROTOCOL.PlayerQueryRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.PlayerQueryRequestPacket.CLASSID
};
this.pid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.pid);
return a
};
this.load=function(a){this.pid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.PlayerQueryRequestPacket";
a.details={};
a.details.pid=this.pid;
return a
}
};
FB_PROTOCOL.PlayerQueryRequestPacket.CLASSID=16;FB_PROTOCOL.PlayerQueryResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.PlayerQueryResponsePacket.CLASSID
};
this.pid={};
this.nick={};
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(0);
this.data=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.pid);
a.writeString(this.nick);
a.writeUnsignedByte(this.status);
a.writeInt(this.data.length);
a.writeArray(this.data);
return a
};
this.load=function(a){this.pid=a.readInt();
this.nick=a.readString();
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(a.readUnsignedByte());
var b=a.readInt();
this.data=a.readArray(b)
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.PlayerQueryResponsePacket";
a.details={};
a.details.pid=this.pid;
a.details.nick=this.nick;
a.details.status=FB_PROTOCOL.ResponseStatusEnum.toString(this.status);
a.details.data=[];
for(b=0;
b<this.data.length;
b++){a.details.data.push(this.data[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.PlayerQueryResponsePacket.CLASSID=17;FB_PROTOCOL.PlayerStatusEnum=function(){};
FB_PROTOCOL.PlayerStatusEnum.CONNECTED=0;
FB_PROTOCOL.PlayerStatusEnum.WAITING_REJOIN=1;
FB_PROTOCOL.PlayerStatusEnum.DISCONNECTED=2;
FB_PROTOCOL.PlayerStatusEnum.LEAVING=3;
FB_PROTOCOL.PlayerStatusEnum.TABLE_LOCAL=4;
FB_PROTOCOL.PlayerStatusEnum.RESERVATION=5;
FB_PROTOCOL.PlayerStatusEnum.makePlayerStatusEnum=function(a){switch(a){case 0:return FB_PROTOCOL.PlayerStatusEnum.CONNECTED;
case 1:return FB_PROTOCOL.PlayerStatusEnum.WAITING_REJOIN;
case 2:return FB_PROTOCOL.PlayerStatusEnum.DISCONNECTED;
case 3:return FB_PROTOCOL.PlayerStatusEnum.LEAVING;
case 4:return FB_PROTOCOL.PlayerStatusEnum.TABLE_LOCAL;
case 5:return FB_PROTOCOL.PlayerStatusEnum.RESERVATION
}return -1
};
FB_PROTOCOL.PlayerStatusEnum.toString=function(a){switch(a){case 0:return"CONNECTED";
case 1:return"WAITING_REJOIN";
case 2:return"DISCONNECTED";
case 3:return"LEAVING";
case 4:return"TABLE_LOCAL";
case 5:return"RESERVATION"
}return"INVALID_ENUM_VALUE"
};FB_PROTOCOL.ProbePacket=function(){this.classId=function(){return FB_PROTOCOL.ProbePacket.CLASSID
};
this.id={};
this.tableid={};
this.stamps=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.id);
a.writeInt(this.tableid);
a.writeInt(this.stamps.length);
var b;
for(b=0;
b<this.stamps.length;
b++){a.writeArray(this.stamps[b].save())
}return a
};
this.load=function(a){this.id=a.readInt();
this.tableid=a.readInt();
var c;
var b=a.readInt();
var d;
this.stamps=[];
for(c=0;
c<b;
c++){d=new FB_PROTOCOL.ProbeStamp();
d.load(a);
this.stamps.push(d)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.ProbePacket";
a.details={};
a.details.id=this.id;
a.details.tableid=this.tableid;
a.details.stamps=[];
for(b=0;
b<this.stamps.length;
b++){a.details.stamps.push(this.stamps[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.ProbePacket.CLASSID=201;FB_PROTOCOL.ProbeStamp=function(){this.classId=function(){return FB_PROTOCOL.ProbeStamp.CLASSID
};
this.clazz={};
this.timestamp={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeString(this.clazz);
a.writeLong(this.timestamp);
return a
};
this.load=function(a){this.clazz=a.readString();
this.timestamp=a.readLong()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.ProbeStamp";
a.details={};
a.details.clazz=this.clazz;
a.details.timestamp=this.timestamp;
return a
}
};
FB_PROTOCOL.ProbeStamp.CLASSID=200;FB_PROTOCOL.ProtocolObjectFactory={};
FB_PROTOCOL.ProtocolObjectFactory.create=function(c,a){var b;
switch(c){case FB_PROTOCOL.VersionPacket.CLASSID:b=new FB_PROTOCOL.VersionPacket();
b.load(a);
return b;
case FB_PROTOCOL.GameVersionPacket.CLASSID:b=new FB_PROTOCOL.GameVersionPacket();
b.load(a);
return b;
case FB_PROTOCOL.GoodPacket.CLASSID:b=new FB_PROTOCOL.GoodPacket();
b.load(a);
return b;
case FB_PROTOCOL.BadPacket.CLASSID:b=new FB_PROTOCOL.BadPacket();
b.load(a);
return b;
case FB_PROTOCOL.SystemMessagePacket.CLASSID:b=new FB_PROTOCOL.SystemMessagePacket();
b.load(a);
return b;
case FB_PROTOCOL.Param.CLASSID:b=new FB_PROTOCOL.Param();
b.load(a);
return b;
case FB_PROTOCOL.ParamFilter.CLASSID:b=new FB_PROTOCOL.ParamFilter();
b.load(a);
return b;
case FB_PROTOCOL.PingPacket.CLASSID:b=new FB_PROTOCOL.PingPacket();
b.load(a);
return b;
case FB_PROTOCOL.Attribute.CLASSID:b=new FB_PROTOCOL.Attribute();
b.load(a);
return b;
case FB_PROTOCOL.LoginRequestPacket.CLASSID:b=new FB_PROTOCOL.LoginRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.LoginResponsePacket.CLASSID:b=new FB_PROTOCOL.LoginResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.LogoutPacket.CLASSID:b=new FB_PROTOCOL.LogoutPacket();
b.load(a);
return b;
case FB_PROTOCOL.PlayerInfoPacket.CLASSID:b=new FB_PROTOCOL.PlayerInfoPacket();
b.load(a);
return b;
case FB_PROTOCOL.ForcedLogoutPacket.CLASSID:b=new FB_PROTOCOL.ForcedLogoutPacket();
b.load(a);
return b;
case FB_PROTOCOL.SeatInfoPacket.CLASSID:b=new FB_PROTOCOL.SeatInfoPacket();
b.load(a);
return b;
case FB_PROTOCOL.PlayerQueryRequestPacket.CLASSID:b=new FB_PROTOCOL.PlayerQueryRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.PlayerQueryResponsePacket.CLASSID:b=new FB_PROTOCOL.PlayerQueryResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.SystemInfoRequestPacket.CLASSID:b=new FB_PROTOCOL.SystemInfoRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.SystemInfoResponsePacket.CLASSID:b=new FB_PROTOCOL.SystemInfoResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.JoinRequestPacket.CLASSID:b=new FB_PROTOCOL.JoinRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.JoinResponsePacket.CLASSID:b=new FB_PROTOCOL.JoinResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.WatchRequestPacket.CLASSID:b=new FB_PROTOCOL.WatchRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.WatchResponsePacket.CLASSID:b=new FB_PROTOCOL.WatchResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.UnwatchRequestPacket.CLASSID:b=new FB_PROTOCOL.UnwatchRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.UnwatchResponsePacket.CLASSID:b=new FB_PROTOCOL.UnwatchResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.LeaveRequestPacket.CLASSID:b=new FB_PROTOCOL.LeaveRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.LeaveResponsePacket.CLASSID:b=new FB_PROTOCOL.LeaveResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.TableQueryRequestPacket.CLASSID:b=new FB_PROTOCOL.TableQueryRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.TableQueryResponsePacket.CLASSID:b=new FB_PROTOCOL.TableQueryResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.CreateTableRequestPacket.CLASSID:b=new FB_PROTOCOL.CreateTableRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.CreateTableResponsePacket.CLASSID:b=new FB_PROTOCOL.CreateTableResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.InvitePlayersRequestPacket.CLASSID:b=new FB_PROTOCOL.InvitePlayersRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.NotifyInvitedPacket.CLASSID:b=new FB_PROTOCOL.NotifyInvitedPacket();
b.load(a);
return b;
case FB_PROTOCOL.NotifyJoinPacket.CLASSID:b=new FB_PROTOCOL.NotifyJoinPacket();
b.load(a);
return b;
case FB_PROTOCOL.NotifyLeavePacket.CLASSID:b=new FB_PROTOCOL.NotifyLeavePacket();
b.load(a);
return b;
case FB_PROTOCOL.NotifyRegisteredPacket.CLASSID:b=new FB_PROTOCOL.NotifyRegisteredPacket();
b.load(a);
return b;
case FB_PROTOCOL.NotifyWatchingPacket.CLASSID:b=new FB_PROTOCOL.NotifyWatchingPacket();
b.load(a);
return b;
case FB_PROTOCOL.KickPlayerPacket.CLASSID:b=new FB_PROTOCOL.KickPlayerPacket();
b.load(a);
return b;
case FB_PROTOCOL.TableChatPacket.CLASSID:b=new FB_PROTOCOL.TableChatPacket();
b.load(a);
return b;
case FB_PROTOCOL.GameTransportPacket.CLASSID:b=new FB_PROTOCOL.GameTransportPacket();
b.load(a);
return b;
case FB_PROTOCOL.ServiceTransportPacket.CLASSID:b=new FB_PROTOCOL.ServiceTransportPacket();
b.load(a);
return b;
case FB_PROTOCOL.LocalServiceTransportPacket.CLASSID:b=new FB_PROTOCOL.LocalServiceTransportPacket();
b.load(a);
return b;
case FB_PROTOCOL.MttTransportPacket.CLASSID:b=new FB_PROTOCOL.MttTransportPacket();
b.load(a);
return b;
case FB_PROTOCOL.EncryptedTransportPacket.CLASSID:b=new FB_PROTOCOL.EncryptedTransportPacket();
b.load(a);
return b;
case FB_PROTOCOL.JoinChatChannelRequestPacket.CLASSID:b=new FB_PROTOCOL.JoinChatChannelRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.JoinChatChannelResponsePacket.CLASSID:b=new FB_PROTOCOL.JoinChatChannelResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.LeaveChatChannelPacket.CLASSID:b=new FB_PROTOCOL.LeaveChatChannelPacket();
b.load(a);
return b;
case FB_PROTOCOL.NotifyChannelChatPacket.CLASSID:b=new FB_PROTOCOL.NotifyChannelChatPacket();
b.load(a);
return b;
case FB_PROTOCOL.ChannelChatPacket.CLASSID:b=new FB_PROTOCOL.ChannelChatPacket();
b.load(a);
return b;
case FB_PROTOCOL.LobbyQueryPacket.CLASSID:b=new FB_PROTOCOL.LobbyQueryPacket();
b.load(a);
return b;
case FB_PROTOCOL.TableSnapshotPacket.CLASSID:b=new FB_PROTOCOL.TableSnapshotPacket();
b.load(a);
return b;
case FB_PROTOCOL.TableUpdatePacket.CLASSID:b=new FB_PROTOCOL.TableUpdatePacket();
b.load(a);
return b;
case FB_PROTOCOL.LobbySubscribePacket.CLASSID:b=new FB_PROTOCOL.LobbySubscribePacket();
b.load(a);
return b;
case FB_PROTOCOL.LobbyUnsubscribePacket.CLASSID:b=new FB_PROTOCOL.LobbyUnsubscribePacket();
b.load(a);
return b;
case FB_PROTOCOL.TableRemovedPacket.CLASSID:b=new FB_PROTOCOL.TableRemovedPacket();
b.load(a);
return b;
case FB_PROTOCOL.TournamentSnapshotPacket.CLASSID:b=new FB_PROTOCOL.TournamentSnapshotPacket();
b.load(a);
return b;
case FB_PROTOCOL.TournamentUpdatePacket.CLASSID:b=new FB_PROTOCOL.TournamentUpdatePacket();
b.load(a);
return b;
case FB_PROTOCOL.TournamentRemovedPacket.CLASSID:b=new FB_PROTOCOL.TournamentRemovedPacket();
b.load(a);
return b;
case FB_PROTOCOL.LobbyObjectSubscribePacket.CLASSID:b=new FB_PROTOCOL.LobbyObjectSubscribePacket();
b.load(a);
return b;
case FB_PROTOCOL.LobbyObjectUnsubscribePacket.CLASSID:b=new FB_PROTOCOL.LobbyObjectUnsubscribePacket();
b.load(a);
return b;
case FB_PROTOCOL.TableSnapshotListPacket.CLASSID:b=new FB_PROTOCOL.TableSnapshotListPacket();
b.load(a);
return b;
case FB_PROTOCOL.TableUpdateListPacket.CLASSID:b=new FB_PROTOCOL.TableUpdateListPacket();
b.load(a);
return b;
case FB_PROTOCOL.TournamentSnapshotListPacket.CLASSID:b=new FB_PROTOCOL.TournamentSnapshotListPacket();
b.load(a);
return b;
case FB_PROTOCOL.TournamentUpdateListPacket.CLASSID:b=new FB_PROTOCOL.TournamentUpdateListPacket();
b.load(a);
return b;
case FB_PROTOCOL.FilteredJoinTableRequestPacket.CLASSID:b=new FB_PROTOCOL.FilteredJoinTableRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.FilteredJoinTableResponsePacket.CLASSID:b=new FB_PROTOCOL.FilteredJoinTableResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.FilteredJoinCancelRequestPacket.CLASSID:b=new FB_PROTOCOL.FilteredJoinCancelRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.FilteredJoinCancelResponsePacket.CLASSID:b=new FB_PROTOCOL.FilteredJoinCancelResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.FilteredJoinTableAvailablePacket.CLASSID:b=new FB_PROTOCOL.FilteredJoinTableAvailablePacket();
b.load(a);
return b;
case FB_PROTOCOL.ProbeStamp.CLASSID:b=new FB_PROTOCOL.ProbeStamp();
b.load(a);
return b;
case FB_PROTOCOL.ProbePacket.CLASSID:b=new FB_PROTOCOL.ProbePacket();
b.load(a);
return b;
case FB_PROTOCOL.MttRegisterRequestPacket.CLASSID:b=new FB_PROTOCOL.MttRegisterRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.MttRegisterResponsePacket.CLASSID:b=new FB_PROTOCOL.MttRegisterResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.MttUnregisterRequestPacket.CLASSID:b=new FB_PROTOCOL.MttUnregisterRequestPacket();
b.load(a);
return b;
case FB_PROTOCOL.MttUnregisterResponsePacket.CLASSID:b=new FB_PROTOCOL.MttUnregisterResponsePacket();
b.load(a);
return b;
case FB_PROTOCOL.MttSeatedPacket.CLASSID:b=new FB_PROTOCOL.MttSeatedPacket();
b.load(a);
return b;
case FB_PROTOCOL.MttPickedUpPacket.CLASSID:b=new FB_PROTOCOL.MttPickedUpPacket();
b.load(a);
return b;
case FB_PROTOCOL.NotifySeatedPacket.CLASSID:b=new FB_PROTOCOL.NotifySeatedPacket();
b.load(a);
return b
}return null
};FB_PROTOCOL.ResponseStatusEnum=function(){};
FB_PROTOCOL.ResponseStatusEnum.OK=0;
FB_PROTOCOL.ResponseStatusEnum.FAILED=1;
FB_PROTOCOL.ResponseStatusEnum.DENIED=2;
FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum=function(a){switch(a){case 0:return FB_PROTOCOL.ResponseStatusEnum.OK;
case 1:return FB_PROTOCOL.ResponseStatusEnum.FAILED;
case 2:return FB_PROTOCOL.ResponseStatusEnum.DENIED
}return -1
};
FB_PROTOCOL.ResponseStatusEnum.toString=function(a){switch(a){case 0:return"OK";
case 1:return"FAILED";
case 2:return"DENIED"
}return"INVALID_ENUM_VALUE"
};FB_PROTOCOL.SeatInfoPacket=function(){this.classId=function(){return FB_PROTOCOL.SeatInfoPacket.CLASSID
};
this.tableid={};
this.seat={};
this.status=FB_PROTOCOL.PlayerStatusEnum.makePlayerStatusEnum(0);
this.player={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeByte(this.seat);
a.writeUnsignedByte(this.status);
a.writeArray(this.player.save());
return a
};
this.load=function(a){this.tableid=a.readInt();
this.seat=a.readByte();
this.status=FB_PROTOCOL.PlayerStatusEnum.makePlayerStatusEnum(a.readUnsignedByte());
this.player=new FB_PROTOCOL.PlayerInfoPacket();
this.player.load(a)
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.SeatInfoPacket";
a.details={};
a.details.tableid=this.tableid;
a.details.seat=this.seat;
a.details.status=FB_PROTOCOL.PlayerStatusEnum.toString(this.status);
a.details.player=this.player.getNormalizedObject();
return a
}
};
FB_PROTOCOL.SeatInfoPacket.CLASSID=15;FB_PROTOCOL.ServiceIdentifierEnum=function(){};
FB_PROTOCOL.ServiceIdentifierEnum.NAMESPACE=0;
FB_PROTOCOL.ServiceIdentifierEnum.CONTRACT=1;
FB_PROTOCOL.ServiceIdentifierEnum.makeServiceIdentifierEnum=function(a){switch(a){case 0:return FB_PROTOCOL.ServiceIdentifierEnum.NAMESPACE;
case 1:return FB_PROTOCOL.ServiceIdentifierEnum.CONTRACT
}return -1
};
FB_PROTOCOL.ServiceIdentifierEnum.toString=function(a){switch(a){case 0:return"NAMESPACE";
case 1:return"CONTRACT"
}return"INVALID_ENUM_VALUE"
};FB_PROTOCOL.ServiceTransportPacket=function(){this.classId=function(){return FB_PROTOCOL.ServiceTransportPacket.CLASSID
};
this.pid={};
this.seq={};
this.service={};
this.idtype={};
this.servicedata=[];
this.attributes=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.pid);
a.writeInt(this.seq);
a.writeString(this.service);
a.writeByte(this.idtype);
a.writeInt(this.servicedata.length);
a.writeArray(this.servicedata);
a.writeInt(this.attributes.length);
var b;
for(b=0;
b<this.attributes.length;
b++){a.writeArray(this.attributes[b].save())
}return a
};
this.load=function(a){this.pid=a.readInt();
this.seq=a.readInt();
this.service=a.readString();
this.idtype=a.readByte();
var b=a.readInt();
this.servicedata=a.readArray(b);
var c;
var d=a.readInt();
var e;
this.attributes=[];
for(c=0;
c<d;
c++){e=new FB_PROTOCOL.Attribute();
e.load(a);
this.attributes.push(e)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.ServiceTransportPacket";
a.details={};
a.details.pid=this.pid;
a.details.seq=this.seq;
a.details.service=this.service;
a.details.idtype=this.idtype;
a.details.servicedata=[];
for(b=0;
b<this.servicedata.length;
b++){a.details.servicedata.push(this.servicedata[b].getNormalizedObject())
}a.details.attributes=[];
for(b=0;
b<this.attributes.length;
b++){a.details.attributes.push(this.attributes[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.ServiceTransportPacket.CLASSID=101;FB_PROTOCOL.SystemInfoRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.SystemInfoRequestPacket.CLASSID
};
this.save=function(){return[]
};
this.load=function(a){};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.SystemInfoRequestPacket";
a.details={};
return a
}
};
FB_PROTOCOL.SystemInfoRequestPacket.CLASSID=18;FB_PROTOCOL.SystemInfoResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.SystemInfoResponsePacket.CLASSID
};
this.players={};
this.params=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.players);
a.writeInt(this.params.length);
var b;
for(b=0;
b<this.params.length;
b++){a.writeArray(this.params[b].save())
}return a
};
this.load=function(a){this.players=a.readInt();
var b;
var d=a.readInt();
var c;
this.params=[];
for(b=0;
b<d;
b++){c=new FB_PROTOCOL.Param();
c.load(a);
this.params.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.SystemInfoResponsePacket";
a.details={};
a.details.players=this.players;
a.details.params=[];
for(b=0;
b<this.params.length;
b++){a.details.params.push(FIREBASE.Styx.getParam(this.params[b]))
}return a
}
};
FB_PROTOCOL.SystemInfoResponsePacket.CLASSID=19;FB_PROTOCOL.SystemMessagePacket=function(){this.classId=function(){return FB_PROTOCOL.SystemMessagePacket.CLASSID
};
this.type={};
this.level={};
this.message={};
this.pids=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.type);
a.writeInt(this.level);
a.writeString(this.message);
a.writeInt(this.pids.length);
var b;
for(b=0;
b<this.pids.length;
b++){a.writeInt(this.pids[b])
}return a
};
this.load=function(a){this.type=a.readInt();
this.level=a.readInt();
this.message=a.readString();
var b;
var c=a.readInt();
this.pids=[];
for(b=0;
b<c;
b++){this.pids.push(a.readInt())
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.SystemMessagePacket";
a.details={};
a.details.type=this.type;
a.details.level=this.level;
a.details.message=this.message;
a.details.pids=[];
for(b=0;
b<this.pids.length;
b++){a.details.pids.push(this.pids[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.SystemMessagePacket.CLASSID=4;FB_PROTOCOL.TableChatPacket=function(){this.classId=function(){return FB_PROTOCOL.TableChatPacket.CLASSID
};
this.tableid={};
this.pid={};
this.message={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeInt(this.pid);
a.writeString(this.message);
return a
};
this.load=function(a){this.tableid=a.readInt();
this.pid=a.readInt();
this.message=a.readString()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TableChatPacket";
a.details={};
a.details.tableid=this.tableid;
a.details.pid=this.pid;
a.details.message=this.message;
return a
}
};
FB_PROTOCOL.TableChatPacket.CLASSID=80;FB_PROTOCOL.TableQueryRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.TableQueryRequestPacket.CLASSID
};
this.tableid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
return a
};
this.load=function(a){this.tableid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TableQueryRequestPacket";
a.details={};
a.details.tableid=this.tableid;
return a
}
};
FB_PROTOCOL.TableQueryRequestPacket.CLASSID=38;FB_PROTOCOL.TableQueryResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.TableQueryResponsePacket.CLASSID
};
this.tableid={};
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(0);
this.seats=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeUnsignedByte(this.status);
a.writeInt(this.seats.length);
var b;
for(b=0;
b<this.seats.length;
b++){a.writeArray(this.seats[b].save())
}return a
};
this.load=function(a){this.tableid=a.readInt();
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(a.readUnsignedByte());
var b;
var d=a.readInt();
var c;
this.seats=[];
for(b=0;
b<d;
b++){c=new FB_PROTOCOL.SeatInfoPacket();
c.load(a);
this.seats.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TableQueryResponsePacket";
a.details={};
a.details.tableid=this.tableid;
a.details.status=FB_PROTOCOL.ResponseStatusEnum.toString(this.status);
a.details.seats=[];
for(b=0;
b<this.seats.length;
b++){a.details.seats.push(this.seats[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.TableQueryResponsePacket.CLASSID=39;FB_PROTOCOL.TableRemovedPacket=function(){this.classId=function(){return FB_PROTOCOL.TableRemovedPacket.CLASSID
};
this.tableid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
return a
};
this.load=function(a){this.tableid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TableRemovedPacket";
a.details={};
a.details.tableid=this.tableid;
return a
}
};
FB_PROTOCOL.TableRemovedPacket.CLASSID=147;FB_PROTOCOL.TableSnapshotListPacket=function(){this.classId=function(){return FB_PROTOCOL.TableSnapshotListPacket.CLASSID
};
this.snapshots=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.snapshots.length);
var b;
for(b=0;
b<this.snapshots.length;
b++){a.writeArray(this.snapshots[b].save())
}return a
};
this.load=function(a){var b;
var d=a.readInt();
var c;
this.snapshots=[];
for(b=0;
b<d;
b++){c=new FB_PROTOCOL.TableSnapshotPacket();
c.load(a);
this.snapshots.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TableSnapshotListPacket";
a.details={};
a.details.snapshots=[];
for(b=0;
b<this.snapshots.length;
b++){a.details.snapshots.push(this.snapshots[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.TableSnapshotListPacket.CLASSID=153;FB_PROTOCOL.TableSnapshotPacket=function(){this.classId=function(){return FB_PROTOCOL.TableSnapshotPacket.CLASSID
};
this.tableid={};
this.address={};
this.name={};
this.capacity={};
this.seated={};
this.params=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeString(this.address);
a.writeString(this.name);
a.writeShort(this.capacity);
a.writeShort(this.seated);
a.writeInt(this.params.length);
var b;
for(b=0;
b<this.params.length;
b++){a.writeArray(this.params[b].save())
}return a
};
this.load=function(a){this.tableid=a.readInt();
this.address=a.readString();
this.name=a.readString();
this.capacity=a.readShort();
this.seated=a.readShort();
var b;
var d=a.readInt();
var c;
this.params=[];
for(b=0;
b<d;
b++){c=new FB_PROTOCOL.Param();
c.load(a);
this.params.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TableSnapshotPacket";
a.details={};
a.details.tableid=this.tableid;
a.details.address=this.address;
a.details.name=this.name;
a.details.capacity=this.capacity;
a.details.seated=this.seated;
a.details.params=[];
for(b=0;
b<this.params.length;
b++){a.details.params.push(FIREBASE.Styx.getParam(this.params[b]))
}return a
}
};
FB_PROTOCOL.TableSnapshotPacket.CLASSID=143;FB_PROTOCOL.TableUpdateListPacket=function(){this.classId=function(){return FB_PROTOCOL.TableUpdateListPacket.CLASSID
};
this.updates=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.updates.length);
var b;
for(b=0;
b<this.updates.length;
b++){a.writeArray(this.updates[b].save())
}return a
};
this.load=function(a){var c;
var d=a.readInt();
var b;
this.updates=[];
for(c=0;
c<d;
c++){b=new FB_PROTOCOL.TableUpdatePacket();
b.load(a);
this.updates.push(b)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TableUpdateListPacket";
a.details={};
a.details.updates=[];
for(b=0;
b<this.updates.length;
b++){a.details.updates.push(this.updates[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.TableUpdateListPacket.CLASSID=154;FB_PROTOCOL.TableUpdatePacket=function(){this.classId=function(){return FB_PROTOCOL.TableUpdatePacket.CLASSID
};
this.tableid={};
this.seated={};
this.params=[];
this.removedParams=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeShort(this.seated);
a.writeInt(this.params.length);
var b;
for(b=0;
b<this.params.length;
b++){a.writeArray(this.params[b].save())
}a.writeInt(this.removedParams.length);
for(b=0;
b<this.removedParams.length;
b++){a.writeString(this.removedParams[b])
}return a
};
this.load=function(a){this.tableid=a.readInt();
this.seated=a.readShort();
var c;
var e=a.readInt();
var d;
this.params=[];
for(c=0;
c<e;
c++){d=new FB_PROTOCOL.Param();
d.load(a);
this.params.push(d)
}var b=a.readInt();
this.removedParams=[];
for(c=0;
c<b;
c++){this.removedParams.push(a.readString())
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TableUpdatePacket";
a.details={};
a.details.tableid=this.tableid;
a.details.seated=this.seated;
a.details.params=[];
for(b=0;
b<this.params.length;
b++){a.details.params.push(FIREBASE.Styx.getParam(this.params[b]))
}a.details.removedParams=[];
for(b=0;
b<this.removedParams.length;
b++){a.details.removedParams.push(this.removedParams[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.TableUpdatePacket.CLASSID=144;FB_PROTOCOL.TournamentAttributesEnum=function(){};
FB_PROTOCOL.TournamentAttributesEnum.NAME=0;
FB_PROTOCOL.TournamentAttributesEnum.CAPACITY=1;
FB_PROTOCOL.TournamentAttributesEnum.REGISTERED=2;
FB_PROTOCOL.TournamentAttributesEnum.ACTIVE_PLAYERS=3;
FB_PROTOCOL.TournamentAttributesEnum.STATUS=4;
FB_PROTOCOL.TournamentAttributesEnum.makeTournamentAttributesEnum=function(a){switch(a){case 0:return FB_PROTOCOL.TournamentAttributesEnum.NAME;
case 1:return FB_PROTOCOL.TournamentAttributesEnum.CAPACITY;
case 2:return FB_PROTOCOL.TournamentAttributesEnum.REGISTERED;
case 3:return FB_PROTOCOL.TournamentAttributesEnum.ACTIVE_PLAYERS;
case 4:return FB_PROTOCOL.TournamentAttributesEnum.STATUS
}return -1
};
FB_PROTOCOL.TournamentAttributesEnum.toString=function(a){switch(a){case 0:return"NAME";
case 1:return"CAPACITY";
case 2:return"REGISTERED";
case 3:return"ACTIVE_PLAYERS";
case 4:return"STATUS"
}return"INVALID_ENUM_VALUE"
};FB_PROTOCOL.TournamentRegisterResponseStatusEnum=function(){};
FB_PROTOCOL.TournamentRegisterResponseStatusEnum.OK=0;
FB_PROTOCOL.TournamentRegisterResponseStatusEnum.FAILED=1;
FB_PROTOCOL.TournamentRegisterResponseStatusEnum.DENIED=2;
FB_PROTOCOL.TournamentRegisterResponseStatusEnum.DENIED_LOW_FUNDS=3;
FB_PROTOCOL.TournamentRegisterResponseStatusEnum.DENIED_MTT_FULL=4;
FB_PROTOCOL.TournamentRegisterResponseStatusEnum.DENIED_NO_ACCESS=5;
FB_PROTOCOL.TournamentRegisterResponseStatusEnum.DENIED_ALREADY_REGISTERED=6;
FB_PROTOCOL.TournamentRegisterResponseStatusEnum.DENIED_TOURNAMENT_RUNNING=7;
FB_PROTOCOL.TournamentRegisterResponseStatusEnum.makeTournamentRegisterResponseStatusEnum=function(a){switch(a){case 0:return FB_PROTOCOL.TournamentRegisterResponseStatusEnum.OK;
case 1:return FB_PROTOCOL.TournamentRegisterResponseStatusEnum.FAILED;
case 2:return FB_PROTOCOL.TournamentRegisterResponseStatusEnum.DENIED;
case 3:return FB_PROTOCOL.TournamentRegisterResponseStatusEnum.DENIED_LOW_FUNDS;
case 4:return FB_PROTOCOL.TournamentRegisterResponseStatusEnum.DENIED_MTT_FULL;
case 5:return FB_PROTOCOL.TournamentRegisterResponseStatusEnum.DENIED_NO_ACCESS;
case 6:return FB_PROTOCOL.TournamentRegisterResponseStatusEnum.DENIED_ALREADY_REGISTERED;
case 7:return FB_PROTOCOL.TournamentRegisterResponseStatusEnum.DENIED_TOURNAMENT_RUNNING
}return -1
};
FB_PROTOCOL.TournamentRegisterResponseStatusEnum.toString=function(a){switch(a){case 0:return"OK";
case 1:return"FAILED";
case 2:return"DENIED";
case 3:return"DENIED_LOW_FUNDS";
case 4:return"DENIED_MTT_FULL";
case 5:return"DENIED_NO_ACCESS";
case 6:return"DENIED_ALREADY_REGISTERED";
case 7:return"DENIED_TOURNAMENT_RUNNING"
}return"INVALID_ENUM_VALUE"
};FB_PROTOCOL.TournamentRemovedPacket=function(){this.classId=function(){return FB_PROTOCOL.TournamentRemovedPacket.CLASSID
};
this.mttid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.mttid);
return a
};
this.load=function(a){this.mttid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TournamentRemovedPacket";
a.details={};
a.details.mttid=this.mttid;
return a
}
};
FB_PROTOCOL.TournamentRemovedPacket.CLASSID=150;FB_PROTOCOL.TournamentSnapshotListPacket=function(){this.classId=function(){return FB_PROTOCOL.TournamentSnapshotListPacket.CLASSID
};
this.snapshots=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.snapshots.length);
var b;
for(b=0;
b<this.snapshots.length;
b++){a.writeArray(this.snapshots[b].save())
}return a
};
this.load=function(a){var b;
var c=a.readInt();
var d;
this.snapshots=[];
for(b=0;
b<c;
b++){d=new FB_PROTOCOL.TournamentSnapshotPacket();
d.load(a);
this.snapshots.push(d)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TournamentSnapshotListPacket";
a.details={};
a.details.snapshots=[];
for(b=0;
b<this.snapshots.length;
b++){a.details.snapshots.push(this.snapshots[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.TournamentSnapshotListPacket.CLASSID=155;FB_PROTOCOL.TournamentSnapshotPacket=function(){this.classId=function(){return FB_PROTOCOL.TournamentSnapshotPacket.CLASSID
};
this.mttid={};
this.address={};
this.params=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.mttid);
a.writeString(this.address);
a.writeInt(this.params.length);
var b;
for(b=0;
b<this.params.length;
b++){a.writeArray(this.params[b].save())
}return a
};
this.load=function(a){this.mttid=a.readInt();
this.address=a.readString();
var b;
var d=a.readInt();
var c;
this.params=[];
for(b=0;
b<d;
b++){c=new FB_PROTOCOL.Param();
c.load(a);
this.params.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TournamentSnapshotPacket";
a.details={};
a.details.mttid=this.mttid;
a.details.address=this.address;
a.details.params=[];
for(b=0;
b<this.params.length;
b++){a.details.params.push(FIREBASE.Styx.getParam(this.params[b]))
}return a
}
};
FB_PROTOCOL.TournamentSnapshotPacket.CLASSID=148;FB_PROTOCOL.TournamentUpdateListPacket=function(){this.classId=function(){return FB_PROTOCOL.TournamentUpdateListPacket.CLASSID
};
this.updates=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.updates.length);
var b;
for(b=0;
b<this.updates.length;
b++){a.writeArray(this.updates[b].save())
}return a
};
this.load=function(a){var b;
var c=a.readInt();
var d;
this.updates=[];
for(b=0;
b<c;
b++){d=new FB_PROTOCOL.TournamentUpdatePacket();
d.load(a);
this.updates.push(d)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TournamentUpdateListPacket";
a.details={};
a.details.updates=[];
for(b=0;
b<this.updates.length;
b++){a.details.updates.push(this.updates[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.TournamentUpdateListPacket.CLASSID=156;FB_PROTOCOL.TournamentUpdatePacket=function(){this.classId=function(){return FB_PROTOCOL.TournamentUpdatePacket.CLASSID
};
this.mttid={};
this.params=[];
this.removedParams=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.mttid);
a.writeInt(this.params.length);
var b;
for(b=0;
b<this.params.length;
b++){a.writeArray(this.params[b].save())
}a.writeInt(this.removedParams.length);
for(b=0;
b<this.removedParams.length;
b++){a.writeString(this.removedParams[b])
}return a
};
this.load=function(a){this.mttid=a.readInt();
var c;
var e=a.readInt();
var d;
this.params=[];
for(c=0;
c<e;
c++){d=new FB_PROTOCOL.Param();
d.load(a);
this.params.push(d)
}var b=a.readInt();
this.removedParams=[];
for(c=0;
c<b;
c++){this.removedParams.push(a.readString())
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.TournamentUpdatePacket";
a.details={};
a.details.mttid=this.mttid;
a.details.params=[];
for(b=0;
b<this.params.length;
b++){a.details.params.push(FIREBASE.Styx.getParam(this.params[b]))
}a.details.removedParams=[];
for(b=0;
b<this.removedParams.length;
b++){a.details.removedParams.push(this.removedParams[b].getNormalizedObject())
}return a
}
};
FB_PROTOCOL.TournamentUpdatePacket.CLASSID=149;FB_PROTOCOL.UnwatchRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.UnwatchRequestPacket.CLASSID
};
this.tableid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
return a
};
this.load=function(a){this.tableid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.UnwatchRequestPacket";
a.details={};
a.details.tableid=this.tableid;
return a
}
};
FB_PROTOCOL.UnwatchRequestPacket.CLASSID=34;FB_PROTOCOL.UnwatchResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.UnwatchResponsePacket.CLASSID
};
this.tableid={};
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(0);
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeUnsignedByte(this.status);
return a
};
this.load=function(a){this.tableid=a.readInt();
this.status=FB_PROTOCOL.ResponseStatusEnum.makeResponseStatusEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.UnwatchResponsePacket";
a.details={};
a.details.tableid=this.tableid;
a.details.status=FB_PROTOCOL.ResponseStatusEnum.toString(this.status);
return a
}
};
FB_PROTOCOL.UnwatchResponsePacket.CLASSID=35;FB_PROTOCOL.VersionPacket=function(){this.classId=function(){return FB_PROTOCOL.VersionPacket.CLASSID
};
this.game={};
this.operatorid={};
this.protocol={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.game);
a.writeInt(this.operatorid);
a.writeInt(this.protocol);
return a
};
this.load=function(a){this.game=a.readInt();
this.operatorid=a.readInt();
this.protocol=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.VersionPacket";
a.details={};
a.details.game=this.game;
a.details.operatorid=this.operatorid;
a.details.protocol=this.protocol;
return a
}
};
FB_PROTOCOL.VersionPacket.CLASSID=0;FB_PROTOCOL.WatchRequestPacket=function(){this.classId=function(){return FB_PROTOCOL.WatchRequestPacket.CLASSID
};
this.tableid={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
return a
};
this.load=function(a){this.tableid=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.WatchRequestPacket";
a.details={};
a.details.tableid=this.tableid;
return a
}
};
FB_PROTOCOL.WatchRequestPacket.CLASSID=32;FB_PROTOCOL.WatchResponsePacket=function(){this.classId=function(){return FB_PROTOCOL.WatchResponsePacket.CLASSID
};
this.tableid={};
this.status=FB_PROTOCOL.WatchResponseStatusEnum.makeWatchResponseStatusEnum(0);
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.tableid);
a.writeUnsignedByte(this.status);
return a
};
this.load=function(a){this.tableid=a.readInt();
this.status=FB_PROTOCOL.WatchResponseStatusEnum.makeWatchResponseStatusEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="FB_PROTOCOL.WatchResponsePacket";
a.details={};
a.details.tableid=this.tableid;
a.details.status=FB_PROTOCOL.WatchResponseStatusEnum.toString(this.status);
return a
}
};
FB_PROTOCOL.WatchResponsePacket.CLASSID=33;FB_PROTOCOL.WatchResponseStatusEnum=function(){};
FB_PROTOCOL.WatchResponseStatusEnum.OK=0;
FB_PROTOCOL.WatchResponseStatusEnum.FAILED=1;
FB_PROTOCOL.WatchResponseStatusEnum.DENIED=2;
FB_PROTOCOL.WatchResponseStatusEnum.DENIED_ALREADY_SEATED=3;
FB_PROTOCOL.WatchResponseStatusEnum.makeWatchResponseStatusEnum=function(a){switch(a){case 0:return FB_PROTOCOL.WatchResponseStatusEnum.OK;
case 1:return FB_PROTOCOL.WatchResponseStatusEnum.FAILED;
case 2:return FB_PROTOCOL.WatchResponseStatusEnum.DENIED;
case 3:return FB_PROTOCOL.WatchResponseStatusEnum.DENIED_ALREADY_SEATED
}return -1
};
FB_PROTOCOL.WatchResponseStatusEnum.toString=function(a){switch(a){case 0:return"OK";
case 1:return"FAILED";
case 2:return"DENIED";
case 3:return"DENIED_ALREADY_SEATED"
}return"INVALID_ENUM_VALUE"
};