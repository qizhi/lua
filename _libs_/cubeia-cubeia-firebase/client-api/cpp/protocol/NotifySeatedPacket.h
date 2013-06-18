// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef NOTIFYSEATEDPACKET_H_50E1963A_INCLUDE
#define NOTIFYSEATEDPACKET_H_50E1963A_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "TableSnapshotPacket.h"


namespace com_cubeia_firebase_io_protocol
{

    class NotifySeatedPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 62;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        int8_t seat;
        int32_t mttid;
        TableSnapshotPacket snapshot;

        NotifySeatedPacket() {}

        NotifySeatedPacket(int32_t tableid, int8_t seat, int32_t mttid, TableSnapshotPacket snapshot) {
            this->tableid = tableid;
            this->seat = seat;
            this->mttid = mttid;
            this->snapshot = snapshot;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const NotifySeatedPacket &notifySeatedPacket)
        {
            packetOutputStream << notifySeatedPacket.tableid;
            packetOutputStream << notifySeatedPacket.seat;
            packetOutputStream << notifySeatedPacket.mttid;
            packetOutputStream << notifySeatedPacket.snapshot;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, NotifySeatedPacket &notifySeatedPacket)
        {
            packetInputStream >> notifySeatedPacket.tableid;
            packetInputStream >> notifySeatedPacket.seat;
            packetInputStream >> notifySeatedPacket.mttid;
            packetInputStream >> notifySeatedPacket.snapshot;
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
