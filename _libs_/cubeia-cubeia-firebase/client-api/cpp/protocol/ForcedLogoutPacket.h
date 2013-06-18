// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef FORCEDLOGOUTPACKET_H_A424F68_INCLUDE
#define FORCEDLOGOUTPACKET_H_A424F68_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class ForcedLogoutPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 14;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t code;
        std::string message;

        ForcedLogoutPacket() {}

        ForcedLogoutPacket(int32_t code, std::string message) {
            this->code = code;
            this->message = message;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const ForcedLogoutPacket &forcedLogoutPacket)
        {
            packetOutputStream << forcedLogoutPacket.code;
            packetOutputStream << forcedLogoutPacket.message;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, ForcedLogoutPacket &forcedLogoutPacket)
        {
            packetInputStream >> forcedLogoutPacket.code;
            packetInputStream >> forcedLogoutPacket.message;
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
