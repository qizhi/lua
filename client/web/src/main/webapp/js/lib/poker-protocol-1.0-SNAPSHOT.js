// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

var com = com || {};
com.cubeia = com.cubeia || {};
com.cubeia.games = com.cubeia.games || {};
com.cubeia.games.poker = com.cubeia.games.poker || {};
com.cubeia.games.poker.io = com.cubeia.games.poker.io || {};
com.cubeia.games.poker.io.protocol = com.cubeia.games.poker.io.protocol || {};


com.cubeia.games.poker.io.protocol.AchievementNotificationPacket = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.AchievementNotificationPacket.CLASSID
    };
    this.playerId = {};
    this.message = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.playerId);
        a.writeString(this.message);
        return a
    };
    this.load = function (a) {
        this.playerId = a.readInt();
        this.message = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.AchievementNotificationPacket";
        a.details = {};
        a.details.playerId = this.playerId;
        a.details.message = this.message;
        return a
    }
};
com.cubeia.games.poker.io.protocol.AchievementNotificationPacket.CLASSID = 75;
com.cubeia.games.poker.io.protocol.ActionTypeEnum = function () {
};
com.cubeia.games.poker.io.protocol.ActionTypeEnum.SMALL_BLIND = 0;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND = 1;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.CALL = 2;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.CHECK = 3;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.BET = 4;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.RAISE = 5;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.FOLD = 6;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.DECLINE_ENTRY_BET = 7;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.ANTE = 8;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND_PLUS_DEAD_SMALL_BLIND = 9;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.DEAD_SMALL_BLIND = 10;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.ENTRY_BET = 11;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.WAIT_FOR_BIG_BLIND = 12;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.DISCARD = 13;
com.cubeia.games.poker.io.protocol.ActionTypeEnum.makeActionTypeEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.SMALL_BLIND;
        case 1:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND;
        case 2:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.CALL;
        case 3:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.CHECK;
        case 4:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.BET;
        case 5:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.RAISE;
        case 6:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.FOLD;
        case 7:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.DECLINE_ENTRY_BET;
        case 8:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.ANTE;
        case 9:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND_PLUS_DEAD_SMALL_BLIND;
        case 10:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.DEAD_SMALL_BLIND;
        case 11:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.ENTRY_BET;
        case 12:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.WAIT_FOR_BIG_BLIND;
        case 13:
            return com.cubeia.games.poker.io.protocol.ActionTypeEnum.DISCARD
    }
    return -1
};
com.cubeia.games.poker.io.protocol.ActionTypeEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"SMALL_BLIND";
        case 1:
            return"BIG_BLIND";
        case 2:
            return"CALL";
        case 3:
            return"CHECK";
        case 4:
            return"BET";
        case 5:
            return"RAISE";
        case 6:
            return"FOLD";
        case 7:
            return"DECLINE_ENTRY_BET";
        case 8:
            return"ANTE";
        case 9:
            return"BIG_BLIND_PLUS_DEAD_SMALL_BLIND";
        case 10:
            return"DEAD_SMALL_BLIND";
        case 11:
            return"ENTRY_BET";
        case 12:
            return"WAIT_FOR_BIG_BLIND";
        case 13:
            return"DISCARD"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.AddOnOffer = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.AddOnOffer.CLASSID
    };
    this.cost = {};
    this.chips = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.cost);
        a.writeString(this.chips);
        return a
    };
    this.load = function (a) {
        this.cost = a.readString();
        this.chips = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.AddOnOffer";
        a.details = {};
        a.details.cost = this.cost;
        a.details.chips = this.chips;
        return a
    }
};
com.cubeia.games.poker.io.protocol.AddOnOffer.CLASSID = 66;
com.cubeia.games.poker.io.protocol.AddOnPeriodClosed = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.AddOnPeriodClosed.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.AddOnPeriodClosed";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.AddOnPeriodClosed.CLASSID = 70;
com.cubeia.games.poker.io.protocol.BestHand = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.BestHand.CLASSID
    };
    this.player = {};
    this.handType = {};
    this.cards = [];
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.player);
        a.writeUnsignedByte(this.handType);
        a.writeInt(this.cards.length);
        var b;
        for (b = 0; b < this.cards.length; b++) {
            a.writeArray(this.cards[b].save())
        }
        return a
    };
    this.load = function (a) {
        this.player = a.readInt();
        this.handType = com.cubeia.games.poker.io.protocol.HandTypeEnum.makeHandTypeEnum(a.readUnsignedByte());
        var b;
        var d = a.readInt();
        var c;
        this.cards = [];
        for (b = 0; b < d; b++) {
            c = new com.cubeia.games.poker.io.protocol.GameCard();
            c.load(a);
            this.cards.push(c)
        }
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.BestHand";
        a.details = {};
        a.details.player = this.player;
        a.details.handType = com.cubeia.games.poker.io.protocol.HandTypeEnum.toString(this.handType);
        a.details.cards = [];
        for (b = 0; b < this.cards.length; b++) {
            a.details.cards.push(this.cards[b].getNormalizedObject())
        }
        return a
    }
};
com.cubeia.games.poker.io.protocol.BestHand.CLASSID = 6;
com.cubeia.games.poker.io.protocol.BetStrategyEnum = function () {
};
com.cubeia.games.poker.io.protocol.BetStrategyEnum.FIXED_LIMIT = 0;
com.cubeia.games.poker.io.protocol.BetStrategyEnum.NO_LIMIT = 1;
com.cubeia.games.poker.io.protocol.BetStrategyEnum.POT_LIMIT = 2;
com.cubeia.games.poker.io.protocol.BetStrategyEnum.makeBetStrategyEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.BetStrategyEnum.FIXED_LIMIT;
        case 1:
            return com.cubeia.games.poker.io.protocol.BetStrategyEnum.NO_LIMIT;
        case 2:
            return com.cubeia.games.poker.io.protocol.BetStrategyEnum.POT_LIMIT
    }
    return -1
};
com.cubeia.games.poker.io.protocol.BetStrategyEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"FIXED_LIMIT";
        case 1:
            return"NO_LIMIT";
        case 2:
            return"POT_LIMIT"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.BlindsAreUpdated = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.BlindsAreUpdated.CLASSID
    };
    this.level = {};
    this.secondsToNextLevel = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeArray(this.level.save());
        a.writeInt(this.secondsToNextLevel);
        return a
    };
    this.load = function (a) {
        this.level = new com.cubeia.games.poker.io.protocol.BlindsLevel();
        this.level.load(a);
        this.secondsToNextLevel = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.BlindsAreUpdated";
        a.details = {};
        a.details.level = this.level.getNormalizedObject();
        a.details.secondsToNextLevel = this.secondsToNextLevel;
        return a
    }
};
com.cubeia.games.poker.io.protocol.BlindsAreUpdated.CLASSID = 44;
com.cubeia.games.poker.io.protocol.BlindsLevel = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.BlindsLevel.CLASSID
    };
    this.smallBlind = {};
    this.bigBlind = {};
    this.ante = {};
    this.isBreak = {};
    this.durationInMinutes = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.smallBlind);
        a.writeString(this.bigBlind);
        a.writeString(this.ante);
        a.writeBoolean(this.isBreak);
        a.writeInt(this.durationInMinutes);
        return a
    };
    this.load = function (a) {
        this.smallBlind = a.readString();
        this.bigBlind = a.readString();
        this.ante = a.readString();
        this.isBreak = a.readBoolean();
        this.durationInMinutes = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.BlindsLevel";
        a.details = {};
        a.details.smallBlind = this.smallBlind;
        a.details.bigBlind = this.bigBlind;
        a.details.ante = this.ante;
        a.details.isBreak = this.isBreak;
        a.details.durationInMinutes = this.durationInMinutes;
        return a
    }
};
com.cubeia.games.poker.io.protocol.BlindsLevel.CLASSID = 45;
com.cubeia.games.poker.io.protocol.BlindsStructure = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.BlindsStructure.CLASSID
    };
    this.blindsLevels = [];
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.blindsLevels.length);
        var b;
        for (b = 0; b < this.blindsLevels.length; b++) {
            a.writeArray(this.blindsLevels[b].save())
        }
        return a
    };
    this.load = function (a) {
        var c;
        var d = a.readInt();
        var b;
        this.blindsLevels = [];
        for (c = 0; c < d; c++) {
            b = new com.cubeia.games.poker.io.protocol.BlindsLevel();
            b.load(a);
            this.blindsLevels.push(b)
        }
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.BlindsStructure";
        a.details = {};
        a.details.blindsLevels = [];
        for (b = 0; b < this.blindsLevels.length; b++) {
            a.details.blindsLevels.push(this.blindsLevels[b].getNormalizedObject())
        }
        return a
    }
};
com.cubeia.games.poker.io.protocol.BlindsStructure.CLASSID = 50;
com.cubeia.games.poker.io.protocol.BuyInInfoRequest = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.BuyInInfoRequest.CLASSID
    };
    this.dummy = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeByte(this.dummy);
        return a
    };
    this.load = function (a) {
        this.dummy = a.readByte()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.BuyInInfoRequest";
        a.details = {};
        a.details.dummy = this.dummy;
        return a
    }
};
com.cubeia.games.poker.io.protocol.BuyInInfoRequest.CLASSID = 23;
com.cubeia.games.poker.io.protocol.BuyInInfoResponse = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.BuyInInfoResponse.CLASSID
    };
    this.maxAmount = {};
    this.minAmount = {};
    this.balanceInWallet = {};
    this.balanceOnTable = {};
    this.mandatoryBuyin = {};
    this.resultCode = {};
    this.currencyCode = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.maxAmount);
        a.writeString(this.minAmount);
        a.writeString(this.balanceInWallet);
        a.writeString(this.balanceOnTable);
        a.writeBoolean(this.mandatoryBuyin);
        a.writeUnsignedByte(this.resultCode);
        a.writeString(this.currencyCode);
        return a
    };
    this.load = function (a) {
        this.maxAmount = a.readString();
        this.minAmount = a.readString();
        this.balanceInWallet = a.readString();
        this.balanceOnTable = a.readString();
        this.mandatoryBuyin = a.readBoolean();
        this.resultCode = com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.makeBuyInInfoResultCodeEnum(a.readUnsignedByte());
        this.currencyCode = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.BuyInInfoResponse";
        a.details = {};
        a.details.maxAmount = this.maxAmount;
        a.details.minAmount = this.minAmount;
        a.details.balanceInWallet = this.balanceInWallet;
        a.details.balanceOnTable = this.balanceOnTable;
        a.details.mandatoryBuyin = this.mandatoryBuyin;
        a.details.resultCode = com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.toString(this.resultCode);
        a.details.currencyCode = this.currencyCode;
        return a
    }
};
com.cubeia.games.poker.io.protocol.BuyInInfoResponse.CLASSID = 24;
com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum = function () {
};
com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.OK = 0;
com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.MAX_LIMIT_REACHED = 1;
com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.UNSPECIFIED_ERROR = 2;
com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.makeBuyInInfoResultCodeEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.OK;
        case 1:
            return com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.MAX_LIMIT_REACHED;
        case 2:
            return com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.UNSPECIFIED_ERROR
    }
    return -1
};
com.cubeia.games.poker.io.protocol.BuyInInfoResultCodeEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"OK";
        case 1:
            return"MAX_LIMIT_REACHED";
        case 2:
            return"UNSPECIFIED_ERROR"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.BuyInRequest = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.BuyInRequest.CLASSID
    };
    this.amount = {};
    this.sitInIfSuccessful = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.amount);
        a.writeBoolean(this.sitInIfSuccessful);
        return a
    };
    this.load = function (a) {
        this.amount = a.readString();
        this.sitInIfSuccessful = a.readBoolean()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.BuyInRequest";
        a.details = {};
        a.details.amount = this.amount;
        a.details.sitInIfSuccessful = this.sitInIfSuccessful;
        return a
    }
};
com.cubeia.games.poker.io.protocol.BuyInRequest.CLASSID = 25;
com.cubeia.games.poker.io.protocol.BuyInResponse = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.BuyInResponse.CLASSID
    };
    this.balance = {};
    this.pendingBalance = {};
    this.amountBroughtIn = {};
    this.resultCode = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.balance);
        a.writeString(this.pendingBalance);
        a.writeString(this.amountBroughtIn);
        a.writeUnsignedByte(this.resultCode);
        return a
    };
    this.load = function (a) {
        this.balance = a.readString();
        this.pendingBalance = a.readString();
        this.amountBroughtIn = a.readString();
        this.resultCode = com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.makeBuyInResultCodeEnum(a.readUnsignedByte())
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.BuyInResponse";
        a.details = {};
        a.details.balance = this.balance;
        a.details.pendingBalance = this.pendingBalance;
        a.details.amountBroughtIn = this.amountBroughtIn;
        a.details.resultCode = com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.toString(this.resultCode);
        return a
    }
};
com.cubeia.games.poker.io.protocol.BuyInResponse.CLASSID = 26;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum = function () {
};
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.OK = 0;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.PENDING = 1;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.INSUFFICIENT_FUNDS_ERROR = 2;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.PARTNER_ERROR = 3;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.MAX_LIMIT_REACHED = 4;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.AMOUNT_TOO_HIGH = 5;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.UNSPECIFIED_ERROR = 6;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.SESSION_NOT_OPEN = 7;
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.makeBuyInResultCodeEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.OK;
        case 1:
            return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.PENDING;
        case 2:
            return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.INSUFFICIENT_FUNDS_ERROR;
        case 3:
            return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.PARTNER_ERROR;
        case 4:
            return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.MAX_LIMIT_REACHED;
        case 5:
            return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.AMOUNT_TOO_HIGH;
        case 6:
            return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.UNSPECIFIED_ERROR;
        case 7:
            return com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.SESSION_NOT_OPEN
    }
    return -1
};
com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"OK";
        case 1:
            return"PENDING";
        case 2:
            return"INSUFFICIENT_FUNDS_ERROR";
        case 3:
            return"PARTNER_ERROR";
        case 4:
            return"MAX_LIMIT_REACHED";
        case 5:
            return"AMOUNT_TOO_HIGH";
        case 6:
            return"UNSPECIFIED_ERROR";
        case 7:
            return"SESSION_NOT_OPEN"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.CardToDeal = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.CardToDeal.CLASSID
    };
    this.player = {};
    this.card = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.player);
        a.writeArray(this.card.save());
        return a
    };
    this.load = function (a) {
        this.player = a.readInt();
        this.card = new com.cubeia.games.poker.io.protocol.GameCard();
        this.card.load(a)
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.CardToDeal";
        a.details = {};
        a.details.player = this.player;
        a.details.card = this.card.getNormalizedObject();
        return a
    }
};
com.cubeia.games.poker.io.protocol.CardToDeal.CLASSID = 8;
com.cubeia.games.poker.io.protocol.ChipStatistics = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.ChipStatistics.CLASSID
    };
    this.minStack = {};
    this.maxStack = {};
    this.averageStack = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.minStack);
        a.writeString(this.maxStack);
        a.writeString(this.averageStack);
        return a
    };
    this.load = function (a) {
        this.minStack = a.readString();
        this.maxStack = a.readString();
        this.averageStack = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.ChipStatistics";
        a.details = {};
        a.details.minStack = this.minStack;
        a.details.maxStack = this.maxStack;
        a.details.averageStack = this.averageStack;
        return a
    }
};
com.cubeia.games.poker.io.protocol.ChipStatistics.CLASSID = 55;
com.cubeia.games.poker.io.protocol.Currency = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.Currency.CLASSID
    };
    this.code = {};
    this.fractionalDigits = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.code);
        a.writeInt(this.fractionalDigits);
        return a
    };
    this.load = function (a) {
        this.code = a.readString();
        this.fractionalDigits = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.Currency";
        a.details = {};
        a.details.code = this.code;
        a.details.fractionalDigits = this.fractionalDigits;
        return a
    }
};
com.cubeia.games.poker.io.protocol.Currency.CLASSID = 74;
com.cubeia.games.poker.io.protocol.DealPrivateCards = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.DealPrivateCards.CLASSID
    };
    this.cards = [];
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.cards.length);
        var b;
        for (b = 0; b < this.cards.length; b++) {
            a.writeArray(this.cards[b].save())
        }
        return a
    };
    this.load = function (a) {
        var b;
        var d = a.readInt();
        var c;
        this.cards = [];
        for (b = 0; b < d; b++) {
            c = new com.cubeia.games.poker.io.protocol.CardToDeal();
            c.load(a);
            this.cards.push(c)
        }
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.DealPrivateCards";
        a.details = {};
        a.details.cards = [];
        for (b = 0; b < this.cards.length; b++) {
            a.details.cards.push(this.cards[b].getNormalizedObject())
        }
        return a
    }
};
com.cubeia.games.poker.io.protocol.DealPrivateCards.CLASSID = 14;
com.cubeia.games.poker.io.protocol.DealPublicCards = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.DealPublicCards.CLASSID
    };
    this.cards = [];
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.cards.length);
        var b;
        for (b = 0; b < this.cards.length; b++) {
            a.writeArray(this.cards[b].save())
        }
        return a
    };
    this.load = function (a) {
        var b;
        var d = a.readInt();
        var c;
        this.cards = [];
        for (b = 0; b < d; b++) {
            c = new com.cubeia.games.poker.io.protocol.GameCard();
            c.load(a);
            this.cards.push(c)
        }
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.DealPublicCards";
        a.details = {};
        a.details.cards = [];
        for (b = 0; b < this.cards.length; b++) {
            a.details.cards.push(this.cards[b].getNormalizedObject())
        }
        return a
    }
};
com.cubeia.games.poker.io.protocol.DealPublicCards.CLASSID = 13;
com.cubeia.games.poker.io.protocol.DealerButton = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.DealerButton.CLASSID
    };
    this.seat = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeByte(this.seat);
        return a
    };
    this.load = function (a) {
        this.seat = a.readByte()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.DealerButton";
        a.details = {};
        a.details.seat = this.seat;
        return a
    }
};
com.cubeia.games.poker.io.protocol.DealerButton.CLASSID = 12;
com.cubeia.games.poker.io.protocol.DeckInfo = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.DeckInfo.CLASSID
    };
    this.size = {};
    this.rankLow = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.size);
        a.writeUnsignedByte(this.rankLow);
        return a
    };
    this.load = function (a) {
        this.size = a.readInt();
        this.rankLow = com.cubeia.games.poker.io.protocol.RankEnum.makeRankEnum(a.readUnsignedByte())
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.DeckInfo";
        a.details = {};
        a.details.size = this.size;
        a.details.rankLow = com.cubeia.games.poker.io.protocol.RankEnum.toString(this.rankLow);
        return a
    }
};
com.cubeia.games.poker.io.protocol.DeckInfo.CLASSID = 36;
com.cubeia.games.poker.io.protocol.ErrorCodeEnum = function () {
};
com.cubeia.games.poker.io.protocol.ErrorCodeEnum.UNSPECIFIED_ERROR = 0;
com.cubeia.games.poker.io.protocol.ErrorCodeEnum.TABLE_CLOSING = 1;
com.cubeia.games.poker.io.protocol.ErrorCodeEnum.TABLE_CLOSING_FORCED = 2;
com.cubeia.games.poker.io.protocol.ErrorCodeEnum.CLOSED_SESSION_DUE_TO_FATAL_ERROR = 3;
com.cubeia.games.poker.io.protocol.ErrorCodeEnum.makeErrorCodeEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.ErrorCodeEnum.UNSPECIFIED_ERROR;
        case 1:
            return com.cubeia.games.poker.io.protocol.ErrorCodeEnum.TABLE_CLOSING;
        case 2:
            return com.cubeia.games.poker.io.protocol.ErrorCodeEnum.TABLE_CLOSING_FORCED;
        case 3:
            return com.cubeia.games.poker.io.protocol.ErrorCodeEnum.CLOSED_SESSION_DUE_TO_FATAL_ERROR
    }
    return -1
};
com.cubeia.games.poker.io.protocol.ErrorCodeEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"UNSPECIFIED_ERROR";
        case 1:
            return"TABLE_CLOSING";
        case 2:
            return"TABLE_CLOSING_FORCED";
        case 3:
            return"CLOSED_SESSION_DUE_TO_FATAL_ERROR"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.ErrorPacket = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.ErrorPacket.CLASSID
    };
    this.code = {};
    this.referenceId = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeUnsignedByte(this.code);
        a.writeString(this.referenceId);
        return a
    };
    this.load = function (a) {
        this.code = com.cubeia.games.poker.io.protocol.ErrorCodeEnum.makeErrorCodeEnum(a.readUnsignedByte());
        this.referenceId = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.ErrorPacket";
        a.details = {};
        a.details.code = com.cubeia.games.poker.io.protocol.ErrorCodeEnum.toString(this.code);
        a.details.referenceId = this.referenceId;
        return a
    }
};
com.cubeia.games.poker.io.protocol.ErrorPacket.CLASSID = 2;
com.cubeia.games.poker.io.protocol.ExposePrivateCards = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.ExposePrivateCards.CLASSID
    };
    this.cards = [];
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.cards.length);
        var b;
        for (b = 0; b < this.cards.length; b++) {
            a.writeArray(this.cards[b].save())
        }
        return a
    };
    this.load = function (a) {
        var b;
        var d = a.readInt();
        var c;
        this.cards = [];
        for (b = 0; b < d; b++) {
            c = new com.cubeia.games.poker.io.protocol.CardToDeal();
            c.load(a);
            this.cards.push(c)
        }
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.ExposePrivateCards";
        a.details = {};
        a.details.cards = [];
        for (b = 0; b < this.cards.length; b++) {
            a.details.cards.push(this.cards[b].getNormalizedObject())
        }
        return a
    }
};
com.cubeia.games.poker.io.protocol.ExposePrivateCards.CLASSID = 15;
com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket.CLASSID
    };
    this.externalTableReference = {};
    this.externalTableSessionReference = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.externalTableReference);
        a.writeString(this.externalTableSessionReference);
        return a
    };
    this.load = function (a) {
        this.externalTableReference = a.readString();
        this.externalTableSessionReference = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket";
        a.details = {};
        a.details.externalTableReference = this.externalTableReference;
        a.details.externalTableSessionReference = this.externalTableSessionReference;
        return a
    }
};
com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket.CLASSID = 37;
com.cubeia.games.poker.io.protocol.FuturePlayerAction = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.FuturePlayerAction.CLASSID
    };
    this.action = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeUnsignedByte(this.action);
        return a
    };
    this.load = function (a) {
        this.action = com.cubeia.games.poker.io.protocol.ActionTypeEnum.makeActionTypeEnum(a.readUnsignedByte())
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.FuturePlayerAction";
        a.details = {};
        a.details.action = com.cubeia.games.poker.io.protocol.ActionTypeEnum.toString(this.action);
        return a
    }
};
com.cubeia.games.poker.io.protocol.FuturePlayerAction.CLASSID = 3;
com.cubeia.games.poker.io.protocol.GameCard = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.GameCard.CLASSID
    };
    this.cardId = {};
    this.suit = {};
    this.rank = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.cardId);
        a.writeUnsignedByte(this.suit);
        a.writeUnsignedByte(this.rank);
        return a
    };
    this.load = function (a) {
        this.cardId = a.readInt();
        this.suit = com.cubeia.games.poker.io.protocol.SuitEnum.makeSuitEnum(a.readUnsignedByte());
        this.rank = com.cubeia.games.poker.io.protocol.RankEnum.makeRankEnum(a.readUnsignedByte())
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.GameCard";
        a.details = {};
        a.details.cardId = this.cardId;
        a.details.suit = com.cubeia.games.poker.io.protocol.SuitEnum.toString(this.suit);
        a.details.rank = com.cubeia.games.poker.io.protocol.RankEnum.toString(this.rank);
        return a
    }
};
com.cubeia.games.poker.io.protocol.GameCard.CLASSID = 5;
com.cubeia.games.poker.io.protocol.GameState = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.GameState.CLASSID
    };
    this.name = {};
    this.capacity = {};
    this.tournamentId = {};
    this.handInfo = {};
    this.currentLevel = {};
    this.secondsToNextLevel = {};
    this.betStrategy = {};
    this.currency = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.name);
        a.writeInt(this.capacity);
        a.writeInt(this.tournamentId);
        a.writeArray(this.handInfo.save());
        a.writeArray(this.currentLevel.save());
        a.writeInt(this.secondsToNextLevel);
        a.writeUnsignedByte(this.betStrategy);
        a.writeArray(this.currency.save());
        return a
    };
    this.load = function (a) {
        this.name = a.readString();
        this.capacity = a.readInt();
        this.tournamentId = a.readInt();
        this.handInfo = new com.cubeia.games.poker.io.protocol.HandStartInfo();
        this.handInfo.load(a);
        this.currentLevel = new com.cubeia.games.poker.io.protocol.BlindsLevel();
        this.currentLevel.load(a);
        this.secondsToNextLevel = a.readInt();
        this.betStrategy = com.cubeia.games.poker.io.protocol.BetStrategyEnum.makeBetStrategyEnum(a.readUnsignedByte());
        this.currency = new com.cubeia.games.poker.io.protocol.Currency();
        this.currency.load(a)
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.GameState";
        a.details = {};
        a.details.name = this.name;
        a.details.capacity = this.capacity;
        a.details.tournamentId = this.tournamentId;
        a.details.handInfo = this.handInfo.getNormalizedObject();
        a.details.currentLevel = this.currentLevel.getNormalizedObject();
        a.details.secondsToNextLevel = this.secondsToNextLevel;
        a.details.betStrategy = com.cubeia.games.poker.io.protocol.BetStrategyEnum.toString(this.betStrategy);
        a.details.currency = this.currency.getNormalizedObject();
        return a
    }
};
com.cubeia.games.poker.io.protocol.GameState.CLASSID = 4;
com.cubeia.games.poker.io.protocol.HandCanceled = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.HandCanceled.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.HandCanceled";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.HandCanceled.CLASSID = 17;
com.cubeia.games.poker.io.protocol.HandEnd = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.HandEnd.CLASSID
    };
    this.playerIdRevealOrder = [];
    this.hands = [];
    this.potTransfers = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.playerIdRevealOrder.length);
        var b;
        for (b = 0; b < this.playerIdRevealOrder.length; b++) {
            a.writeInt(this.playerIdRevealOrder[b])
        }
        a.writeInt(this.hands.length);
        for (b = 0; b < this.hands.length; b++) {
            a.writeArray(this.hands[b].save())
        }
        a.writeArray(this.potTransfers.save());
        return a
    };
    this.load = function (a) {
        var c;
        var b = a.readInt();
        this.playerIdRevealOrder = [];
        for (c = 0; c < b; c++) {
            this.playerIdRevealOrder.push(a.readInt())
        }
        var d = a.readInt();
        var e;
        this.hands = [];
        for (c = 0; c < d; c++) {
            e = new com.cubeia.games.poker.io.protocol.BestHand();
            e.load(a);
            this.hands.push(e)
        }
        this.potTransfers = new com.cubeia.games.poker.io.protocol.PotTransfers();
        this.potTransfers.load(a)
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.HandEnd";
        a.details = {};
        a.details.playerIdRevealOrder = [];
        for (b = 0; b < this.playerIdRevealOrder.length; b++) {
            a.details.playerIdRevealOrder.push(this.playerIdRevealOrder[b].getNormalizedObject())
        }
        a.details.hands = [];
        for (b = 0; b < this.hands.length; b++) {
            a.details.hands.push(this.hands[b].getNormalizedObject())
        }
        a.details.potTransfers = this.potTransfers.getNormalizedObject();
        return a
    }
};
com.cubeia.games.poker.io.protocol.HandEnd.CLASSID = 16;
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum = function () {
};
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.BETTING = 0;
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.THIRD_STREET = 1;
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.FOURTH_STREET = 2;
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.FIFTH_STREET = 3;
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.makeHandPhase5cardEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.BETTING;
        case 1:
            return com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.THIRD_STREET;
        case 2:
            return com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.FOURTH_STREET;
        case 3:
            return com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.FIFTH_STREET
    }
    return -1
};
com.cubeia.games.poker.io.protocol.HandPhase5cardEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"BETTING";
        case 1:
            return"THIRD_STREET";
        case 2:
            return"FOURTH_STREET";
        case 3:
            return"FIFTH_STREET"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum = function () {
};
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.PREFLOP = 0;
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.FLOP = 1;
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.TURN = 2;
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.RIVER = 3;
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.makeHandPhaseHoldemEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.PREFLOP;
        case 1:
            return com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.FLOP;
        case 2:
            return com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.TURN;
        case 3:
            return com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.RIVER
    }
    return -1
};
com.cubeia.games.poker.io.protocol.HandPhaseHoldemEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"PREFLOP";
        case 1:
            return"FLOP";
        case 2:
            return"TURN";
        case 3:
            return"RIVER"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.HandStartInfo = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.HandStartInfo.CLASSID
    };
    this.handId = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.handId);
        return a
    };
    this.load = function (a) {
        this.handId = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.HandStartInfo";
        a.details = {};
        a.details.handId = this.handId;
        return a
    }
};
com.cubeia.games.poker.io.protocol.HandStartInfo.CLASSID = 11;
com.cubeia.games.poker.io.protocol.HandTypeEnum = function () {
};
com.cubeia.games.poker.io.protocol.HandTypeEnum.UNKNOWN = 0;
com.cubeia.games.poker.io.protocol.HandTypeEnum.HIGH_CARD = 1;
com.cubeia.games.poker.io.protocol.HandTypeEnum.PAIR = 2;
com.cubeia.games.poker.io.protocol.HandTypeEnum.TWO_PAIR = 3;
com.cubeia.games.poker.io.protocol.HandTypeEnum.THREE_OF_A_KIND = 4;
com.cubeia.games.poker.io.protocol.HandTypeEnum.STRAIGHT = 5;
com.cubeia.games.poker.io.protocol.HandTypeEnum.FLUSH = 6;
com.cubeia.games.poker.io.protocol.HandTypeEnum.FULL_HOUSE = 7;
com.cubeia.games.poker.io.protocol.HandTypeEnum.FOUR_OF_A_KIND = 8;
com.cubeia.games.poker.io.protocol.HandTypeEnum.STRAIGHT_FLUSH = 9;
com.cubeia.games.poker.io.protocol.HandTypeEnum.ROYAL_STRAIGHT_FLUSH = 10;
com.cubeia.games.poker.io.protocol.HandTypeEnum.makeHandTypeEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.HandTypeEnum.UNKNOWN;
        case 1:
            return com.cubeia.games.poker.io.protocol.HandTypeEnum.HIGH_CARD;
        case 2:
            return com.cubeia.games.poker.io.protocol.HandTypeEnum.PAIR;
        case 3:
            return com.cubeia.games.poker.io.protocol.HandTypeEnum.TWO_PAIR;
        case 4:
            return com.cubeia.games.poker.io.protocol.HandTypeEnum.THREE_OF_A_KIND;
        case 5:
            return com.cubeia.games.poker.io.protocol.HandTypeEnum.STRAIGHT;
        case 6:
            return com.cubeia.games.poker.io.protocol.HandTypeEnum.FLUSH;
        case 7:
            return com.cubeia.games.poker.io.protocol.HandTypeEnum.FULL_HOUSE;
        case 8:
            return com.cubeia.games.poker.io.protocol.HandTypeEnum.FOUR_OF_A_KIND;
        case 9:
            return com.cubeia.games.poker.io.protocol.HandTypeEnum.STRAIGHT_FLUSH;
        case 10:
            return com.cubeia.games.poker.io.protocol.HandTypeEnum.ROYAL_STRAIGHT_FLUSH
    }
    return -1
};
com.cubeia.games.poker.io.protocol.HandTypeEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"UNKNOWN";
        case 1:
            return"HIGH_CARD";
        case 2:
            return"PAIR";
        case 3:
            return"TWO_PAIR";
        case 4:
            return"THREE_OF_A_KIND";
        case 5:
            return"STRAIGHT";
        case 6:
            return"FLUSH";
        case 7:
            return"FULL_HOUSE";
        case 8:
            return"FOUR_OF_A_KIND";
        case 9:
            return"STRAIGHT_FLUSH";
        case 10:
            return"ROYAL_STRAIGHT_FLUSH"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.InformFutureAllowedActions = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.InformFutureAllowedActions.CLASSID
    };
    this.actions = [];
    this.callAmount = {};
    this.minBetAmount = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.actions.length);
        var b;
        for (b = 0; b < this.actions.length; b++) {
            a.writeArray(this.actions[b].save())
        }
        a.writeString(this.callAmount);
        a.writeString(this.minBetAmount);
        return a
    };
    this.load = function (a) {
        var c;
        var d = a.readInt();
        var b;
        this.actions = [];
        for (c = 0; c < d; c++) {
            b = new com.cubeia.games.poker.io.protocol.FuturePlayerAction();
            b.load(a);
            this.actions.push(b)
        }
        this.callAmount = a.readString();
        this.minBetAmount = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.InformFutureAllowedActions";
        a.details = {};
        a.details.actions = [];
        for (b = 0; b < this.actions.length; b++) {
            a.details.actions.push(this.actions[b].getNormalizedObject())
        }
        a.details.callAmount = this.callAmount;
        a.details.minBetAmount = this.minBetAmount;
        return a
    }
};
com.cubeia.games.poker.io.protocol.InformFutureAllowedActions.CLASSID = 10;
com.cubeia.games.poker.io.protocol.LevelInfo = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.LevelInfo.CLASSID
    };
    this.currentLevel = {};
    this.timeToNextLevel = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.currentLevel);
        a.writeInt(this.timeToNextLevel);
        return a
    };
    this.load = function (a) {
        this.currentLevel = a.readInt();
        this.timeToNextLevel = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.LevelInfo";
        a.details = {};
        a.details.currentLevel = this.currentLevel;
        a.details.timeToNextLevel = this.timeToNextLevel;
        return a
    }
};
com.cubeia.games.poker.io.protocol.LevelInfo.CLASSID = 56;
com.cubeia.games.poker.io.protocol.Payout = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.Payout.CLASSID
    };
    this.position = {};
    this.payoutAmount = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.position);
        a.writeString(this.payoutAmount);
        return a
    };
    this.load = function (a) {
        this.position = a.readInt();
        this.payoutAmount = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.Payout";
        a.details = {};
        a.details.position = this.position;
        a.details.payoutAmount = this.payoutAmount;
        return a
    }
};
com.cubeia.games.poker.io.protocol.Payout.CLASSID = 53;
com.cubeia.games.poker.io.protocol.PayoutInfo = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PayoutInfo.CLASSID
    };
    this.prizePool = {};
    this.payouts = [];
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.prizePool);
        a.writeInt(this.payouts.length);
        var b;
        for (b = 0; b < this.payouts.length; b++) {
            a.writeArray(this.payouts[b].save())
        }
        return a
    };
    this.load = function (b) {
        this.prizePool = b.readString();
        var c;
        var a = b.readInt();
        var d;
        this.payouts = [];
        for (c = 0; c < a; c++) {
            d = new com.cubeia.games.poker.io.protocol.Payout();
            d.load(b);
            this.payouts.push(d)
        }
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PayoutInfo";
        a.details = {};
        a.details.prizePool = this.prizePool;
        a.details.payouts = [];
        for (b = 0; b < this.payouts.length; b++) {
            a.details.payouts.push(this.payouts[b].getNormalizedObject())
        }
        return a
    }
};
com.cubeia.games.poker.io.protocol.PayoutInfo.CLASSID = 52;
com.cubeia.games.poker.io.protocol.PerformAction = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PerformAction.CLASSID
    };
    this.seq = {};
    this.player = {};
    this.action = {};
    this.betAmount = {};
    this.raiseAmount = {};
    this.stackAmount = {};
    this.timeout = {};
    this.cardsToDiscard = [];
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.seq);
        a.writeInt(this.player);
        a.writeArray(this.action.save());
        a.writeString(this.betAmount);
        a.writeString(this.raiseAmount);
        a.writeString(this.stackAmount);
        a.writeBoolean(this.timeout);
        a.writeInt(this.cardsToDiscard.length);
        var b;
        for (b = 0; b < this.cardsToDiscard.length; b++) {
            a.writeInt(this.cardsToDiscard[b])
        }
        return a
    };
    this.load = function (a) {
        this.seq = a.readInt();
        this.player = a.readInt();
        this.action = new com.cubeia.games.poker.io.protocol.PlayerAction();
        this.action.load(a);
        this.betAmount = a.readString();
        this.raiseAmount = a.readString();
        this.stackAmount = a.readString();
        this.timeout = a.readBoolean();
        var c;
        var b = a.readInt();
        this.cardsToDiscard = [];
        for (c = 0; c < b; c++) {
            this.cardsToDiscard.push(a.readInt())
        }
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PerformAction";
        a.details = {};
        a.details.seq = this.seq;
        a.details.player = this.player;
        a.details.action = this.action.getNormalizedObject();
        a.details.betAmount = this.betAmount;
        a.details.raiseAmount = this.raiseAmount;
        a.details.stackAmount = this.stackAmount;
        a.details.timeout = this.timeout;
        a.details.cardsToDiscard = [];
        for (b = 0; b < this.cardsToDiscard.length; b++) {
            a.details.cardsToDiscard.push(this.cardsToDiscard[b].getNormalizedObject())
        }
        return a
    }
};
com.cubeia.games.poker.io.protocol.PerformAction.CLASSID = 20;
com.cubeia.games.poker.io.protocol.PerformAddOn = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PerformAddOn.CLASSID
    };
    this.tableId = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.tableId);
        return a
    };
    this.load = function (a) {
        this.tableId = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PerformAddOn";
        a.details = {};
        a.details.tableId = this.tableId;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PerformAddOn.CLASSID = 67;
com.cubeia.games.poker.io.protocol.PingPacket = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PingPacket.CLASSID
    };
    this.identifier = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.identifier);
        return a
    };
    this.load = function (a) {
        this.identifier = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PingPacket";
        a.details = {};
        a.details.identifier = this.identifier;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PingPacket.CLASSID = 40;
com.cubeia.games.poker.io.protocol.PlayerAction = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PlayerAction.CLASSID
    };
    this.type = {};
    this.minAmount = {};
    this.maxAmount = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeUnsignedByte(this.type);
        a.writeString(this.minAmount);
        a.writeString(this.maxAmount);
        return a
    };
    this.load = function (a) {
        this.type = com.cubeia.games.poker.io.protocol.ActionTypeEnum.makeActionTypeEnum(a.readUnsignedByte());
        this.minAmount = a.readString();
        this.maxAmount = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PlayerAction";
        a.details = {};
        a.details.type = com.cubeia.games.poker.io.protocol.ActionTypeEnum.toString(this.type);
        a.details.minAmount = this.minAmount;
        a.details.maxAmount = this.maxAmount;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PlayerAction.CLASSID = 1;
com.cubeia.games.poker.io.protocol.PlayerBalance = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PlayerBalance.CLASSID
    };
    this.balance = {};
    this.pendingBalance = {};
    this.player = {};
    this.playersContributionToPot = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.balance);
        a.writeString(this.pendingBalance);
        a.writeInt(this.player);
        a.writeString(this.playersContributionToPot);
        return a
    };
    this.load = function (a) {
        this.balance = a.readString();
        this.pendingBalance = a.readString();
        this.player = a.readInt();
        this.playersContributionToPot = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PlayerBalance";
        a.details = {};
        a.details.balance = this.balance;
        a.details.pendingBalance = this.pendingBalance;
        a.details.player = this.player;
        a.details.playersContributionToPot = this.playersContributionToPot;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PlayerBalance.CLASSID = 22;
com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket.CLASSID
    };
    this.playerId = {};
    this.timebank = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.playerId);
        a.writeInt(this.timebank);
        return a
    };
    this.load = function (a) {
        this.playerId = a.readInt();
        this.timebank = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket";
        a.details = {};
        a.details.playerId = this.playerId;
        a.details.timebank = this.timebank;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket.CLASSID = 38;
com.cubeia.games.poker.io.protocol.PlayerHandStartStatus = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PlayerHandStartStatus.CLASSID
    };
    this.player = {};
    this.status = {};
    this.away = {};
    this.sitOutNextHand = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.player);
        a.writeUnsignedByte(this.status);
        a.writeBoolean(this.away);
        a.writeBoolean(this.sitOutNextHand);
        return a
    };
    this.load = function (a) {
        this.player = a.readInt();
        this.status = com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.makePlayerTableStatusEnum(a.readUnsignedByte());
        this.away = a.readBoolean();
        this.sitOutNextHand = a.readBoolean()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PlayerHandStartStatus";
        a.details = {};
        a.details.player = this.player;
        a.details.status = com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.toString(this.status);
        a.details.away = this.away;
        a.details.sitOutNextHand = this.sitOutNextHand;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PlayerHandStartStatus.CLASSID = 33;
com.cubeia.games.poker.io.protocol.PlayerPerformedAddOn = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PlayerPerformedAddOn.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PlayerPerformedAddOn";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.PlayerPerformedAddOn.CLASSID = 69;
com.cubeia.games.poker.io.protocol.PlayerPerformedRebuy = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PlayerPerformedRebuy.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PlayerPerformedRebuy";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.PlayerPerformedRebuy.CLASSID = 68;
com.cubeia.games.poker.io.protocol.PlayerPokerStatus = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PlayerPokerStatus.CLASSID
    };
    this.player = {};
    this.status = {};
    this.inCurrentHand = {};
    this.sitOutNextHand = {};
    this.away = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.player);
        a.writeUnsignedByte(this.status);
        a.writeBoolean(this.inCurrentHand);
        a.writeBoolean(this.sitOutNextHand);
        a.writeBoolean(this.away);
        return a
    };
    this.load = function (a) {
        this.player = a.readInt();
        this.status = com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.makePlayerTableStatusEnum(a.readUnsignedByte());
        this.inCurrentHand = a.readBoolean();
        this.sitOutNextHand = a.readBoolean();
        this.away = a.readBoolean()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PlayerPokerStatus";
        a.details = {};
        a.details.player = this.player;
        a.details.status = com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.toString(this.status);
        a.details.inCurrentHand = this.inCurrentHand;
        a.details.sitOutNextHand = this.sitOutNextHand;
        a.details.away = this.away;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PlayerPokerStatus.CLASSID = 32;
com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket.CLASSID
    };
    this.playerId = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.playerId);
        return a
    };
    this.load = function (a) {
        this.playerId = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket";
        a.details = {};
        a.details.playerId = this.playerId;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket.CLASSID = 39;
com.cubeia.games.poker.io.protocol.PlayerSitinRequest = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PlayerSitinRequest.CLASSID
    };
    this.player = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.player);
        return a
    };
    this.load = function (a) {
        this.player = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PlayerSitinRequest";
        a.details = {};
        a.details.player = this.player;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PlayerSitinRequest.CLASSID = 34;
com.cubeia.games.poker.io.protocol.PlayerSitoutRequest = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PlayerSitoutRequest.CLASSID
    };
    this.player = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.player);
        return a
    };
    this.load = function (a) {
        this.player = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PlayerSitoutRequest";
        a.details = {};
        a.details.player = this.player;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PlayerSitoutRequest.CLASSID = 35;
com.cubeia.games.poker.io.protocol.PlayerState = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PlayerState.CLASSID
    };
    this.player = {};
    this.cards = [];
    this.balance = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.player);
        a.writeInt(this.cards.length);
        var b;
        for (b = 0; b < this.cards.length; b++) {
            a.writeArray(this.cards[b].save())
        }
        a.writeString(this.balance);
        return a
    };
    this.load = function (a) {
        this.player = a.readInt();
        var b;
        var d = a.readInt();
        var c;
        this.cards = [];
        for (b = 0; b < d; b++) {
            c = new com.cubeia.games.poker.io.protocol.GameCard();
            c.load(a);
            this.cards.push(c)
        }
        this.balance = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PlayerState";
        a.details = {};
        a.details.player = this.player;
        a.details.cards = [];
        for (b = 0; b < this.cards.length; b++) {
            a.details.cards.push(this.cards[b].getNormalizedObject())
        }
        a.details.balance = this.balance;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PlayerState.CLASSID = 7;
com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum = function () {
};
com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.SITIN = 0;
com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.SITOUT = 1;
com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.makePlayerTableStatusEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.SITIN;
        case 1:
            return com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.SITOUT
    }
    return -1
};
com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"SITIN";
        case 1:
            return"SITOUT"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.PlayersLeft = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PlayersLeft.CLASSID
    };
    this.remainingPlayers = {};
    this.registeredPlayers = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.remainingPlayers);
        a.writeInt(this.registeredPlayers);
        return a
    };
    this.load = function (a) {
        this.remainingPlayers = a.readInt();
        this.registeredPlayers = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PlayersLeft";
        a.details = {};
        a.details.remainingPlayers = this.remainingPlayers;
        a.details.registeredPlayers = this.registeredPlayers;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PlayersLeft.CLASSID = 57;
com.cubeia.games.poker.io.protocol.PongPacket = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PongPacket.CLASSID
    };
    this.identifier = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.identifier);
        return a
    };
    this.load = function (a) {
        this.identifier = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PongPacket";
        a.details = {};
        a.details.identifier = this.identifier;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PongPacket.CLASSID = 41;
com.cubeia.games.poker.io.protocol.Pot = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.Pot.CLASSID
    };
    this.id = {};
    this.type = {};
    this.amount = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeByte(this.id);
        a.writeUnsignedByte(this.type);
        a.writeString(this.amount);
        return a
    };
    this.load = function (a) {
        this.id = a.readByte();
        this.type = com.cubeia.games.poker.io.protocol.PotTypeEnum.makePotTypeEnum(a.readUnsignedByte());
        this.amount = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.Pot";
        a.details = {};
        a.details.id = this.id;
        a.details.type = com.cubeia.games.poker.io.protocol.PotTypeEnum.toString(this.type);
        a.details.amount = this.amount;
        return a
    }
};
com.cubeia.games.poker.io.protocol.Pot.CLASSID = 27;
com.cubeia.games.poker.io.protocol.PotTransfer = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PotTransfer.CLASSID
    };
    this.potId = {};
    this.playerId = {};
    this.amount = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeByte(this.potId);
        a.writeInt(this.playerId);
        a.writeString(this.amount);
        return a
    };
    this.load = function (a) {
        this.potId = a.readByte();
        this.playerId = a.readInt();
        this.amount = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PotTransfer";
        a.details = {};
        a.details.potId = this.potId;
        a.details.playerId = this.playerId;
        a.details.amount = this.amount;
        return a
    }
};
com.cubeia.games.poker.io.protocol.PotTransfer.CLASSID = 28;
com.cubeia.games.poker.io.protocol.PotTransfers = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.PotTransfers.CLASSID
    };
    this.fromPlayerToPot = {};
    this.transfers = [];
    this.pots = [];
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeBoolean(this.fromPlayerToPot);
        a.writeInt(this.transfers.length);
        var b;
        for (b = 0; b < this.transfers.length; b++) {
            a.writeArray(this.transfers[b].save())
        }
        a.writeInt(this.pots.length);
        for (b = 0; b < this.pots.length; b++) {
            a.writeArray(this.pots[b].save())
        }
        return a
    };
    this.load = function (b) {
        this.fromPlayerToPot = b.readBoolean();
        var e;
        var d = b.readInt();
        var a;
        this.transfers = [];
        for (e = 0; e < d; e++) {
            a = new com.cubeia.games.poker.io.protocol.PotTransfer();
            a.load(b);
            this.transfers.push(a)
        }
        var f = b.readInt();
        var c;
        this.pots = [];
        for (e = 0; e < f; e++) {
            c = new com.cubeia.games.poker.io.protocol.Pot();
            c.load(b);
            this.pots.push(c)
        }
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.PotTransfers";
        a.details = {};
        a.details.fromPlayerToPot = this.fromPlayerToPot;
        a.details.transfers = [];
        for (b = 0; b < this.transfers.length; b++) {
            a.details.transfers.push(this.transfers[b].getNormalizedObject())
        }
        a.details.pots = [];
        for (b = 0; b < this.pots.length; b++) {
            a.details.pots.push(this.pots[b].getNormalizedObject())
        }
        return a
    }
};
com.cubeia.games.poker.io.protocol.PotTransfers.CLASSID = 29;
com.cubeia.games.poker.io.protocol.PotTypeEnum = function () {
};
com.cubeia.games.poker.io.protocol.PotTypeEnum.MAIN = 0;
com.cubeia.games.poker.io.protocol.PotTypeEnum.SIDE = 1;
com.cubeia.games.poker.io.protocol.PotTypeEnum.makePotTypeEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.PotTypeEnum.MAIN;
        case 1:
            return com.cubeia.games.poker.io.protocol.PotTypeEnum.SIDE
    }
    return -1
};
com.cubeia.games.poker.io.protocol.PotTypeEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"MAIN";
        case 1:
            return"SIDE"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.ProtocolObjectFactory = {};
