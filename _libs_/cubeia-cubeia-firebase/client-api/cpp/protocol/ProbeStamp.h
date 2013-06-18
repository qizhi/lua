// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef PROBESTAMP_H_130412E0_INCLUDE
#define PROBESTAMP_H_130412E0_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class ProbeStamp : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 200;

        virtual uint8_t classId() {
            return CLASSID;
        }

        std::string clazz;
        int64_t timestamp;

        ProbeStamp() {}

        ProbeStamp(std::string clazz, int64_t timestamp) {
            this->clazz = clazz;
            this->timestamp = timestamp;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const ProbeStamp &probeStamp)
        {
            packetOutputStream << probeStamp.clazz;
            packetOutputStream << probeStamp.timestamp;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, ProbeStamp &probeStamp)
        {
            packetInputStream >> probeStamp.clazz;
            packetInputStream >> probeStamp.timestamp;
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
