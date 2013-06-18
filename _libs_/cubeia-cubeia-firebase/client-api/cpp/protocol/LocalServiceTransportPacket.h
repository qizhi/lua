// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef LOCALSERVICETRANSPORTPACKET_H_27D5814C_INCLUDE
#define LOCALSERVICETRANSPORTPACKET_H_27D5814C_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class LocalServiceTransportPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 103;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t seq;
        std::vector<uint8_t> servicedata;

        LocalServiceTransportPacket() {}

        LocalServiceTransportPacket(int32_t seq, std::vector<uint8_t> servicedata) {
            this->seq = seq;
            this->servicedata = servicedata;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const LocalServiceTransportPacket &localServiceTransportPacket)
        {
            packetOutputStream << localServiceTransportPacket.seq;
            packetOutputStream << localServiceTransportPacket.servicedata;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, LocalServiceTransportPacket &localServiceTransportPacket)
        {
            packetInputStream >> localServiceTransportPacket.seq;
            packetInputStream >> localServiceTransportPacket.servicedata;
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
