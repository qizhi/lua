// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef PINGPACKET_H_91AEA4D_INCLUDE
#define PINGPACKET_H_91AEA4D_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class PingPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 7;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t id;

        PingPacket() {}

        PingPacket(int32_t id) {
            this->id = id;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const PingPacket &pingPacket)
        {
            packetOutputStream << pingPacket.id;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, PingPacket &pingPacket)
        {
            packetInputStream >> pingPacket.id;
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
