// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef NOTIFYINVITEDPACKET_H_5F14011A_INCLUDE
#define NOTIFYINVITEDPACKET_H_5F14011A_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class NotifyInvitedPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 43;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t inviter;
        std::string screenname;
        int32_t tableid;
        int8_t seat;

        NotifyInvitedPacket() {}

        NotifyInvitedPacket(int32_t inviter, std::string screenname, int32_t tableid, int8_t seat) {
            this->inviter = inviter;
            this->screenname = screenname;
            this->tableid = tableid;
            this->seat = seat;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const NotifyInvitedPacket &notifyInvitedPacket)
        {
            packetOutputStream << notifyInvitedPacket.inviter;
            packetOutputStream << notifyInvitedPacket.screenname;
            packetOutputStream << notifyInvitedPacket.tableid;
            packetOutputStream << notifyInvitedPacket.seat;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, NotifyInvitedPacket &notifyInvitedPacket)
        {
            packetInputStream >> notifyInvitedPacket.inviter;
            packetInputStream >> notifyInvitedPacket.screenname;
            packetInputStream >> notifyInvitedPacket.tableid;
            packetInputStream >> notifyInvitedPacket.seat;
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
