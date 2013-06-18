var Poker = Poker || {};

describe("Poker.TableLayoutManager Test", function(){

    //Mock classes
    Poker.AppCtx = {
        getSoundRepository : function(){ return new Poker.SoundRepository(); }
    };
    var MockBuyInDialog = Class.extend({});
    var MockMyActionsManager = Class.extend({});
    var MockDealerButton = Class.extend({});
    var MockSoundManager = {
        playerAction:function() {},
        playSound:function() {}
    };

    var MockSeat = Class.extend({
        mockSeatPos : -1,
        seatId : -1,
        player : null,
        init : function(elementId, seatId, player, templateManager, animationManager) {
            this.seatId = seatId;
            this.player = player;
        },
        setSeatPos : function(previousPos, position) {
            this.mockSeatPos = position;
        },
        clearSeat : function() {

        },
        isMySeat : function(){return false;}
    });
    var MockMyPlayerSeat = MockSeat.extend({
        init : function(tableId,elementId, seatId, player, templateManager, myActionsManager, animationManager) {
            this._super(elementId, seatId, player, templateManager, animationManager);
        },
        isMySeat : function(){return true;}
    });

    var tableLayoutManager = null;
    var mockTableComHandler = null;

    beforeEach(function() {
        //Mocks of classes that are created within the table listener class
        Poker.MockUtils.mock("Seat",MockSeat);
        Poker.MockUtils.mock("BuyInDialog",MockBuyInDialog);
        Poker.MockUtils.mock("MyActionsManager",MockMyActionsManager);
        Poker.MockUtils.mock("DealerButton",MockDealerButton);
        Poker.MockUtils.mock("MyPlayerSeat",MockMyPlayerSeat);


        tableLayoutManager = new Poker.TableLayoutManager(1, $("#testTable"),
            new Poker.TemplateManager(), 10, MockSoundManager);
    });
    afterEach(function(){
        Poker.MockUtils.resetMocks();
    });

    it("test normalize seat positions", function(){
        //since when sitting down you are always at position 0 (no matter what seat id)
        //all positions are moved around

        //my player position = 0  id = 2
        tableLayoutManager.myPlayerSeatId = 2;

        //then seat with id 4 will have position 2
        var normalized = tableLayoutManager._getNormalizedSeatPosition(4);

        expect(normalized).toEqual(2);

        tableLayoutManager.myPlayerSeatId = 6;
        normalized = tableLayoutManager._getNormalizedSeatPosition(4);

        //%10
        expect(normalized).toEqual(8);

    });

    it("test add/remove player", function(){
        tableLayoutManager.onPlayerAdded(0,new Poker.Player(10,"TestPlayer10"));

        tableLayoutManager.onPlayerAdded(1,new Poker.Player(11,"TestPlayer11"));

        tableLayoutManager.onPlayerAdded(4,new Poker.Player(13,"TestPlayer13"));

        var s = tableLayoutManager.getSeatByPlayerId(10);
        expect(s.seatId).toEqual(0);
        expect(s.mockSeatPos).toEqual(0);

        s = tableLayoutManager.getSeatByPlayerId(11);
        expect(s.seatId).toEqual(1);
        expect(s.mockSeatPos).toEqual(1);

        s = tableLayoutManager.getSeatByPlayerId(13);
        expect(s.seatId).toEqual(4);
        expect(s.mockSeatPos).toEqual(4);

        tableLayoutManager.onPlayerRemoved(10);
        s = tableLayoutManager.getSeatByPlayerId(10);
        expect(s).toBeNull();
    });

    it("test add my player", function(){
        Poker.MyPlayer.id = 13;
        tableLayoutManager.onPlayerAdded(0,new Poker.Player(10,"TestPlayer10"));


        tableLayoutManager.onPlayerAdded(1,new Poker.Player(11,"TestPlayer11"));

        tableLayoutManager.onPlayerAdded(4,new Poker.Player(13,"TestPlayer13"));

        var s = tableLayoutManager.getSeatByPlayerId(13);
        expect(s.isMySeat()).toBeTruthy();
        expect(s.seatId).toEqual(4);
        expect(s.mockSeatPos).toEqual(-1); //we don't hold the seat position since it's always 0 on my player

        s = tableLayoutManager.getSeatByPlayerId(10);
        expect(s.seatId).toEqual(0);
        expect(s.mockSeatPos).toEqual(6);

        s = tableLayoutManager.getSeatByPlayerId(11);
        expect(s.seatId).toEqual(1);
        expect(s.mockSeatPos).toEqual(7);

    });

});