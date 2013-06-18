// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TABLEUPDATELISTPACKET_H_1AD9C0BF_INCLUDE
#define TABLEUPDATELISTPACKET_H_1AD9C0BF_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "TableUpdatePacket.h"


namespace com_cubeia_firebase_io_protocol
{

    class TableUpdateListPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 154;

        virtual uint8_t classId() {
            return CLASSID;
        }

        std::vector<TableUpdatePacket> updates;

        TableUpdateListPacket() {}

        TableUpdateListPacket(std::vector<TableUpdatePacket> updates) {
            this->updates = updates;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TableUpdateListPacket &tableUpdateListPacket)
        {
            packetOutputStream << tableUpdateListPacket.updates;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TableUpdateListPacket &tableUpdateListPacket)
        {
            packetInputStream >> tableUpdateListPacket.updates;
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
