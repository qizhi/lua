// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class BuyInRequest implements ProtocolObject {
        public static const CLASSID:int = 24;

        public function classId():int {
            return BuyInRequest.CLASSID;
        }

        public var amount:int;
        public var sitInIfSuccessful:Boolean;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveInt(amount);
            ps.saveBoolean(sitInIfSuccessful);
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            amount = ps.loadInt();
            sitInIfSuccessful = ps.loadBoolean();
        }
        

        public function toString():String
        {
            var result:String = "BuyInRequest :";
            result += " amount["+amount+"]" ;
            result += " sit_in_if_successful["+sitInIfSuccessful+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

