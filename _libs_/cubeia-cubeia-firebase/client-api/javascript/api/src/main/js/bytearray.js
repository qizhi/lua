/**
 * @fileOverview This file contains a utility class for encondig/decoding of the styx wire format
 * @author <a href="http://www.cubeia.org">Peter Lundh</a>
 * @version 1.0-SNAPSHOT
 */

/**
 * @author Peter Lundh
 */

var FIREBASE = FIREBASE || {};
var utf8 = utf8 || {};

/**
 *  @namespace FIREBASE	
 */
FIREBASE = FIREBASE || {};

/**
 * Creates a new ByteArray
 *  @class
 *	@param {byte[]} [array] array of bytes to wrap.
 *  @returns a new ByteArray object
 */
FIREBASE.ByteArray = function(array) {

	/**
	 * Max value for a 'long'. This is 2^53 for full precision.
	 */
	var MAX_SIGNED_INT64 = 9007199254740992;
	
	/**
	 * Min value for a 'long'. This is 2^53 for full precision.
	 */
	var MIN_SIGNED_INT64 = - MAX_SIGNED_INT64;
	
	/**
	 * max value for a 32 bit unsigned integer
	 * @constant
     * @type number
	 */
	var MAX_INT32 = 4294967295;
	
	/**
	 * max value for a 32 bit signed integer svn status
	 * @constant
     * @type number
	 */
	var MAX_SIGNED_INT32 =  2147483647;

	/**
	 * max value for a 16 bit unsigned integer
	 * @constant
     * @type number
	 */
	var MAX_INT16 = 65535;
	
	
	/**
	 * max value for a 16 bit signed integer
	 * @constant
     * @type number
	 */
	var MAX_SIGNED_INT16 =  32767;

	/**
	 * max value for an unsigned  byte
	 * @constant
     * @type number
	 */
	var MAX_BYTE = 255;
	
	/**
	 * max value for a signed  byte
	 * @constant
     * @type number
	 */
	var MAX_SIGNED_BYTE =  127;

    var HI32_VALUE = 0x100000000;

    var MAX_INT_64 = Math.pow(2, 53);

	/**
	 * Holds the array of bytes
	 * @private
	 */
	this.buffer = [];

	// wrap incoming array if provided
	if ( array ) {
		this.buffer = array;
	} else {
		this.buffer = [];
	}
	
	/**
	 * Current read position
	 * @type number
	 */
	this.position = 0;
	
	/**
	 * Return number of unread bytes
	 * @returns {number} remaining 
	 */
	this.remaining  = function() {
		return this.buffer.length - this.position;
	};
	
	/**
	 * Return wrapped buffer
	 * @returns {byte[]} buffer    
	 */
	this.getBuffer = function() {
		return this.buffer;
	};
	
	
	/**
	 * Read a boolean value from the stream
	 * @returns {Boolean}
	 */
	this.readBoolean = function() {
		this.checkBuffer(1);
		return this.readByte() !== 0;
	};
	
	
	/**
	 * Check if there is enough data in the buffer
	 * @param {Number} size number of bytes required
     * @throws {FIREBASE.FirebaseException} buffer underrun exception
	 */
	this.checkBuffer = function(size) {
		if ( this.remaining() < size ) {
			FIREBASE.FirebaseException.Throw(FIREBASE.ErrorCodes.BUFFER_UNDERRUN, "Buffer underrun");
		}
	};
	
	/**
	 * Read a 64 bit signed integer in network byte order (big endian)
	 * See documentation for readLong for constraints of the min and max value.
	 * @returns {number} 64 bit integer value 
	 */
	this.readLong = function() {
		this.checkBuffer(8);

        // Running sum of octets, doing a 2's complement
        var negate = this.buffer[this.position] & 0x80, x = 0, carry = 1;
        var i, v, m;
        for (i = 7, m = 1; i >= 0; i--, m *= 256) {
            v = this.buffer[i+this.position];
            // 2's complement for negative numbers
            if (negate) {
                v = (v ^ 0xff) + carry;
                carry = v >> 8;
                v = v & 0xff;
            }
            x += v * m;
        }
        if ( x > MAX_INT_64 ) {
            throw new RangeError("Precision lost when converting 64bit number");
        }
        this.position += 8;
        return negate ? -x : x;

	};
	
	/**
	 * Read a 32 bit signed integer in network byte order (big endian)
	 * @returns {number} 32 bit integer value 
	 */
	this.readInt = function() {
		this.checkBuffer(4);
		var value = (this.buffer[this.position++] << 24) ;
		value += (this.buffer[this.position++] << 16) ;
		value += (this.buffer[this.position++] << 8) ;
		value += this.buffer[this.position++];
		if ( value > MAX_SIGNED_INT32 ) {
			value -= MAX_INT32 + 1;
		}
		return value;
	};
	
	/**
	 * Read a 16 bit signed integer in network byte order (big endian)
	 * @returns {number} 16 bit integer value 
	 */
	this.readShort = function() {
		this.checkBuffer(2);
		var value = (this.buffer[this.position++] << 8) ;
		value += this.buffer[this.position++];
		if ( value > MAX_SIGNED_INT16 ) {
			value -= MAX_INT16 + 1;
		}
		return value;
	};

	/**
	 * Read a signed byte 
	 * @returns {number} byte value 
	 */
	this.readByte = function() {
		this.checkBuffer(1);
		var value = this.buffer[this.position++];
		if ( value > MAX_SIGNED_BYTE ) {
			value -= MAX_BYTE + 1;
		}
		return value;
	};


	/**
	 * Read a 32 bit unsigned integer in network byte order (big endian)
	 * @returns {number} unsigned 32 bit integer value 
	 */
	this.readUnsignedInt = function() {
		var value = this.readInt();
		if ( value < 0 ) {
			value += MAX_INT32 + 1;
		}
		return value;
	};

	/**
	 * Read a 16 bit unsigned integer in network byte order (big endian)
	 * @returns {number} unsigned 16 bit integer value 
	 */
	this.readUnsignedShort = function() {
		var value = this.readShort();
		if ( value < 0 ) {
			value += MAX_INT16 + 1;
		}
		return value;
	};


	/**
	 * Read an unsigned byte 
	 * @returns {number} unsigned byte value 
	 */
	this.readUnsignedByte = function() {
		var value = this.readByte();
		if ( value < 0 ) {
			value += MAX_BYTE + 1;
		}
		return value;
	};
	
	/**
	 * Read a string with a 16 bit integer length prefix 
	 * @returns {String} string value 
	 */
	this.readString = function() {
		// we should have at least two bytes left (string length)
		this.checkBuffer(2);
		var length = this.readShort();
		var byteArray = this.buffer.splice(this.position, length);
	    return utf8.fromByteArray(byteArray);	
	};

	/**
	 * Write a boolean value to the stream
	 * @param {Boolean} value boolean value to write
	 */
	this.writeBoolean = function(value) {
		this.writeByte(value === true ? 1: 0);
	};
	
	/**
	 * Write a string with a 16 bit integer length prefix 
	 * @param {String} str string to write 
	 */
	this.writeString = function(str) {
		var i;
		var byteArray = utf8.toByteArray(str);
		this.writeUnsignedShort(byteArray.length);
		for (i = 0; i < byteArray.length; i++) {
			this.writeUnsignedByte(byteArray[i]);
		}
	};

    this.twoCompliment = function(buffer) {
        var carry = 1;
        var v, i;
        for (i = 7; i >= 0; i--) {
            v = (buffer[i] ^ 0xff) + carry;
            buffer[i] = v & 0xff;
            carry = v >> 8;
        }
    };
    
	/**
	 * Write a 64 bit signed integer in network byte order (big endian).
	 * Note that Javascript can't handle values outside +/- 2^53 without losing precision. If a value
	 * is given outside this range an RangeError will be thrown.
	 * @param {number} value 53 bit integer value, if value is outside +/- 2^53 a RangeError will be thrown.
	 */
	this.writeLong = function(value) {
		var negative;
		var buffer = [0,0,0,0,0,0,0,0];
		var lowPart;
		var i;
		
		if (value > MAX_SIGNED_INT64) {
            throw new RangeError("value to big for long");
		} else if (value < MIN_SIGNED_INT64) {
            throw new RangeError("value to small for long");
		}
		
        negative = value < 0;
        value = Math.abs(value);
        lowPart = value % HI32_VALUE;
        value = value / HI32_VALUE;
        value = value | 0;

        for (i = 7; i >= 0; i--) {
            buffer[i] = (lowPart & 0xff);
            lowPart = i === 4 ? value : lowPart >>> 8;
        }

        if (negative) {
            this.twoCompliment(buffer);
        }

        for (i = 0; i < buffer.length; i ++ ) {
            this.buffer.push(buffer[i]);
        }


    };
	/**
	 * Write a 32 bit signed integer in network byte order (big endian)
	 * @param {number} value 32 bit integer value 
	 */
	this.writeInt = function(value) {
        var byte1 = (value & 0xFF000000) >> 24;

        if ( byte1 < 0 ) {
            byte1 += 256;
        }

		this.buffer.push(byte1);
		this.buffer.push((value & 0x00FF0000) >> 16);
		this.buffer.push((value & 0x0000FF00) >> 8);
		this.buffer.push(value & 0x000000FF);
	};
	
	/**
	 * Write a 16 bit signed integer in network byte order (big endian)
	 * @param {number} value 16 bit integer value 
	 */
	this.writeShort = function(value) {
		this.buffer.push(((value & 0xFF00) >> 8 ));
		this.buffer.push(value & 0x00FF);
	};

	/**
	 * Write an 8 bit byte
	 * @param {number} value  
	 */
	this.writeByte = function (value) {
		this.buffer.push(value & 0xFF);
	};
	
	
	/**
	 * Write a 32 bit unsigned integer in network byte order (big endian)
	 * @param {number} value 32 bit unsigned integer value 
	 */
	this.writeUnsignedInt = function(value) {
		if ( value < 0 ) {
			value += MAX_INT32;
		}
		this.writeInt(value);
	};
	
	/**
	 * Write a 16 bit unsigned integer in network byte order (big endian)
	 * @param {number} value 16 bit unsigned integer value 
	 */
	this.writeUnsignedShort = function(value) {
		value &= 0xffff;
		if ( value < 0 ) {
			value += MAX_INT16;
		}
		this.writeShort(value);
	};
	
	/**
	 * Write an 8 bit unsigned byte 
	 * @param {number} value unsigned byte
	 */
	this.writeUnsignedByte = function(value) {
		value &= 0xff;
		if ( value < 0 ) {
			value += MAX_BYTE;
		}
		this.writeByte(value);
	};

    /**
     * Create service data array
     * @param classId
     */
    this.createServiceDataArray = function(classId) {
        return this.createGameDataArray(classId);
    };

    /**
     * Create a data array
     */
    this.createDataArray = function() {
        var adata = new FIREBASE.ByteArray();
        adata.writeUnsignedInt(this.buffer.length);
        var arrayToSend = adata.buffer.concat(this.buffer);
        return arrayToSend;
    };



	/**
	 * Create a game data array
	 * @param {Number} classId 
	 */
	this.createGameDataArray = function(classId) {
		var header = new FIREBASE.ByteArray();
		header.writeUnsignedInt(this.buffer.length);
		header.writeUnsignedByte(classId);
		var arrayToSend = header.buffer.concat(this.buffer);
		return arrayToSend;
	};
	
	/**
	 * Write a byte array into buffer
	 * @param {FIREBASE.ByteArray} byteArray;
	 */
	this.writeArray = function(byteArray) {
        var source;
        source = (byteArray instanceof Array && byteArray.length > 0) ? new FIREBASE.ByteArray(byteArray) : byteArray;

		var newBuffer = this.buffer.concat(source.buffer);
		this.buffer = newBuffer;
	};


    /**
     * Read an array of bytes
     * @param {Number} count number of bytes to read
     * @return {Array}
     */
    this.readArray = function(count) {
        var i, len, element, target = [];
        len = count || this.remaining();

        for (i = 0; i < len; i ++ ) {
            element = this.readUnsignedByte();
            if ( typeof element !== "undefined" ) {
                target.push(element);
            }
        }
        return target;
    };
};



