// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef NOTIFYREGISTEREDPACKET_H_51F333EE_INCLUDE
#define NOTIFYREGISTEREDPACKET_H_51F333EE_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class NotifyRegisteredPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 211;

        virtual uint8_t classId() {
            return CLASSID;
        }

        std::vector<int32_t> tournaments;

        NotifyRegisteredPacket() {}

        NotifyRegisteredPacket(std::vector<int32_t> tournaments) {
            this->tournaments = tournaments;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const NotifyRegisteredPacket &notifyRegisteredPacket)
        {
            packetOutputStream << notifyRegisteredPacket.tournaments;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, NotifyRegisteredPacket &notifyRegisteredPacket)
        {
            packetInputStream >> notifyRegisteredPacket.tournaments;
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
