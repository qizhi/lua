describe("Poker.Utils Test", function(){

    beforeEach(function() {
        Poker.Utils.currencySymbol="&euro;";
    });

    it("Format currency", function(){
        var c = Poker.Utils.formatCurrency("1.00");
        expect(c).toEqual("1");

        c = Poker.Utils.formatCurrency("120.50");
        expect(c).toEqual("120.5");


        c = Poker.Utils.formatCurrency("120000.5");
        expect(c).toEqual("120,000.5");

        c = Poker.Utils.formatCurrency("120120000.5");
        expect(c).toEqual("120,120,000.5");

        c = Poker.Utils.formatCurrency("0.01");
        expect(c).toEqual("0.01");

        var currency = {
            code : "EUR",
            fractionalDigits : 8
        };

        c = Poker.Utils.formatCurrency("0.00000001",currency);
        expect(c).toEqual("0.00000001");


        c = Poker.Utils.formatCurrency("2003.00000001",currency);
        expect(c).toEqual("2,003.00000001");
    });

    it("Format currency string", function(){
        var c = Poker.Utils.formatCurrencyString("1.00");
        expect(c).toEqual("&euro;1");
    });

    it("Format blinds", function(){
        //no 0 decimal in blinds displayed in lobby
        var c = Poker.Utils.formatCurrency("1.00");
        expect(c).toEqual("1");

        c = Poker.Utils.formatCurrency("2.00");
        expect(c).toEqual("2");

        c = Poker.Utils.formatCurrency("2.50");
        expect(c).toEqual("2.5");

        c = Poker.Utils.formatCurrency("0.25");
        expect(c).toEqual("0.25");
    });
});