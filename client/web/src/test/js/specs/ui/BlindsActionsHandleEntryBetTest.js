var Poker = Poker || {};

describe("Poker.BlindsActionsHandleEntryBetTest", function(){

    /**
     * @type {Poker.BlindsActions}
     */
    var blindsActions = null;
    var actionTypeResult = null;
    var amountResult = -1;
    beforeEach(function() {
        blindsActions = new Poker.BlindsActions($("#blindsActionTest"),1,function(actionType,amount){
            actionTypeResult = actionType;
            amountResult = amount;
        });
    });
    afterEach(function(){
        actionTypeResult = null;
        amountResult = -1;
    });

    it("test wait for big blind", function(){
        var actions = [];
        actions.push(new Poker.Action(Poker.ActionType.WAIT_FOR_BIG_BLIND,0,0));
        actions.push(new Poker.Action(Poker.ActionType.ENTRY_BET,10,10));
        actions.push(new Poker.Action(Poker.ActionType.DECLINE_ENTRY_BET,0,0));

        expect(blindsActions.waitForBigBlind.isEnabled()).toEqual(true);
        expect(blindsActions.handleEntryBigBlind(actions)).toEqual(true);
        expect(actionTypeResult).toEqual(Poker.ActionType.WAIT_FOR_BIG_BLIND);
        expect(amountResult).toEqual(0);
    });

    it("test wait post entry bet before bb", function(){
        var actions = [];
        actions.push(new Poker.Action(Poker.ActionType.WAIT_FOR_BIG_BLIND,0,0));
        actions.push(new Poker.Action(Poker.ActionType.ENTRY_BET,10,10));
        actions.push(new Poker.Action(Poker.ActionType.DECLINE_ENTRY_BET,0,0));
        blindsActions.waitForBigBlind.setEnabled(false);
        expect(blindsActions.waitForBigBlind.isEnabled()).toEqual(false);
        expect(blindsActions.handleEntryBigBlind(actions)).toEqual(true);
        expect(actionTypeResult).toEqual(Poker.ActionType.ENTRY_BET);
        expect(amountResult).toEqual(10);
    });

    it("test force post entry bet on BB position", function(){
        var actions = [];
        actions.push(new Poker.Action(Poker.ActionType.ENTRY_BET,10,10));
        actions.push(new Poker.Action(Poker.ActionType.DECLINE_ENTRY_BET,0,0));
        blindsActions.waitForBigBlind.setEnabled(true);
        expect(blindsActions.waitForBigBlind.isEnabled()).toEqual(true);
        expect(blindsActions.handleEntryBigBlind(actions)).toEqual(true);
        expect(actionTypeResult).toEqual(Poker.ActionType.ENTRY_BET);
        expect(amountResult).toEqual(10);
    });

    it("test no action to handle", function(){
        var actions = [];
        actions.push(new Poker.Action(Poker.ActionType.BIG_BLIND,10,10));
        actions.push(new Poker.Action(Poker.ActionType.DECLINE_ENTRY_BET,0,0));
        blindsActions.waitForBigBlind.setEnabled(true);
        expect(blindsActions.waitForBigBlind.isEnabled()).toEqual(true);
        expect(blindsActions.handleEntryBigBlind(actions)).toEqual(false);
        expect(actionTypeResult).toEqual(null);
        expect(amountResult).toEqual(-1);
    });

});