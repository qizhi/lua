describe("Poker.Map Test", function(){
    var map = null;

    beforeEach(function() {
        map = new Poker.Map();
    });

    it("test put", function(){
        map.put(1,2);
        expect(map.size()).toEqual(1);
        expect(map.get(1)).toEqual(2);
        expect(map.get(23)).toEqual(null);
        expect(map.put(1,2)).toEqual(2);
        expect(map.size()).toEqual(1);

    });

    it("Test remove",function() {
        map.put("testKey1",{testObj : "val"});
        map.put("testKey2", {testObj : "value2"});
        expect(map.size()).toEqual(2);
        expect(map.remove("testKey1")).toEqual({testObj : "val"});
        expect(map.size()).toEqual(1);

    });

    it("Test keyValue",function() {
        var o1 = {testObj : "val"};
        var o2 = {testObj : "value2"};
        map.put("testKey1",o1);
        map.put("testKey2", o2);
        expect(map.size()).toEqual(2);
        var arr = map.keyValuePairs();
        expect(arr.length).toEqual(2);
    });

    it("Should return the values", function() {
        var o1 = {testObj : "val"};
        var o2 = {testObj : "value2"};
        map.put("testKey1",o1);
        map.put("testKey2", o2);
        console.log("Values:");
        console.log(map.values());

        var values = new Array();
        values.push(o1);
        values.push(o2);
        expect(map.values()).toEqual(values);
    });

});