// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef LOBBYOBJECTUNSUBSCRIBEPACKET_H_4B71A80A_INCLUDE
#define LOBBYOBJECTUNSUBSCRIBEPACKET_H_4B71A80A_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class LobbyObjectUnsubscribePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 152;

        virtual uint8_t classId() {
            return CLASSID;
        }

        LobbyType::Enum type;
        int32_t gameid;
        std::string address;
        int32_t objectid;

        LobbyObjectUnsubscribePacket() {}

        LobbyObjectUnsubscribePacket(LobbyType::Enum type, int32_t gameid, std::string address, int32_t objectid) {
            this->type = type;
            this->gameid = gameid;
            this->address = address;
            this->objectid = objectid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const LobbyObjectUnsubscribePacket &lobbyObjectUnsubscribePacket)
        {
            packetOutputStream << static_cast<uint8_t>(lobbyObjectUnsubscribePacket.type);
            packetOutputStream << lobbyObjectUnsubscribePacket.gameid;
            packetOutputStream << lobbyObjectUnsubscribePacket.address;
            packetOutputStream << lobbyObjectUnsubscribePacket.objectid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, LobbyObjectUnsubscribePacket &lobbyObjectUnsubscribePacket)
        {
            {
                uint8_t temp;
                packetInputStream >> temp;
                lobbyObjectUnsubscribePacket.type = static_cast<LobbyType::Enum>(temp);
            }
            packetInputStream >> lobbyObjectUnsubscribePacket.gameid;
            packetInputStream >> lobbyObjectUnsubscribePacket.address;
            packetInputStream >> lobbyObjectUnsubscribePacket.objectid;
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
