// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef GAMEVERSIONPACKET_H_78264C6D_INCLUDE
#define GAMEVERSIONPACKET_H_78264C6D_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class GameVersionPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 1;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t game;
        int32_t operatorid;
        std::string version;

        GameVersionPacket() {}

        GameVersionPacket(int32_t game, int32_t operatorid, std::string version) {
            this->game = game;
            this->operatorid = operatorid;
            this->version = version;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const GameVersionPacket &gameVersionPacket)
        {
            packetOutputStream << gameVersionPacket.game;
            packetOutputStream << gameVersionPacket.operatorid;
            packetOutputStream << gameVersionPacket.version;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, GameVersionPacket &gameVersionPacket)
        {
            packetInputStream >> gameVersionPacket.game;
            packetInputStream >> gameVersionPacket.operatorid;
            packetInputStream >> gameVersionPacket.version;
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
