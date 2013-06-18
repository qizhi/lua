// This file contains mock implementations of the firebase protocol objects

var FB_PROTOCOL = FB_PROTOCOL || {};
var FIREBASE = FIREBASE || {};
var TESTGAME = TESTGAME || {};

// CLASSID definitions
FB_PROTOCOL.Attribute = FB_PROTOCOL.Attribute || {};
FB_PROTOCOL.BadPacket = FB_PROTOCOL.BadPacket || {};
FB_PROTOCOL.ChannelChatPacket = FB_PROTOCOL.ChannelChatPacket || {};
FB_PROTOCOL.CreateTableRequestPacket = FB_PROTOCOL.CreateTableRequestPacket
		|| {};
FB_PROTOCOL.CreateTableResponsePacket = FB_PROTOCOL.CreateTableResponsePacket
		|| {};
FB_PROTOCOL.EncryptedTransportPacket = FB_PROTOCOL.EncryptedTransportPacket
		|| {};
FB_PROTOCOL.FilteredJoinCancelRequestPacket = FB_PROTOCOL.FilteredJoinCancelRequestPacket
		|| {};
FB_PROTOCOL.FilteredJoinCancelResponsePacket = FB_PROTOCOL.FilteredJoinCancelResponsePacket
		|| {};
FB_PROTOCOL.FilteredJoinTableAvailablePacket = FB_PROTOCOL.FilteredJoinTableAvailablePacket
		|| {};
FB_PROTOCOL.FilteredJoinTableRequestPacket = FB_PROTOCOL.FilteredJoinTableRequestPacket
		|| {};
FB_PROTOCOL.FilteredJoinTableResponsePacket = FB_PROTOCOL.FilteredJoinTableResponsePacket
		|| {};
FB_PROTOCOL.ForcedLogoutPacket = FB_PROTOCOL.ForcedLogoutPacket || {};
FB_PROTOCOL.GameTransportPacket = FB_PROTOCOL.GameTransportPacket || {};
FB_PROTOCOL.GameVersionPacket = FB_PROTOCOL.GameVersionPacket || {};
FB_PROTOCOL.GoodPacket = FB_PROTOCOL.GoodPacket || {};
FB_PROTOCOL.InvitePlayersRequestPacket = FB_PROTOCOL.InvitePlayersRequestPacket
		|| {};
FB_PROTOCOL.JoinChatChannelRequestPacket = FB_PROTOCOL.JoinChatChannelRequestPacket
		|| {};
FB_PROTOCOL.JoinChatChannelResponsePacket = FB_PROTOCOL.JoinChatChannelResponsePacket
		|| {};
FB_PROTOCOL.JoinRequestPacket = FB_PROTOCOL.JoinRequestPacket || {};
FB_PROTOCOL.JoinResponsePacket = FB_PROTOCOL.JoinResponsePacket || {};
FB_PROTOCOL.KickPlayerPacket = FB_PROTOCOL.KickPlayerPacket || {};
FB_PROTOCOL.LeaveChatChannelPacket = FB_PROTOCOL.LeaveChatChannelPacket || {};
FB_PROTOCOL.LeaveRequestPacket = FB_PROTOCOL.LeaveRequestPacket || {};
FB_PROTOCOL.LeaveResponsePacket = FB_PROTOCOL.LeaveResponsePacket || {};
FB_PROTOCOL.LobbyObjectSubscribePacket = FB_PROTOCOL.LobbyObjectSubscribePacket
		|| {};
FB_PROTOCOL.LobbyObjectUnsubscribePacket = FB_PROTOCOL.LobbyObjectUnsubscribePacket
		|| {};
FB_PROTOCOL.LobbyQueryPacket = FB_PROTOCOL.LobbyQueryPacket || {};
FB_PROTOCOL.LobbySubscribePacket = FB_PROTOCOL.LobbySubscribePacket || {};
FB_PROTOCOL.LobbyUnsubscribePacket = FB_PROTOCOL.LobbyUnsubscribePacket || {};
FB_PROTOCOL.LocalServiceTransportPacket = FB_PROTOCOL.LocalServiceTransportPacket
		|| {};
