// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class PotTransfers implements ProtocolObject {
        public static const CLASSID:int = 28;

        public function classId():int {
            return PotTransfers.CLASSID;
        }

        public var fromPlayerToPot:Boolean;
        public var transfers:Array = new Array();
        public var pots:Array = new Array();

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveBoolean(fromPlayerToPot);
            ps.saveInt(transfers.length);
            var i:int;
            for( i = 0; i != transfers.length; i ++)
            {
                var _tmp_transfers:ByteArray = transfers[i].save();
                ps.saveArray(_tmp_transfers);
            }
            ps.saveInt(pots.length);
            for( i = 0; i != pots.length; i ++)
            {
                var _tmp_pots:ByteArray = pots[i].save();
                ps.saveArray(_tmp_pots);
            }
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            fromPlayerToPot = ps.loadBoolean();
            var i:int;
            var transfersCount:int = ps.loadInt();
            transfers = new Array();
            for( i = 0; i < transfersCount; i ++) {
                var _tmp1:PotTransfer  = new PotTransfer();
                _tmp1.load(buffer);
                transfers[i] = _tmp1;
            }
            var potsCount:int = ps.loadInt();
            pots = new Array();
            for( i = 0; i < potsCount; i ++) {
                var _tmp2:Pot  = new Pot();
                _tmp2.load(buffer);
                pots[i] = _tmp2;
            }
        }
        

        public function toString():String
        {
            var result:String = "PotTransfers :";
            result += " fromPlayerToPot["+fromPlayerToPot+"]" ;
            result += " transfers["+transfers+"]" ;
            result += " pots["+pots+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

