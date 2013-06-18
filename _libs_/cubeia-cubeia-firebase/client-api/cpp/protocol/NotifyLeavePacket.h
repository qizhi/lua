// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef NOTIFYLEAVEPACKET_H_30C2B1E3_INCLUDE
#define NOTIFYLEAVEPACKET_H_30C2B1E3_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class NotifyLeavePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 61;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        int32_t pid;

        NotifyLeavePacket() {}

        NotifyLeavePacket(int32_t tableid, int32_t pid) {
            this->tableid = tableid;
            this->pid = pid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const NotifyLeavePacket &notifyLeavePacket)
        {
            packetOutputStream << notifyLeavePacket.tableid;
            packetOutputStream << notifyLeavePacket.pid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, NotifyLeavePacket &notifyLeavePacket)
        {
            packetInputStream >> notifyLeavePacket.tableid;
            packetInputStream >> notifyLeavePacket.pid;
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
