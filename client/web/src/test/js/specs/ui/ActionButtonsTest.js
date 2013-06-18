var Poker = Poker || {};

describe("Poker.ActionButtonsTest", function(){

    /**
     * @type {Poker.ActionButtons}
     */
    var actionButtons = null;
    beforeEach(function() {
        var actionCallback = function(actionType,amount) {};
        var raiseAndBetCallback = function(minAmount,maxAmount,mainPot) {};
        var amountCallback = function() {
            return 10;
        };
        actionButtons = new Poker.ActionButtons($("#actionButtonsTest"), actionCallback,
            raiseAndBetCallback,raiseAndBetCallback, amountCallback);
    });
    afterEach(function(){

    });

    it("test hide/show all", function(){

        $("#actionButtonsTest div").show();

        actionButtons.hideAll();
        var actionTypes = [Poker.ActionType.RAISE,Poker.ActionType.BET,Poker.ActionType.CALL,
            Poker.ActionType.FOLD];
        for(var x in actionTypes) {
            expect(actionButtons.getButton(actionTypes[x]).el.is(":visible")).toBeFalsy();
        }
        expect(actionButtons.doBetActionButton.el.is(":visible")).toBeFalsy();
        expect(actionButtons.doRaiseActionButton.el.is(":visible")).toBeFalsy();
        expect(actionButtons.cancelBetActionButton.el.is(":visible")).toBeFalsy();
        expect(actionButtons.fixedBetActionButton.el.is(":visible")).toBeFalsy();
        expect(actionButtons.fixedRaiseActionButton.el.is(":visible")).toBeFalsy();


    });




});