// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef MTTPICKEDUPPACKET_H_57FCC5CB_INCLUDE
#define MTTPICKEDUPPACKET_H_57FCC5CB_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class MttPickedUpPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 210;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t mttid;
        int32_t tableid;
        bool keep_watching;

        MttPickedUpPacket() {}

        MttPickedUpPacket(int32_t mttid, int32_t tableid, bool keep_watching) {
            this->mttid = mttid;
            this->tableid = tableid;
            this->keep_watching = keep_watching;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const MttPickedUpPacket &mttPickedUpPacket)
        {
            packetOutputStream << mttPickedUpPacket.mttid;
            packetOutputStream << mttPickedUpPacket.tableid;
            packetOutputStream << mttPickedUpPacket.keep_watching;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, MttPickedUpPacket &mttPickedUpPacket)
        {
            packetInputStream >> mttPickedUpPacket.mttid;
            packetInputStream >> mttPickedUpPacket.tableid;
            packetInputStream >> mttPickedUpPacket.keep_watching;
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
