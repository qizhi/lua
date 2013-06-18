// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class ErrorPacket implements ProtocolObject {
        public static const CLASSID:int = 2;

        public function classId():int {
            return ErrorPacket.CLASSID;
        }

        public var code:uint;
        public var referenceId:String;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveUnsignedByte(code);
            ps.saveString(referenceId);
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            code = ErrorCodeEnum.makeErrorCodeEnum(ps.loadUnsignedByte());
            referenceId = ps.loadString();
        }
        

        public function toString():String
        {
            var result:String = "ErrorPacket :";
            result += " code["+code+"]" ;
            result += " reference_id["+referenceId+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

