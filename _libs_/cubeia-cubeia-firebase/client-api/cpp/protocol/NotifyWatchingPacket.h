// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef NOTIFYWATCHINGPACKET_H_7D700009_INCLUDE
#define NOTIFYWATCHINGPACKET_H_7D700009_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class NotifyWatchingPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 63;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;

        NotifyWatchingPacket() {}

        NotifyWatchingPacket(int32_t tableid) {
            this->tableid = tableid;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const NotifyWatchingPacket &notifyWatchingPacket)
        {
            packetOutputStream << notifyWatchingPacket.tableid;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, NotifyWatchingPacket &notifyWatchingPacket)
        {
            packetInputStream >> notifyWatchingPacket.tableid;
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
