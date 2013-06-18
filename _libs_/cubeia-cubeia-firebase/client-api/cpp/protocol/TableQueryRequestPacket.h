// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TABLEQUERYREQUESTPACKET_H_33C9A9DE_INCLUDE
#define TABLEQUERYREQUESTPACKET_H_33C9A9DE_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class TableQueryRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 38;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;

        TableQueryRequestPacket() {}

        TableQueryRequestPacket(int32_t tableid) {
            this->tableid = tableid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TableQueryRequestPacket &tableQueryRequestPacket)
        {
            packetOutputStream << tableQueryRequestPacket.tableid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TableQueryRequestPacket &tableQueryRequestPacket)
        {
            packetInputStream >> tableQueryRequestPacket.tableid;
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
