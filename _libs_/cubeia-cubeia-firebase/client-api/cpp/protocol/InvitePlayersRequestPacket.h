// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef INVITEPLAYERSREQUESTPACKET_H_2B76F992_INCLUDE
#define INVITEPLAYERSREQUESTPACKET_H_2B76F992_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class InvitePlayersRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 42;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        std::vector<int32_t> invitees;

        InvitePlayersRequestPacket() {}

        InvitePlayersRequestPacket(int32_t tableid, std::vector<int32_t> invitees) {
            this->tableid = tableid;
            this->invitees = invitees;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const InvitePlayersRequestPacket &invitePlayersRequestPacket)
        {
            packetOutputStream << invitePlayersRequestPacket.tableid;
            packetOutputStream << invitePlayersRequestPacket.invitees;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, InvitePlayersRequestPacket &invitePlayersRequestPacket)
        {
            packetInputStream >> invitePlayersRequestPacket.tableid;
            packetInputStream >> invitePlayersRequestPacket.invitees;
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
