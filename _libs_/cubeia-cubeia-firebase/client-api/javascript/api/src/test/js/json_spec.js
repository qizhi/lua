describe('json serializer/deserializer sanity checks', function() {

	describe('serializing', function() {
		it('simple', function() {
			var object = { str: 'string', num: 12345 };
			var json = JSON.stringify(object);
			expect(json).toBe('{"str":"string","num":12345}');
		});
		
		it('complex', function() {
			var object = {
				id: 1234,
				persons: [
				    {name: 'gubben', age: 34, value: 56.78 },
				    {name: 'snubbe', value: null },
				]
			};
			var json = JSON.stringify(object);
			expect(json).toBe('{"id":1234,"persons":[{"name":"gubben","age":34,"value":56.78},{"name":"snubbe","value":null}]}');
		});
	});
	
	describe('deserializing', function() {
		it('simple', function() {
			var json = '{"str":"string","num":12345}';
			var object = JSON.parse(json);
			expect(object.str).toBe('string');
			expect(object.num).toBe(12345);
		});
		
		it('complex', function() {
			var json = '{"id":1234,"persons":[{"name":"gubben","age":34,"value":56.78},{"name":"snubbe","value":null}]}';
			var object = JSON.parse(json);
			expect(object.id).toBe(1234);
			var persons = object.persons;
			expect(persons.length).toBe(2);
			expect(persons[0].name).toBe('gubben');
			expect(persons[0].age).toBe(34);
			expect(persons[0].value).toBe(56.78);
			expect(persons[1].name).toBe('snubbe');
			expect(persons[1].age).toBe(undefined);
			expect(persons[1].value).toBe(null);
		});
	});
	
});