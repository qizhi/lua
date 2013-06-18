// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)
package com.cubeia.games.poker.io.protocol {

    import com.cubeia.firebase.io.PacketInputStream;
    import com.cubeia.firebase.io.PacketOutputStream;
    import com.cubeia.firebase.io.ProtocolObject;
  
    import flash.utils.ByteArray;

    public class InformFutureAllowedActions implements ProtocolObject {
        public static const CLASSID:int = 9;

        public function classId():int {
            return InformFutureAllowedActions.CLASSID;
        }

        public var actions:Array = new Array();

        public function save():ByteArray
        {
            var buffer:ByteArray = new ByteArray();
            var ps:PacketOutputStream = new PacketOutputStream(buffer);
            ps.saveInt(actions.length);
            var i:int;
            for( i = 0; i != actions.length; i ++)
            {
                var _tmp_actions:ByteArray = actions[i].save();
                ps.saveArray(_tmp_actions);
            }
            return buffer;
        }

        public function load(buffer:ByteArray):void 
        {
            var ps:PacketInputStream = new PacketInputStream(buffer);
            var i:int;
            var actionsCount:int = ps.loadInt();
            actions = new Array();
            for( i = 0; i < actionsCount; i ++) {
                var _tmp1:FuturePlayerAction  = new FuturePlayerAction();
                _tmp1.load(buffer);
                actions[i] = _tmp1;
            }
        }
        

        public function toString():String
        {
            var result:String = "InformFutureAllowedActions :";
            result += " actions["+actions+"]" ;
            return result;
        }

    }
}

// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

