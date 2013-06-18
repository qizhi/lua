// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef MTTUNREGISTERREQUESTPACKET_H_21DDFC5A_INCLUDE
#define MTTUNREGISTERREQUESTPACKET_H_21DDFC5A_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class MttUnregisterRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 207;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t mttid;

        MttUnregisterRequestPacket() {}

        MttUnregisterRequestPacket(int32_t mttid) {
            this->mttid = mttid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const MttUnregisterRequestPacket &mttUnregisterRequestPacket)
        {
            packetOutputStream << mttUnregisterRequestPacket.mttid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, MttUnregisterRequestPacket &mttUnregisterRequestPacket)
        {
            packetInputStream >> mttUnregisterRequestPacket.mttid;
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
