// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TOURNAMENTUPDATEPACKET_H_66CF4B1D_INCLUDE
#define TOURNAMENTUPDATEPACKET_H_66CF4B1D_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "Param.h"


namespace com_cubeia_firebase_io_protocol
{

    class TournamentUpdatePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 149;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t mttid;
        std::vector<Param> params;
        std::vector<std::string> removed_params;

        TournamentUpdatePacket() {}

        TournamentUpdatePacket(int32_t mttid, std::vector<Param> params, std::vector<std::string> removed_params) {
            this->mttid = mttid;
            this->params = params;
            this->removed_params = removed_params;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TournamentUpdatePacket &tournamentUpdatePacket)
        {
            packetOutputStream << tournamentUpdatePacket.mttid;
            packetOutputStream << tournamentUpdatePacket.params;
            packetOutputStream << tournamentUpdatePacket.removed_params;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TournamentUpdatePacket &tournamentUpdatePacket)
        {
            packetInputStream >> tournamentUpdatePacket.mttid;
            packetInputStream >> tournamentUpdatePacket.params;
            packetInputStream >> tournamentUpdatePacket.removed_params;
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
