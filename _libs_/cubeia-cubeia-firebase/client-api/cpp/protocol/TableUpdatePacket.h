// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TABLEUPDATEPACKET_H_528CED92_INCLUDE
#define TABLEUPDATEPACKET_H_528CED92_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"
#include "Param.h"


namespace com_cubeia_firebase_io_protocol
{

    class TableUpdatePacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 144;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        int16_t seated;
        std::vector<Param> params;
        std::vector<std::string> removed_params;

        TableUpdatePacket() {}

        TableUpdatePacket(int32_t tableid, int16_t seated, std::vector<Param> params, std::vector<std::string> removed_params) {
            this->tableid = tableid;
            this->seated = seated;
            this->params = params;
            this->removed_params = removed_params;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TableUpdatePacket &tableUpdatePacket)
        {
            packetOutputStream << tableUpdatePacket.tableid;
            packetOutputStream << tableUpdatePacket.seated;
            packetOutputStream << tableUpdatePacket.params;
            packetOutputStream << tableUpdatePacket.removed_params;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TableUpdatePacket &tableUpdatePacket)
        {
            packetInputStream >> tableUpdatePacket.tableid;
            packetInputStream >> tableUpdatePacket.seated;
            packetInputStream >> tableUpdatePacket.params;
            packetInputStream >> tableUpdatePacket.removed_params;
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
