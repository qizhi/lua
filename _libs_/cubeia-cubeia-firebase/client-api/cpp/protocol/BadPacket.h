// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef BADPACKET_H_3F59FDE6_INCLUDE
#define BADPACKET_H_3F59FDE6_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class BadPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 3;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int8_t cmd;
        int8_t error;

        BadPacket() {}

        BadPacket(int8_t cmd, int8_t error) {
            this->cmd = cmd;
            this->error = error;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const BadPacket &badPacket)
        {
            packetOutputStream << badPacket.cmd;
            packetOutputStream << badPacket.error;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, BadPacket &badPacket)
        {
            packetInputStream >> badPacket.cmd;
            packetInputStream >> badPacket.error;
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
