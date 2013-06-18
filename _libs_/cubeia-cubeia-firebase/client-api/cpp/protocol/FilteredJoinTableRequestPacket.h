// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef FILTEREDJOINTABLEREQUESTPACKET_H_7B7A7400_INCLUDE
#define FILTEREDJOINTABLEREQUESTPACKET_H_7B7A7400_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "ParamFilter.h"


namespace com_cubeia_firebase_io_protocol
{

    class FilteredJoinTableRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 170;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t seq;
        int32_t gameid;
        std::string address;
        std::vector<ParamFilter> params;

        FilteredJoinTableRequestPacket() {}

        FilteredJoinTableRequestPacket(int32_t seq, int32_t gameid, std::string address, std::vector<ParamFilter> params) {
            this->seq = seq;
            this->gameid = gameid;
            this->address = address;
            this->params = params;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const FilteredJoinTableRequestPacket &filteredJoinTableRequestPacket)
        {
            packetOutputStream << filteredJoinTableRequestPacket.seq;
            packetOutputStream << filteredJoinTableRequestPacket.gameid;
            packetOutputStream << filteredJoinTableRequestPacket.address;
            packetOutputStream << filteredJoinTableRequestPacket.params;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, FilteredJoinTableRequestPacket &filteredJoinTableRequestPacket)
        {
            packetInputStream >> filteredJoinTableRequestPacket.seq;
            packetInputStream >> filteredJoinTableRequestPacket.gameid;
            packetInputStream >> filteredJoinTableRequestPacket.address;
            packetInputStream >> filteredJoinTableRequestPacket.params;
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
