// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef LOBBYQUERYPACKET_H_660C531A_INCLUDE
#define LOBBYQUERYPACKET_H_660C531A_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class LobbyQueryPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 142;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t gameid;
        std::string address;
        LobbyType::Enum type;

        LobbyQueryPacket() {}

        LobbyQueryPacket(int32_t gameid, std::string address, LobbyType::Enum type) {
            this->gameid = gameid;
            this->address = address;
            this->type = type;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const LobbyQueryPacket &lobbyQueryPacket)
        {
            packetOutputStream << lobbyQueryPacket.gameid;
            packetOutputStream << lobbyQueryPacket.address;
            packetOutputStream << static_cast<uint8_t>(lobbyQueryPacket.type);
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, LobbyQueryPacket &lobbyQueryPacket)
        {
            packetInputStream >> lobbyQueryPacket.gameid;
            packetInputStream >> lobbyQueryPacket.address;
            {
                uint8_t temp;
                packetInputStream >> temp;
                lobbyQueryPacket.type = static_cast<LobbyType::Enum>(temp);
            }
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
