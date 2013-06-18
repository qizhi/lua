(function( $ ) {
	  $.fn.describe = function() {
		 
		  return this.each(function(){
			   var defaultMsg = $(this).attr("title");


               $(this).val(defaultMsg);
			   $(this).bind("focus",function(e){

                   if($(this).val()==defaultMsg) {
		      		  $(this).val("");
		      		  $(this).removeClass("describe");
		      	  }
		        });
		        $(this).bind("blur",function(e){
                    if($(this).val()=="") {
		        		$(this).val(defaultMsg);
		        		$(this).addClass("describe");
		        	}
		        });
		  });
	  };
	})( jQuery );

