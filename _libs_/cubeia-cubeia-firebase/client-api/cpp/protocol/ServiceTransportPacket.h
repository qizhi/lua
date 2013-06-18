// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef SERVICETRANSPORTPACKET_H_5CDF269E_INCLUDE
#define SERVICETRANSPORTPACKET_H_5CDF269E_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class ServiceTransportPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 101;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t pid;
        int32_t seq;
        std::string service;
        int8_t idtype;
        std::vector<uint8_t> servicedata;

        ServiceTransportPacket() {}

        ServiceTransportPacket(int32_t pid, int32_t seq, std::string service, int8_t idtype, std::vector<uint8_t> servicedata) {
            this->pid = pid;
            this->seq = seq;
            this->service = service;
            this->idtype = idtype;
            this->servicedata = servicedata;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const ServiceTransportPacket &serviceTransportPacket)
        {
            packetOutputStream << serviceTransportPacket.pid;
            packetOutputStream << serviceTransportPacket.seq;
            packetOutputStream << serviceTransportPacket.service;
            packetOutputStream << serviceTransportPacket.idtype;
            packetOutputStream << serviceTransportPacket.servicedata;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, ServiceTransportPacket &serviceTransportPacket)
        {
            packetInputStream >> serviceTransportPacket.pid;
            packetInputStream >> serviceTransportPacket.seq;
            packetInputStream >> serviceTransportPacket.service;
            packetInputStream >> serviceTransportPacket.idtype;
            packetInputStream >> serviceTransportPacket.servicedata;
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
