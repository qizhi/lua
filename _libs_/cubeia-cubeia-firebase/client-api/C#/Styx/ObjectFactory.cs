using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Styx
{
    /**
     * An implementation of this interface is needed for the PacketHandler
     * to work as expected. The implementation is typically automatically
     * generated.
     */
    public interface ObjectFactory
    {
        int version();
        ProtocolObject create(int classId);
    }
}
