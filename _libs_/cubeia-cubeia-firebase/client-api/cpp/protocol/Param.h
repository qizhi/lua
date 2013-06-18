// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef PARAM_H_6DC0D914_INCLUDE
#define PARAM_H_6DC0D914_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class Param : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 5;

        virtual uint8_t classId() {
            return CLASSID;
        }

        std::string key;
        int8_t type;
        std::vector<uint8_t> value;

        Param() {}

        Param(std::string key, int8_t type, std::vector<uint8_t> value) {
            this->key = key;
            this->type = type;
            this->value = value;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const Param &param)
        {
            packetOutputStream << param.key;
            packetOutputStream << param.type;
            packetOutputStream << param.value;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, Param &param)
        {
            packetInputStream >> param.key;
            packetInputStream >> param.type;
            packetInputStream >> param.value;
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
