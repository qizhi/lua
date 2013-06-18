// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef CREATETABLERESPONSEPACKET_H_7F2EA82F_INCLUDE
#define CREATETABLERESPONSEPACKET_H_7F2EA82F_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class CreateTableResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 41;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t seq;
        int32_t tableid;
        int8_t seat;
        ResponseStatus::Enum status;
        int32_t code;

        CreateTableResponsePacket() {}

        CreateTableResponsePacket(int32_t seq, int32_t tableid, int8_t seat, ResponseStatus::Enum status, int32_t code) {
            this->seq = seq;
            this->tableid = tableid;
            this->seat = seat;
            this->status = status;
            this->code = code;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const CreateTableResponsePacket &createTableResponsePacket)
        {
            packetOutputStream << createTableResponsePacket.seq;
            packetOutputStream << createTableResponsePacket.tableid;
            packetOutputStream << createTableResponsePacket.seat;
            packetOutputStream << static_cast<uint8_t>(createTableResponsePacket.status);
            packetOutputStream << createTableResponsePacket.code;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, CreateTableResponsePacket &createTableResponsePacket)
        {
            packetInputStream >> createTableResponsePacket.seq;
            packetInputStream >> createTableResponsePacket.tableid;
            packetInputStream >> createTableResponsePacket.seat;
            {
                uint8_t temp;
                packetInputStream >> temp;
                createTableResponsePacket.status = static_cast<ResponseStatus::Enum>(temp);
            }
            packetInputStream >> createTableResponsePacket.code;
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
