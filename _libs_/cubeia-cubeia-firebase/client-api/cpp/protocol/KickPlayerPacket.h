// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef KICKPLAYERPACKET_H_64CC98D5_INCLUDE
#define KICKPLAYERPACKET_H_64CC98D5_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class KickPlayerPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 64;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        int16_t reasonCode;

        KickPlayerPacket() {}

        KickPlayerPacket(int32_t tableid, int16_t reasonCode) {
            this->tableid = tableid;
            this->reasonCode = reasonCode;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const KickPlayerPacket &kickPlayerPacket)
        {
            packetOutputStream << kickPlayerPacket.tableid;
            packetOutputStream << kickPlayerPacket.reasonCode;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, KickPlayerPacket &kickPlayerPacket)
        {
            packetInputStream >> kickPlayerPacket.tableid;
            packetInputStream >> kickPlayerPacket.reasonCode;
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
