describe('cometd adapter', function() {

	it('sets up url and protocol on construction', function() {
		var cometd = { }
		cometd.unregisterTransport = function(name) { };
		cometd.configure = function(params) { }
		cometd.subscribe = function(channel, handler) { };
		cometd.addListener = function(channel, handler) { };
		var wsAdapter = new FIREBASE.CometdAdapter("example.org", 1234, "/firebase", true, function() { return cometd});
		expect(wsAdapter.protocol).toBe("https://");
		expect(wsAdapter.firebaseUrl).toBe("https://example.org:1234/firebase");
		expect(wsAdapter.cometd).toBe(cometd);
	});
});