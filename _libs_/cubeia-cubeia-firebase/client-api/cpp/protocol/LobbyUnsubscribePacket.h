// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef LOBBYUNSUBSCRIBEPACKET_H_32F48768_INCLUDE
#define LOBBYUNSUBSCRIBEPACKET_H_32F48768_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class LobbyUnsubscribePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 146;

        virtual uint8_t classId() {
            return CLASSID;
        }

        LobbyType::Enum type;
        int32_t gameid;
        std::string address;

        LobbyUnsubscribePacket() {}

        LobbyUnsubscribePacket(LobbyType::Enum type, int32_t gameid, std::string address) {
            this->type = type;
            this->gameid = gameid;
            this->address = address;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const LobbyUnsubscribePacket &lobbyUnsubscribePacket)
        {
            packetOutputStream << static_cast<uint8_t>(lobbyUnsubscribePacket.type);
            packetOutputStream << lobbyUnsubscribePacket.gameid;
            packetOutputStream << lobbyUnsubscribePacket.address;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, LobbyUnsubscribePacket &lobbyUnsubscribePacket)
        {
            {
                uint8_t temp;
                packetInputStream >> temp;
                lobbyUnsubscribePacket.type = static_cast<LobbyType::Enum>(temp);
            }
            packetInputStream >> lobbyUnsubscribePacket.gameid;
            packetInputStream >> lobbyUnsubscribePacket.address;
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
