// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class PlayerBalance implements ProtocolObject {
        public static const CLASSID:int = 21;

        public function classId():int {
            return PlayerBalance.CLASSID;
        }

        public var balance:int;
        public var pendingBalance:int;
        public var player:int;
        public var playersContributionToPot:int;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveInt(balance);
            ps.saveInt(pendingBalance);
            ps.saveInt(player);
            ps.saveInt(playersContributionToPot);
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            balance = ps.loadInt();
            pendingBalance = ps.loadInt();
            player = ps.loadInt();
            playersContributionToPot = ps.loadInt();
        }
        

        public function toString():String
        {
            var result:String = "PlayerBalance :";
            result += " balance["+balance+"]" ;
            result += " pendingBalance["+pendingBalance+"]" ;
            result += " player["+player+"]" ;
            result += " players_contribution_to_pot["+playersContributionToPot+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