FB_PROTOCOL.LoginRequestPacket = FB_PROTOCOL.LoginRequestPacket || {};
FB_PROTOCOL.LoginResponsePacket = FB_PROTOCOL.LoginResponsePacket || {};
FB_PROTOCOL.LogoutPacket = FB_PROTOCOL.LogoutPacket || {};
FB_PROTOCOL.MttPickedUpPacket = FB_PROTOCOL.MttPickedUpPacket || {};
FB_PROTOCOL.MttRegisterRequestPacket = FB_PROTOCOL.MttRegisterRequestPacket
		|| {};
FB_PROTOCOL.MttRegisterResponsePacket = FB_PROTOCOL.MttRegisterResponsePacket
		|| {};
FB_PROTOCOL.MttSeatedPacket = FB_PROTOCOL.MttSeatedPacket || {};
FB_PROTOCOL.MttTransportPacket = FB_PROTOCOL.MttTransportPacket || {};
FB_PROTOCOL.MttUnregisterRequestPacket = FB_PROTOCOL.MttUnregisterRequestPacket
		|| {};
FB_PROTOCOL.MttUnregisterResponsePacket = FB_PROTOCOL.MttUnregisterResponsePacket
		|| {};
FB_PROTOCOL.NotifyChannelChatPacket = FB_PROTOCOL.NotifyChannelChatPacket || {};
FB_PROTOCOL.NotifyInvitedPacket = FB_PROTOCOL.NotifyInvitedPacket || {};
FB_PROTOCOL.NotifyJoinPacket = FB_PROTOCOL.NotifyJoinPacket || {};
FB_PROTOCOL.NotifyLeavePacket = FB_PROTOCOL.NotifyLeavePacket || {};
FB_PROTOCOL.NotifyRegisteredPacket = FB_PROTOCOL.NotifyRegisteredPacket || {};
FB_PROTOCOL.NotifySeatedPacket = FB_PROTOCOL.NotifySeatedPacket || {};
FB_PROTOCOL.NotifyWatchingPacket = FB_PROTOCOL.NotifyWatchingPacket || {};
FB_PROTOCOL.Param = FB_PROTOCOL.Param || {};
FB_PROTOCOL.ParamFilter = FB_PROTOCOL.ParamFilter || {};
FB_PROTOCOL.PingPacket = FB_PROTOCOL.PingPacket || {};
FB_PROTOCOL.PlayerInfoPacket = FB_PROTOCOL.PlayerInfoPacket || {};
FB_PROTOCOL.PlayerQueryRequestPacket = FB_PROTOCOL.PlayerQueryRequestPacket
		|| {};
FB_PROTOCOL.PlayerQueryResponsePacket = FB_PROTOCOL.PlayerQueryResponsePacket
		|| {};
FB_PROTOCOL.ProbePacket = FB_PROTOCOL.ProbePacket || {};
FB_PROTOCOL.ProbeStamp = FB_PROTOCOL.ProbeStamp || {};
FB_PROTOCOL.SeatInfoPacket = FB_PROTOCOL.SeatInfoPacket || {};
FB_PROTOCOL.ServiceTransportPacket = FB_PROTOCOL.ServiceTransportPacket || {};
FB_PROTOCOL.SystemInfoRequestPacket = FB_PROTOCOL.SystemInfoRequestPacket || {};
FB_PROTOCOL.SystemInfoResponsePacket = FB_PROTOCOL.SystemInfoResponsePacket
		|| {};
FB_PROTOCOL.SystemMessagePacket = FB_PROTOCOL.SystemMessagePacket || {};
FB_PROTOCOL.TableChatPacket = FB_PROTOCOL.TableChatPacket || {};
FB_PROTOCOL.TableQueryRequestPacket = FB_PROTOCOL.TableQueryRequestPacket || {};
FB_PROTOCOL.TableQueryResponsePacket = FB_PROTOCOL.TableQueryResponsePacket
		|| {};
