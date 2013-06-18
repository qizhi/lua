// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TABLESNAPSHOTPACKET_H_76C53310_INCLUDE
#define TABLESNAPSHOTPACKET_H_76C53310_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "Param.h"


namespace com_cubeia_firebase_io_protocol
{

    class TableSnapshotPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 143;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        std::string address;
        std::string name;
        int16_t capacity;
        int16_t seated;
        std::vector<Param> params;

        TableSnapshotPacket() {}

        TableSnapshotPacket(int32_t tableid, std::string address, std::string name, int16_t capacity, int16_t seated, std::vector<Param> params) {
            this->tableid = tableid;
            this->address = address;
            this->name = name;
            this->capacity = capacity;
            this->seated = seated;
            this->params = params;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TableSnapshotPacket &tableSnapshotPacket)
        {
            packetOutputStream << tableSnapshotPacket.tableid;
            packetOutputStream << tableSnapshotPacket.address;
            packetOutputStream << tableSnapshotPacket.name;
            packetOutputStream << tableSnapshotPacket.capacity;
            packetOutputStream << tableSnapshotPacket.seated;
            packetOutputStream << tableSnapshotPacket.params;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TableSnapshotPacket &tableSnapshotPacket)
        {
            packetInputStream >> tableSnapshotPacket.tableid;
            packetInputStream >> tableSnapshotPacket.address;
            packetInputStream >> tableSnapshotPacket.name;
            packetInputStream >> tableSnapshotPacket.capacity;
            packetInputStream >> tableSnapshotPacket.seated;
            packetInputStream >> tableSnapshotPacket.params;
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
