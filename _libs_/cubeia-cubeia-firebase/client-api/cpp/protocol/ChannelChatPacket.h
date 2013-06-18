// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef CHANNELCHATPACKET_H_740BBDD_INCLUDE
#define CHANNELCHATPACKET_H_740BBDD_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class ChannelChatPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 124;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t channelid;
        int32_t targetid;
        std::string message;

        ChannelChatPacket() {}

        ChannelChatPacket(int32_t channelid, int32_t targetid, std::string message) {
            this->channelid = channelid;
            this->targetid = targetid;
            this->message = message;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const ChannelChatPacket &channelChatPacket)
        {
            packetOutputStream << channelChatPacket.channelid;
            packetOutputStream << channelChatPacket.targetid;
            packetOutputStream << channelChatPacket.message;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, ChannelChatPacket &channelChatPacket)
        {
            packetInputStream >> channelChatPacket.channelid;
            packetInputStream >> channelChatPacket.targetid;
            packetInputStream >> channelChatPacket.message;
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
