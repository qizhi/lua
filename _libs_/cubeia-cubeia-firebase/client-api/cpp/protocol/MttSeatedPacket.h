// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef MTTSEATEDPACKET_H_921D2C5_INCLUDE
#define MTTSEATEDPACKET_H_921D2C5_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class MttSeatedPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 209;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t mttid;
        int32_t tableid;
        int8_t seat;

        MttSeatedPacket() {}

        MttSeatedPacket(int32_t mttid, int32_t tableid, int8_t seat) {
            this->mttid = mttid;
            this->tableid = tableid;
            this->seat = seat;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const MttSeatedPacket &mttSeatedPacket)
        {
            packetOutputStream << mttSeatedPacket.mttid;
            packetOutputStream << mttSeatedPacket.tableid;
            packetOutputStream << mttSeatedPacket.seat;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, MttSeatedPacket &mttSeatedPacket)
        {
            packetInputStream >> mttSeatedPacket.mttid;
            packetInputStream >> mttSeatedPacket.tableid;
            packetInputStream >> mttSeatedPacket.seat;
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
