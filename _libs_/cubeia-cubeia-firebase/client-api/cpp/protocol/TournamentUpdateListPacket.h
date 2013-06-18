// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TOURNAMENTUPDATELISTPACKET_H_6F0A046A_INCLUDE
#define TOURNAMENTUPDATELISTPACKET_H_6F0A046A_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "TournamentUpdatePacket.h"


namespace com_cubeia_firebase_io_protocol
{

    class TournamentUpdateListPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 156;

        virtual uint8_t classId() {
            return CLASSID;
        }

        std::vector<TournamentUpdatePacket> updates;

        TournamentUpdateListPacket() {}

        TournamentUpdateListPacket(std::vector<TournamentUpdatePacket> updates) {
            this->updates = updates;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TournamentUpdateListPacket &tournamentUpdateListPacket)
        {
            packetOutputStream << tournamentUpdateListPacket.updates;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TournamentUpdateListPacket &tournamentUpdateListPacket)
        {
            packetInputStream >> tournamentUpdateListPacket.updates;
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
