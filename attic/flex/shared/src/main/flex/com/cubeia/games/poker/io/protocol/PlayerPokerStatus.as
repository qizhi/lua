// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class PlayerPokerStatus implements ProtocolObject {
        public static const CLASSID:int = 31;

        public function classId():int {
            return PlayerPokerStatus.CLASSID;
        }

        public var player:int;
        public var status:uint;
        public var inCurrentHand:Boolean;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveInt(player);
            ps.saveUnsignedByte(status);
            ps.saveBoolean(inCurrentHand);
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            player = ps.loadInt();
            status = PlayerTableStatusEnum.makePlayerTableStatusEnum(ps.loadUnsignedByte());
            inCurrentHand = ps.loadBoolean();
        }
        

        public function toString():String
        {
            var result:String = "PlayerPokerStatus :";
            result += " player["+player+"]" ;
            result += " status["+status+"]" ;
            result += " in_current_hand["+inCurrentHand+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

