// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TABLEQUERYRESPONSEPACKET_H_739ACCCB_INCLUDE
#define TABLEQUERYRESPONSEPACKET_H_739ACCCB_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "SeatInfoPacket.h"


namespace com_cubeia_firebase_io_protocol
{

    class TableQueryResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 39;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        ResponseStatus::Enum status;
        std::vector<SeatInfoPacket> seats;

        TableQueryResponsePacket() {}

        TableQueryResponsePacket(int32_t tableid, ResponseStatus::Enum status, std::vector<SeatInfoPacket> seats) {
            this->tableid = tableid;
            this->status = status;
            this->seats = seats;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TableQueryResponsePacket &tableQueryResponsePacket)
        {
            packetOutputStream << tableQueryResponsePacket.tableid;
            packetOutputStream << static_cast<uint8_t>(tableQueryResponsePacket.status);
            packetOutputStream << tableQueryResponsePacket.seats;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TableQueryResponsePacket &tableQueryResponsePacket)
        {
            packetInputStream >> tableQueryResponsePacket.tableid;
            {
                uint8_t temp;
                packetInputStream >> temp;
                tableQueryResponsePacket.status = static_cast<ResponseStatus::Enum>(temp);
            }
            packetInputStream >> tableQueryResponsePacket.seats;
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
