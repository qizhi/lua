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


#ifndef STYX_PACKETINPUTSTREAM_H
#define STYX_PACKETINPUTSTREAM_H

#include "styx_support.h"

#include <string>
#include <cassert>
#include <memory>

namespace styx {

    class PacketInputStream
    {
    public:

        explicit PacketInputStream(const StyxBuffer & input, bool hasHeader = true);
        PacketInputStream & operator>>(bool &);
        PacketInputStream & operator>>(std::string &);

        template<class T>
        PacketInputStream & operator>>(T & value)
        {
            T newValue;
            assert(available() >= sizeof(T));
            memcpy(&newValue, &(styxBuffer[readPosition]), sizeof(T));
            value = byteSwap(newValue);
            readPosition += sizeof(T);
            return *this;
        }

        uint8_t class_id() const
        {
            return classId;
        }

    private:

        uint32_t available() const
        {
            return static_cast<uint32_t>(styxBuffer.size()) - readPosition;
        }

        uint8_t     classId;
        uint32_t    readPosition;
        StyxBuffer  styxBuffer;
        uint32_t length;
    };

    template<class T>
    PacketInputStream & operator>>(PacketInputStream & packetInputStream, std::vector<T> & packetList)
    {
        int32_t length;
        packetInputStream >> length;
        assert(length >= 0);
        packetList.resize(length);
        for (int32_t i = 0; i != length; i ++) {
            packetInputStream >> packetList[i];
        }
        return packetInputStream;
    }

}
#endif // STYX_PACKETINPUTSTREAM_H