FB_PROTOCOL.TableRemovedPacket = FB_PROTOCOL.TableRemovedPacket || {};
FB_PROTOCOL.TableSnapshotListPacket = FB_PROTOCOL.TableSnapshotListPacket || {};
FB_PROTOCOL.TableSnapshotPacket = FB_PROTOCOL.TableSnapshotPacket || {};
FB_PROTOCOL.TableUpdateListPacket = FB_PROTOCOL.TableUpdateListPacket || {};
FB_PROTOCOL.TableUpdatePacket = FB_PROTOCOL.TableUpdatePacket || {};
FB_PROTOCOL.TournamentRemovedPacket = FB_PROTOCOL.TournamentRemovedPacket || {};
FB_PROTOCOL.TournamentSnapshotListPacket = FB_PROTOCOL.TournamentSnapshotListPacket
		|| {};
FB_PROTOCOL.TournamentSnapshotPacket = FB_PROTOCOL.TournamentSnapshotPacket
		|| {};
FB_PROTOCOL.TournamentUpdateListPacket = FB_PROTOCOL.TournamentUpdateListPacket
		|| {};
FB_PROTOCOL.TournamentUpdatePacket = FB_PROTOCOL.TournamentUpdatePacket || {};
FB_PROTOCOL.UnwatchRequestPacket = FB_PROTOCOL.UnwatchRequestPacket || {};
FB_PROTOCOL.UnwatchResponsePacket = FB_PROTOCOL.UnwatchResponsePacket || {};
FB_PROTOCOL.VersionPacket = FB_PROTOCOL.VersionPacket || {};
FB_PROTOCOL.WatchRequestPacket = FB_PROTOCOL.WatchRequestPacket || {};
FB_PROTOCOL.WatchResponsePacket = FB_PROTOCOL.WatchResponsePacket || {};

FB_PROTOCOL.CreateTableRequestPacket = function() {
	this.classId = function() {
		return FB_PROTOCOL.CreateTableRequestPacket.CLASSID;
	};

	this.seq = {}; // int;
	this.gameid = {}; // int;
	this.seats = {}; // int;
	this.params = [];
	this.invitees = [];

	this.save = function() {
		var byteArray = new FIREBASE.ByteArray();
        byteArray.writeInt(this.seq);
        byteArray.writeInt(this.gameid);
        byteArray.writeByte(this.seats);
        byteArray.writeInt(this.params.length);
        var i;
        for( i = 0; i < this.params.length; i ++) {
            byteArray.writeArray(this.params[i].save());
        }
        byteArray.writeInt(this.invitees.length);
        for( i = 0; i < this.invitees.length; i ++) {
            byteArray.writeInt(this.invitees[i]);
        }
        return byteArray;
	};
};

FB_PROTOCOL.Param = function() {
	this.classId = function() {
		return FB_PROTOCOL.Param.CLASSID;
	};

	this.key = {}; // String;
	this.type = {}; // int;
	this.value = [];
};

FB_PROTOCOL.ParameterTypeEnum = function() {
};
FB_PROTOCOL.ParameterTypeEnum.STRING = 0;
FB_PROTOCOL.ParameterTypeEnum.INT = 1;
FB_PROTOCOL.ParameterTypeEnum.DATE = 2;

FB_PROTOCOL.JoinRequestPacket = function() {
	this.classId = function() {
		return FB_PROTOCOL.JoinRequestPacket.CLASSID;
	};

	this.tableid = {}; // int;
	this.seat = {}; // int;
	this.params = [];
};

FB_PROTOCOL.LoginRequestPacket = function() {
	this.classId = function() {
		return FB_PROTOCOL.LoginRequestPacket.CLASSID;
	};

	this.user = {}; // String;
	this.password = {}; // String;
	this.operatorid = {}; // int;
	this.credentials = [];

};

FB_PROTOCOL.LogoutPacket = function() {
	this.classId = function() {
		return FB_PROTOCOL.LogoutPacket.CLASSID;
	};

	this.leaveTables = {}; // Boolean;
};

FB_PROTOCOL.JoinRequestPacket = function() {
	this.classId = function() {
		return FB_PROTOCOL.JoinRequestPacket.CLASSID;
	};

	this.tableid = {}; // int;
	this.seat = {}; // int;
	this.params = [];

};

