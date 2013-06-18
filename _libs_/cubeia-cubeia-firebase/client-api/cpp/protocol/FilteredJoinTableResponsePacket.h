// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef FILTEREDJOINTABLERESPONSEPACKET_H_23FA0B4E_INCLUDE
#define FILTEREDJOINTABLERESPONSEPACKET_H_23FA0B4E_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class FilteredJoinTableResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 171;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t seq;
        int32_t gameid;
        std::string address;
        FilteredJoinResponseStatus::Enum status;

        FilteredJoinTableResponsePacket() {}

        FilteredJoinTableResponsePacket(int32_t seq, int32_t gameid, std::string address, FilteredJoinResponseStatus::Enum status) {
            this->seq = seq;
            this->gameid = gameid;
            this->address = address;
            this->status = status;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const FilteredJoinTableResponsePacket &filteredJoinTableResponsePacket)
        {
            packetOutputStream << filteredJoinTableResponsePacket.seq;
            packetOutputStream << filteredJoinTableResponsePacket.gameid;
            packetOutputStream << filteredJoinTableResponsePacket.address;
            packetOutputStream << static_cast<uint8_t>(filteredJoinTableResponsePacket.status);
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, FilteredJoinTableResponsePacket &filteredJoinTableResponsePacket)
        {
            packetInputStream >> filteredJoinTableResponsePacket.seq;
            packetInputStream >> filteredJoinTableResponsePacket.gameid;
            packetInputStream >> filteredJoinTableResponsePacket.address;
            {
                uint8_t temp;
                packetInputStream >> temp;
                filteredJoinTableResponsePacket.status = static_cast<FilteredJoinResponseStatus::Enum>(temp);
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
