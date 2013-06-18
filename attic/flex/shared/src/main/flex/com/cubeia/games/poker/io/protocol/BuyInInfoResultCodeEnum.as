// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public final class BuyInInfoResultCodeEnum
    {
        public static const OK:int = 0;
        public static const MAX_LIMIT_REACHED:int = 1;
        public static const UNSPECIFIED_ERROR:int = 2;

        public static function makeBuyInInfoResultCodeEnum(value:int):int  {
            switch(value) {
                case 0: return BuyInInfoResultCodeEnum.OK;
                case 1: return BuyInInfoResultCodeEnum.MAX_LIMIT_REACHED;
                case 2: return BuyInInfoResultCodeEnum.UNSPECIFIED_ERROR;
            }
            return -1;
        }

}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

    }
