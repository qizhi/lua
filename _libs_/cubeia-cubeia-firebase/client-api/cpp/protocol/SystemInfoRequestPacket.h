// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef SYSTEMINFOREQUESTPACKET_H_54B354C2_INCLUDE
#define SYSTEMINFOREQUESTPACKET_H_54B354C2_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class SystemInfoRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 18;

        virtual uint8_t classId() {
            return CLASSID;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const SystemInfoRequestPacket &systemInfoRequestPacket)
        {
            STYX_UNUSED(systemInfoRequestPacket)
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, SystemInfoRequestPacket &systemInfoRequestPacket)
        {
            STYX_UNUSED(systemInfoRequestPacket)
            return packetInputStream;
        }

        virtual void load(const styx::StyxBuffer &buffer)
        {
            styx::PacketInputStream packetInputStream(buffer);
            packetInputStream >> *this;
        }

        virtual styx::StyxBuffer save(void) const
        {
            styx::PacketOutputStream packetOutputStream(CLASSID);
            packetOutputStream << *this;
            return packetOutputStream.packet();
        }
    };
}

#endif
