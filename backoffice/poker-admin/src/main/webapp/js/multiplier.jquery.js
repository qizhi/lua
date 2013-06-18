var Admin = Admin || {};

Admin.multiply = function(src,target,multiplier) {
    $(src).on('keyup',function(e){
        if($.trim($(this).val())!="" && $(target).is(":disabled") == false) {
            var val = parseFloat($(this).val());
            $(target).val(val * multiplier);
            $(target).trigger("keyup");
        } else {
            $(target).val("");
        }
    });
}
