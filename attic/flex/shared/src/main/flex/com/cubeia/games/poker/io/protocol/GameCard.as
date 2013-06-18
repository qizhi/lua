// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class GameCard implements ProtocolObject {
        public static const CLASSID:int = 4;

        public function classId():int {
            return GameCard.CLASSID;
        }

        public var cardId:int;
        public var suit:uint;
        public var rank:uint;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveInt(cardId);
            ps.saveUnsignedByte(suit);
            ps.saveUnsignedByte(rank);
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            cardId = ps.loadInt();
            suit = SuitEnum.makeSuitEnum(ps.loadUnsignedByte());
            rank = RankEnum.makeRankEnum(ps.loadUnsignedByte());
        }
        

        public function toString():String
        {
            var result:String = "GameCard :";
            result += " card_id["+cardId+"]" ;
            result += " suit["+suit+"]" ;
            result += " rank["+rank+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

