// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

var com = com || {};
com.cubeia = com.cubeia || {};
com.cubeia.games = com.cubeia.games || {};
com.cubeia.games.poker = com.cubeia.games.poker || {};
com.cubeia.games.poker.io = com.cubeia.games.poker.io || {};
com.cubeia.games.poker.io.protocol = com.cubeia.games.poker.io.protocol || {};


com.cubeia.games.poker.io.protocol.ActionTypeEnum=function(){};
com.cubeia.games.poker.io.protocol.ActionTypeEnum.SMALL_BLIND=0;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND=1;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.CALL=2;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.CHECK=3;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.BET=4;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.RAISE=5;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.FOLD=6;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.DECLINE_ENTRY_BET=7;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.ANTE=8;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND_PLUS_DEAD_SMALL_BLIND=9;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.DEAD_SMALL_BLIND=10;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.makeActionTypeEnum=function(a){switch(a){case 0:return com.cubeia.games.poker.io.protocol.ActionTypeEnum.SMALL_BLIND;
case 1:return com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND;
case 2:return com.cubeia.games.poker.io.protocol.ActionTypeEnum.CALL;
case 3:return com.cubeia.games.poker.io.protocol.ActionTypeEnum.CHECK;
case 4:return com.cubeia.games.poker.io.protocol.ActionTypeEnum.BET;
case 5:return com.cubeia.games.poker.io.protocol.ActionTypeEnum.RAISE;
case 6:return com.cubeia.games.poker.io.protocol.ActionTypeEnum.FOLD;
case 7:return com.cubeia.games.poker.io.protocol.ActionTypeEnum.DECLINE_ENTRY_BET;
case 8:return com.cubeia.games.poker.io.protocol.ActionTypeEnum.ANTE;
case 9:return com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND_PLUS_DEAD_SMALL_BLIND;
case 10:return com.cubeia.games.poker.io.protocol.ActionTypeEnum.DEAD_SMALL_BLIND
}return -1
};
com.cubeia.games.poker.io.protocol.ActionTypeEnum.toString=function(a){switch(a){case 0:return"SMALL_BLIND";
case 1:return"BIG_BLIND";
case 2:return"CALL";
case 3:return"CHECK";
case 4:return"BET";
case 5:return"RAISE";
case 6:return"FOLD";
case 7:return"DECLINE_ENTRY_BET";
case 8:return"ANTE";
case 9:return"BIG_BLIND_PLUS_DEAD_SMALL_BLIND";
case 10:return"DEAD_SMALL_BLIND"
}return"INVALID_ENUM_VALUE"
};com.cubeia.games.poker.io.protocol.BestHand=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.BestHand.CLASSID
};
this.player={};
this.handType={};
this.cards=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.player);
a.writeUnsignedByte(this.handType);
a.writeInt(this.cards.length);
var b;
for(b=0;
b<this.cards.length;
b++){a.writeArray(this.cards[b].save())
}return a
};
this.load=function(a){this.player=a.readInt();
this.handType=com.cubeia.games.poker.io.protocol.HandTypeEnum.makeHandTypeEnum(a.readUnsignedByte());
var b;
var d=a.readInt();
var c;
this.cards=[];
for(b=0;
b<d;
b++){c=new com.cubeia.games.poker.io.protocol.GameCard();
c.load(a);
this.cards.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.BestHand";
a.details={};
a.details.player=this.player;
a.details.handType=com.cubeia.games.poker.io.protocol.HandTypeEnum.toString(this.handType);
a.details.cards=[];
for(b=0;
b<this.cards.length;
b++){a.details.cards.push(this.cards[b].getNormalizedObject())
}return a
}
};
com.cubeia.games.poker.io.protocol.BestHand.CLASSID=5;com.cubeia.games.poker.io.protocol.BuyInInfoRequest=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.BuyInInfoRequest.CLASSID
};
this.save=function(){return[]
};
this.load=function(a){};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.BuyInInfoRequest";
a.details={};
return a
}
};
com.cubeia.games.poker.io.protocol.BuyInInfoRequest.CLASSID=22;com.cubeia.games.poker.io.protocol.BuyInInfoResponse=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.BuyInInfoResponse.CLASSID
};
this.maxAmount={};
this.minAmount={};
this.balanceInWallet={};
this.balanceOnTable={};
this.mandatoryBuyin={};
this.resultCode={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.maxAmount);
a.writeInt(this.minAmount);
a.writeInt(this.balanceInWallet);
a.writeInt(this.balanceOnTable);
a.writeBoolean(this.mandatoryBuyin);
a.writeUnsignedByte(this.resultCode);
return a
};
this.load=function(a){this.maxAmount=a.readInt();
this.minAmount=a.readInt();
this.balanceInWallet=a.readInt();
this.balanceOnTable=a.readInt();
this.mandatoryBuyin=a.readBoolean();
this.resultCode=com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.makeBuyInInfoResultCodeEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.BuyInInfoResponse";
a.details={};
a.details.maxAmount=this.maxAmount;
a.details.minAmount=this.minAmount;
a.details.balanceInWallet=this.balanceInWallet;
a.details.balanceOnTable=this.balanceOnTable;
a.details.mandatoryBuyin=this.mandatoryBuyin;
a.details.resultCode=com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.toString(this.resultCode);
return a
}
};
com.cubeia.games.poker.io.protocol.BuyInInfoResponse.CLASSID=23;com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum=function(){};
com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.OK=0;
com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.MAX_LIMIT_REACHED=1;
com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.UNSPECIFIED_ERROR=2;
com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.makeBuyInInfoResultCodeEnum=function(a){switch(a){case 0:return com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.OK;
case 1:return com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.MAX_LIMIT_REACHED;
case 2:return com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.UNSPECIFIED_ERROR
}return -1
};
com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.toString=function(a){switch(a){case 0:return"OK";
case 1:return"MAX_LIMIT_REACHED";
case 2:return"UNSPECIFIED_ERROR"
}return"INVALID_ENUM_VALUE"
};com.cubeia.games.poker.io.protocol.BuyInRequest=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.BuyInRequest.CLASSID
};
this.amount={};
this.sitInIfSuccessful={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.amount);
a.writeBoolean(this.sitInIfSuccessful);
return a
};
this.load=function(a){this.amount=a.readInt();
this.sitInIfSuccessful=a.readBoolean()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.BuyInRequest";
a.details={};
a.details.amount=this.amount;
a.details.sitInIfSuccessful=this.sitInIfSuccessful;
return a
}
};
com.cubeia.games.poker.io.protocol.BuyInRequest.CLASSID=24;com.cubeia.games.poker.io.protocol.BuyInResponse=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.BuyInResponse.CLASSID
};
this.balance={};
this.pendingBalance={};
this.amountBroughtIn={};
this.resultCode={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.balance);
a.writeInt(this.pendingBalance);
a.writeInt(this.amountBroughtIn);
a.writeUnsignedByte(this.resultCode);
return a
};
this.load=function(a){this.balance=a.readInt();
this.pendingBalance=a.readInt();
this.amountBroughtIn=a.readInt();
this.resultCode=com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.makeBuyInResultCodeEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.BuyInResponse";
a.details={};
a.details.balance=this.balance;
a.details.pendingBalance=this.pendingBalance;
a.details.amountBroughtIn=this.amountBroughtIn;
a.details.resultCode=com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.toString(this.resultCode);
return a
}
};
com.cubeia.games.poker.io.protocol.BuyInResponse.CLASSID=25;com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum=function(){};
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.OK=0;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.PENDING=1;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.INSUFFICIENT_FUNDS_ERROR=2;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.PARTNER_ERROR=3;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.MAX_LIMIT_REACHED=4;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.AMOUNT_TOO_HIGH=5;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.UNSPECIFIED_ERROR=6;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.SESSION_NOT_OPEN=7;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.makeBuyInResultCodeEnum=function(a){switch(a){case 0:return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.OK;
case 1:return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.PENDING;
case 2:return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.INSUFFICIENT_FUNDS_ERROR;
case 3:return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.PARTNER_ERROR;
case 4:return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.MAX_LIMIT_REACHED;
case 5:return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.AMOUNT_TOO_HIGH;
case 6:return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.UNSPECIFIED_ERROR;
case 7:return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.SESSION_NOT_OPEN
}return -1
};
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.toString=function(a){switch(a){case 0:return"OK";
case 1:return"PENDING";
case 2:return"INSUFFICIENT_FUNDS_ERROR";
case 3:return"PARTNER_ERROR";
case 4:return"MAX_LIMIT_REACHED";
case 5:return"AMOUNT_TOO_HIGH";
case 6:return"UNSPECIFIED_ERROR";
case 7:return"SESSION_NOT_OPEN"
}return"INVALID_ENUM_VALUE"
};com.cubeia.games.poker.io.protocol.CardToDeal=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.CardToDeal.CLASSID
};
this.player={};
this.card={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.player);
a.writeArray(this.card.save());
return a
};
this.load=function(a){this.player=a.readInt();
this.card=new com.cubeia.games.poker.io.protocol.GameCard();
this.card.load(a)
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.CardToDeal";
a.details={};
a.details.player=this.player;
a.details.card=this.card.getNormalizedObject();
return a
}
};
com.cubeia.games.poker.io.protocol.CardToDeal.CLASSID=7;com.cubeia.games.poker.io.protocol.DealPrivateCards=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.DealPrivateCards.CLASSID
};
this.cards=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.cards.length);
var b;
for(b=0;
b<this.cards.length;
b++){a.writeArray(this.cards[b].save())
}return a
};
this.load=function(a){var b;
var d=a.readInt();
var c;
this.cards=[];
for(b=0;
b<d;
b++){c=new com.cubeia.games.poker.io.protocol.CardToDeal();
c.load(a);
this.cards.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.DealPrivateCards";
a.details={};
a.details.cards=[];
for(b=0;
b<this.cards.length;
b++){a.details.cards.push(this.cards[b].getNormalizedObject())
}return a
}
};
com.cubeia.games.poker.io.protocol.DealPrivateCards.CLASSID=13;com.cubeia.games.poker.io.protocol.DealPublicCards=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.DealPublicCards.CLASSID
};
this.cards=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.cards.length);
var b;
for(b=0;
b<this.cards.length;
b++){a.writeArray(this.cards[b].save())
}return a
};
this.load=function(a){var b;
var d=a.readInt();
var c;
this.cards=[];
for(b=0;
b<d;
b++){c=new com.cubeia.games.poker.io.protocol.GameCard();
c.load(a);
this.cards.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.DealPublicCards";
a.details={};
a.details.cards=[];
for(b=0;
b<this.cards.length;
b++){a.details.cards.push(this.cards[b].getNormalizedObject())
}return a
}
};
com.cubeia.games.poker.io.protocol.DealPublicCards.CLASSID=12;com.cubeia.games.poker.io.protocol.DealerButton=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.DealerButton.CLASSID
};
this.seat={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeByte(this.seat);
return a
};
this.load=function(a){this.seat=a.readByte()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.DealerButton";
a.details={};
a.details.seat=this.seat;
return a
}
};
com.cubeia.games.poker.io.protocol.DealerButton.CLASSID=11;com.cubeia.games.poker.io.protocol.DeckInfo=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.DeckInfo.CLASSID
};
this.size={};
this.rankLow={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.size);
a.writeUnsignedByte(this.rankLow);
return a
};
this.load=function(a){this.size=a.readInt();
this.rankLow=com.cubeia.games.poker.io.protocol.RankEnum.makeRankEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.DeckInfo";
a.details={};
a.details.size=this.size;
a.details.rankLow=com.cubeia.games.poker.io.protocol.RankEnum.toString(this.rankLow);
return a
}
};
com.cubeia.games.poker.io.protocol.DeckInfo.CLASSID=35;com.cubeia.games.poker.io.protocol.ErrorCodeEnum=function(){};
com.cubeia.games.poker.io.protocol.ErrorCodeEnum.UNSPECIFIED_ERROR=0;
com.cubeia.games.poker.io.protocol.ErrorCodeEnum.TABLE_CLOSING=1;
com.cubeia.games.poker.io.protocol.ErrorCodeEnum.TABLE_CLOSING_FORCED=2;
com.cubeia.games.poker.io.protocol.ErrorCodeEnum.CLOSED_SESSION_DUE_TO_FATAL_ERROR=3;
com.cubeia.games.poker.io.protocol.ErrorCodeEnum.makeErrorCodeEnum=function(a){switch(a){case 0:return com.cubeia.games.poker.io.protocol.ErrorCodeEnum.UNSPECIFIED_ERROR;
case 1:return com.cubeia.games.poker.io.protocol.ErrorCodeEnum.TABLE_CLOSING;
case 2:return com.cubeia.games.poker.io.protocol.ErrorCodeEnum.TABLE_CLOSING_FORCED;
case 3:return com.cubeia.games.poker.io.protocol.ErrorCodeEnum.CLOSED_SESSION_DUE_TO_FATAL_ERROR
}return -1
};
com.cubeia.games.poker.io.protocol.ErrorCodeEnum.toString=function(a){switch(a){case 0:return"UNSPECIFIED_ERROR";
case 1:return"TABLE_CLOSING";
case 2:return"TABLE_CLOSING_FORCED";
case 3:return"CLOSED_SESSION_DUE_TO_FATAL_ERROR"
}return"INVALID_ENUM_VALUE"
};com.cubeia.games.poker.io.protocol.ErrorPacket=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.ErrorPacket.CLASSID
};
this.code={};
this.referenceId={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeUnsignedByte(this.code);
a.writeString(this.referenceId);
return a
};
this.load=function(a){this.code=com.cubeia.games.poker.io.protocol.ErrorCodeEnum.makeErrorCodeEnum(a.readUnsignedByte());
this.referenceId=a.readString()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.ErrorPacket";
a.details={};
a.details.code=com.cubeia.games.poker.io.protocol.ErrorCodeEnum.toString(this.code);
a.details.referenceId=this.referenceId;
return a
}
};
com.cubeia.games.poker.io.protocol.ErrorPacket.CLASSID=2;com.cubeia.games.poker.io.protocol.ExposePrivateCards=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.ExposePrivateCards.CLASSID
};
this.cards=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.cards.length);
var b;
for(b=0;
b<this.cards.length;
b++){a.writeArray(this.cards[b].save())
}return a
};
this.load=function(a){var b;
var d=a.readInt();
var c;
this.cards=[];
for(b=0;
b<d;
b++){c=new com.cubeia.games.poker.io.protocol.CardToDeal();
c.load(a);
this.cards.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.ExposePrivateCards";
a.details={};
a.details.cards=[];
for(b=0;
b<this.cards.length;
b++){a.details.cards.push(this.cards[b].getNormalizedObject())
}return a
}
};
com.cubeia.games.poker.io.protocol.ExposePrivateCards.CLASSID=14;com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket.CLASSID
};
this.externalTableReference={};
this.externalTableSessionReference={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeString(this.externalTableReference);
a.writeString(this.externalTableSessionReference);
return a
};
this.load=function(a){this.externalTableReference=a.readString();
this.externalTableSessionReference=a.readString()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket";
a.details={};
a.details.externalTableReference=this.externalTableReference;
a.details.externalTableSessionReference=this.externalTableSessionReference;
return a
}
};
com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket.CLASSID=36;com.cubeia.games.poker.io.protocol.FuturePlayerAction=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.FuturePlayerAction.CLASSID
};
this.action={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeUnsignedByte(this.action);
return a
};
this.load=function(a){this.action=com.cubeia.games.poker.io.protocol.ActionTypeEnum.makeActionTypeEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.FuturePlayerAction";
a.details={};
a.details.action=com.cubeia.games.poker.io.protocol.ActionTypeEnum.toString(this.action);
return a
}
};
com.cubeia.games.poker.io.protocol.FuturePlayerAction.CLASSID=3;com.cubeia.games.poker.io.protocol.GameCard=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.GameCard.CLASSID
};
this.cardId={};
this.suit={};
this.rank={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.cardId);
a.writeUnsignedByte(this.suit);
a.writeUnsignedByte(this.rank);
return a
};
this.load=function(a){this.cardId=a.readInt();
this.suit=com.cubeia.games.poker.io.protocol.SuitEnum.makeSuitEnum(a.readUnsignedByte());
this.rank=com.cubeia.games.poker.io.protocol.RankEnum.makeRankEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.GameCard";
a.details={};
a.details.cardId=this.cardId;
a.details.suit=com.cubeia.games.poker.io.protocol.SuitEnum.toString(this.suit);
a.details.rank=com.cubeia.games.poker.io.protocol.RankEnum.toString(this.rank);
return a
}
};
com.cubeia.games.poker.io.protocol.GameCard.CLASSID=4;com.cubeia.games.poker.io.protocol.HandCanceled=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.HandCanceled.CLASSID
};
this.save=function(){return[]
};
this.load=function(a){};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.HandCanceled";
a.details={};
return a
}
};
com.cubeia.games.poker.io.protocol.HandCanceled.CLASSID=16;com.cubeia.games.poker.io.protocol.HandEnd=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.HandEnd.CLASSID
};
this.playerIdRevealOrder=[];
this.hands=[];
this.potTransfers={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.playerIdRevealOrder.length);
var b;
for(b=0;
b<this.playerIdRevealOrder.length;
b++){a.writeInt(this.playerIdRevealOrder[b])
}a.writeInt(this.hands.length);
for(b=0;
b<this.hands.length;
b++){a.writeArray(this.hands[b].save())
}a.writeArray(this.potTransfers.save());
return a
};
this.load=function(a){var c;
var b=a.readInt();
this.playerIdRevealOrder=[];
for(c=0;
c<b;
c++){this.playerIdRevealOrder.push(a.readInt())
}var d=a.readInt();
var e;
this.hands=[];
for(c=0;
c<d;
c++){e=new com.cubeia.games.poker.io.protocol.BestHand();
e.load(a);
this.hands.push(e)
}this.potTransfers=new com.cubeia.games.poker.io.protocol.PotTransfers();
this.potTransfers.load(a)
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.HandEnd";
a.details={};
a.details.playerIdRevealOrder=[];
for(b=0;
b<this.playerIdRevealOrder.length;
b++){a.details.playerIdRevealOrder.push(this.playerIdRevealOrder[b].getNormalizedObject())
}a.details.hands=[];
for(b=0;
b<this.hands.length;
b++){a.details.hands.push(this.hands[b].getNormalizedObject())
}a.details.potTransfers=this.potTransfers.getNormalizedObject();
return a
}
};
com.cubeia.games.poker.io.protocol.HandEnd.CLASSID=15;com.cubeia.games.poker.io.protocol.HandPhase5cardEnum=function(){};
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.BETTING=0;
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.THIRD_STREET=1;
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.FOURTH_STREET=2;
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.FIFTH_STREET=3;
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.makeHandPhase5cardEnum=function(a){switch(a){case 0:return com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.BETTING;
case 1:return com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.THIRD_STREET;
case 2:return com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.FOURTH_STREET;
case 3:return com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.FIFTH_STREET
}return -1
};
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.toString=function(a){switch(a){case 0:return"BETTING";
case 1:return"THIRD_STREET";
case 2:return"FOURTH_STREET";
case 3:return"FIFTH_STREET"
}return"INVALID_ENUM_VALUE"
};com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum=function(){};
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.PREFLOP=0;
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.FLOP=1;
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.TURN=2;
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.RIVER=3;
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.makeHandPhaseHoldemEnum=function(a){switch(a){case 0:return com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.PREFLOP;
case 1:return com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.FLOP;
case 2:return com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.TURN;
case 3:return com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.RIVER
}return -1
};
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.toString=function(a){switch(a){case 0:return"PREFLOP";
case 1:return"FLOP";
case 2:return"TURN";
case 3:return"RIVER"
}return"INVALID_ENUM_VALUE"
};com.cubeia.games.poker.io.protocol.HandTypeEnum=function(){};
com.cubeia.games.poker.io.protocol.HandTypeEnum.UNKNOWN=0;
com.cubeia.games.poker.io.protocol.HandTypeEnum.HIGH_CARD=1;
com.cubeia.games.poker.io.protocol.HandTypeEnum.PAIR=2;
com.cubeia.games.poker.io.protocol.HandTypeEnum.TWO_PAIR=3;
com.cubeia.games.poker.io.protocol.HandTypeEnum.THREE_OF_A_KIND=4;
com.cubeia.games.poker.io.protocol.HandTypeEnum.STRAIGHT=5;
com.cubeia.games.poker.io.protocol.HandTypeEnum.FLUSH=6;
com.cubeia.games.poker.io.protocol.HandTypeEnum.FULL_HOUSE=7;
com.cubeia.games.poker.io.protocol.HandTypeEnum.FOUR_OF_A_KIND=8;
com.cubeia.games.poker.io.protocol.HandTypeEnum.STRAIGHT_FLUSH=9;
com.cubeia.games.poker.io.protocol.HandTypeEnum.ROYAL_STRAIGHT_FLUSH=10;
com.cubeia.games.poker.io.protocol.HandTypeEnum.makeHandTypeEnum=function(a){switch(a){case 0:return com.cubeia.games.poker.io.protocol.HandTypeEnum.UNKNOWN;
case 1:return com.cubeia.games.poker.io.protocol.HandTypeEnum.HIGH_CARD;
case 2:return com.cubeia.games.poker.io.protocol.HandTypeEnum.PAIR;
case 3:return com.cubeia.games.poker.io.protocol.HandTypeEnum.TWO_PAIR;
case 4:return com.cubeia.games.poker.io.protocol.HandTypeEnum.THREE_OF_A_KIND;
case 5:return com.cubeia.games.poker.io.protocol.HandTypeEnum.STRAIGHT;
case 6:return com.cubeia.games.poker.io.protocol.HandTypeEnum.FLUSH;
case 7:return com.cubeia.games.poker.io.protocol.HandTypeEnum.FULL_HOUSE;
case 8:return com.cubeia.games.poker.io.protocol.HandTypeEnum.FOUR_OF_A_KIND;
case 9:return com.cubeia.games.poker.io.protocol.HandTypeEnum.STRAIGHT_FLUSH;
case 10:return com.cubeia.games.poker.io.protocol.HandTypeEnum.ROYAL_STRAIGHT_FLUSH
}return -1
};
com.cubeia.games.poker.io.protocol.HandTypeEnum.toString=function(a){switch(a){case 0:return"UNKNOWN";
case 1:return"HIGH_CARD";
case 2:return"PAIR";
case 3:return"TWO_PAIR";
case 4:return"THREE_OF_A_KIND";
case 5:return"STRAIGHT";
case 6:return"FLUSH";
case 7:return"FULL_HOUSE";
case 8:return"FOUR_OF_A_KIND";
case 9:return"STRAIGHT_FLUSH";
case 10:return"ROYAL_STRAIGHT_FLUSH"
}return"INVALID_ENUM_VALUE"
};com.cubeia.games.poker.io.protocol.InformFutureAllowedActions=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.InformFutureAllowedActions.CLASSID
};
this.actions=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.actions.length);
var b;
for(b=0;
b<this.actions.length;
b++){a.writeArray(this.actions[b].save())
}return a
};
this.load=function(a){var c;
var d=a.readInt();
var b;
this.actions=[];
for(c=0;
c<d;
c++){b=new com.cubeia.games.poker.io.protocol.FuturePlayerAction();
b.load(a);
this.actions.push(b)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.InformFutureAllowedActions";
a.details={};
a.details.actions=[];
for(b=0;
b<this.actions.length;
b++){a.details.actions.push(this.actions[b].getNormalizedObject())
}return a
}
};
com.cubeia.games.poker.io.protocol.InformFutureAllowedActions.CLASSID=9;com.cubeia.games.poker.io.protocol.PerformAction=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PerformAction.CLASSID
};
this.seq={};
this.player={};
this.action={};
this.betAmount={};
this.raiseAmount={};
this.stackAmount={};
this.timeout={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.seq);
a.writeInt(this.player);
a.writeArray(this.action.save());
a.writeInt(this.betAmount);
a.writeInt(this.raiseAmount);
a.writeInt(this.stackAmount);
a.writeBoolean(this.timeout);
return a
};
this.load=function(a){this.seq=a.readInt();
this.player=a.readInt();
this.action=new com.cubeia.games.poker.io.protocol.PlayerAction();
this.action.load(a);
this.betAmount=a.readInt();
this.raiseAmount=a.readInt();
this.stackAmount=a.readInt();
this.timeout=a.readBoolean()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PerformAction";
a.details={};
a.details.seq=this.seq;
a.details.player=this.player;
a.details.action=this.action.getNormalizedObject();
a.details.betAmount=this.betAmount;
a.details.raiseAmount=this.raiseAmount;
a.details.stackAmount=this.stackAmount;
a.details.timeout=this.timeout;
return a
}
};
com.cubeia.games.poker.io.protocol.PerformAction.CLASSID=19;com.cubeia.games.poker.io.protocol.PingPacket=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PingPacket.CLASSID
};
this.identifier={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.identifier);
return a
};
this.load=function(a){this.identifier=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PingPacket";
a.details={};
a.details.identifier=this.identifier;
return a
}
};
com.cubeia.games.poker.io.protocol.PingPacket.CLASSID=39;com.cubeia.games.poker.io.protocol.PlayerAction=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PlayerAction.CLASSID
};
this.type={};
this.minAmount={};
this.maxAmount={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeUnsignedByte(this.type);
a.writeInt(this.minAmount);
a.writeInt(this.maxAmount);
return a
};
this.load=function(a){this.type=com.cubeia.games.poker.io.protocol.ActionTypeEnum.makeActionTypeEnum(a.readUnsignedByte());
this.minAmount=a.readInt();
this.maxAmount=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PlayerAction";
a.details={};
a.details.type=com.cubeia.games.poker.io.protocol.ActionTypeEnum.toString(this.type);
a.details.minAmount=this.minAmount;
a.details.maxAmount=this.maxAmount;
return a
}
};
com.cubeia.games.poker.io.protocol.PlayerAction.CLASSID=1;com.cubeia.games.poker.io.protocol.PlayerBalance=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PlayerBalance.CLASSID
};
this.balance={};
this.pendingBalance={};
this.player={};
this.playersContributionToPot={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.balance);
a.writeInt(this.pendingBalance);
a.writeInt(this.player);
a.writeInt(this.playersContributionToPot);
return a
};
this.load=function(a){this.balance=a.readInt();
this.pendingBalance=a.readInt();
this.player=a.readInt();
this.playersContributionToPot=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PlayerBalance";
a.details={};
a.details.balance=this.balance;
a.details.pendingBalance=this.pendingBalance;
a.details.player=this.player;
a.details.playersContributionToPot=this.playersContributionToPot;
return a
}
};
com.cubeia.games.poker.io.protocol.PlayerBalance.CLASSID=21;com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket.CLASSID
};
this.playerId={};
this.timebank={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.playerId);
a.writeInt(this.timebank);
return a
};
this.load=function(a){this.playerId=a.readInt();
this.timebank=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket";
a.details={};
a.details.playerId=this.playerId;
a.details.timebank=this.timebank;
return a
}
};
com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket.CLASSID=37;com.cubeia.games.poker.io.protocol.PlayerHandStartStatus=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PlayerHandStartStatus.CLASSID
};
this.player={};
this.status={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.player);
a.writeUnsignedByte(this.status);
return a
};
this.load=function(a){this.player=a.readInt();
this.status=com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.makePlayerTableStatusEnum(a.readUnsignedByte())
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PlayerHandStartStatus";
a.details={};
a.details.player=this.player;
a.details.status=com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.toString(this.status);
return a
}
};
com.cubeia.games.poker.io.protocol.PlayerHandStartStatus.CLASSID=32;com.cubeia.games.poker.io.protocol.PlayerPokerStatus=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PlayerPokerStatus.CLASSID
};
this.player={};
this.status={};
this.inCurrentHand={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.player);
a.writeUnsignedByte(this.status);
a.writeBoolean(this.inCurrentHand);
return a
};
this.load=function(a){this.player=a.readInt();
this.status=com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.makePlayerTableStatusEnum(a.readUnsignedByte());
this.inCurrentHand=a.readBoolean()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PlayerPokerStatus";
a.details={};
a.details.player=this.player;
a.details.status=com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.toString(this.status);
a.details.inCurrentHand=this.inCurrentHand;
return a
}
};
com.cubeia.games.poker.io.protocol.PlayerPokerStatus.CLASSID=31;com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket.CLASSID
};
this.playerId={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.playerId);
return a
};
this.load=function(a){this.playerId=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket";
a.details={};
a.details.playerId=this.playerId;
return a
}
};
com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket.CLASSID=38;com.cubeia.games.poker.io.protocol.PlayerSitinRequest=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PlayerSitinRequest.CLASSID
};
this.player={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.player);
return a
};
this.load=function(a){this.player=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PlayerSitinRequest";
a.details={};
a.details.player=this.player;
return a
}
};
com.cubeia.games.poker.io.protocol.PlayerSitinRequest.CLASSID=33;com.cubeia.games.poker.io.protocol.PlayerSitoutRequest=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PlayerSitoutRequest.CLASSID
};
this.player={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.player);
return a
};
this.load=function(a){this.player=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PlayerSitoutRequest";
a.details={};
a.details.player=this.player;
return a
}
};
com.cubeia.games.poker.io.protocol.PlayerSitoutRequest.CLASSID=34;com.cubeia.games.poker.io.protocol.PlayerState=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PlayerState.CLASSID
};
this.player={};
this.cards=[];
this.balance={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.player);
a.writeInt(this.cards.length);
var b;
for(b=0;
b<this.cards.length;
b++){a.writeArray(this.cards[b].save())
}a.writeInt(this.balance);
return a
};
this.load=function(a){this.player=a.readInt();
var b;
var d=a.readInt();
var c;
this.cards=[];
for(b=0;
b<d;
b++){c=new com.cubeia.games.poker.io.protocol.GameCard();
c.load(a);
this.cards.push(c)
}this.balance=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PlayerState";
a.details={};
a.details.player=this.player;
a.details.cards=[];
for(b=0;
b<this.cards.length;
b++){a.details.cards.push(this.cards[b].getNormalizedObject())
}a.details.balance=this.balance;
return a
}
};
com.cubeia.games.poker.io.protocol.PlayerState.CLASSID=6;com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum=function(){};
com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.SITIN=0;
com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.SITOUT=1;
com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.makePlayerTableStatusEnum=function(a){switch(a){case 0:return com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.SITIN;
case 1:return com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.SITOUT
}return -1
};
com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.toString=function(a){switch(a){case 0:return"SITIN";
case 1:return"SITOUT"
}return"INVALID_ENUM_VALUE"
};com.cubeia.games.poker.io.protocol.PongPacket=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PongPacket.CLASSID
};
this.identifier={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.identifier);
return a
};
this.load=function(a){this.identifier=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PongPacket";
a.details={};
a.details.identifier=this.identifier;
return a
}
};
com.cubeia.games.poker.io.protocol.PongPacket.CLASSID=40;com.cubeia.games.poker.io.protocol.Pot=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.Pot.CLASSID
};
this.id={};
this.type={};
this.amount={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeByte(this.id);
a.writeUnsignedByte(this.type);
a.writeInt(this.amount);
return a
};
this.load=function(a){this.id=a.readByte();
this.type=com.cubeia.games.poker.io.protocol.PotTypeEnum.makePotTypeEnum(a.readUnsignedByte());
this.amount=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.Pot";
a.details={};
a.details.id=this.id;
a.details.type=com.cubeia.games.poker.io.protocol.PotTypeEnum.toString(this.type);
a.details.amount=this.amount;
return a
}
};
com.cubeia.games.poker.io.protocol.Pot.CLASSID=26;com.cubeia.games.poker.io.protocol.PotTransfer=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PotTransfer.CLASSID
};
this.potId={};
this.playerId={};
this.amount={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeByte(this.potId);
a.writeInt(this.playerId);
a.writeInt(this.amount);
return a
};
this.load=function(a){this.potId=a.readByte();
this.playerId=a.readInt();
this.amount=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PotTransfer";
a.details={};
a.details.potId=this.potId;
a.details.playerId=this.playerId;
a.details.amount=this.amount;
return a
}
};
com.cubeia.games.poker.io.protocol.PotTransfer.CLASSID=27;com.cubeia.games.poker.io.protocol.PotTransfers=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.PotTransfers.CLASSID
};
this.fromPlayerToPot={};
this.transfers=[];
this.pots=[];
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeBoolean(this.fromPlayerToPot);
a.writeInt(this.transfers.length);
var b;
for(b=0;
b<this.transfers.length;
b++){a.writeArray(this.transfers[b].save())
}a.writeInt(this.pots.length);
for(b=0;
b<this.pots.length;
b++){a.writeArray(this.pots[b].save())
}return a
};
this.load=function(b){this.fromPlayerToPot=b.readBoolean();
var e;
var d=b.readInt();
var a;
this.transfers=[];
for(e=0;
e<d;
e++){a=new com.cubeia.games.poker.io.protocol.PotTransfer();
a.load(b);
this.transfers.push(a)
}var f=b.readInt();
var c;
this.pots=[];
for(e=0;
e<f;
e++){c=new com.cubeia.games.poker.io.protocol.Pot();
c.load(b);
this.pots.push(c)
}};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.PotTransfers";
a.details={};
a.details.fromPlayerToPot=this.fromPlayerToPot;
a.details.transfers=[];
for(b=0;
b<this.transfers.length;
b++){a.details.transfers.push(this.transfers[b].getNormalizedObject())
}a.details.pots=[];
for(b=0;
b<this.pots.length;
b++){a.details.pots.push(this.pots[b].getNormalizedObject())
}return a
}
};
com.cubeia.games.poker.io.protocol.PotTransfers.CLASSID=28;com.cubeia.games.poker.io.protocol.PotTypeEnum=function(){};
com.cubeia.games.poker.io.protocol.PotTypeEnum.MAIN=0;
com.cubeia.games.poker.io.protocol.PotTypeEnum.SIDE=1;
com.cubeia.games.poker.io.protocol.PotTypeEnum.makePotTypeEnum=function(a){switch(a){case 0:return com.cubeia.games.poker.io.protocol.PotTypeEnum.MAIN;
case 1:return com.cubeia.games.poker.io.protocol.PotTypeEnum.SIDE
}return -1
};
com.cubeia.games.poker.io.protocol.PotTypeEnum.toString=function(a){switch(a){case 0:return"MAIN";
case 1:return"SIDE"
}return"INVALID_ENUM_VALUE"
};com.cubeia.games.poker.io.protocol.ProtocolObjectFactory={};
com.cubeia.games.poker.io.protocol.ProtocolObjectFactory.create=function(c,a){var b;
switch(c){case com.cubeia.games.poker.io.protocol.PlayerAction.CLASSID:b=new com.cubeia.games.poker.io.protocol.PlayerAction();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.ErrorPacket.CLASSID:b=new com.cubeia.games.poker.io.protocol.ErrorPacket();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.FuturePlayerAction.CLASSID:b=new com.cubeia.games.poker.io.protocol.FuturePlayerAction();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.GameCard.CLASSID:b=new com.cubeia.games.poker.io.protocol.GameCard();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.BestHand.CLASSID:b=new com.cubeia.games.poker.io.protocol.BestHand();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PlayerState.CLASSID:b=new com.cubeia.games.poker.io.protocol.PlayerState();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.CardToDeal.CLASSID:b=new com.cubeia.games.poker.io.protocol.CardToDeal();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.RequestAction.CLASSID:b=new com.cubeia.games.poker.io.protocol.RequestAction();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.InformFutureAllowedActions.CLASSID:b=new com.cubeia.games.poker.io.protocol.InformFutureAllowedActions();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.StartNewHand.CLASSID:b=new com.cubeia.games.poker.io.protocol.StartNewHand();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.DealerButton.CLASSID:b=new com.cubeia.games.poker.io.protocol.DealerButton();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.DealPublicCards.CLASSID:b=new com.cubeia.games.poker.io.protocol.DealPublicCards();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.DealPrivateCards.CLASSID:b=new com.cubeia.games.poker.io.protocol.DealPrivateCards();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.ExposePrivateCards.CLASSID:b=new com.cubeia.games.poker.io.protocol.ExposePrivateCards();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.HandEnd.CLASSID:b=new com.cubeia.games.poker.io.protocol.HandEnd();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.HandCanceled.CLASSID:b=new com.cubeia.games.poker.io.protocol.HandCanceled();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.StartHandHistory.CLASSID:b=new com.cubeia.games.poker.io.protocol.StartHandHistory();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.StopHandHistory.CLASSID:b=new com.cubeia.games.poker.io.protocol.StopHandHistory();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PerformAction.CLASSID:b=new com.cubeia.games.poker.io.protocol.PerformAction();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.TournamentOut.CLASSID:b=new com.cubeia.games.poker.io.protocol.TournamentOut();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PlayerBalance.CLASSID:b=new com.cubeia.games.poker.io.protocol.PlayerBalance();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.BuyInInfoRequest.CLASSID:b=new com.cubeia.games.poker.io.protocol.BuyInInfoRequest();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.BuyInInfoResponse.CLASSID:b=new com.cubeia.games.poker.io.protocol.BuyInInfoResponse();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.BuyInRequest.CLASSID:b=new com.cubeia.games.poker.io.protocol.BuyInRequest();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.BuyInResponse.CLASSID:b=new com.cubeia.games.poker.io.protocol.BuyInResponse();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.Pot.CLASSID:b=new com.cubeia.games.poker.io.protocol.Pot();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PotTransfer.CLASSID:b=new com.cubeia.games.poker.io.protocol.PotTransfer();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PotTransfers.CLASSID:b=new com.cubeia.games.poker.io.protocol.PotTransfers();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.TakeBackUncalledBet.CLASSID:b=new com.cubeia.games.poker.io.protocol.TakeBackUncalledBet();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.RakeInfo.CLASSID:b=new com.cubeia.games.poker.io.protocol.RakeInfo();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PlayerPokerStatus.CLASSID:b=new com.cubeia.games.poker.io.protocol.PlayerPokerStatus();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PlayerHandStartStatus.CLASSID:b=new com.cubeia.games.poker.io.protocol.PlayerHandStartStatus();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PlayerSitinRequest.CLASSID:b=new com.cubeia.games.poker.io.protocol.PlayerSitinRequest();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PlayerSitoutRequest.CLASSID:b=new com.cubeia.games.poker.io.protocol.PlayerSitoutRequest();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.DeckInfo.CLASSID:b=new com.cubeia.games.poker.io.protocol.DeckInfo();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket.CLASSID:b=new com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket.CLASSID:b=new com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket.CLASSID:b=new com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PingPacket.CLASSID:b=new com.cubeia.games.poker.io.protocol.PingPacket();
b.load(a);
return b;
case com.cubeia.games.poker.io.protocol.PongPacket.CLASSID:b=new com.cubeia.games.poker.io.protocol.PongPacket();
b.load(a);
return b
}return null
};com.cubeia.games.poker.io.protocol.RakeInfo=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.RakeInfo.CLASSID
};
this.totalPot={};
this.totalRake={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.totalPot);
a.writeInt(this.totalRake);
return a
};
this.load=function(a){this.totalPot=a.readInt();
this.totalRake=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.RakeInfo";
a.details={};
a.details.totalPot=this.totalPot;
a.details.totalRake=this.totalRake;
return a
}
};
com.cubeia.games.poker.io.protocol.RakeInfo.CLASSID=30;com.cubeia.games.poker.io.protocol.RankEnum=function(){};
com.cubeia.games.poker.io.protocol.RankEnum.TWO=0;
com.cubeia.games.poker.io.protocol.RankEnum.THREE=1;
com.cubeia.games.poker.io.protocol.RankEnum.FOUR=2;
com.cubeia.games.poker.io.protocol.RankEnum.FIVE=3;
com.cubeia.games.poker.io.protocol.RankEnum.SIX=4;
com.cubeia.games.poker.io.protocol.RankEnum.SEVEN=5;
com.cubeia.games.poker.io.protocol.RankEnum.EIGHT=6;
com.cubeia.games.poker.io.protocol.RankEnum.NINE=7;
com.cubeia.games.poker.io.protocol.RankEnum.TEN=8;
com.cubeia.games.poker.io.protocol.RankEnum.JACK=9;
com.cubeia.games.poker.io.protocol.RankEnum.QUEEN=10;
com.cubeia.games.poker.io.protocol.RankEnum.KING=11;
com.cubeia.games.poker.io.protocol.RankEnum.ACE=12;
com.cubeia.games.poker.io.protocol.RankEnum.HIDDEN=13;
com.cubeia.games.poker.io.protocol.RankEnum.makeRankEnum=function(a){switch(a){case 0:return com.cubeia.games.poker.io.protocol.RankEnum.TWO;
case 1:return com.cubeia.games.poker.io.protocol.RankEnum.THREE;
case 2:return com.cubeia.games.poker.io.protocol.RankEnum.FOUR;
case 3:return com.cubeia.games.poker.io.protocol.RankEnum.FIVE;
case 4:return com.cubeia.games.poker.io.protocol.RankEnum.SIX;
case 5:return com.cubeia.games.poker.io.protocol.RankEnum.SEVEN;
case 6:return com.cubeia.games.poker.io.protocol.RankEnum.EIGHT;
case 7:return com.cubeia.games.poker.io.protocol.RankEnum.NINE;
case 8:return com.cubeia.games.poker.io.protocol.RankEnum.TEN;
case 9:return com.cubeia.games.poker.io.protocol.RankEnum.JACK;
case 10:return com.cubeia.games.poker.io.protocol.RankEnum.QUEEN;
case 11:return com.cubeia.games.poker.io.protocol.RankEnum.KING;
case 12:return com.cubeia.games.poker.io.protocol.RankEnum.ACE;
case 13:return com.cubeia.games.poker.io.protocol.RankEnum.HIDDEN
}return -1
};
com.cubeia.games.poker.io.protocol.RankEnum.toString=function(a){switch(a){case 0:return"TWO";
case 1:return"THREE";
case 2:return"FOUR";
case 3:return"FIVE";
case 4:return"SIX";
case 5:return"SEVEN";
case 6:return"EIGHT";
case 7:return"NINE";
case 8:return"TEN";
case 9:return"JACK";
case 10:return"QUEEN";
case 11:return"KING";
case 12:return"ACE";
case 13:return"HIDDEN"
}return"INVALID_ENUM_VALUE"
};com.cubeia.games.poker.io.protocol.RequestAction=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.RequestAction.CLASSID
};
this.currentPotSize={};
this.seq={};
this.player={};
this.allowedActions=[];
this.timeToAct={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.currentPotSize);
a.writeInt(this.seq);
a.writeInt(this.player);
a.writeInt(this.allowedActions.length);
var b;
for(b=0;
b<this.allowedActions.length;
b++){a.writeArray(this.allowedActions[b].save())
}a.writeInt(this.timeToAct);
return a
};
this.load=function(b){this.currentPotSize=b.readInt();
this.seq=b.readInt();
this.player=b.readInt();
var c;
var a=b.readInt();
var d;
this.allowedActions=[];
for(c=0;
c<a;
c++){d=new com.cubeia.games.poker.io.protocol.PlayerAction();
d.load(b);
this.allowedActions.push(d)
}this.timeToAct=b.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.RequestAction";
a.details={};
a.details.currentPotSize=this.currentPotSize;
a.details.seq=this.seq;
a.details.player=this.player;
a.details.allowedActions=[];
for(b=0;
b<this.allowedActions.length;
b++){a.details.allowedActions.push(this.allowedActions[b].getNormalizedObject())
}a.details.timeToAct=this.timeToAct;
return a
}
};
com.cubeia.games.poker.io.protocol.RequestAction.CLASSID=8;com.cubeia.games.poker.io.protocol.StartHandHistory=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.StartHandHistory.CLASSID
};
this.save=function(){return[]
};
this.load=function(a){};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.StartHandHistory";
a.details={};
return a
}
};
com.cubeia.games.poker.io.protocol.StartHandHistory.CLASSID=17;com.cubeia.games.poker.io.protocol.StartNewHand=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.StartNewHand.CLASSID
};
this.dealerSeatId={};
this.handId={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.dealerSeatId);
a.writeString(this.handId);
return a
};
this.load=function(a){this.dealerSeatId=a.readInt();
this.handId=a.readString()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.StartNewHand";
a.details={};
a.details.dealerSeatId=this.dealerSeatId;
a.details.handId=this.handId;
return a
}
};
com.cubeia.games.poker.io.protocol.StartNewHand.CLASSID=10;com.cubeia.games.poker.io.protocol.StopHandHistory=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.StopHandHistory.CLASSID
};
this.save=function(){return[]
};
this.load=function(a){};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.StopHandHistory";
a.details={};
return a
}
};
com.cubeia.games.poker.io.protocol.StopHandHistory.CLASSID=18;com.cubeia.games.poker.io.protocol.SuitEnum=function(){};
com.cubeia.games.poker.io.protocol.SuitEnum.CLUBS=0;
com.cubeia.games.poker.io.protocol.SuitEnum.DIAMONDS=1;
com.cubeia.games.poker.io.protocol.SuitEnum.HEARTS=2;
com.cubeia.games.poker.io.protocol.SuitEnum.SPADES=3;
com.cubeia.games.poker.io.protocol.SuitEnum.HIDDEN=4;
com.cubeia.games.poker.io.protocol.SuitEnum.makeSuitEnum=function(a){switch(a){case 0:return com.cubeia.games.poker.io.protocol.SuitEnum.CLUBS;
case 1:return com.cubeia.games.poker.io.protocol.SuitEnum.DIAMONDS;
case 2:return com.cubeia.games.poker.io.protocol.SuitEnum.HEARTS;
case 3:return com.cubeia.games.poker.io.protocol.SuitEnum.SPADES;
case 4:return com.cubeia.games.poker.io.protocol.SuitEnum.HIDDEN
}return -1
};
com.cubeia.games.poker.io.protocol.SuitEnum.toString=function(a){switch(a){case 0:return"CLUBS";
case 1:return"DIAMONDS";
case 2:return"HEARTS";
case 3:return"SPADES";
case 4:return"HIDDEN"
}return"INVALID_ENUM_VALUE"
};com.cubeia.games.poker.io.protocol.TakeBackUncalledBet=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.TakeBackUncalledBet.CLASSID
};
this.playerId={};
this.amount={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.playerId);
a.writeInt(this.amount);
return a
};
this.load=function(a){this.playerId=a.readInt();
this.amount=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.TakeBackUncalledBet";
a.details={};
a.details.playerId=this.playerId;
a.details.amount=this.amount;
return a
}
};
com.cubeia.games.poker.io.protocol.TakeBackUncalledBet.CLASSID=29;com.cubeia.games.poker.io.protocol.TournamentOut=function(){this.classId=function(){return com.cubeia.games.poker.io.protocol.TournamentOut.CLASSID
};
this.player={};
this.position={};
this.save=function(){var a=new FIREBASE.ByteArray();
a.writeInt(this.player);
a.writeInt(this.position);
return a
};
this.load=function(a){this.player=a.readInt();
this.position=a.readInt()
};
this.getNormalizedObject=function(){var a={};
var b;
a.summary="com.cubeia.games.poker.io.protocol.TournamentOut";
a.details={};
a.details.player=this.player;
a.details.position=this.position;
return a
}
};
com.cubeia.games.poker.io.protocol.TournamentOut.CLASSID=20;