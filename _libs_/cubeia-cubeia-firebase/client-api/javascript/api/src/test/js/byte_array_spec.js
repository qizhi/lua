describe('the byte array object', function() {
	
	describe('initialization', function() {
		it('should be empty by default', function() {
			var byteArray = new FIREBASE.ByteArray();
			expect(byteArray.getBuffer().length).toBe(0);
		});
		
		it('should wrap given array if given', function() {
			var array = [100, 200, 300];
			var byteArray = new FIREBASE.ByteArray(array);
			var wrappedBuffer = byteArray.getBuffer();
			expect(wrappedBuffer.length).toBe(array.length);
			expect(wrappedBuffer[0]).toBe(100);
			expect(wrappedBuffer[1]).toBe(200);
			expect(wrappedBuffer[2]).toBe(300);
		});
	});
		
	describe('bytes', function() {
		it('unsigned byte', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeUnsignedByte(213);
			expect(byteArray.getBuffer().length).toBe(1);
			expect(byteArray.readUnsignedByte()).toBe(213);
		});
		
		it('too big should truncated', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeUnsignedByte(257);
			expect(byteArray.getBuffer().length).toBe(1);
			expect(byteArray.readUnsignedByte()).toBe(1);
		});
		
		it('signed byte', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeByte(-3);
			expect(byteArray.getBuffer().length).toBe(1);
			expect(byteArray.readByte()).toBe(-3);
		});
	});


    describe('boolean', function() {
        it('should read the same sequence of booleans as it writes', function() {   // readBoolean
            var byteArray = new FIREBASE.ByteArray();
            var firstValue = true;
            var secondValue = false;
            byteArray.writeBoolean(firstValue);
            byteArray.writeBoolean(secondValue);
            expect(byteArray.readBoolean()).toBe(firstValue);
            expect(byteArray.readBoolean()).toBe(secondValue);
        });
    });

    describe('write array (recursive)', function() {
        it('should read the same arrays data as it writes', function() {   // readBoolean
            var byteArray = new FIREBASE.ByteArray();
            var firstByteArray = new FIREBASE.ByteArray();
            var secondByteArray = new FIREBASE.ByteArray();
            var firstValue = true;
            var secondValue = false;
            var thirdValue = 123;

            firstByteArray.writeBoolean(firstValue);
            secondByteArray.writeUnsignedByte(thirdValue);
            firstByteArray.writeBoolean(secondValue);
            byteArray.writeArray(firstByteArray);
            byteArray.writeArray(secondByteArray);

            expect(byteArray.readBoolean()).toBe(firstValue);
            expect(byteArray.readBoolean()).toBe(secondValue);
            expect(byteArray.readUnsignedByte()).toBe(thirdValue);
        });

    });

    describe('game data array', function() {
        it('should find that the last value of a game data array is the classId', function() {   // readBoolean
            var byteArray = new FIREBASE.ByteArray();
            var classId = 82;
            var returnedArray = byteArray.createGameDataArray(classId);
            expect(returnedArray[returnedArray.length - 1]).toBe(classId);
            var byteArray2 = new FIREBASE.ByteArray(returnedArray);
            expect(byteArray2.readUnsignedInt()).toBe(0);
            expect(byteArray2.readUnsignedByte()).toBe(82);
        });
    });

    describe('base 64 string', function() {
        it('result of input aGVqaG9wcA== to fromBase64String should match hejhopp', function() {
            var string = "aGVqaG9wcA==";
            var result = FIREBASE.ByteArray.fromBase64String(string);
            var recreatedWord = "";
            for (var i = 0; i < result.length; i++) {
                recreatedWord += String.fromCharCode(result[i]);
            }
            var word = "hejhopp";
            expect(recreatedWord).toBe(word);
        });

        it('result of input arrayOfBytes toBase64String should match aGVqaG9wcA==', function() {
            var arrayOfBytes = [104, 101, 106, 104, 111, 112, 112];
            var result = FIREBASE.ByteArray.toBase64String(arrayOfBytes);
            var string = "aGVqaG9wcA==";
            expect(result).toBe(string);
        });

        it('should handle UUID:s', function() {
            // 7a4023d5-c69e-4377-98c7-d3d0b5a4ff46
            var arrayOfBytes = [55, 97, 52, 48, 50, 51, 100, 53, 45, 99, 54, 57, 101, 45, 52, 51, 55, 55, 45, 57, 56, 99, 55, 45, 100, 51, 100, 48, 98, 53, 97, 52, 102, 102, 52, 54];
            var result = FIREBASE.ByteArray.toBase64String(arrayOfBytes);
            var string = "N2E0MDIzZDUtYzY5ZS00Mzc3LTk4YzctZDNkMGI1YTRmZjQ2";
            // var string = "AAAALgEAAAAA////wAkN2E0MDIzZDUtYzY5ZS00Mzc3LTk4YzctZDNkMGI1YTRmZjQ2";
            expect(result).toBe(string);
        });

    });

	describe('shorts', function() {
		it('unsigned short', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeUnsignedShort(40000);
			expect(byteArray.getBuffer().length).toBe(2);
			expect(byteArray.readUnsignedShort()).toBe(40000);
		});
		
		it('too big should truncated', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeUnsignedShort(65541);
			expect(byteArray.getBuffer().length).toBe(2);
			expect(byteArray.readUnsignedShort()).toBe(5);
		});
		
		it('signed byte', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeShort(-30000);
			expect(byteArray.getBuffer().length).toBe(2);
			expect(byteArray.readShort()).toBe(-30000);
		});
	});
	
	describe('ints', function() {
		it('unsigned int', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeUnsignedInt(4000000000);
			expect(byteArray.getBuffer().length).toBe(4);
			expect(byteArray.readUnsignedInt()).toBe(4000000000);
		});
		
		it('too big should truncated', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeUnsignedInt(4294967296 + 5);
			expect(byteArray.getBuffer().length).toBe(4);
			expect(byteArray.readUnsignedInt()).toBe(5);
		});
		
		it('signed int', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeInt(2000000000);
			expect(byteArray.getBuffer().length).toBe(4);
			expect(byteArray.readInt()).toBe(2000000000);
		});
		
		it('signed int 1', function() {
			var bytes = [0,0,0,5];
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeInt(5);
			expect(byteArray.getBuffer().length).toBe(4);
			expect(byteArray.getBuffer()).toEqual(bytes);
		});
		
		it('signed negative int', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeInt(-2000000000);
			expect(byteArray.getBuffer().length).toBe(4);
			expect(byteArray.readInt()).toBe(-2000000000);
		});

        it('signed int -1', function() {

            var byteArray = new FIREBASE.ByteArray();
            byteArray.writeInt(-1);
            var test = FIREBASE.ByteArray.toBase64String(byteArray.getBuffer());
            expect(test).toBe("/////w==");
            var bytes2 = FIREBASE.ByteArray.fromBase64String(test);
            expect(byteArray.getBuffer()).toEqual([255,255,255,255]);
            expect(byteArray.readInt()).toEqual(-1);
        });

        it('signed short -1', function() {

            var byteArray = new FIREBASE.ByteArray();
            byteArray.writeShort(-1);
            var test = FIREBASE.ByteArray.toBase64String(byteArray.getBuffer());
            expect(test).toBe("//8=");
            var bytes2 = FIREBASE.ByteArray.fromBase64String(test);
            expect(byteArray.getBuffer()).toEqual([255,255]);
            expect(byteArray.readShort()).toEqual(-1);
        });

        it('signed short -2', function() {

            var byteArray = new FIREBASE.ByteArray();
            byteArray.writeShort(-2);
            var test = FIREBASE.ByteArray.toBase64String(byteArray.getBuffer());
            expect(test).toBe("//4=");
            var bytes2 = FIREBASE.ByteArray.fromBase64String(test);
            expect(byteArray.getBuffer()).toEqual([255,254]);
            expect(byteArray.readShort()).toEqual(-2);
        });

        it('signed byte -1', function() {

            var byteArray = new FIREBASE.ByteArray();
            byteArray.writeByte(-1);
            var test = FIREBASE.ByteArray.toBase64String(byteArray.getBuffer());
            expect(test).toBe("/w==");
            var bytes2 = FIREBASE.ByteArray.fromBase64String(test);
            expect(byteArray.getBuffer()).toEqual([255]);
            expect(byteArray.readByte()).toEqual(-1);

        });

        it('signed int max', function() {

            var byteArray = new FIREBASE.ByteArray();
            byteArray.writeInt(2147483647);
            var test = FIREBASE.ByteArray.toBase64String(byteArray.getBuffer());
            expect(test).toBe("f////w==");
            var bytes2 = FIREBASE.ByteArray.fromBase64String(test);
            expect(byteArray.getBuffer()).toEqual([0x7f,0xff,0xff,0xff]);
            expect(byteArray.readInt()).toEqual(2147483647);

        });

        it('signed int min', function() {

            var byteArray = new FIREBASE.ByteArray();
            byteArray.writeInt(-2147483648);
            var test = FIREBASE.ByteArray.toBase64String(byteArray.getBuffer());
            expect(test).toBe("gAAAAA==");
            var bytes2 = FIREBASE.ByteArray.fromBase64String(test);
            expect(byteArray.getBuffer()).toEqual([0x80,0x00,0x00,0x00]);
            expect(byteArray.readInt()).toEqual(-2147483648);

        });
    });
	
	describe('longs', function() {
		/* Note that javascript can't support full precision of numbers bigger than 2^53
		 * 
		 * http://stackoverflow.com/questions/307179/what-is-javascripts-max-int-whats-the-highest-integer-value-a-number-can-go-t
		 */
		
		var MAX_NUM = 9007199254740992;
		var MIN_NUM = - MAX_NUM;
		
		it('signed long positive', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeLong(1234567890);
			expect(byteArray.getBuffer().length).toBe(8);
			expect(byteArray.readLong()).toBe(1234567890);
		});
		
		it('signed long negative', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeLong(-1234567890);
			expect(byteArray.getBuffer().length).toBe(8);
			expect(byteArray.readLong()).toBe(-1234567890);
		});
		
		it('signed zero', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeLong(0);
			expect(byteArray.getBuffer().length).toBe(8);
			expect(byteArray.readLong()).toBe(0);
		});
		
		it('signed long max', function() {
			var byteArray = new FIREBASE.ByteArray();
			
			byteArray.writeLong(MAX_NUM);
			expect(byteArray.getBuffer().length).toBe(8);
			expect(byteArray.readLong()).toBe(MAX_NUM);
		});
		
		it('signed long max - 1', function() {
			var byteArray = new FIREBASE.ByteArray();
			
			byteArray.writeLong(MAX_NUM - 1);
			expect(byteArray.getBuffer().length).toBe(8);
			expect(byteArray.readLong()).toBe(MAX_NUM - 1);
		});
		
		it('signed long min', function() {
			var byteArray = new FIREBASE.ByteArray();
			
			byteArray.writeLong(MIN_NUM);
			expect(byteArray.getBuffer().length).toBe(8);
			expect(byteArray.readLong()).toBe(MIN_NUM);
		});
		
		it('signed long min + 1', function() {
			var byteArray = new FIREBASE.ByteArray();
			
			byteArray.writeLong(MIN_NUM + 1);
			expect(byteArray.getBuffer().length).toBe(8);
			expect(byteArray.readLong()).toBe(MIN_NUM + 1);
		});
		
		it('too big should throw error', function() {
			var byteArray = new FIREBASE.ByteArray();
			expect(function() { byteArray.writeLong(90071992547409920000) }).toThrow(new RangeError("value to big for long"));
		});
		
		it('too small should throw error', function() {
			var byteArray = new FIREBASE.ByteArray();
			expect(function() { byteArray.writeLong(-90071992547409920000) }).toThrow(new RangeError("value to small for long"));
		});
    });
	
	
	describe('strings', function() {
		it('ASCII', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeString("gubbe");
			expect(byteArray.getBuffer().length).toBe(5 + 2);
			expect(byteArray.readString()).toBe("gubbe");
		});
		
		it('UTF-8', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeString("Räksmörgås");
			expect(byteArray.getBuffer().length).toBe(10 + 3 + 2);
			expect(byteArray.readString()).toBe("Räksmörgås");
		});
		
		it('UTF-8 special characters', function() {
			var byteArray = new FIREBASE.ByteArray();
			byteArray.writeString("%!\\¤?$&");
			expect(byteArray.readString()).toBe("%!\\¤?$&");
		});
	});
	
	describe('sequences', function() {
		it('mixed data', function() {
			var byteArray = new FIREBASE.ByteArray();
			
			byteArray.writeByte(-12);
			byteArray.writeUnsignedByte(42);
			byteArray.writeShort(-31000);
			byteArray.writeUnsignedShort(31000);
			byteArray.writeInt(-2000000000);
			byteArray.writeUnsignedInt(2000000000);
			byteArray.writeString("gubbe");
			
			expect(byteArray.getBuffer().length).toBe(1 + 1 + 2 + 2 + 4 + 4 + 5 + 2);
			expect(byteArray.readByte()).toBe(-12);
			expect(byteArray.readUnsignedByte()).toBe(42);
			expect(byteArray.readShort()).toBe(-31000);
			expect(byteArray.readUnsignedShort()).toBe(31000);
			expect(byteArray.readInt()).toBe(-2000000000);
			expect(byteArray.readUnsignedInt()).toBe(2000000000);
			expect(byteArray.readString()).toBe("gubbe");
		});
	});
	
	describe('base64 support', function() {
		it('test encode and decode', function() {
			var i, j, testData, resultData, base64String;
			
			for ( i = 1; i <= 10; i ++) {
				testData = [];
				for ( j = 0; j < i; j ++) {
					testData.push(j);
				}
				base64String = FIREBASE.ByteArray.toBase64String(testData);
				resultData = FIREBASE.ByteArray.fromBase64String(base64String);
				expect(resultData).toEqual(testData);
			}
		});
	});
	
	describe('error handling', function() {
		it('read byte buffer underrun if empty', function() {
			var byteArray = new FIREBASE.ByteArray();
			var firebaseException = new FIREBASE.FirebaseException(FIREBASE.ErrorCodes.BUFFER_UNDERRUN, "Buffer underrun");
			expect(function() {byteArray.readByte();}).toThrow(firebaseException);
		});
		
		it('read short buffer underrun if empty', function() {
			var byteArray = new FIREBASE.ByteArray();
            var firebaseException = new FIREBASE.FirebaseException(FIREBASE.ErrorCodes.BUFFER_UNDERRUN, "Buffer underrun");
            expect(function() {byteArray.readShort();}).toThrow(firebaseException);

		});
		
		it('read int buffer underrun if empty', function() {
			var byteArray = new FIREBASE.ByteArray();
            var firebaseException = new FIREBASE.FirebaseException(FIREBASE.ErrorCodes.BUFFER_UNDERRUN, "Buffer underrun");
            expect(function() {byteArray.readInt();}).toThrow(firebaseException);
		});
		
		it('read string buffer underrun if empty', function() {
			var byteArray = new FIREBASE.ByteArray();
            var firebaseException = new FIREBASE.FirebaseException(FIREBASE.ErrorCodes.BUFFER_UNDERRUN, "Buffer underrun");
            expect(function() {byteArray.readString();}).toThrow(firebaseException);
		});
	});

    describe('array manipulation', function() {
        it('test read and write array', function() {
            var i, j, testData, resultData, byteArray;

            for ( i = 1; i <= 10; i ++) {
                testData = [];
                byteArray = new FIREBASE.ByteArray();
                for ( j = 0; j < i; j ++) {
                    testData.push(j);
                }
                byteArray.writeArray(testData);
                resultData = byteArray.readArray();
                expect(resultData).toEqual(testData);
            }
        });

        it('test read and write empty array', function() {
            var testData, resultData, byteArray;
            testData = [];
            byteArray = new FIREBASE.ByteArray();
            byteArray.writeArray(testData);
            resultData = byteArray.readArray();
            expect(resultData).toEqual(testData);
        });

        it('test read and write array with count', function() {
            var testData, resultData, byteArray;
            testData = [1,2,3,4];
            byteArray = new FIREBASE.ByteArray();
            byteArray.writeArray(testData);
            resultData = byteArray.readArray(3);
            expect(resultData).toEqual([1,2,3]);
        });
    });

});