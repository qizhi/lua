var Poker = Poker || {};

describe("Poker.BlindsActionsNoMoreBlindsTest", function(){

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
        blindsActions.getRequestHandler = function(){
            return {
                onMyPlayerAction : function(actionType, amount) {
                    actionTypeResult = actionType;
                    amountResult = amount;
                }
            }
        };
    });
    afterEach(function(){
        actionTypeResult = null;
        amountResult = -1;
    });

    it("test post big blind", function(){
        var actions = [];
        actions.push(new Poker.Action(Poker.ActionType.BIG_BLIND,10,10));
        actions.push(new Poker.Action(Poker.ActionType.DECLINE_ENTRY_BET,0,0));

        expect(blindsActions.noMoreBlinds.isEnabled()).toEqual(false);

        var result =  blindsActions.handleBlinds(actions);

        expect(result).toEqual(true);
        expect(actionTypeResult).toEqual(Poker.ActionType.BIG_BLIND);
        expect(amountResult).toEqual(10);


    });

    it("test decline entry bet", function(){
        var actions = [];
        actions.push(new Poker.Action(Poker.ActionType.BIG_BLIND,10,10));
        actions.push(new Poker.Action(Poker.ActionType.DECLINE_ENTRY_BET,0,0));

        blindsActions.noMoreBlinds.setEnabled(true);
        expect(blindsActions.noMoreBlinds.isEnabled()).toEqual(true);

        var result =  blindsActions.handleBlinds(actions);

        expect(result).toEqual(true);
        expect(actionTypeResult).toEqual(Poker.ActionType.DECLINE_ENTRY_BET);
        expect(amountResult).toEqual(0);


    });

    it("test no action to handle", function(){
        var actions = [];
        actions.push(new Poker.Action(Poker.ActionType.CALL,10,10));
        actions.push(new Poker.Action(Poker.ActionType.FOLD,0,0));

        blindsActions.noMoreBlinds.setEnabled(true);
        expect(blindsActions.noMoreBlinds.isEnabled()).toEqual(true);

        var result =  blindsActions.handleBlinds(actions);

        expect(result).toEqual(false);
        expect(actionTypeResult).toEqual(null);
        expect(amountResult).toEqual(-1);

    });


});