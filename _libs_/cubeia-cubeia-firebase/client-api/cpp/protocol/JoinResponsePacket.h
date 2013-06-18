// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef JOINRESPONSEPACKET_H_F8FBE23_INCLUDE
#define JOINRESPONSEPACKET_H_F8FBE23_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class JoinResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 31;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        int8_t seat;
        JoinResponseStatus::Enum status;
        int32_t code;

        JoinResponsePacket() {}

        JoinResponsePacket(int32_t tableid, int8_t seat, JoinResponseStatus::Enum status, int32_t code) {
            this->tableid = tableid;
            this->seat = seat;
            this->status = status;
            this->code = code;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const JoinResponsePacket &joinResponsePacket)
        {
            packetOutputStream << joinResponsePacket.tableid;
            packetOutputStream << joinResponsePacket.seat;
            packetOutputStream << static_cast<uint8_t>(joinResponsePacket.status);
            packetOutputStream << joinResponsePacket.code;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, JoinResponsePacket &joinResponsePacket)
        {
            packetInputStream >> joinResponsePacket.tableid;
            packetInputStream >> joinResponsePacket.seat;
            {
                uint8_t temp;
                packetInputStream >> temp;
                joinResponsePacket.status = static_cast<JoinResponseStatus::Enum>(temp);
            }
            packetInputStream >> joinResponsePacket.code;
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
