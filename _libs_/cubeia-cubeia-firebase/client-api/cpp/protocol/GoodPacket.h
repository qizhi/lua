// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef GOODPACKET_H_21D877C2_INCLUDE
#define GOODPACKET_H_21D877C2_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class GoodPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 2;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int8_t cmd;
        int32_t extra;

        GoodPacket() {}

        GoodPacket(int8_t cmd, int32_t extra) {
            this->cmd = cmd;
            this->extra = extra;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const GoodPacket &goodPacket)
        {
            packetOutputStream << goodPacket.cmd;
            packetOutputStream << goodPacket.extra;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, GoodPacket &goodPacket)
        {
            packetInputStream >> goodPacket.cmd;
            packetInputStream >> goodPacket.extra;
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
