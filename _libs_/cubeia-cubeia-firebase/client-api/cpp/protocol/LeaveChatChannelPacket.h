// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef LEAVECHATCHANNELPACKET_H_49868935_INCLUDE
#define LEAVECHATCHANNELPACKET_H_49868935_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class LeaveChatChannelPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 122;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t channelid;

        LeaveChatChannelPacket() {}

        LeaveChatChannelPacket(int32_t channelid) {
            this->channelid = channelid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const LeaveChatChannelPacket &leaveChatChannelPacket)
        {
            packetOutputStream << leaveChatChannelPacket.channelid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, LeaveChatChannelPacket &leaveChatChannelPacket)
        {
            packetInputStream >> leaveChatChannelPacket.channelid;
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
