// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef LOGINRESPONSEPACKET_H_59CECF05_INCLUDE
#define LOGINRESPONSEPACKET_H_59CECF05_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class LoginResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 11;

        virtual uint8_t classId() {
            return CLASSID;
        }

        std::string screenname;
        int32_t pid;
        ResponseStatus::Enum status;
        int32_t code;
        std::string message;
        std::vector<uint8_t> credentials;

        LoginResponsePacket() {}

        LoginResponsePacket(std::string screenname, int32_t pid, ResponseStatus::Enum status, int32_t code, std::string message, std::vector<uint8_t> credentials) {
            this->screenname = screenname;
            this->pid = pid;
            this->status = status;
            this->code = code;
            this->message = message;
            this->credentials = credentials;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const LoginResponsePacket &loginResponsePacket)
        {
            packetOutputStream << loginResponsePacket.screenname;
            packetOutputStream << loginResponsePacket.pid;
            packetOutputStream << static_cast<uint8_t>(loginResponsePacket.status);
            packetOutputStream << loginResponsePacket.code;
            packetOutputStream << loginResponsePacket.message;
            packetOutputStream << loginResponsePacket.credentials;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, LoginResponsePacket &loginResponsePacket)
        {
            packetInputStream >> loginResponsePacket.screenname;
            packetInputStream >> loginResponsePacket.pid;
            {
                uint8_t temp;
                packetInputStream >> temp;
                loginResponsePacket.status = static_cast<ResponseStatus::Enum>(temp);
            }
            packetInputStream >> loginResponsePacket.code;
            packetInputStream >> loginResponsePacket.message;
            packetInputStream >> loginResponsePacket.credentials;
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
