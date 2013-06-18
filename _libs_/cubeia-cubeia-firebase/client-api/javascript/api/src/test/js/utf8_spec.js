describe('the utf8 object', function() {
	
	describe('Test to bytes' , function() {
		var str = "Да!";
		var control = [ 208, 148, 208, 176, 33 ];
		var bytes = utf8.toByteArray(str);
		expect(bytes.length).toBe(control.length);
		var i;
		for (i = 0; i < bytes.length; i++) {
			expect(bytes[i]).toBe(control[i]);
		}
	});
	
	describe('Test from bytes' , function() {
		var control = "Да!";
		var bytes = [ 208, 148, 208, 176, 33 ];
		var test = utf8.fromByteArray(bytes);
		expect(test).toBe(control);
	});
});