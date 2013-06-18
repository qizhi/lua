describe("Poker.Animation Test", function(){
    var mockEl = null;
    beforeEach(function() {
        mockEl = {
            style : {}
        };
        //eq to a webkit browser
        mockEl.style["WebkitTransition"]="";
        mockEl.style["WebkitTransform"]="";
        mockEl.style["WebkitTransformOrigin"]="";
    });

    it("Remaining time test", function(){
        var animation = new Poker.TransformAnimation(mockEl);

        animation.startTime = 0; //mock start time to 0
        animation.getNow = function(){ return 100 };// mock "now" time to be 100
        animation.setTimed(true);
        animation.addTransition("transform",1,"linear");

        var remaining = animation.getRemainingTime();

        expect(remaining).toEqual(0.9);

    });

    it("Scale timed animation test", function(){

        var animation = new Poker.TransformAnimation(mockEl);

        animation.startTime = 0; //mock start time to 0
        animation.getNow = function(){ return 500 };// mock "now" time to be 100
        animation.setTimed(true);
        animation.addTransition("transform",1,"linear");
        animation.addScale3d(0,0,1);
        animation.prepareElement();

        //start transform after 500 ms
        expect(mockEl.style["WebkitTransform"]).toEqual("scale3d(0.5,0.5,1)");
        animation.prepare();
        animation.animate();
        expect(mockEl.style["WebkitTransition"]).toEqual("-webkit-transform 0.5s linear");
        expect(mockEl.style["WebkitTransform"]).toEqual("scale3d(0,0,1)");


    });
    it("Translate timed animation test", function(){

        var animation = new Poker.TransformAnimation(mockEl);
        animation.startTime = 0; //mock start time to 0
        animation.getNow = function(){ return 500 }; //mock "now"
        animation.setTimed(true);
        //start values at time 0
        animation.addStartTranslate(0,0,0,"%");
        animation.addTransition("transform",1,"linear");
        animation.addTranslate3d(100,100,0,"%");
        //calculate the start values at time 500
        animation.prepareElement();

        //start transform after 500 ms
        expect(mockEl.style["WebkitTransform"]).toEqual("translate3d(50%,50%,0)");
        animation.prepare();
        animation.animate();
        //check complete values at time 1000 (transition complete)
        expect(mockEl.style["WebkitTransition"]).toEqual("-webkit-transform 0.5s linear");
        expect(mockEl.style["WebkitTransform"]).toEqual("translate3d(100%,100%,0)");


    });

    it("Rotate timed animation test", function(){

        var animation = new Poker.TransformAnimation(mockEl);
        animation.startTime = 0; //mock start time to 0
        animation.getNow = function(){ return 500 }; //mock "now"
        animation.setTimed(true);
        //start values at time 0
        animation.addStartRotate(100)
        animation.addTransition("transform",1,"linear");
        animation.addRotate(200)
        //calculate the start values at time 500
        animation.prepareElement();

        //start transform after 500 ms
        expect(mockEl.style["WebkitTransform"]).toEqual("rotate(150deg)");
        animation.prepare();
        animation.animate();
        //check complete values at time 1000 (transition complete)
        expect(mockEl.style["WebkitTransition"]).toEqual("-webkit-transform 0.5s linear");
        expect(mockEl.style["WebkitTransform"]).toEqual("rotate(200deg)");


    });
});