// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef SYSTEMMESSAGEPACKET_H_52F5D02B_INCLUDE
#define SYSTEMMESSAGEPACKET_H_52F5D02B_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class SystemMessagePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 4;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t type;
        int32_t level;
        std::string message;
        std::vector<int32_t> pids;

        SystemMessagePacket() {}

        SystemMessagePacket(int32_t type, int32_t level, std::string message, std::vector<int32_t> pids) {
            this->type = type;
            this->level = level;
            this->message = message;
            this->pids = pids;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const SystemMessagePacket &systemMessagePacket)
        {
            packetOutputStream << systemMessagePacket.type;
            packetOutputStream << systemMessagePacket.level;
            packetOutputStream << systemMessagePacket.message;
            packetOutputStream << systemMessagePacket.pids;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, SystemMessagePacket &systemMessagePacket)
        {
            packetInputStream >> systemMessagePacket.type;
            packetInputStream >> systemMessagePacket.level;
            packetInputStream >> systemMessagePacket.message;
            packetInputStream >> systemMessagePacket.pids;
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
