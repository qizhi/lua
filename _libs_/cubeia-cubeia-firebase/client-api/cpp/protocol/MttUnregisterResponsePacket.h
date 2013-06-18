// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef MTTUNREGISTERRESPONSEPACKET_H_2EDEA2E0_INCLUDE
#define MTTUNREGISTERRESPONSEPACKET_H_2EDEA2E0_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class MttUnregisterResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 208;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t mttid;
        TournamentRegisterResponseStatus::Enum status;

        MttUnregisterResponsePacket() {}

        MttUnregisterResponsePacket(int32_t mttid, TournamentRegisterResponseStatus::Enum status) {
            this->mttid = mttid;
            this->status = status;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const MttUnregisterResponsePacket &mttUnregisterResponsePacket)
        {
            packetOutputStream << mttUnregisterResponsePacket.mttid;
            packetOutputStream << static_cast<uint8_t>(mttUnregisterResponsePacket.status);
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, MttUnregisterResponsePacket &mttUnregisterResponsePacket)
        {
            packetInputStream >> mttUnregisterResponsePacket.mttid;
            {
                uint8_t temp;
                packetInputStream >> temp;
                mttUnregisterResponsePacket.status = static_cast<TournamentRegisterResponseStatus::Enum>(temp);
            }
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
