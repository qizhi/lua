using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
/* Converted from java */
namespace Styx
{
    /**
    * Any object that should be serializable via the Styx wire protocol
    * should implement this interface. Files implementing this protocol
    * are typically automatically generated.
    */
    public interface ProtocolObject 
    {
        /**
        * An object needs a unique classId.
        */
        byte classId();
   
        /**
        * Serialization method.
        */
        void save(PacketOutputStream ps);

        /**
        * Deserialization method.
        */
        void load(PacketInputStream ps);
    }
}


