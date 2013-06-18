// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class DeckInfo implements ProtocolObject {
        public static const CLASSID:int = 35;

        public function classId():int {
            return DeckInfo.CLASSID;
        }

        public var size:int;
        public var rankLow:uint;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveInt(size);
            ps.saveUnsignedByte(rankLow);
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            size = ps.loadInt();
            rankLow = RankEnum.makeRankEnum(ps.loadUnsignedByte());
        }
        

        public function toString():String
        {
            var result:String = "DeckInfo :";
            result += " size["+size+"]" ;
            result += " rank_low["+rankLow+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

