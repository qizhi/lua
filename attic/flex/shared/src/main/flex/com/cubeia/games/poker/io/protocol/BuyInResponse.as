// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class BuyInResponse implements ProtocolObject {
        public static const CLASSID:int = 25;

        public function classId():int {
            return BuyInResponse.CLASSID;
        }

        public var balance:int;
        public var pendingBalance:int;
        public var amountBroughtIn:int;
        public var resultCode:uint;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveInt(balance);
            ps.saveInt(pendingBalance);
            ps.saveInt(amountBroughtIn);
            ps.saveUnsignedByte(resultCode);
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            balance = ps.loadInt();
            pendingBalance = ps.loadInt();
            amountBroughtIn = ps.loadInt();
            resultCode = BuyInResultCodeEnum.makeBuyInResultCodeEnum(ps.loadUnsignedByte());
        }
        

        public function toString():String
        {
            var result:String = "BuyInResponse :";
            result += " balance["+balance+"]" ;
            result += " pending_balance["+pendingBalance+"]" ;
            result += " amount_brought_in["+amountBroughtIn+"]" ;
            result += " result_code["+resultCode+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

