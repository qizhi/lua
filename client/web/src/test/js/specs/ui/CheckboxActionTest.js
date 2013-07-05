var Poker = Poker || {};

describe("Poker.CheckboxAction Test", function(){

    /**
     * @type {Poker.CheckboxAction}
     */

    $.ga = {
        _trackEvent:function() {}
    };

    var checkbox = null;
    beforeEach(function() {

    });
    afterEach(function(){
        if(checkbox!=null) {
            checkbox.checkbox.off();
        }
    });

    it("test checkbox create checked", function(){
        checkbox = new Poker.CheckboxAction($("body"),$("#checkboxActionTest"), true);

        expect(checkbox.isEnabled()).toBeTruthy();
        expect(checkbox.checkbox.is(":checked")).toBeTruthy();


    });

    it("test checkbox create unchecked", function(){
        checkbox = new Poker.CheckboxAction($("body"),$("#checkboxActionTest"), false);
        expect(checkbox.isEnabled()).toBeFalsy();
        expect(checkbox.checkbox.is(":checked")).toBeFalsy();
    });

    it("test set value", function(){

        checkbox = new Poker.CheckboxAction($("body"),$("#checkboxActionTest"), false);

        expect(checkbox.isEnabled()).toBeFalsy();

        checkbox.setEnabled(true);

        expect(checkbox.isEnabled()).toEqual(true);

        expect(checkbox.checkbox.is(":checked")).toBeTruthy();

        checkbox.setEnabled(false);

        expect(checkbox.isEnabled()).toBeFalsy();
        expect(checkbox.checkbox.is(":checked")).toBeFalsy();

    });

    it("test tick checkbox ", function(){
        checkbox = new Poker.CheckboxAction($("body"),$("#checkboxActionTest"), false);
        var onChangeValue = false;
        checkbox.onChange(function(enabled){
            onChangeValue = enabled;
        });
        expect(checkbox.isEnabled()).toBeFalsy();

        checkbox.checkbox.attr("checked",true).change();

        expect(onChangeValue).toBeTruthy();
        expect(checkbox.isEnabled()).toBeTruthy();

        checkbox.checkbox.attr("checked",false).change();

        expect(onChangeValue).toBeFalsy();
        expect(checkbox.isEnabled()).toBeFalsy();



    });



});