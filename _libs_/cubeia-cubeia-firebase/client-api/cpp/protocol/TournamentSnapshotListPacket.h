// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TOURNAMENTSNAPSHOTLISTPACKET_H_417D5754_INCLUDE
#define TOURNAMENTSNAPSHOTLISTPACKET_H_417D5754_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "TournamentSnapshotPacket.h"


namespace com_cubeia_firebase_io_protocol
{

    class TournamentSnapshotListPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 155;

        virtual uint8_t classId() {
            return CLASSID;
        }

        std::vector<TournamentSnapshotPacket> snapshots;

        TournamentSnapshotListPacket() {}

        TournamentSnapshotListPacket(std::vector<TournamentSnapshotPacket> snapshots) {
            this->snapshots = snapshots;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TournamentSnapshotListPacket &tournamentSnapshotListPacket)
        {
            packetOutputStream << tournamentSnapshotListPacket.snapshots;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TournamentSnapshotListPacket &tournamentSnapshotListPacket)
        {
            packetInputStream >> tournamentSnapshotListPacket.snapshots;
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
