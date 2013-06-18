// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef MTTTRANSPORTPACKET_H_3B5224C6_INCLUDE
#define MTTTRANSPORTPACKET_H_3B5224C6_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class MttTransportPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 104;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t mttid;
        int32_t pid;
        std::vector<uint8_t> mttdata;

        MttTransportPacket() {}

        MttTransportPacket(int32_t mttid, int32_t pid, std::vector<uint8_t> mttdata) {
            this->mttid = mttid;
            this->pid = pid;
            this->mttdata = mttdata;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const MttTransportPacket &mttTransportPacket)
        {
            packetOutputStream << mttTransportPacket.mttid;
            packetOutputStream << mttTransportPacket.pid;
            packetOutputStream << mttTransportPacket.mttdata;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, MttTransportPacket &mttTransportPacket)
        {
            packetInputStream >> mttTransportPacket.mttid;
            packetInputStream >> mttTransportPacket.pid;
            packetInputStream >> mttTransportPacket.mttdata;
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
