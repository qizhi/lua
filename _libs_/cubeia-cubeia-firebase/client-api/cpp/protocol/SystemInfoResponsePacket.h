// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef SYSTEMINFORESPONSEPACKET_H_60000D14_INCLUDE
#define SYSTEMINFORESPONSEPACKET_H_60000D14_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "Param.h"


namespace com_cubeia_firebase_io_protocol
{

    class SystemInfoResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 19;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t players;
        std::vector<Param> params;

        SystemInfoResponsePacket() {}

        SystemInfoResponsePacket(int32_t players, std::vector<Param> params) {
            this->players = players;
            this->params = params;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const SystemInfoResponsePacket &systemInfoResponsePacket)
        {
            packetOutputStream << systemInfoResponsePacket.players;
            packetOutputStream << systemInfoResponsePacket.params;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, SystemInfoResponsePacket &systemInfoResponsePacket)
        {
            packetInputStream >> systemInfoResponsePacket.players;
            packetInputStream >> systemInfoResponsePacket.params;
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
