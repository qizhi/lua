describe('Styx ', function() {

    describe('protocol', function() {

        it('should be able to be saved and normalized', function() {
            var struct = new STYXTEST.First();
            struct.text = "test";
            struct.number = 112;
            var byteArray = struct.save();
            expect(struct.text).toBe("test");
            expect(struct.number).toBe(112);
            expect(struct.classId()).toBe(1);

            var norm = struct.getNormalizedObject();
            expect(norm.details["text"]).toBe("test");
            expect(norm.details["number"]).toBe(112);
        });

        it('second struct should work', function() {
            var struct = new STYXTEST.Second();
            var byteArray = struct.save();
            expect(struct.classId()).toBe(2);
            expect(struct.symbol).toBe(0);

            var norm = struct.getNormalizedObject();
            expect(norm.details["symbol"]).toBe("A");
        });

        it('third struct should work', function() {
            var struct = new STYXTEST.Third();
            var byteArray = struct.save();
            expect(struct.classId()).toBe(3);
            expect(struct.symbols.length).toBe(0);

            struct.symbols.push(2); // C
            struct.symbols.push(1); // B
            struct.symbols.push(0); // A
            byteArray = struct.save();
            expect(struct.symbols.length).toBe(3);

            var norm = struct.getNormalizedObject();
            expect(norm.details["symbols"][0]).toBe('C');
            expect(norm.details["symbols"][1]).toBe('B');
            expect(norm.details["symbols"][2]).toBe('A');
        });

        it('fourth struct should work', function() {
            var struct = new STYXTEST.Fourth();
            var byteArray = struct.save();
            expect(struct.classId()).toBe(4);
            var norm = struct.getNormalizedObject();
        });

        it('fifth struct should work', function() {
            var struct = new STYXTEST.Fifth();
            struct.texts.push("AA");
            struct.numbers.push(10);

            var byteArray = struct.save();
            expect(struct.classId()).toBe(5);
            var norm = struct.getNormalizedObject();
            expect(norm).not.toBeNull();
        });
    });

    describe('Firebase API', function() {

        it('should be able to wrap first struct', function() {
            var struct = new STYXTEST.First();
            struct.text = "test";
            struct.number = 112;

            var packet = FIREBASE.Styx.wrapInGameTransportPacket(11, 22, struct);
            expect(packet.data).not.toBeNull();
        });

        it('should be able to wrap second struct', function() {
            var struct = new STYXTEST.Second();

            var packet = FIREBASE.Styx.wrapInGameTransportPacket(11, 22, struct);
            expect(packet.data).not.toBeNull();
        });

        it('should be able to wrap third struct', function() {
            var struct = new STYXTEST.Third();
            struct.symbols.push(2); // C
            struct.symbols.push(1); // B
            struct.symbols.push(0); // A

            var packet = FIREBASE.Styx.wrapInGameTransportPacket(11, 22, struct);
            expect(packet.pid).toBe(11);
            expect(packet.tableid).toBe(22);
            expect(packet.data).not.toBeNull();
        });

        it('should be able to wrap fourth struct', function() {
            var struct = new STYXTEST.Fourth();

            var packet = FIREBASE.Styx.wrapInGameTransportPacket(11, 22, struct);
            expect(packet.pid).toBe(11);
            expect(packet.tableid).toBe(22);
            expect(packet.data).not.toBeNull();
        });

    });
});
