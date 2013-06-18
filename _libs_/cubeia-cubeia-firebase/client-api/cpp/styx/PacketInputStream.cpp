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


#include "PacketInputStream.h"

#include <algorithm>

namespace styx
{
    PacketInputStream::PacketInputStream(const StyxBuffer & input, bool hasHeader) :
      readPosition(0),
      styxBuffer(input)
    {
        if ( hasHeader ) {
            assert(input.size() >= 5);
            (*this) >> length;
            (*this) >> classId;
        }
    }

    PacketInputStream & PacketInputStream::operator>>(bool &value)
    {
        assert(available() >= 1);
        value = styxBuffer[readPosition++] == 0 ? false : true;
        return *this;
    }

    PacketInputStream & PacketInputStream::operator>>(std::string &value)
    {
        uint16_t length;
        (*this) >> length;
        assert(available() >= uint32_t(length));
        value.resize(length);
        if(length > 0)
        {
            uint8_t * string_start = &styxBuffer[readPosition];
            std::copy(string_start, string_start+length, value.begin());
            readPosition += length;
        }
        return *this;
    }

}
