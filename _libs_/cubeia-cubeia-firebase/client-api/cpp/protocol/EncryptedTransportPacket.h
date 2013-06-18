// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef ENCRYPTEDTRANSPORTPACKET_H_7138A804_INCLUDE
#define ENCRYPTEDTRANSPORTPACKET_H_7138A804_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class EncryptedTransportPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 105;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int8_t func;
        std::vector<uint8_t> payload;

        EncryptedTransportPacket() {}

        EncryptedTransportPacket(int8_t func, std::vector<uint8_t> payload) {
            this->func = func;
            this->payload = payload;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const EncryptedTransportPacket &encryptedTransportPacket)
        {
            packetOutputStream << encryptedTransportPacket.func;
            packetOutputStream << encryptedTransportPacket.payload;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, EncryptedTransportPacket &encryptedTransportPacket)
        {
            packetInputStream >> encryptedTransportPacket.func;
            packetInputStream >> encryptedTransportPacket.payload;
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
