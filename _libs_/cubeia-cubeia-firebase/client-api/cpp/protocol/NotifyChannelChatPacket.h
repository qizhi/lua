// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef NOTIFYCHANNELCHATPACKET_H_5E31C495_INCLUDE
#define NOTIFYCHANNELCHATPACKET_H_5E31C495_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class NotifyChannelChatPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 123;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t pid;
        int32_t channelid;
        int32_t targetid;
        std::string nick;
        std::string message;

        NotifyChannelChatPacket() {}

        NotifyChannelChatPacket(int32_t pid, int32_t channelid, int32_t targetid, std::string nick, std::string message) {
            this->pid = pid;
            this->channelid = channelid;
            this->targetid = targetid;
            this->nick = nick;
            this->message = message;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const NotifyChannelChatPacket &notifyChannelChatPacket)
        {
            packetOutputStream << notifyChannelChatPacket.pid;
            packetOutputStream << notifyChannelChatPacket.channelid;
            packetOutputStream << notifyChannelChatPacket.targetid;
            packetOutputStream << notifyChannelChatPacket.nick;
            packetOutputStream << notifyChannelChatPacket.message;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, NotifyChannelChatPacket &notifyChannelChatPacket)
        {
            packetInputStream >> notifyChannelChatPacket.pid;
            packetInputStream >> notifyChannelChatPacket.channelid;
            packetInputStream >> notifyChannelChatPacket.targetid;
            packetInputStream >> notifyChannelChatPacket.nick;
            packetInputStream >> notifyChannelChatPacket.message;
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