com.cubeia.games.poker.io.protocol.ProtocolObjectFactory.create = function (c, a) {
    var b;
    switch (c) {
        case com.cubeia.games.poker.io.protocol.PlayerAction.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PlayerAction();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.ErrorPacket.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.ErrorPacket();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.FuturePlayerAction.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.FuturePlayerAction();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.GameState.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.GameState();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.GameCard.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.GameCard();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.BestHand.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.BestHand();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PlayerState.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PlayerState();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.CardToDeal.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.CardToDeal();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.RequestAction.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.RequestAction();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.InformFutureAllowedActions.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.InformFutureAllowedActions();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.HandStartInfo.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.HandStartInfo();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.DealerButton.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.DealerButton();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.DealPublicCards.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.DealPublicCards();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.DealPrivateCards.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.DealPrivateCards();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.ExposePrivateCards.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.ExposePrivateCards();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.HandEnd.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.HandEnd();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.HandCanceled.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.HandCanceled();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.StartHandHistory.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.StartHandHistory();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.StopHandHistory.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.StopHandHistory();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PerformAction.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PerformAction();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.TournamentOut.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.TournamentOut();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PlayerBalance.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PlayerBalance();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.BuyInInfoRequest.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.BuyInInfoRequest();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.BuyInInfoResponse.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.BuyInInfoResponse();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.BuyInRequest.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.BuyInRequest();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.BuyInResponse.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.BuyInResponse();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.Pot.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.Pot();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PotTransfer.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PotTransfer();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PotTransfers.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PotTransfers();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.TakeBackUncalledBet.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.TakeBackUncalledBet();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.RakeInfo.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.RakeInfo();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PlayerPokerStatus.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PlayerPokerStatus();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PlayerHandStartStatus.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PlayerHandStartStatus();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PlayerSitinRequest.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PlayerSitinRequest();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PlayerSitoutRequest.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PlayerSitoutRequest();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.DeckInfo.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.DeckInfo();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PingPacket.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PingPacket();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PongPacket.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PongPacket();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.WaitingToStartBreak.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.WaitingToStartBreak();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.WaitingForPlayers.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.WaitingForPlayers();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.BlindsAreUpdated.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.BlindsAreUpdated();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.BlindsLevel.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.BlindsLevel();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.RequestTournamentPlayerList.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.RequestTournamentPlayerList();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.TournamentPlayerList.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.TournamentPlayerList();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.TournamentPlayer.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.TournamentPlayer();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.RequestBlindsStructure.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.RequestBlindsStructure();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.BlindsStructure.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.BlindsStructure();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.RequestPayoutInfo.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.RequestPayoutInfo();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PayoutInfo.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PayoutInfo();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.Payout.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.Payout();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.RequestTournamentStatistics.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.RequestTournamentStatistics();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.ChipStatistics.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.ChipStatistics();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.LevelInfo.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.LevelInfo();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PlayersLeft.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PlayersLeft();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.TournamentStatistics.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.TournamentStatistics();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.TournamentInfo.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.TournamentInfo();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.RequestTournamentLobbyData.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.RequestTournamentLobbyData();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.TournamentLobbyData.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.TournamentLobbyData();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.RequestTournamentTable.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.RequestTournamentTable();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.TournamentTable.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.TournamentTable();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.RebuyOffer.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.RebuyOffer();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.RebuyResponse.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.RebuyResponse();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.AddOnOffer.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.AddOnOffer();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PerformAddOn.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PerformAddOn();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PlayerPerformedRebuy.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PlayerPerformedRebuy();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.PlayerPerformedAddOn.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.PlayerPerformedAddOn();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.AddOnPeriodClosed.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.AddOnPeriodClosed();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.TournamentDestroyed.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.TournamentDestroyed();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.RequestTournamentRegistrationInfo.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.RequestTournamentRegistrationInfo();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.TournamentRegistrationInfo.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.TournamentRegistrationInfo();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.Currency.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.Currency();
            b.load(a);
            return b;
        case com.cubeia.games.poker.io.protocol.AchievementNotificationPacket.CLASSID:
            b = new com.cubeia.games.poker.io.protocol.AchievementNotificationPacket();
            b.load(a);
            return b
    }
    return null
};
com.cubeia.games.poker.io.protocol.RakeInfo = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.RakeInfo.CLASSID
    };
    this.totalPot = {};
    this.totalRake = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.totalPot);
        a.writeString(this.totalRake);
        return a
    };
    this.load = function (a) {
        this.totalPot = a.readString();
        this.totalRake = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.RakeInfo";
        a.details = {};
        a.details.totalPot = this.totalPot;
        a.details.totalRake = this.totalRake;
        return a
    }
};
com.cubeia.games.poker.io.protocol.RakeInfo.CLASSID = 31;
com.cubeia.games.poker.io.protocol.RankEnum = function () {
};
com.cubeia.games.poker.io.protocol.RankEnum.TWO = 0;
com.cubeia.games.poker.io.protocol.RankEnum.THREE = 1;
com.cubeia.games.poker.io.protocol.RankEnum.FOUR = 2;
com.cubeia.games.poker.io.protocol.RankEnum.FIVE = 3;
com.cubeia.games.poker.io.protocol.RankEnum.SIX = 4;
com.cubeia.games.poker.io.protocol.RankEnum.SEVEN = 5;
com.cubeia.games.poker.io.protocol.RankEnum.EIGHT = 6;
com.cubeia.games.poker.io.protocol.RankEnum.NINE = 7;
com.cubeia.games.poker.io.protocol.RankEnum.TEN = 8;
com.cubeia.games.poker.io.protocol.RankEnum.JACK = 9;
com.cubeia.games.poker.io.protocol.RankEnum.QUEEN = 10;
com.cubeia.games.poker.io.protocol.RankEnum.KING = 11;
com.cubeia.games.poker.io.protocol.RankEnum.ACE = 12;
com.cubeia.games.poker.io.protocol.RankEnum.HIDDEN = 13;
com.cubeia.games.poker.io.protocol.RankEnum.makeRankEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.RankEnum.TWO;
        case 1:
            return com.cubeia.games.poker.io.protocol.RankEnum.THREE;
        case 2:
            return com.cubeia.games.poker.io.protocol.RankEnum.FOUR;
        case 3:
            return com.cubeia.games.poker.io.protocol.RankEnum.FIVE;
        case 4:
            return com.cubeia.games.poker.io.protocol.RankEnum.SIX;
        case 5:
            return com.cubeia.games.poker.io.protocol.RankEnum.SEVEN;
        case 6:
            return com.cubeia.games.poker.io.protocol.RankEnum.EIGHT;
        case 7:
            return com.cubeia.games.poker.io.protocol.RankEnum.NINE;
        case 8:
            return com.cubeia.games.poker.io.protocol.RankEnum.TEN;
        case 9:
            return com.cubeia.games.poker.io.protocol.RankEnum.JACK;
        case 10:
            return com.cubeia.games.poker.io.protocol.RankEnum.QUEEN;
        case 11:
            return com.cubeia.games.poker.io.protocol.RankEnum.KING;
        case 12:
            return com.cubeia.games.poker.io.protocol.RankEnum.ACE;
        case 13:
            return com.cubeia.games.poker.io.protocol.RankEnum.HIDDEN
    }
    return -1
};
com.cubeia.games.poker.io.protocol.RankEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"TWO";
        case 1:
            return"THREE";
        case 2:
            return"FOUR";
        case 3:
            return"FIVE";
        case 4:
            return"SIX";
        case 5:
            return"SEVEN";
        case 6:
            return"EIGHT";
        case 7:
            return"NINE";
        case 8:
            return"TEN";
        case 9:
            return"JACK";
        case 10:
            return"QUEEN";
        case 11:
            return"KING";
        case 12:
            return"ACE";
        case 13:
            return"HIDDEN"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.RebuyOffer = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.RebuyOffer.CLASSID
    };
    this.cost = {};
    this.chips = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.cost);
        a.writeString(this.chips);
        return a
    };
    this.load = function (a) {
        this.cost = a.readString();
        this.chips = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.RebuyOffer";
        a.details = {};
        a.details.cost = this.cost;
        a.details.chips = this.chips;
        return a
    }
};
com.cubeia.games.poker.io.protocol.RebuyOffer.CLASSID = 64;
com.cubeia.games.poker.io.protocol.RebuyResponse = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.RebuyResponse.CLASSID
    };
    this.answer = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeBoolean(this.answer);
        return a
    };
    this.load = function (a) {
        this.answer = a.readBoolean()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.RebuyResponse";
        a.details = {};
        a.details.answer = this.answer;
        return a
    }
};
com.cubeia.games.poker.io.protocol.RebuyResponse.CLASSID = 65;
com.cubeia.games.poker.io.protocol.RequestAction = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.RequestAction.CLASSID
    };
    this.currentPotSize = {};
    this.seq = {};
    this.player = {};
    this.allowedActions = [];
    this.timeToAct = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.currentPotSize);
        a.writeInt(this.seq);
        a.writeInt(this.player);
        a.writeInt(this.allowedActions.length);
        var b;
        for (b = 0; b < this.allowedActions.length; b++) {
            a.writeArray(this.allowedActions[b].save())
        }
        a.writeInt(this.timeToAct);
        return a
    };
    this.load = function (b) {
        this.currentPotSize = b.readString();
        this.seq = b.readInt();
        this.player = b.readInt();
        var c;
        var a = b.readInt();
        var d;
        this.allowedActions = [];
        for (c = 0; c < a; c++) {
            d = new com.cubeia.games.poker.io.protocol.PlayerAction();
            d.load(b);
            this.allowedActions.push(d)
        }
        this.timeToAct = b.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.RequestAction";
        a.details = {};
        a.details.currentPotSize = this.currentPotSize;
        a.details.seq = this.seq;
        a.details.player = this.player;
        a.details.allowedActions = [];
        for (b = 0; b < this.allowedActions.length; b++) {
            a.details.allowedActions.push(this.allowedActions[b].getNormalizedObject())
        }
        a.details.timeToAct = this.timeToAct;
        return a
    }
};
com.cubeia.games.poker.io.protocol.RequestAction.CLASSID = 9;
com.cubeia.games.poker.io.protocol.RequestBlindsStructure = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.RequestBlindsStructure.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.RequestBlindsStructure";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.RequestBlindsStructure.CLASSID = 49;
com.cubeia.games.poker.io.protocol.RequestPayoutInfo = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.RequestPayoutInfo.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.RequestPayoutInfo";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.RequestPayoutInfo.CLASSID = 51;
com.cubeia.games.poker.io.protocol.RequestTournamentLobbyData = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.RequestTournamentLobbyData.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.RequestTournamentLobbyData";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.RequestTournamentLobbyData.CLASSID = 60;
com.cubeia.games.poker.io.protocol.RequestTournamentPlayerList = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.RequestTournamentPlayerList.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.RequestTournamentPlayerList";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.RequestTournamentPlayerList.CLASSID = 46;
com.cubeia.games.poker.io.protocol.RequestTournamentRegistrationInfo = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.RequestTournamentRegistrationInfo.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.RequestTournamentRegistrationInfo";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.RequestTournamentRegistrationInfo.CLASSID = 72;
com.cubeia.games.poker.io.protocol.RequestTournamentStatistics = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.RequestTournamentStatistics.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.RequestTournamentStatistics";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.RequestTournamentStatistics.CLASSID = 54;
com.cubeia.games.poker.io.protocol.RequestTournamentTable = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.RequestTournamentTable.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.RequestTournamentTable";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.RequestTournamentTable.CLASSID = 62;
com.cubeia.games.poker.io.protocol.StartHandHistory = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.StartHandHistory.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.StartHandHistory";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.StartHandHistory.CLASSID = 18;
com.cubeia.games.poker.io.protocol.StopHandHistory = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.StopHandHistory.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.StopHandHistory";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.StopHandHistory.CLASSID = 19;
com.cubeia.games.poker.io.protocol.SuitEnum = function () {
};
com.cubeia.games.poker.io.protocol.SuitEnum.CLUBS = 0;
com.cubeia.games.poker.io.protocol.SuitEnum.DIAMONDS = 1;
com.cubeia.games.poker.io.protocol.SuitEnum.HEARTS = 2;
com.cubeia.games.poker.io.protocol.SuitEnum.SPADES = 3;
com.cubeia.games.poker.io.protocol.SuitEnum.HIDDEN = 4;
com.cubeia.games.poker.io.protocol.SuitEnum.makeSuitEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.SuitEnum.CLUBS;
        case 1:
            return com.cubeia.games.poker.io.protocol.SuitEnum.DIAMONDS;
        case 2:
            return com.cubeia.games.poker.io.protocol.SuitEnum.HEARTS;
        case 3:
            return com.cubeia.games.poker.io.protocol.SuitEnum.SPADES;
        case 4:
            return com.cubeia.games.poker.io.protocol.SuitEnum.HIDDEN
    }
    return -1
};
com.cubeia.games.poker.io.protocol.SuitEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"CLUBS";
        case 1:
            return"DIAMONDS";
        case 2:
            return"HEARTS";
        case 3:
            return"SPADES";
        case 4:
            return"HIDDEN"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.TakeBackUncalledBet = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.TakeBackUncalledBet.CLASSID
    };
    this.playerId = {};
    this.amount = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.playerId);
        a.writeString(this.amount);
        return a
    };
    this.load = function (a) {
        this.playerId = a.readInt();
        this.amount = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.TakeBackUncalledBet";
        a.details = {};
        a.details.playerId = this.playerId;
        a.details.amount = this.amount;
        return a
    }
};
com.cubeia.games.poker.io.protocol.TakeBackUncalledBet.CLASSID = 30;
com.cubeia.games.poker.io.protocol.TournamentDestroyed = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.TournamentDestroyed.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.TournamentDestroyed";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.TournamentDestroyed.CLASSID = 71;
com.cubeia.games.poker.io.protocol.TournamentInfo = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.TournamentInfo.CLASSID
    };
    this.tournamentName = {};
    this.gameType = {};
    this.startTime = {};
    this.registrationStartTime = {};
    this.buyIn = {};
    this.fee = {};
    this.minPlayers = {};
    this.maxPlayers = {};
    this.tournamentStatus = {};
    this.buyInCurrencyCode = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.tournamentName);
        a.writeString(this.gameType);
        a.writeString(this.startTime);
        a.writeString(this.registrationStartTime);
        a.writeString(this.buyIn);
        a.writeString(this.fee);
        a.writeInt(this.minPlayers);
        a.writeInt(this.maxPlayers);
        a.writeUnsignedByte(this.tournamentStatus);
        a.writeString(this.buyInCurrencyCode);
        return a
    };
    this.load = function (a) {
        this.tournamentName = a.readString();
        this.gameType = a.readString();
        this.startTime = a.readString();
        this.registrationStartTime = a.readString();
        this.buyIn = a.readString();
        this.fee = a.readString();
        this.minPlayers = a.readInt();
        this.maxPlayers = a.readInt();
        this.tournamentStatus = com.cubeia.games.poker.io.protocol.TournamentStatusEnum.makeTournamentStatusEnum(a.readUnsignedByte());
        this.buyInCurrencyCode = a.readString()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.TournamentInfo";
        a.details = {};
        a.details.tournamentName = this.tournamentName;
        a.details.gameType = this.gameType;
        a.details.startTime = this.startTime;
        a.details.registrationStartTime = this.registrationStartTime;
        a.details.buyIn = this.buyIn;
        a.details.fee = this.fee;
        a.details.minPlayers = this.minPlayers;
        a.details.maxPlayers = this.maxPlayers;
        a.details.tournamentStatus = com.cubeia.games.poker.io.protocol.TournamentStatusEnum.toString(this.tournamentStatus);
        a.details.buyInCurrencyCode = this.buyInCurrencyCode;
        return a
    }
};
com.cubeia.games.poker.io.protocol.TournamentInfo.CLASSID = 59;
com.cubeia.games.poker.io.protocol.TournamentLobbyData = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.TournamentLobbyData.CLASSID
    };
    this.players = {};
    this.blindsStructure = {};
    this.payoutInfo = {};
    this.tournamentStatistics = {};
    this.tournamentInfo = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeArray(this.players.save());
        a.writeArray(this.blindsStructure.save());
        a.writeArray(this.payoutInfo.save());
        a.writeArray(this.tournamentStatistics.save());
        a.writeArray(this.tournamentInfo.save());
        return a
    };
    this.load = function (a) {
        this.players = new com.cubeia.games.poker.io.protocol.TournamentPlayerList();
        this.players.load(a);
        this.blindsStructure = new com.cubeia.games.poker.io.protocol.BlindsStructure();
        this.blindsStructure.load(a);
        this.payoutInfo = new com.cubeia.games.poker.io.protocol.PayoutInfo();
        this.payoutInfo.load(a);
        this.tournamentStatistics = new com.cubeia.games.poker.io.protocol.TournamentStatistics();
        this.tournamentStatistics.load(a);
        this.tournamentInfo = new com.cubeia.games.poker.io.protocol.TournamentInfo();
        this.tournamentInfo.load(a)
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.TournamentLobbyData";
        a.details = {};
        a.details.players = this.players.getNormalizedObject();
        a.details.blindsStructure = this.blindsStructure.getNormalizedObject();
        a.details.payoutInfo = this.payoutInfo.getNormalizedObject();
        a.details.tournamentStatistics = this.tournamentStatistics.getNormalizedObject();
        a.details.tournamentInfo = this.tournamentInfo.getNormalizedObject();
        return a
    }
};
com.cubeia.games.poker.io.protocol.TournamentLobbyData.CLASSID = 61;
com.cubeia.games.poker.io.protocol.TournamentOut = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.TournamentOut.CLASSID
    };
    this.player = {};
    this.position = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.player);
        a.writeInt(this.position);
        return a
    };
    this.load = function (a) {
        this.player = a.readInt();
        this.position = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.TournamentOut";
        a.details = {};
        a.details.player = this.player;
        a.details.position = this.position;
        return a
    }
};
com.cubeia.games.poker.io.protocol.TournamentOut.CLASSID = 21;
com.cubeia.games.poker.io.protocol.TournamentPlayer = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.TournamentPlayer.CLASSID
    };
    this.name = {};
    this.stackSize = {};
    this.position = {};
    this.winnings = {};
    this.tableId = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.name);
        a.writeString(this.stackSize);
        a.writeInt(this.position);
        a.writeString(this.winnings);
        a.writeInt(this.tableId);
        return a
    };
    this.load = function (a) {
        this.name = a.readString();
        this.stackSize = a.readString();
        this.position = a.readInt();
        this.winnings = a.readString();
        this.tableId = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.TournamentPlayer";
        a.details = {};
        a.details.name = this.name;
        a.details.stackSize = this.stackSize;
        a.details.position = this.position;
        a.details.winnings = this.winnings;
        a.details.tableId = this.tableId;
        return a
    }
};
com.cubeia.games.poker.io.protocol.TournamentPlayer.CLASSID = 48;
com.cubeia.games.poker.io.protocol.TournamentPlayerList = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.TournamentPlayerList.CLASSID
    };
    this.players = [];
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.players.length);
        var b;
        for (b = 0; b < this.players.length; b++) {
            a.writeArray(this.players[b].save())
        }
        return a
    };
    this.load = function (b) {
        var d;
        var a = b.readInt();
        var c;
        this.players = [];
        for (d = 0; d < a; d++) {
            c = new com.cubeia.games.poker.io.protocol.TournamentPlayer();
            c.load(b);
            this.players.push(c)
        }
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.TournamentPlayerList";
        a.details = {};
        a.details.players = [];
        for (b = 0; b < this.players.length; b++) {
            a.details.players.push(this.players[b].getNormalizedObject())
        }
        return a
    }
};
com.cubeia.games.poker.io.protocol.TournamentPlayerList.CLASSID = 47;
com.cubeia.games.poker.io.protocol.TournamentRegistrationInfo = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.TournamentRegistrationInfo.CLASSID
    };
    this.buyIn = {};
    this.fee = {};
    this.currency = {};
    this.balanceInWallet = {};
    this.sufficientFunds = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeString(this.buyIn);
        a.writeString(this.fee);
        a.writeString(this.currency);
        a.writeString(this.balanceInWallet);
        a.writeBoolean(this.sufficientFunds);
        return a
    };
    this.load = function (a) {
        this.buyIn = a.readString();
        this.fee = a.readString();
        this.currency = a.readString();
        this.balanceInWallet = a.readString();
        this.sufficientFunds = a.readBoolean()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.TournamentRegistrationInfo";
        a.details = {};
        a.details.buyIn = this.buyIn;
        a.details.fee = this.fee;
        a.details.currency = this.currency;
        a.details.balanceInWallet = this.balanceInWallet;
        a.details.sufficientFunds = this.sufficientFunds;
        return a
    }
};
com.cubeia.games.poker.io.protocol.TournamentRegistrationInfo.CLASSID = 73;
com.cubeia.games.poker.io.protocol.TournamentStatistics = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.TournamentStatistics.CLASSID
    };
    this.chipStatistics = {};
    this.levelInfo = {};
    this.playersLeft = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeArray(this.chipStatistics.save());
        a.writeArray(this.levelInfo.save());
        a.writeArray(this.playersLeft.save());
        return a
    };
    this.load = function (a) {
        this.chipStatistics = new com.cubeia.games.poker.io.protocol.ChipStatistics();
        this.chipStatistics.load(a);
        this.levelInfo = new com.cubeia.games.poker.io.protocol.LevelInfo();
        this.levelInfo.load(a);
        this.playersLeft = new com.cubeia.games.poker.io.protocol.PlayersLeft();
        this.playersLeft.load(a)
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.TournamentStatistics";
        a.details = {};
        a.details.chipStatistics = this.chipStatistics.getNormalizedObject();
        a.details.levelInfo = this.levelInfo.getNormalizedObject();
        a.details.playersLeft = this.playersLeft.getNormalizedObject();
        return a
    }
};
com.cubeia.games.poker.io.protocol.TournamentStatistics.CLASSID = 58;
com.cubeia.games.poker.io.protocol.TournamentStatusEnum = function () {
};
com.cubeia.games.poker.io.protocol.TournamentStatusEnum.ANNOUNCED = 0;
com.cubeia.games.poker.io.protocol.TournamentStatusEnum.REGISTERING = 1;
com.cubeia.games.poker.io.protocol.TournamentStatusEnum.RUNNING = 2;
com.cubeia.games.poker.io.protocol.TournamentStatusEnum.ON_BREAK = 3;
com.cubeia.games.poker.io.protocol.TournamentStatusEnum.PREPARING_BREAK = 4;
com.cubeia.games.poker.io.protocol.TournamentStatusEnum.FINISHED = 5;
com.cubeia.games.poker.io.protocol.TournamentStatusEnum.CANCELLED = 6;
com.cubeia.games.poker.io.protocol.TournamentStatusEnum.CLOSED = 7;
com.cubeia.games.poker.io.protocol.TournamentStatusEnum.makeTournamentStatusEnum = function (a) {
    switch (a) {
        case 0:
            return com.cubeia.games.poker.io.protocol.TournamentStatusEnum.ANNOUNCED;
        case 1:
            return com.cubeia.games.poker.io.protocol.TournamentStatusEnum.REGISTERING;
        case 2:
            return com.cubeia.games.poker.io.protocol.TournamentStatusEnum.RUNNING;
        case 3:
            return com.cubeia.games.poker.io.protocol.TournamentStatusEnum.ON_BREAK;
        case 4:
            return com.cubeia.games.poker.io.protocol.TournamentStatusEnum.PREPARING_BREAK;
        case 5:
            return com.cubeia.games.poker.io.protocol.TournamentStatusEnum.FINISHED;
        case 6:
            return com.cubeia.games.poker.io.protocol.TournamentStatusEnum.CANCELLED;
        case 7:
            return com.cubeia.games.poker.io.protocol.TournamentStatusEnum.CLOSED
    }
    return -1
};
com.cubeia.games.poker.io.protocol.TournamentStatusEnum.toString = function (a) {
    switch (a) {
        case 0:
            return"ANNOUNCED";
        case 1:
            return"REGISTERING";
        case 2:
            return"RUNNING";
        case 3:
            return"ON_BREAK";
        case 4:
            return"PREPARING_BREAK";
        case 5:
            return"FINISHED";
        case 6:
            return"CANCELLED";
        case 7:
            return"CLOSED"
    }
    return"INVALID_ENUM_VALUE"
};
com.cubeia.games.poker.io.protocol.TournamentTable = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.TournamentTable.CLASSID
    };
    this.tableId = {};
    this.save = function () {
        var a = new FIREBASE.ByteArray();
        a.writeInt(this.tableId);
        return a
    };
    this.load = function (a) {
        this.tableId = a.readInt()
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.TournamentTable";
        a.details = {};
        a.details.tableId = this.tableId;
        return a
    }
};
com.cubeia.games.poker.io.protocol.TournamentTable.CLASSID = 63;
com.cubeia.games.poker.io.protocol.WaitingForPlayers = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.WaitingForPlayers.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.WaitingForPlayers";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.WaitingForPlayers.CLASSID = 43;
com.cubeia.games.poker.io.protocol.WaitingToStartBreak = function () {
    this.classId = function () {
        return com.cubeia.games.poker.io.protocol.WaitingToStartBreak.CLASSID
    };
    this.save = function () {
        return[]
    };
    this.load = function (a) {
    };
    this.getNormalizedObject = function () {
        var a = {};
        var b;
        a.summary = "com.cubeia.games.poker.io.protocol.WaitingToStartBreak";
        a.details = {};
        return a
    }
};
com.cubeia.games.poker.io.protocol.WaitingToStartBreak.CLASSID = 42;