/**
 * Convert a base64 encoded string into an array of bytes
 * @param {String} input base64 encoded string 
 * @returns {Array}
 */
FIREBASE.ByteArray.fromBase64String = function(input) {
	var result = [];
	// private property
	var _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

	var chr1, chr2, chr3;
	var enc1, enc2, enc3, enc4;
	var i = 0;

	input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

	while (i < input.length) {

		enc1 = _keyStr.indexOf(input.charAt(i++));
		enc2 = _keyStr.indexOf(input.charAt(i++));
		enc3 = _keyStr.indexOf(input.charAt(i++));
		enc4 = _keyStr.indexOf(input.charAt(i++));

		chr1 = (enc1 << 2) | (enc2 >> 4);
		chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
		chr3 = ((enc3 & 3) << 6) | enc4;

		result.push(chr1);

		if (enc3 !== 64) {
			result.push(chr2);
		}
		if (enc4 !== 64) {
			result.push(chr3);
		}

	}

	return result;

};

/**
 * Convert an array of bytes into a base64 encoded string 
 * @param {Array} input an array of bytes to be converted 
 * @returns {String} base64 encoded string
 */
FIREBASE.ByteArray.toBase64String = function(input) {
	var output = "";
	var _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

	var i = 0;
	var current = 0;
	var previous = 0;
	var byteNum = 0;

	while (i < input.length) {
		current = input[i] < 0 ? input[i] + 256 : input[i];

		byteNum = i % 3;

		switch (byteNum) {
			case 0: //first byte
				output += _keyStr.charAt(current >> 2);
				break;
	
			case 1: //second byte
				output += _keyStr.charAt((previous & 3) << 4 | (current >> 4));
				break;
	
			case 2: //third byte
				output += _keyStr.charAt((previous & 0x0f) << 2 | (current >> 6));
				output += _keyStr.charAt(current & 0x3f);
				break;
		}

		previous = current;
		i++;
	}

	if (byteNum === 0) {
		output += _keyStr.charAt((previous & 3) << 4);
		output += "==";
	} else if (byteNum === 1) {
		output += _keyStr.charAt((previous & 0x0f) << 2);
		output += "=";
	}
	return output;
};


