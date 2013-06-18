// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TOURNAMENTREMOVEDPACKET_H_6CE1A6AE_INCLUDE
#define TOURNAMENTREMOVEDPACKET_H_6CE1A6AE_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class TournamentRemovedPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 150;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t mttid;

        TournamentRemovedPacket() {}

        TournamentRemovedPacket(int32_t mttid) {
            this->mttid = mttid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TournamentRemovedPacket &tournamentRemovedPacket)
        {
            packetOutputStream << tournamentRemovedPacket.mttid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TournamentRemovedPacket &tournamentRemovedPacket)
        {
            packetInputStream >> tournamentRemovedPacket.mttid;
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
