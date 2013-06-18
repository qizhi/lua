// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef PROBEPACKET_H_1133F3AF_INCLUDE
#define PROBEPACKET_H_1133F3AF_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "ProbeStamp.h"


namespace com_cubeia_firebase_io_protocol
{

    class ProbePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 201;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t id;
        int32_t tableid;
        std::vector<ProbeStamp> stamps;

        ProbePacket() {}

        ProbePacket(int32_t id, int32_t tableid, std::vector<ProbeStamp> stamps) {
            this->id = id;
            this->tableid = tableid;
            this->stamps = stamps;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const ProbePacket &probePacket)
        {
            packetOutputStream << probePacket.id;
            packetOutputStream << probePacket.tableid;
            packetOutputStream << probePacket.stamps;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, ProbePacket &probePacket)
        {
            packetInputStream >> probePacket.id;
            packetInputStream >> probePacket.tableid;
            packetInputStream >> probePacket.stamps;
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