FB_PROTOCOL.GameTransportPacket = function() {
	this.classId = function() {
		return FB_PROTOCOL.GameTransportPacket.CLASSID;
	};

	this.tableid = {}; // int;
	this.pid = {}; // int;
	this.gamedata = [];
	this.attributes = [];

};

FB_PROTOCOL.LeaveRequestPacket = function() {
	this.classId = function() {
		return FB_PROTOCOL.LeaveRequestPacket.CLASSID;
	};

	this.tableid = {}; // int;

};

TESTGAME.Bet = function() {
	this.classId = function() {
		return TESTGAME.Bet.CLASSID;
	};

	this.betAmount = {}; // int;
	this.betNumber = {}; // int;
	this.value = [];

	this.save = function() {
		var byteArray = new FIREBASE.ByteArray();
		byteArray.writeInt(this.betAmount);
		byteArray.writeInt(this.betNumber);
		byteArray.writeInt(this.value.length);
		byteArray.writeArray(this.value);
		return byteArray;
	};

};

FB_PROTOCOL.LobbyTypeEnum = FB_PROTOCOL.LobbyTypeEnum || {};
FB_PROTOCOL.LobbyTypeEnum.REGULAR = 0;
FB_PROTOCOL.LobbyTypeEnum.MTT = 1;

FB_PROTOCOL.LobbySubscribePacket = function() {
	this.classId = function() {
		return FB_PROTOCOL.LobbySubscribePacket.CLASSID;
	};

	this.type = {};
	this.gameid = {}; // int;
	this.address = {}; // String;

};

FB_PROTOCOL.WatchRequestPacket = function() {
	this.classId = function() {
		return FB_PROTOCOL.WatchRequestPacket.CLASSID;
	};

	this.tableid = {}; // int;
};

