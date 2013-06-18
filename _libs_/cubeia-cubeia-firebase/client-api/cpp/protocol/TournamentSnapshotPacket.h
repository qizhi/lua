// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TOURNAMENTSNAPSHOTPACKET_H_9EC144D_INCLUDE
#define TOURNAMENTSNAPSHOTPACKET_H_9EC144D_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "Param.h"


namespace com_cubeia_firebase_io_protocol
{

    class TournamentSnapshotPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 148;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t mttid;
        std::string address;
        std::vector<Param> params;

        TournamentSnapshotPacket() {}

        TournamentSnapshotPacket(int32_t mttid, std::string address, std::vector<Param> params) {
            this->mttid = mttid;
            this->address = address;
            this->params = params;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TournamentSnapshotPacket &tournamentSnapshotPacket)
        {
            packetOutputStream << tournamentSnapshotPacket.mttid;
            packetOutputStream << tournamentSnapshotPacket.address;
            packetOutputStream << tournamentSnapshotPacket.params;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TournamentSnapshotPacket &tournamentSnapshotPacket)
        {
            packetInputStream >> tournamentSnapshotPacket.mttid;
            packetInputStream >> tournamentSnapshotPacket.address;
            packetInputStream >> tournamentSnapshotPacket.params;
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
