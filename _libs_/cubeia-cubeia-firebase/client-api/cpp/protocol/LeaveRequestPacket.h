// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef LEAVEREQUESTPACKET_H_3D193ABF_INCLUDE
#define LEAVEREQUESTPACKET_H_3D193ABF_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class LeaveRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 36;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;

        LeaveRequestPacket() {}

        LeaveRequestPacket(int32_t tableid) {
            this->tableid = tableid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const LeaveRequestPacket &leaveRequestPacket)
        {
            packetOutputStream << leaveRequestPacket.tableid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, LeaveRequestPacket &leaveRequestPacket)
        {
            packetInputStream >> leaveRequestPacket.tableid;
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
