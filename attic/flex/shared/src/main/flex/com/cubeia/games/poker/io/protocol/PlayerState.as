// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class PlayerState implements ProtocolObject {
        public static const CLASSID:int = 6;

        public function classId():int {
            return PlayerState.CLASSID;
        }

        public var player:int;
        public var cards:Array = new Array();
        public var balance:int;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveInt(player);
            ps.saveInt(cards.length);
            var i:int;
            for( i = 0; i != cards.length; i ++)
            {
                var _tmp_cards:ByteArray = cards[i].save();
                ps.saveArray(_tmp_cards);
            }
            ps.saveInt(balance);
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            player = ps.loadInt();
            var i:int;
            var cardsCount:int = ps.loadInt();
            cards = new Array();
            for( i = 0; i < cardsCount; i ++) {
                var _tmp1:GameCard  = new GameCard();
                _tmp1.load(buffer);
                cards[i] = _tmp1;
            }
            balance = ps.loadInt();
        }
        

        public function toString():String
        {
            var result:String = "PlayerState :";
            result += " player["+player+"]" ;
            result += " cards["+cards+"]" ;
            result += " balance["+balance+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

