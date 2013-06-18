// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef LOBBYOBJECTSUBSCRIBEPACKET_H_6C0CA80D_INCLUDE
#define LOBBYOBJECTSUBSCRIBEPACKET_H_6C0CA80D_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class LobbyObjectSubscribePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 151;

        virtual uint8_t classId() {
            return CLASSID;
        }

        LobbyType::Enum type;
        int32_t gameid;
        std::string address;
        int32_t objectid;

        LobbyObjectSubscribePacket() {}

        LobbyObjectSubscribePacket(LobbyType::Enum type, int32_t gameid, std::string address, int32_t objectid) {
            this->type = type;
            this->gameid = gameid;
            this->address = address;
            this->objectid = objectid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const LobbyObjectSubscribePacket &lobbyObjectSubscribePacket)
        {
            packetOutputStream << static_cast<uint8_t>(lobbyObjectSubscribePacket.type);
            packetOutputStream << lobbyObjectSubscribePacket.gameid;
            packetOutputStream << lobbyObjectSubscribePacket.address;
            packetOutputStream << lobbyObjectSubscribePacket.objectid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, LobbyObjectSubscribePacket &lobbyObjectSubscribePacket)
        {
            {
                uint8_t temp;
                packetInputStream >> temp;
                lobbyObjectSubscribePacket.type = static_cast<LobbyType::Enum>(temp);
            }
            packetInputStream >> lobbyObjectSubscribePacket.gameid;
            packetInputStream >> lobbyObjectSubscribePacket.address;
            packetInputStream >> lobbyObjectSubscribePacket.objectid;
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
