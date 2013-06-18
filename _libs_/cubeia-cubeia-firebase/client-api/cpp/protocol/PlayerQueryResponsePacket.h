// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef PLAYERQUERYRESPONSEPACKET_H_3832D6D5_INCLUDE
#define PLAYERQUERYRESPONSEPACKET_H_3832D6D5_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class PlayerQueryResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 17;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t pid;
        std::string nick;
        ResponseStatus::Enum status;
        std::vector<uint8_t> data;

        PlayerQueryResponsePacket() {}

        PlayerQueryResponsePacket(int32_t pid, std::string nick, ResponseStatus::Enum status, std::vector<uint8_t> data) {
            this->pid = pid;
            this->nick = nick;
            this->status = status;
            this->data = data;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const PlayerQueryResponsePacket &playerQueryResponsePacket)
        {
            packetOutputStream << playerQueryResponsePacket.pid;
            packetOutputStream << playerQueryResponsePacket.nick;
            packetOutputStream << static_cast<uint8_t>(playerQueryResponsePacket.status);
            packetOutputStream << playerQueryResponsePacket.data;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, PlayerQueryResponsePacket &playerQueryResponsePacket)
        {
            packetInputStream >> playerQueryResponsePacket.pid;
            packetInputStream >> playerQueryResponsePacket.nick;
            {
                uint8_t temp;
                packetInputStream >> temp;
                playerQueryResponsePacket.status = static_cast<ResponseStatus::Enum>(temp);
            }
            packetInputStream >> playerQueryResponsePacket.data;
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
