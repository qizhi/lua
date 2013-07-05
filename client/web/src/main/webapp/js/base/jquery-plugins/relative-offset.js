(function( $ ) {
    $.fn.relativeOffset = function(targetElement) {
        var el = this[0];
        var targetOffset = $(targetElement).offset();
        var offset = $(el).offset();

        var top = offset.top - targetOffset.top;
        var left = offset.left - targetOffset.left;

        return { top : top, left : left };
    };
})( jQuery );