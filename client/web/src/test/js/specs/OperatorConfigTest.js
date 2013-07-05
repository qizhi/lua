describe("Poker.OperatorConfig Test", function(){

    beforeEach(function() {

    });

    it("Format currency", function(){
        Poker.OperatorConfig.populate({ CLIENT_HELP_URL : "abc", TEST_URL : "123" });
        expect(Poker.OperatorConfig.getClientHelpUrl()).toEqual("abc");
        expect(Poker.OperatorConfig.getValue("TEST_URL","DEFAULT")).toEqual("123");
        expect(Poker.OperatorConfig.getValue("PARAM_NOT_EXISTING","000")).toEqual("000");
    });
});