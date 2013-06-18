// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef CREATETABLEREQUESTPACKET_H_15CC8E96_INCLUDE
#define CREATETABLEREQUESTPACKET_H_15CC8E96_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "Param.h"


namespace com_cubeia_firebase_io_protocol
{

    class CreateTableRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 40;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t seq;
        int32_t gameid;
        int8_t seats;
        std::vector<Param> params;
        std::vector<int32_t> invitees;

        CreateTableRequestPacket() {}

        CreateTableRequestPacket(int32_t seq, int32_t gameid, int8_t seats, std::vector<Param> params, std::vector<int32_t> invitees) {
            this->seq = seq;
            this->gameid = gameid;
            this->seats = seats;
            this->params = params;
            this->invitees = invitees;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const CreateTableRequestPacket &createTableRequestPacket)
        {
            packetOutputStream << createTableRequestPacket.seq;
            packetOutputStream << createTableRequestPacket.gameid;
            packetOutputStream << createTableRequestPacket.seats;
            packetOutputStream << createTableRequestPacket.params;
            packetOutputStream << createTableRequestPacket.invitees;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, CreateTableRequestPacket &createTableRequestPacket)
        {
            packetInputStream >> createTableRequestPacket.seq;
            packetInputStream >> createTableRequestPacket.gameid;
            packetInputStream >> createTableRequestPacket.seats;
            packetInputStream >> createTableRequestPacket.params;
            packetInputStream >> createTableRequestPacket.invitees;
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
