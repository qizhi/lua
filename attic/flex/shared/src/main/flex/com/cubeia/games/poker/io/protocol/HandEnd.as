// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class HandEnd implements ProtocolObject {
        public static const CLASSID:int = 15;

        public function classId():int {
            return HandEnd.CLASSID;
        }

        public var playerIdRevealOrder:Array = new Array();
        public var hands:Array = new Array();
        public var potTransfers:PotTransfers;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveInt(playerIdRevealOrder.length);
            var i:int;
            for( i = 0; i != playerIdRevealOrder.length; i ++)
            {
                ps.saveInt(playerIdRevealOrder[i]);
            }
            ps.saveInt(hands.length);
            for( i = 0; i != hands.length; i ++)
            {
                var _tmp_hands:ByteArray = hands[i].save();
                ps.saveArray(_tmp_hands);
            }
            ps.saveArray(potTransfers.save());
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            var i:int;
            var playerIdRevealOrderCount:int = ps.loadInt();
            playerIdRevealOrder = new Array();
            for (i = 0; i < playerIdRevealOrderCount; i ++)
            {
                playerIdRevealOrder[i] = ps.loadInt();
            }
            var handsCount:int = ps.loadInt();
            hands = new Array();
            for( i = 0; i < handsCount; i ++) {
                var _tmp1:BestHand  = new BestHand();
                _tmp1.load(buffer);
                hands[i] = _tmp1;
            }
            potTransfers = new PotTransfers();
            potTransfers.load(buffer);
        }
        

        public function toString():String
        {
            var result:String = "HandEnd :";
            result += " player_id_reveal_order["+playerIdRevealOrder+"]" ;
            result += " hands["+hands+"]" ;
            result += " pot_transfers["+potTransfers+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

