// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public final class HandPhase5cardEnum
    {
        public static const BETTING:int = 0;
        public static const THIRD_STREET:int = 1;
        public static const FOURTH_STREET:int = 2;
        public static const FIFTH_STREET:int = 3;

        public static function makeHandPhase5cardEnum(value:int):int  {
            switch(value) {
                case 0: return HandPhase5cardEnum.BETTING;
                case 1: return HandPhase5cardEnum.THIRD_STREET;
                case 2: return HandPhase5cardEnum.FOURTH_STREET;
                case 3: return HandPhase5cardEnum.FIFTH_STREET;
            }
            return -1;
        }

}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

    }
