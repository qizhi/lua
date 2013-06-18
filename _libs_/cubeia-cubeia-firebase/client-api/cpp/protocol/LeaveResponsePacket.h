// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef LEAVERESPONSEPACKET_H_57277222_INCLUDE
#define LEAVERESPONSEPACKET_H_57277222_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class LeaveResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 37;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        ResponseStatus::Enum status;
        int32_t code;

        LeaveResponsePacket() {}

        LeaveResponsePacket(int32_t tableid, ResponseStatus::Enum status, int32_t code) {
            this->tableid = tableid;
            this->status = status;
            this->code = code;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const LeaveResponsePacket &leaveResponsePacket)
        {
            packetOutputStream << leaveResponsePacket.tableid;
            packetOutputStream << static_cast<uint8_t>(leaveResponsePacket.status);
            packetOutputStream << leaveResponsePacket.code;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, LeaveResponsePacket &leaveResponsePacket)
        {
            packetInputStream >> leaveResponsePacket.tableid;
            {
                uint8_t temp;
                packetInputStream >> temp;
                leaveResponsePacket.status = static_cast<ResponseStatus::Enum>(temp);
            }
            packetInputStream >> leaveResponsePacket.code;
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
