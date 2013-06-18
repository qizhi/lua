/*
Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*/


#ifndef STYX_PACKETOUTPUTSTREAM_H
#define STYX_PACKETOUTPUTSTREAM_H

#include "styx_support.h"

#include <string>
#include <cassert>
#include <memory>

namespace styx {

    class PacketOutputStream
    {
    public:
        PacketOutputStream(uint8_t class_id);
        PacketOutputStream& operator<<(bool);
        PacketOutputStream& operator<<(const std::string &);

        template<class T>
        PacketOutputStream & operator<<(T value)
        {
            value = byteSwap(value);
            uint8_t valueBuffer[sizeof(T)];
            memcpy(valueBuffer, &value, sizeof(T));
            write(valueBuffer, sizeof(T));
            return *this;
        }

        void write(const void *bytes, int32_t length);
        uint32_t finish(void);
        const StyxBuffer &packet() const;
        void reset(uint8_t class_id);

    private:
        StyxBuffer styxBuffer;
    };

    template<class T>
    PacketOutputStream& operator<<(PacketOutputStream& packetOutputStream, const std::vector<T> &packetList)
    {
        packetOutputStream << int32_t(packetList.size());
        for(unsigned int i = 0; i != packetList.size(); i++) {
            packetOutputStream << packetList[i];
        }
        return packetOutputStream;
    }

}

#endif // STYX_PACKETOUTPUTSTREAM_H
