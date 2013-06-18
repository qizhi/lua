// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef MTTREGISTERRESPONSEPACKET_H_5C3188DF_INCLUDE
#define MTTREGISTERRESPONSEPACKET_H_5C3188DF_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class MttRegisterResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 206;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t mttid;
        TournamentRegisterResponseStatus::Enum status;

        MttRegisterResponsePacket() {}

        MttRegisterResponsePacket(int32_t mttid, TournamentRegisterResponseStatus::Enum status) {
            this->mttid = mttid;
            this->status = status;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const MttRegisterResponsePacket &mttRegisterResponsePacket)
        {
            packetOutputStream << mttRegisterResponsePacket.mttid;
            packetOutputStream << static_cast<uint8_t>(mttRegisterResponsePacket.status);
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, MttRegisterResponsePacket &mttRegisterResponsePacket)
        {
            packetInputStream >> mttRegisterResponsePacket.mttid;
            {
                uint8_t temp;
                packetInputStream >> temp;
                mttRegisterResponsePacket.status = static_cast<TournamentRegisterResponseStatus::Enum>(temp);
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
