// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef NOTIFYJOINPACKET_H_4F12E8F4_INCLUDE
#define NOTIFYJOINPACKET_H_4F12E8F4_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class NotifyJoinPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 60;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        int32_t pid;
        std::string nick;
        int8_t seat;

        NotifyJoinPacket() {}

        NotifyJoinPacket(int32_t tableid, int32_t pid, std::string nick, int8_t seat) {
            this->tableid = tableid;
            this->pid = pid;
            this->nick = nick;
            this->seat = seat;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const NotifyJoinPacket &notifyJoinPacket)
        {
            packetOutputStream << notifyJoinPacket.tableid;
            packetOutputStream << notifyJoinPacket.pid;
            packetOutputStream << notifyJoinPacket.nick;
            packetOutputStream << notifyJoinPacket.seat;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, NotifyJoinPacket &notifyJoinPacket)
        {
            packetInputStream >> notifyJoinPacket.tableid;
            packetInputStream >> notifyJoinPacket.pid;
            packetInputStream >> notifyJoinPacket.nick;
            packetInputStream >> notifyJoinPacket.seat;
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
