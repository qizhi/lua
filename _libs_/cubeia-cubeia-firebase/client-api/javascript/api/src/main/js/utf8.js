/**
 * @namespace FIREBASE
 */
var utf8 = utf8 || {};

utf8.toByteArray = function(str) {
	var i, j;
	var bytes = [];
	for (i = 0; i < str.length; i++) {
		if (str.charCodeAt(i) <= 0x7F) {
			bytes.push(str.charCodeAt(i));
		} else {
			var h = encodeURIComponent(str.charAt(i)).substr(1).split('%');
			for (j = 0; j < h.length; j++) {
				bytes.push(parseInt(h[j], 16));
			}
		}
	}
	return bytes;
};

utf8.fromByteArray = function(bytes) {
	var i;
	var str = '';
	for (i = 0; i < bytes.length; i++) {
		if (bytes[i] <= 0x7F) {
			if (bytes[i] === 0x25) {
				str += "%25";
			} else {
				str += String.fromCharCode(bytes[i]);
			}
		} else {
			str += "%" + bytes[i].toString(16).toUpperCase();
		}
	}
	return decodeURIComponent(str);
};