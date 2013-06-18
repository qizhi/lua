// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef LOGOUTPACKET_H_36B08A34_INCLUDE
#define LOGOUTPACKET_H_36B08A34_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class LogoutPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 12;

        virtual uint8_t classId() {
            return CLASSID;
        }

        bool leave_tables;

        LogoutPacket() {}

        LogoutPacket(bool leave_tables) {
            this->leave_tables = leave_tables;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const LogoutPacket &logoutPacket)
        {
            packetOutputStream << logoutPacket.leave_tables;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, LogoutPacket &logoutPacket)
        {
            packetInputStream >> logoutPacket.leave_tables;
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
