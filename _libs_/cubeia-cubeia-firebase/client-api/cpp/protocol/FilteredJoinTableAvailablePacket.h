// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef FILTEREDJOINTABLEAVAILABLEPACKET_H_1C60D2CE_INCLUDE
#define FILTEREDJOINTABLEAVAILABLEPACKET_H_1C60D2CE_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class FilteredJoinTableAvailablePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 174;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t seq;
        int32_t tableid;
        int8_t seat;

        FilteredJoinTableAvailablePacket() {}

        FilteredJoinTableAvailablePacket(int32_t seq, int32_t tableid, int8_t seat) {
            this->seq = seq;
            this->tableid = tableid;
            this->seat = seat;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const FilteredJoinTableAvailablePacket &filteredJoinTableAvailablePacket)
        {
            packetOutputStream << filteredJoinTableAvailablePacket.seq;
            packetOutputStream << filteredJoinTableAvailablePacket.tableid;
            packetOutputStream << filteredJoinTableAvailablePacket.seat;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, FilteredJoinTableAvailablePacket &filteredJoinTableAvailablePacket)
        {
            packetInputStream >> filteredJoinTableAvailablePacket.seq;
            packetInputStream >> filteredJoinTableAvailablePacket.tableid;
            packetInputStream >> filteredJoinTableAvailablePacket.seat;
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
