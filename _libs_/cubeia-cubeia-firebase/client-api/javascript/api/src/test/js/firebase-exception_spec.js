
describe('the firebase exception object', function() {
	
	describe('initialization', function() {
		it('should be initialized properly', function() {
			var firebaseException = new FIREBASE.FirebaseException(0xaa55, "hello");
			expect(firebaseException.code).toBe(0xaa55);
			expect(firebaseException.name).toBe("FIREBASE.FirebaseException");
			expect(firebaseException.message).toBe("hello");
		});
	});
	
	describe('throw', function() {
		it('exception should be thrown', function() {
			var firebaseException = new FIREBASE.FirebaseException(0xaa55, "hello");
			expect(function() {FIREBASE.FirebaseException.Throw(0xaa55, "hello");}).toThrow(firebaseException);
			
		});
	});
});
	
	
	