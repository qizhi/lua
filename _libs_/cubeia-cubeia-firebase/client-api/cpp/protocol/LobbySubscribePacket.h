// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef LOBBYSUBSCRIBEPACKET_H_3F260EE1_INCLUDE
#define LOBBYSUBSCRIBEPACKET_H_3F260EE1_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class LobbySubscribePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 145;

        virtual uint8_t classId() {
            return CLASSID;
        }

        LobbyType::Enum type;
        int32_t gameid;
        std::string address;

        LobbySubscribePacket() {}

        LobbySubscribePacket(LobbyType::Enum type, int32_t gameid, std::string address) {
            this->type = type;
            this->gameid = gameid;
            this->address = address;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const LobbySubscribePacket &lobbySubscribePacket)
        {
            packetOutputStream << static_cast<uint8_t>(lobbySubscribePacket.type);
            packetOutputStream << lobbySubscribePacket.gameid;
            packetOutputStream << lobbySubscribePacket.address;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, LobbySubscribePacket &lobbySubscribePacket)
        {
            {
                uint8_t temp;
                packetInputStream >> temp;
                lobbySubscribePacket.type = static_cast<LobbyType::Enum>(temp);
            }
            packetInputStream >> lobbySubscribePacket.gameid;
            packetInputStream >> lobbySubscribePacket.address;
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
