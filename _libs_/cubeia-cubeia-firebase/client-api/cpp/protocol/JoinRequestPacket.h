// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef JOINREQUESTPACKET_H_40085A65_INCLUDE
#define JOINREQUESTPACKET_H_40085A65_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "Param.h"


namespace com_cubeia_firebase_io_protocol
{

    class JoinRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 30;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        int8_t seat;
        std::vector<Param> params;

        JoinRequestPacket() {}

        JoinRequestPacket(int32_t tableid, int8_t seat, std::vector<Param> params) {
            this->tableid = tableid;
            this->seat = seat;
            this->params = params;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const JoinRequestPacket &joinRequestPacket)
        {
            packetOutputStream << joinRequestPacket.tableid;
            packetOutputStream << joinRequestPacket.seat;
            packetOutputStream << joinRequestPacket.params;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, JoinRequestPacket &joinRequestPacket)
        {
            packetInputStream >> joinRequestPacket.tableid;
            packetInputStream >> joinRequestPacket.seat;
            packetInputStream >> joinRequestPacket.params;
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
