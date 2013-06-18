// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef WATCHREQUESTPACKET_H_347D83C8_INCLUDE
#define WATCHREQUESTPACKET_H_347D83C8_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class WatchRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 32;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;

        WatchRequestPacket() {}

        WatchRequestPacket(int32_t tableid) {
            this->tableid = tableid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const WatchRequestPacket &watchRequestPacket)
        {
            packetOutputStream << watchRequestPacket.tableid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, WatchRequestPacket &watchRequestPacket)
        {
            packetInputStream >> watchRequestPacket.tableid;
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
