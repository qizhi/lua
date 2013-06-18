// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class PlayerDisconnectedPacket implements ProtocolObject {
        public static const CLASSID:int = 37;

        public function classId():int {
            return PlayerDisconnectedPacket.CLASSID;
        }

        public var playerId:int;
        public var timebank:int;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveInt(playerId);
            ps.saveInt(timebank);
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            playerId = ps.loadInt();
            timebank = ps.loadInt();
        }
        

        public function toString():String
        {
            var result:String = "PlayerDisconnectedPacket :";
            result += " player_id["+playerId+"]" ;
            result += " timebank["+timebank+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

