// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef WATCHRESPONSEPACKET_H_1C8158A_INCLUDE
#define WATCHRESPONSEPACKET_H_1C8158A_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class WatchResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 33;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        WatchResponseStatus::Enum status;

        WatchResponsePacket() {}

        WatchResponsePacket(int32_t tableid, WatchResponseStatus::Enum status) {
            this->tableid = tableid;
            this->status = status;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const WatchResponsePacket &watchResponsePacket)
        {
            packetOutputStream << watchResponsePacket.tableid;
            packetOutputStream << static_cast<uint8_t>(watchResponsePacket.status);
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, WatchResponsePacket &watchResponsePacket)
        {
            packetInputStream >> watchResponsePacket.tableid;
            {
                uint8_t temp;
                packetInputStream >> temp;
                watchResponsePacket.status = static_cast<WatchResponseStatus::Enum>(temp);
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
