// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef FILTEREDJOINCANCELRESPONSEPACKET_H_74508894_INCLUDE
#define FILTEREDJOINCANCELRESPONSEPACKET_H_74508894_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class FilteredJoinCancelResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 173;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t seq;
        ResponseStatus::Enum status;

        FilteredJoinCancelResponsePacket() {}

        FilteredJoinCancelResponsePacket(int32_t seq, ResponseStatus::Enum status) {
            this->seq = seq;
            this->status = status;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const FilteredJoinCancelResponsePacket &filteredJoinCancelResponsePacket)
        {
            packetOutputStream << filteredJoinCancelResponsePacket.seq;
            packetOutputStream << static_cast<uint8_t>(filteredJoinCancelResponsePacket.status);
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, FilteredJoinCancelResponsePacket &filteredJoinCancelResponsePacket)
        {
            packetInputStream >> filteredJoinCancelResponsePacket.seq;
            {
                uint8_t temp;
                packetInputStream >> temp;
                filteredJoinCancelResponsePacket.status = static_cast<ResponseStatus::Enum>(temp);
            }
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
