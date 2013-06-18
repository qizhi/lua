// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public final class ErrorCodeEnum
    {
        public static const UNSPECIFIED_ERROR:int = 0;
        public static const TABLE_CLOSING:int = 1;
        public static const TABLE_CLOSING_FORCED:int = 2;
        public static const CLOSED_SESSION_DUE_TO_FATAL_ERROR:int = 3;

        public static function makeErrorCodeEnum(value:int):int  {
            switch(value) {
                case 0: return ErrorCodeEnum.UNSPECIFIED_ERROR;
                case 1: return ErrorCodeEnum.TABLE_CLOSING;
                case 2: return ErrorCodeEnum.TABLE_CLOSING_FORCED;
                case 3: return ErrorCodeEnum.CLOSED_SESSION_DUE_TO_FATAL_ERROR;
            }
            return -1;
        }

}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

    }
