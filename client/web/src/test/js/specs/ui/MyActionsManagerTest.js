var Poker = Poker || {};

describe("Poker.MyActionsManagerTest", function(){

    /**
     * @type {Poker.MyActionsManager}
     */
    var myActionsManager = null;
    beforeEach(function() {
        var actionCallback = function(actionType,amount) {};
        Poker.AppCtx = Poker.AppCtx || {};
        Poker.AppCtx.getTournamentManager = Poker.AppCtx.getTournamentManager || function(){ return { isTournamentTable : function(){}}};
        myActionsManager = new Poker.MyActionsManager($("#myActionsManagerTest"),0, actionCallback);

    });
    afterEach(function(){

    });

    it("test on watching table", function(){
        myActionsManager.onWatchingTable();

        var leave = myActionsManager.tableButtons.getButton(Poker.ActionType.LEAVE);
        expect(leave.el.is(":visible")).toEqual(true);

        var join = myActionsManager.tableButtons.getButton(Poker.ActionType.JOIN);
        expect(join.el.is(":visible")).toEqual(true);

        $("#myActionsManagerTest .user-actions div").each(function(i,e){
            if(!$(e).hasClass("action-join")) {
                expect($(e).is(":visible")).toEqual(false);
            }
        });
        expect(myActionsManager.blindsActions.waitForBigBlind.container.is(":visible")).toEqual(false);
        expect(myActionsManager.blindsActions.noMoreBlinds.container.is(":visible")).toEqual(false);
        expect(myActionsManager.sitOutNextHand.container.is(":visible")).toEqual(false);
        expect(myActionsManager.futureActions.container.is(":visible")).toEqual(false);
    });

    it("test on sat down at table", function(){
        myActionsManager.onSatDown();

        var leave = myActionsManager.tableButtons.getButton(Poker.ActionType.LEAVE);
        expect(leave.el.is(":visible")).toEqual(true);


        $("#myActionsManagerTest .user-actions div").each(function(i,e){
            expect($(e).is(":visible")).toEqual(false);
        });
        expect(myActionsManager.blindsActions.waitForBigBlind.container.is(":visible")).toEqual(true);
        expect(myActionsManager.blindsActions.noMoreBlinds.container.is(":visible")).toEqual(true);
        expect(myActionsManager.sitOutNextHand.container.is(":visible")).toEqual(true);
        expect(myActionsManager.futureActions.container.is(":visible")).toEqual(false);
    });

    it("test sit in", function(){
        myActionsManager.onSitIn();

        var leave = myActionsManager.tableButtons.getButton(Poker.ActionType.LEAVE);
        expect(leave.el.is(":visible")).toEqual(true);


        $("#myActionsManagerTest .user-actions div").each(function(i,e){
            expect($(e).is(":visible")).toEqual(false);
        });

        expect(myActionsManager.blindsActions.waitForBigBlind.container.is(":visible")).toEqual(false);
        expect(myActionsManager.blindsActions.noMoreBlinds.container.is(":visible")).toEqual(true);
        expect(myActionsManager.sitOutNextHand.container.is(":visible")).toEqual(true);
        expect(myActionsManager.futureActions.container.is(":visible")).toEqual(false);
    });




});