// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef PLAYERQUERYREQUESTPACKET_H_5BF57BAD_INCLUDE
#define PLAYERQUERYREQUESTPACKET_H_5BF57BAD_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class PlayerQueryRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 16;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t pid;

        PlayerQueryRequestPacket() {}

        PlayerQueryRequestPacket(int32_t pid) {
            this->pid = pid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const PlayerQueryRequestPacket &playerQueryRequestPacket)
        {
            packetOutputStream << playerQueryRequestPacket.pid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, PlayerQueryRequestPacket &playerQueryRequestPacket)
        {
            packetInputStream >> playerQueryRequestPacket.pid;
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