FB_PROTOCOL.Attribute.CLASSID = 8;
FB_PROTOCOL.BadPacket.CLASSID = 3;
FB_PROTOCOL.ChannelChatPacket.CLASSID = 124;
FB_PROTOCOL.CreateTableRequestPacket.CLASSID = 40;
FB_PROTOCOL.CreateTableResponsePacket.CLASSID = 41;
FB_PROTOCOL.EncryptedTransportPacket.CLASSID = 105;
FB_PROTOCOL.FilteredJoinCancelRequestPacket.CLASSID = 172;
FB_PROTOCOL.FilteredJoinCancelResponsePacket.CLASSID = 173;
FB_PROTOCOL.FilteredJoinTableAvailablePacket.CLASSID = 174;
FB_PROTOCOL.FilteredJoinTableRequestPacket.CLASSID = 170;
FB_PROTOCOL.FilteredJoinTableResponsePacket.CLASSID = 171;
FB_PROTOCOL.ForcedLogoutPacket.CLASSID = 14;
FB_PROTOCOL.GameTransportPacket.CLASSID = 100;
FB_PROTOCOL.GameVersionPacket.CLASSID = 1;
FB_PROTOCOL.GoodPacket.CLASSID = 2;
FB_PROTOCOL.InvitePlayersRequestPacket.CLASSID = 42;
FB_PROTOCOL.JoinChatChannelRequestPacket.CLASSID = 120;
FB_PROTOCOL.JoinChatChannelResponsePacket.CLASSID = 121;
FB_PROTOCOL.JoinRequestPacket.CLASSID = 30;
FB_PROTOCOL.JoinResponsePacket.CLASSID = 31;
FB_PROTOCOL.KickPlayerPacket.CLASSID = 64;
FB_PROTOCOL.LeaveChatChannelPacket.CLASSID = 122;
FB_PROTOCOL.LeaveRequestPacket.CLASSID = 36;
FB_PROTOCOL.LeaveResponsePacket.CLASSID = 37;
FB_PROTOCOL.LobbyObjectSubscribePacket.CLASSID = 151;
FB_PROTOCOL.LobbyObjectUnsubscribePacket.CLASSID = 152;
FB_PROTOCOL.LobbyQueryPacket.CLASSID = 142;
FB_PROTOCOL.LobbySubscribePacket.CLASSID = 145;
FB_PROTOCOL.LobbyUnsubscribePacket.CLASSID = 146;
FB_PROTOCOL.LocalServiceTransportPacket.CLASSID = 103;
FB_PROTOCOL.LoginRequestPacket.CLASSID = 10;
FB_PROTOCOL.LoginResponsePacket.CLASSID = 11;
FB_PROTOCOL.LogoutPacket.CLASSID = 12;
FB_PROTOCOL.MttPickedUpPacket.CLASSID = 210;
FB_PROTOCOL.MttRegisterRequestPacket.CLASSID = 205;
FB_PROTOCOL.MttRegisterResponsePacket.CLASSID = 206;
FB_PROTOCOL.MttSeatedPacket.CLASSID = 209;
FB_PROTOCOL.MttTransportPacket.CLASSID = 104;
FB_PROTOCOL.MttUnregisterRequestPacket.CLASSID = 207;
FB_PROTOCOL.MttUnregisterResponsePacket.CLASSID = 208;
FB_PROTOCOL.NotifyChannelChatPacket.CLASSID = 123;
FB_PROTOCOL.NotifyInvitedPacket.CLASSID = 43;
FB_PROTOCOL.NotifyJoinPacket.CLASSID = 60;
FB_PROTOCOL.NotifyLeavePacket.CLASSID = 61;
FB_PROTOCOL.NotifyRegisteredPacket.CLASSID = 211;
FB_PROTOCOL.NotifySeatedPacket.CLASSID = 62;
FB_PROTOCOL.NotifyWatchingPacket.CLASSID = 63;
FB_PROTOCOL.Param.CLASSID = 5;
FB_PROTOCOL.ParamFilter.CLASSID = 6;
FB_PROTOCOL.PingPacket.CLASSID = 7;
FB_PROTOCOL.PlayerInfoPacket.CLASSID = 13;
FB_PROTOCOL.PlayerQueryRequestPacket.CLASSID = 16;
FB_PROTOCOL.PlayerQueryResponsePacket.CLASSID = 17;
FB_PROTOCOL.ProbePacket.CLASSID = 201;
FB_PROTOCOL.ProbeStamp.CLASSID = 200;
FB_PROTOCOL.SeatInfoPacket.CLASSID = 15;
FB_PROTOCOL.ServiceTransportPacket.CLASSID = 101;
FB_PROTOCOL.SystemInfoRequestPacket.CLASSID = 18;
FB_PROTOCOL.SystemInfoResponsePacket.CLASSID = 19;
FB_PROTOCOL.SystemMessagePacket.CLASSID = 4;
FB_PROTOCOL.TableChatPacket.CLASSID = 80;
FB_PROTOCOL.TableQueryRequestPacket.CLASSID = 38;
FB_PROTOCOL.TableQueryResponsePacket.CLASSID = 39;
FB_PROTOCOL.TableRemovedPacket.CLASSID = 147;
FB_PROTOCOL.TableSnapshotListPacket.CLASSID = 153;
FB_PROTOCOL.TableSnapshotPacket.CLASSID = 143;
FB_PROTOCOL.TableUpdateListPacket.CLASSID = 154;
FB_PROTOCOL.TableUpdatePacket.CLASSID = 144;
FB_PROTOCOL.TournamentRemovedPacket.CLASSID = 150;
FB_PROTOCOL.TournamentSnapshotListPacket.CLASSID = 155;
FB_PROTOCOL.TournamentSnapshotPacket.CLASSID = 148;
FB_PROTOCOL.TournamentUpdateListPacket.CLASSID = 156;
FB_PROTOCOL.TournamentUpdatePacket.CLASSID = 149;
FB_PROTOCOL.UnwatchRequestPacket.CLASSID = 34;
FB_PROTOCOL.UnwatchResponsePacket.CLASSID = 35;
FB_PROTOCOL.VersionPacket.CLASSID = 0;
FB_PROTOCOL.WatchRequestPacket.CLASSID = 32;
FB_PROTOCOL.WatchResponsePacket.CLASSID = 33;
