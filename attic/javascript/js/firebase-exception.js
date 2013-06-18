/**
 * @fileOverview This file contains the firebase exception object
 * @author <a href="http://www.cubeia.org">Peter Lundh</a>
 * @version 1.0-SNAPSHOT
 */


/**
 *  @namespace FIREBASE
 */
var FIREBASE = FIREBASE || {};

/**
 * Firebase Exception.
 * 
 * Constructs a firebase exception
 * 
 * @constructor
 * @param {Number} errorCode Error code @see error-codes.js
 * @param {String} errorMessage Description of the messages - bubbles up from the underlying subsystems  
 */
FIREBASE.FirebaseException = function(errorCode, errorMessage) {
	this.name = "FIREBASE.FirebaseException";
	this.message = errorMessage;
	this.code = errorCode;
};

FIREBASE.FirebaseException.Throw = function(errorCode, errorMessage) {
	throw new FIREBASE.FirebaseException(errorCode, errorMessage);
};

