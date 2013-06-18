// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef PLAYERINFOPACKET_H_7E693367_INCLUDE
#define PLAYERINFOPACKET_H_7E693367_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "Param.h"


namespace com_cubeia_firebase_io_protocol
{

    class PlayerInfoPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 13;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t pid;
        std::string nick;
        std::vector<Param> details;

        PlayerInfoPacket() {}

        PlayerInfoPacket(int32_t pid, std::string nick, std::vector<Param> details) {
            this->pid = pid;
            this->nick = nick;
            this->details = details;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const PlayerInfoPacket &playerInfoPacket)
        {
            packetOutputStream << playerInfoPacket.pid;
            packetOutputStream << playerInfoPacket.nick;
            packetOutputStream << playerInfoPacket.details;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, PlayerInfoPacket &playerInfoPacket)
        {
            packetInputStream >> playerInfoPacket.pid;
            packetInputStream >> playerInfoPacket.nick;
            packetInputStream >> playerInfoPacket.details;
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
