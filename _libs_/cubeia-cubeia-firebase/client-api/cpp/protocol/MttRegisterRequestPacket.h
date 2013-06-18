// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef MTTREGISTERREQUESTPACKET_H_23E513B5_INCLUDE
#define MTTREGISTERREQUESTPACKET_H_23E513B5_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "Param.h"


namespace com_cubeia_firebase_io_protocol
{

    class MttRegisterRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 205;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t mttid;
        std::vector<Param> params;

        MttRegisterRequestPacket() {}

        MttRegisterRequestPacket(int32_t mttid, std::vector<Param> params) {
            this->mttid = mttid;
            this->params = params;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const MttRegisterRequestPacket &mttRegisterRequestPacket)
        {
            packetOutputStream << mttRegisterRequestPacket.mttid;
            packetOutputStream << mttRegisterRequestPacket.params;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, MttRegisterRequestPacket &mttRegisterRequestPacket)
        {
            packetInputStream >> mttRegisterRequestPacket.mttid;
            packetInputStream >> mttRegisterRequestPacket.params;
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
