// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef GAMETRANSPORTPACKET_H_75B6EF21_INCLUDE
#define GAMETRANSPORTPACKET_H_75B6EF21_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class GameTransportPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 100;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        int32_t pid;
        std::vector<uint8_t> gamedata;

        GameTransportPacket() {}

        GameTransportPacket(int32_t tableid, int32_t pid, std::vector<uint8_t> gamedata) {
            this->tableid = tableid;
            this->pid = pid;
            this->gamedata = gamedata;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const GameTransportPacket &gameTransportPacket)
        {
            packetOutputStream << gameTransportPacket.tableid;
            packetOutputStream << gameTransportPacket.pid;
            packetOutputStream << gameTransportPacket.gamedata;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, GameTransportPacket &gameTransportPacket)
        {
            packetInputStream >> gameTransportPacket.tableid;
            packetInputStream >> gameTransportPacket.pid;
            packetInputStream >> gameTransportPacket.gamedata;
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
