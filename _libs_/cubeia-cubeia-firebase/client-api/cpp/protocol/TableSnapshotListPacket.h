// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TABLESNAPSHOTLISTPACKET_H_6812CA69_INCLUDE
#define TABLESNAPSHOTLISTPACKET_H_6812CA69_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "TableSnapshotPacket.h"


namespace com_cubeia_firebase_io_protocol
{

    class TableSnapshotListPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 153;

        virtual uint8_t classId() {
            return CLASSID;
        }

        std::vector<TableSnapshotPacket> snapshots;

        TableSnapshotListPacket() {}

        TableSnapshotListPacket(std::vector<TableSnapshotPacket> snapshots) {
            this->snapshots = snapshots;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TableSnapshotListPacket &tableSnapshotListPacket)
        {
            packetOutputStream << tableSnapshotListPacket.snapshots;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TableSnapshotListPacket &tableSnapshotListPacket)
        {
            packetInputStream >> tableSnapshotListPacket.snapshots;
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
