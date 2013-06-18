// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef JOINCHATCHANNELRESPONSEPACKET_H_11CF52D0_INCLUDE
#define JOINCHATCHANNELRESPONSEPACKET_H_11CF52D0_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class JoinChatChannelResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 121;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t channelid;
        ResponseStatus::Enum status;

        JoinChatChannelResponsePacket() {}

        JoinChatChannelResponsePacket(int32_t channelid, ResponseStatus::Enum status) {
            this->channelid = channelid;
            this->status = status;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const JoinChatChannelResponsePacket &joinChatChannelResponsePacket)
        {
            packetOutputStream << joinChatChannelResponsePacket.channelid;
            packetOutputStream << static_cast<uint8_t>(joinChatChannelResponsePacket.status);
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, JoinChatChannelResponsePacket &joinChatChannelResponsePacket)
        {
            packetInputStream >> joinChatChannelResponsePacket.channelid;
            {
                uint8_t temp;
                packetInputStream >> temp;
                joinChatChannelResponsePacket.status = static_cast<ResponseStatus::Enum>(temp);
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
