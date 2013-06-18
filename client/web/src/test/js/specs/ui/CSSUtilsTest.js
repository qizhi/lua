describe("Poker.CSSUtils Test", function(){
    var cssUtils = null;
    var mockEl = { style : {} };

    beforeEach(function() {
       cssUtils = new Poker.CSSUtils();
       mockEl.style["WebkitTransform"] = "";
       mockEl.style["WebkitTransformOrigin"] = "";
       mockEl.style["WebkitTransition"] = "";
    });

    it("Translate3d string", function(){
        var translate = cssUtils.toTranslate3dString(1,2,3,"%");
        expect(translate).toEqual("translate3d(1%,2%,3%)");
    });

    it("rotate string", function(){
        var translate = cssUtils.toRotateString(100);
        expect(translate).toEqual("rotate(100deg)");
    });

    it("scale string", function(){
        var translate = cssUtils.toScale3dString(0.5,0.2,1);
        expect(translate).toEqual("scale3d(0.5,0.2,1)");
    });
    it("add transform", function(){
        var translate = cssUtils.toTranslate3dString(1,2,3,"%");
        var translate = cssUtils.addTransform(mockEl,translate);
        expect(mockEl.style["WebkitTransform"]).toEqual("translate3d(1%,2%,3%)");
    });
    it("add transition", function(){
        var transition = cssUtils.addTransition(mockEl,"transform 0.5s linear");
        expect(mockEl.style["WebkitTransition"]).toEqual("-webkit-transform 0.5s linear");
    });
    it("set translate3d", function(){
        cssUtils.setTranslate3d(mockEl,1,2,3,"%","center");
        expect(mockEl.style["WebkitTransform"]).toEqual("translate3d(1%,2%,3%)");
        expect(mockEl.style["WebkitTransformOrigin"]).toEqual("center");
    });


});