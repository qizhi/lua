// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef LOGINREQUESTPACKET_H_15FF377F_INCLUDE
#define LOGINREQUESTPACKET_H_15FF377F_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class LoginRequestPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 10;

        virtual uint8_t classId() {
            return CLASSID;
        }

        std::string user;
        std::string password;
        int32_t operatorid;
        std::vector<uint8_t> credentials;

        LoginRequestPacket() {}

        LoginRequestPacket(std::string user, std::string password, int32_t operatorid, std::vector<uint8_t> credentials) {
            this->user = user;
            this->password = password;
            this->operatorid = operatorid;
            this->credentials = credentials;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const LoginRequestPacket &loginRequestPacket)
        {
            packetOutputStream << loginRequestPacket.user;
            packetOutputStream << loginRequestPacket.password;
            packetOutputStream << loginRequestPacket.operatorid;
            packetOutputStream << loginRequestPacket.credentials;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, LoginRequestPacket &loginRequestPacket)
        {
            packetInputStream >> loginRequestPacket.user;
            packetInputStream >> loginRequestPacket.password;
            packetInputStream >> loginRequestPacket.operatorid;
            packetInputStream >> loginRequestPacket.credentials;
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
