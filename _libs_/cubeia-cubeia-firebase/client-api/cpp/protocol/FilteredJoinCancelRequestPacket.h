// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef FILTEREDJOINCANCELREQUESTPACKET_H_5F389081_INCLUDE
#define FILTEREDJOINCANCELREQUESTPACKET_H_5F389081_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class FilteredJoinCancelRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 172;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t seq;

        FilteredJoinCancelRequestPacket() {}

        FilteredJoinCancelRequestPacket(int32_t seq) {
            this->seq = seq;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const FilteredJoinCancelRequestPacket &filteredJoinCancelRequestPacket)
        {
            packetOutputStream << filteredJoinCancelRequestPacket.seq;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, FilteredJoinCancelRequestPacket &filteredJoinCancelRequestPacket)
        {
            packetInputStream >> filteredJoinCancelRequestPacket.seq;
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
