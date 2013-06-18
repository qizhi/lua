/*!
 * jQuery Google Analytics Plugin
 * jquery.ga.js
 * http://www.shamasis.net/projects/ga/
 *
 * Copyright (c) 2009 Shamasis Bhattacharya
 * Complies and conforms to all jQuery licensing schemes.
 * http://docs.jquery.com/License
 *
 * Date: 2009-08-23
 * Revision: 13
 */

(function($) {

    /**
     * Contains the various Google Analytics routines.
     *
     * @code
     * $(document).ready(function() {
     *    $.ga.load('UA-0000000-0');
     * });
     *
     *
     * @id jQuery.ga
     * @return Nothing
     * @type undefined
     * @since 1.0
     * @compat=IE6|IE7|IE8|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
     */
    $.ga = { };

    /**
     * Loads the Google Analytics core tracking scripts (ga.js) and other
     * routines.
     *
     * @code
     * $(document).ready(function() {
     *    $.ga.load('UA-0000000-0');
     * });
     *
     *
     * @param {String} uid Google Anayltics account id that will be used to
     * report analysis. The account it somewhat looks like "UA-0000000-0".
     * 
     * @param {Function} callback
     *
     * @id jQuery.ga.load
     * @return Nothing
     * @type undefined
     * @since 1.0
     * @compat=IE6|IE7|IE8|FF1|FF2|FF3|OPERA|SAFARI2|SAFARI3|KONQ
     */
    $.ga.load = function(uid, callback) {
        jQuery.ajax({
            type: 'GET',
            url: (document.location.protocol == "https:" ?
                "https://ssl" : "http://www") + '.google-analytics.com/ga.js',
            cache: true,
            success: function() {

                // check whether _gat is undefined
                if (typeof _gat == undefined) {
                    throw "_gat has not been defined";
                }

                // create a new tracker
                t = _gat._getTracker(uid);

                // map all underscore functions of tracker to $.ga
                bind();

                // call the callback function for user to do whatever
                // required.
                if( $.isFunction(callback) ) {
                    callback(t);
                }

                // initialize GATC
                t._trackPageview();
            },
            dataType: 'script',
            data: null
         });
    };


    /**
     *  The pageTracker variable, holding the pageTracker retrieved from _gat
     *  @access: private
     */
    var t;

    /**
     *  Maps all user API of pageTracker to $.ga.* after dropping the
     *  underscore.
     *  @access: private
     */
    var bind = function() {
        
        // check whether tracker exists
        if (noT()) {
            throw "pageTracker has not been defined";
        }

        // for each function of tracker that starts with underscore, map it to
        // $.ga.* after dropping the underscore.
        for(var $1 in t) {
            if($1.charAt(0) != '_') continue;
            $.ga[$1.substr(1)] = t[$1];
        }
    };

    /**
     *  Returns whether pageTracker has been defined or not after the launch of
     *  core GATC logic.
     *  @access: private
     *  @type Boolean
     */
    var noT = function() {
        return t == undefined;
    };


})(jQuery);