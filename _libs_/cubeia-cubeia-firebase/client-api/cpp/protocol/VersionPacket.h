// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef VERSIONPACKET_H_65DBA9F7_INCLUDE
#define VERSIONPACKET_H_65DBA9F7_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class VersionPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 0;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t game;
        int32_t operatorid;
        int32_t protocol;

        VersionPacket() {}

        VersionPacket(int32_t game, int32_t operatorid, int32_t protocol) {
            this->game = game;
            this->operatorid = operatorid;
            this->protocol = protocol;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const VersionPacket &versionPacket)
        {
            packetOutputStream << versionPacket.game;
            packetOutputStream << versionPacket.operatorid;
            packetOutputStream << versionPacket.protocol;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, VersionPacket &versionPacket)
        {
            packetInputStream >> versionPacket.game;
            packetInputStream >> versionPacket.operatorid;
            packetInputStream >> versionPacket.protocol;
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
