// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class PotTransfer implements ProtocolObject {
        public static const CLASSID:int = 27;

        public function classId():int {
            return PotTransfer.CLASSID;
        }

        public var potId:int;
        public var playerId:int;
        public var amount:int;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveByte(potId);
            ps.saveInt(playerId);
            ps.saveInt(amount);
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            potId = ps.loadByte();
            playerId = ps.loadInt();
            amount = ps.loadInt();
        }
        

        public function toString():String
        {
            var result:String = "PotTransfer :";
            result += " pot_id["+potId+"]" ;
            result += " player_id["+playerId+"]" ;
            result += " amount["+amount+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

