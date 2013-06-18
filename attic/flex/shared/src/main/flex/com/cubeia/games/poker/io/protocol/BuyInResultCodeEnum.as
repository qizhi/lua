// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public final class BuyInResultCodeEnum
    {
        public static const OK:int = 0;
        public static const PENDING:int = 1;
        public static const INSUFFICIENT_FUNDS_ERROR:int = 2;
        public static const PARTNER_ERROR:int = 3;
        public static const MAX_LIMIT_REACHED:int = 4;
        public static const AMOUNT_TOO_HIGH:int = 5;
        public static const UNSPECIFIED_ERROR:int = 6;
        public static const SESSION_NOT_OPEN:int = 7;

        public static function makeBuyInResultCodeEnum(value:int):int  {
            switch(value) {
                case 0: return BuyInResultCodeEnum.OK;
                case 1: return BuyInResultCodeEnum.PENDING;
                case 2: return BuyInResultCodeEnum.INSUFFICIENT_FUNDS_ERROR;
                case 3: return BuyInResultCodeEnum.PARTNER_ERROR;
                case 4: return BuyInResultCodeEnum.MAX_LIMIT_REACHED;
                case 5: return BuyInResultCodeEnum.AMOUNT_TOO_HIGH;
                case 6: return BuyInResultCodeEnum.UNSPECIFIED_ERROR;
                case 7: return BuyInResultCodeEnum.SESSION_NOT_OPEN;
            }
            return -1;
        }

}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

    }
