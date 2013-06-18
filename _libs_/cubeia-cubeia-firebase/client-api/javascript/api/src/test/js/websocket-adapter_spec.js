describe('websocket adapter', function() {

	it('sets up url and protocol on construction', function() {
		var wsAdapter = new FIREBASE.WebSocketAdapter("example.org", 1234, "firebase", true);
		expect(wsAdapter.protocol).toBe("wss://");
		expect(wsAdapter.url).toBe("wss://example.org:1234/firebase");
	});
	
	it('creates a web socket and callbacks on connect', function() {
		WebSocket = function() {};
		var wsAdapter = new FIREBASE.WebSocketAdapter("example.org", 1234, "firebase", false);
		var status = undefined;
		
		wsAdapter.connect(function(s) { status = s; }, function(data) {});
		expect(status).toBe(FIREBASE.ConnectionStatus.CONNECTING);
		expect(wsAdapter.url).toBe("ws://example.org:1234/firebase");
		expect(wsAdapter.getSocket().onopen).toBeDefined();
		expect(wsAdapter.getSocket().onmessage).toBeDefined();
		expect(wsAdapter.getSocket().onclose).toBeDefined();
	});
	
	it('creates sane callbacks on websocket', function() {
		WebSocket = function() {};
		var wsAdapter = new FIREBASE.WebSocketAdapter("example.org", 1234, "firebase", false);
		var status = undefined;
		var data = undefined;
		
		wsAdapter.connect(function(s) { status = s; }, function(d) { data = d; });
		expect(status).toBe(FIREBASE.ConnectionStatus.CONNECTING);
		
		wsAdapter.getSocket().onmessage("1337 h4xx0r");
		expect(data).toBe("1337 h4xx0r");
		
		wsAdapter.getSocket().onclose();
		expect(status).toBe(FIREBASE.ConnectionStatus.DISCONNECTED);
	});
	
	it('sends a message on the socket when send is called', function() {
		WebSocket = function() {
			this.send = function(msg) {
				this.lastMessage = msg;
			};
		};
		var wsAdapter = new FIREBASE.WebSocketAdapter("example.org", 1234, "firebase", false);
		wsAdapter.connect(function(s) {}, function(d) {});
		
		wsAdapter.send("message");
		expect(wsAdapter.getSocket().lastMessage).toBe("message");
	});
	
});