// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TABLEREMOVEDPACKET_H_197F2459_INCLUDE
#define TABLEREMOVEDPACKET_H_197F2459_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class TableRemovedPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 147;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;

        TableRemovedPacket() {}

        TableRemovedPacket(int32_t tableid) {
            this->tableid = tableid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TableRemovedPacket &tableRemovedPacket)
        {
            packetOutputStream << tableRemovedPacket.tableid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TableRemovedPacket &tableRemovedPacket)
        {
            packetInputStream >> tableRemovedPacket.tableid;
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
