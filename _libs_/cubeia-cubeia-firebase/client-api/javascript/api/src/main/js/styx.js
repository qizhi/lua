/**
 * @fileOverview This file contains styx utility functions
 * @author <a href="http://www.cubeia.org">Peter Lundh</a>
 * @version 1.0-SNAPSHOT
 */

/**
 * @namespace FIREBASE
 */
var FIREBASE = FIREBASE || {};
var FB_PROTOCOL = FB_PROTOCOL || {};

FIREBASE.Styx = function() {
};

/**
 * This method takes a Styx protocol object and wraps it in a
 * GameTransportPacket. The game data field will be correctly packet according
 * to the Styx binary specification.
 * 
 * @param (Number)
 *            pid Player id
 * @param (Number)
 *            tid Table id
 * @param {Object}
 *            protocolObject Styx protocol object to wrap
 * @return {Object} A Game transport packet
 */
FIREBASE.Styx.wrapInGameTransportPacket = function(pid, tid, protocolObject) {
	var gameTransportPacket = new FB_PROTOCOL.GameTransportPacket();
	gameTransportPacket.tableid = tid;
	gameTransportPacket.pid = pid;
	var byteArray = protocolObject.save();
	gameTransportPacket.gamedata = FIREBASE.ByteArray.toBase64String(byteArray.createGameDataArray(protocolObject.classId()));
	return gameTransportPacket;
};

/**
 * Check if array only contains bytes
 * 
 * @static
 * @param arr
 * @returns {Boolean} true if array only contain bytes
 */
FIREBASE.Styx.isByteArray = function(arr) {
	var i;
	for (i = 0; i < arr.length; i++) {
		if (typeof (arr[i]) !== "number" || arr[i] > 256) {
			return false;
		}
	}
	return true;
};

/**
 * Safely deep clone a protocol object and convert byte arrays to base64
 * 
 * @static
 * @param protocolObject
 * @returns {Object} protocol object clone
 */
FIREBASE.Styx.cloneObject = function(protocolObject) {
	var i, name = "", newObject = {};
	// loop through all properties
	for (name in protocolObject) {
		// is it an object?
		if (typeof (protocolObject[name]) === "object") {
			// is it an array?
			if (protocolObject[name] instanceof Array) {
				if (protocolObject[name].length === 0) {
					newObject[name] = [];
				} else if (this.isByteArray(protocolObject[name])) {
					// it is a byte array, convert to base64 string
					newObject[name] = FIREBASE.ByteArray
							.toBase64String(protocolObject[name]);
				} else {
					// array of objects
					newObject[name] = [];
					for (i = 0; i < protocolObject[name].length; i++) {
						if (typeof (protocolObject[name][i]) === "object") {
							// object inside array, clone it recursively
							newObject[name].push(FIREBASE.Styx
									.cloneObject(protocolObject[name][i]));
						} else {
							newObject[name].push(protocolObject[name][i]);
						}
					}
				}
			} else {
				// object is not an array, clone it recursively
				newObject[name] = FIREBASE.Styx
						.cloneObject(protocolObject[name]);
			}
		} else if (typeof (protocolObject[name]) !== "function") {
			// normal field
			newObject[name] = protocolObject[name];
		} else if (name === "classId") {
			// classId, we treat it specially by creating a property with the
			// result from the function call
			newObject[name] = protocolObject[name]();
		}
	}
	// return the cloned object
	return newObject;
};

/**
 * Write a key value pair to a parameter object
 * 
 * @static
 * @param {FB_PROTOCOL.Param}
 *            param
 * @param {String}
 *            key
 * @param {Object}
 *            value
 */
FIREBASE.Styx.writeParam = function(param, key, value) {
	var byteArray = new FIREBASE.ByteArray();

	if (typeof (value) === "string") {
		param.type = FB_PROTOCOL.ParameterTypeEnum.STRING;
		byteArray.writeString(value);
	} else if (typeof (value) === "number") {
		param.type = FB_PROTOCOL.ParameterTypeEnum.INT;
		byteArray.writeInt(value);
	} else {
		return;
	}

	param.key = key;
	param.value = byteArray.getBuffer();

};

/**
 * Read a value from a parameter object
 * 
 * @static
 * @param {FB_PROTOCOL.Param}
 *            param
 * @return {Object} value
 */
FIREBASE.Styx.readParam = function(param) {
	var byteArray = new FIREBASE.ByteArray(param.value);

	if (param.type === FB_PROTOCOL.ParameterTypeEnum.STRING) {
		return byteArray.readString();
	} else if (param.type === FB_PROTOCOL.ParameterTypeEnum.INT) {
		return byteArray.readInt();
	}

	return null;
};

/**
 * Get a parameter object
 * 
 * @static
 * @param {FB_PROTOCOL.Param}
 *            param
 * @return {Object} object with name, value fields
 */
FIREBASE.Styx.getParam = function(param) {
	var retObject = {};
	retObject.name = param.key;
	retObject.value = FIREBASE.Styx.readParam(param);
	return retObject;
};


/**
 * Convert a firebase protocol object to a JSON string
 * 
 * @static
 * @param {Object}
 *            protocolObject object to convert
 * @return {String} object as a JSON String
 */
FIREBASE.Styx.toJSON = function(protocolObject) {
	var objectClone = FIREBASE.Styx.cloneObject(protocolObject);
	return JSON.stringify(objectClone);
};


