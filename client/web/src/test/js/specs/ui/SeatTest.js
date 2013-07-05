var contextPath = "";
var Poker = Poker || {};
Poker.SkinConfiguration = {
    name : "test"
};

describe("Poker.Seat Test", function(){

    var seat = null;
    var mockAnimationManager;

    beforeEach(function() {
        Poker.AppCtx = Poker.AppCtx || {};
        Poker.AppCtx.getTemplateManager = Poker.AppCtx.getTemplateManager || function(){ return new Poker.TemplateManager();};
        mockAnimationManager = jasmine.createSpyObj('mockAnimationManager',['animate']);
        var player = new Poker.Player(1,"TestPlayer");
        seat = new Poker.Seat("testSeat", 0, player, mockAnimationManager);
    });

    it("Test render seat", function(){
        var name = $("#testSeat .player-name").html();
        expect($.trim(name)).toEqual("TestPlayer");
    });

    it("Test update player", function(){
        var p = new Poker.Player(1,"TestPlayer");
        p.balance = 1000;
        seat.updatePlayer(p);
        var balance = seat.seatBalance.html();
        expect(true).toEqual(balance.indexOf(1000)!=-1); //for somereason &euro; isn't working in jasmine

        p.balance = 0;
        seat.updatePlayer(p);
        balance = $.trim(seat.seatBalance.html());
        expect(balance).toEqual("All in");

    });

    it("Test on action bet", function(){
        seat.onAction(Poker.ActionType.BET,100);
        var actionAmount = seat.actionAmount.html();
        expect(actionAmount).toContain("100");
        expect(actionAmount).toContain("action-bet-icon");
        expect(mockAnimationManager.animate).toHaveBeenCalled();

    });
    it("Test on action raise", function(){
        seat.onAction(Poker.ActionType.RAISE,999);
        var actionAmount = seat.actionAmount.html();
        expect(actionAmount).toContain("999");
        expect(actionAmount).toContain("action-raise-icon");
        expect(mockAnimationManager.animate).toHaveBeenCalled();

    });
    it("Test on action call", function(){
        seat.onAction(Poker.ActionType.CALL,123);

        var actionAmount = seat.actionAmount.html();
        expect(actionAmount).toContain("123");
        expect(actionAmount).toContain("action-call-icon");
        expect(mockAnimationManager.animate).toHaveBeenCalled();

    });
    it("Test on action big blind", function(){
        seat.onAction(Poker.ActionType.BIG_BLIND,122);

        var actionAmount = seat.actionAmount.html();
        expect(actionAmount).toContain("122");
        expect(actionAmount).toContain("action-big-blind-icon");
        expect(mockAnimationManager.animate).toHaveBeenCalled();

    });
    it("Test on action small blind", function(){
        seat.onAction(Poker.ActionType.SMALL_BLIND,100);
        var actionAmount = seat.actionAmount.html();
        expect(actionAmount).toContain("100");
        expect(actionAmount).toContain("action-small-blind-icon");
        expect(mockAnimationManager.animate).toHaveBeenCalled();

    });

    it("Test on action fold", function(){
        seat.onAction(Poker.ActionType.FOLD,0);
        expect(seat.actionAmount.is(":visible")).toBeTruthy();

    });

    it("Test on activate/inactivate seat",function(){
        seat.activateSeat([], 15000, 300);
        expect(seat.seatElement.hasClass("active-seat")).toBeTruthy();
        expect(seat.progressBarElement.is(":visible")).toBeTruthy();

        seat.inactivateSeat();

        expect(seat.seatElement.hasClass("active-seat")).toBeFalsy();
        expect(seat.progressBarElement.is(":visible")).toBeFalsy();

    });


});