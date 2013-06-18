// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef SEATINFOPACKET_H_7DFFC730_INCLUDE
#define SEATINFOPACKET_H_7DFFC730_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "PlayerInfoPacket.h"


namespace com_cubeia_firebase_io_protocol
{

    class SeatInfoPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 15;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        int8_t seat;
        PlayerStatus::Enum status;
        PlayerInfoPacket player;

        SeatInfoPacket() {}

        SeatInfoPacket(int32_t tableid, int8_t seat, PlayerStatus::Enum status, PlayerInfoPacket player) {
            this->tableid = tableid;
            this->seat = seat;
            this->status = status;
            this->player = player;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const SeatInfoPacket &seatInfoPacket)
        {
            packetOutputStream << seatInfoPacket.tableid;
            packetOutputStream << seatInfoPacket.seat;
            packetOutputStream << static_cast<uint8_t>(seatInfoPacket.status);
            packetOutputStream << seatInfoPacket.player;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, SeatInfoPacket &seatInfoPacket)
        {
            packetInputStream >> seatInfoPacket.tableid;
            packetInputStream >> seatInfoPacket.seat;
            {
                uint8_t temp;
                packetInputStream >> temp;
                seatInfoPacket.status = static_cast<PlayerStatus::Enum>(temp);
            }
            packetInputStream >> seatInfoPacket.player;
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
