// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class RakeInfo implements ProtocolObject {
        public static const CLASSID:int = 30;

        public function classId():int {
            return RakeInfo.CLASSID;
        }

        public var totalPot:int;
        public var totalRake:int;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveInt(totalPot);
            ps.saveInt(totalRake);
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            totalPot = ps.loadInt();
            totalRake = ps.loadInt();
        }
        

        public function toString():String
        {
            var result:String = "RakeInfo :";
            result += " total_pot["+totalPot+"]" ;
            result += " total_rake["+totalRake+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

