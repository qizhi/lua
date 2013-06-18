// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef PARAMFILTER_H_380E5EF9_INCLUDE
#define PARAMFILTER_H_380E5EF9_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "Param.h"


namespace com_cubeia_firebase_io_protocol
{

    class ParamFilter : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 6;

        virtual uint8_t classId() {
            return CLASSID;
        }

        Param param;
        int8_t op;

        ParamFilter() {}

        ParamFilter(Param param, int8_t op) {
            this->param = param;
            this->op = op;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const ParamFilter &paramFilter)
        {
            packetOutputStream << paramFilter.param;
            packetOutputStream << paramFilter.op;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, ParamFilter &paramFilter)
        {
            packetInputStream >> paramFilter.param;
            packetInputStream >> paramFilter.op;
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
