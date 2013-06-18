// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class BuyInInfoResponse implements ProtocolObject {
        public static const CLASSID:int = 23;

        public function classId():int {
            return BuyInInfoResponse.CLASSID;
        }

        public var maxAmount:int;
        public var minAmount:int;
        public var balanceInWallet:int;
        public var balanceOnTable:int;
        public var mandatoryBuyin:Boolean;
        public var resultCode:uint;

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveInt(maxAmount);
            ps.saveInt(minAmount);
            ps.saveInt(balanceInWallet);
            ps.saveInt(balanceOnTable);
            ps.saveBoolean(mandatoryBuyin);
            ps.saveUnsignedByte(resultCode);
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            maxAmount = ps.loadInt();
            minAmount = ps.loadInt();
            balanceInWallet = ps.loadInt();
            balanceOnTable = ps.loadInt();
            mandatoryBuyin = ps.loadBoolean();
            resultCode = BuyInInfoResultCodeEnum.makeBuyInInfoResultCodeEnum(ps.loadUnsignedByte());
        }
        

        public function toString():String
        {
            var result:String = "BuyInInfoResponse :";
            result += " max_amount["+maxAmount+"]" ;
            result += " min_amount["+minAmount+"]" ;
            result += " balance_in_wallet["+balanceInWallet+"]" ;
            result += " balance_on_table["+balanceOnTable+"]" ;
            result += " mandatory_buyin["+mandatoryBuyin+"]" ;
            result += " result_code["+resultCode+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

