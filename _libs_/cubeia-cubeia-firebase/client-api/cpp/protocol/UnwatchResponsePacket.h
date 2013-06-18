// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef UNWATCHRESPONSEPACKET_H_11CB026D_INCLUDE
#define UNWATCHRESPONSEPACKET_H_11CB026D_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class UnwatchResponsePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 35;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        ResponseStatus::Enum status;

        UnwatchResponsePacket() {}

        UnwatchResponsePacket(int32_t tableid, ResponseStatus::Enum status) {
            this->tableid = tableid;
            this->status = status;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const UnwatchResponsePacket &unwatchResponsePacket)
        {
            packetOutputStream << unwatchResponsePacket.tableid;
            packetOutputStream << static_cast<uint8_t>(unwatchResponsePacket.status);
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, UnwatchResponsePacket &unwatchResponsePacket)
        {
            packetInputStream >> unwatchResponsePacket.tableid;
            {
                uint8_t temp;
                packetInputStream >> temp;
                unwatchResponsePacket.status = static_cast<ResponseStatus::Enum>(temp);
            }
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
