// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef JOINCHATCHANNELREQUESTPACKET_H_6B8C6B86_INCLUDE
#define JOINCHATCHANNELREQUESTPACKET_H_6B8C6B86_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class JoinChatChannelRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 120;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t channelid;

        JoinChatChannelRequestPacket() {}

        JoinChatChannelRequestPacket(int32_t channelid) {
            this->channelid = channelid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const JoinChatChannelRequestPacket &joinChatChannelRequestPacket)
        {
            packetOutputStream << joinChatChannelRequestPacket.channelid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, JoinChatChannelRequestPacket &joinChatChannelRequestPacket)
        {
            packetInputStream >> joinChatChannelRequestPacket.channelid;
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
