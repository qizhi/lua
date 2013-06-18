// I AM AUTO-GENERATED, DON'T CHECK ME INTO SUBVERSION (or else...)

#ifndef TABLECHATPACKET_H_346075CD_INCLUDE
#define TABLECHATPACKET_H_346075CD_INCLUDE

#include "styx_support.h"
#include "PacketInputStream.h"
#include "PacketOutputStream.h"
#include "ProtocolEnums.h"
#include "ProtocolObject.h"


namespace com_cubeia_firebase_io_protocol
{

    class TableChatPacket : public styx::ProtocolObject {

    public:

        static const uint8_t CLASSID = 80;

        virtual uint8_t classId() {
            return CLASSID;
        }

        int32_t tableid;
        int32_t pid;
        std::string message;

        TableChatPacket() {}

        TableChatPacket(int32_t tableid, int32_t pid, std::string message) {
            this->tableid = tableid;
            this->pid = pid;
            this->message = message;
        }

        friend styx::PacketOutputStream& operator<<(styx::PacketOutputStream &packetOutputStream, const TableChatPacket &tableChatPacket)
        {
            packetOutputStream << tableChatPacket.tableid;
            packetOutputStream << tableChatPacket.pid;
            packetOutputStream << tableChatPacket.message;
            packetOutputStream.finish();
            return packetOutputStream;
        }

        friend styx::PacketInputStream & operator>>(styx::PacketInputStream &packetInputStream, TableChatPacket &tableChatPacket)
        {
            packetInputStream >> tableChatPacket.tableid;
            packetInputStream >> tableChatPacket.pid;
            packetInputStream >> tableChatPacket.message;
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
