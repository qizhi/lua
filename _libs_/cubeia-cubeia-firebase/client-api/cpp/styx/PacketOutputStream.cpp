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

#include "PacketOutputStream.h"

#include <cassert>

namespace styx {

    PacketOutputStream::PacketOutputStream(uint8_t classId)
    {
      reset(classId);
    }

    PacketOutputStream & PacketOutputStream::operator<<(bool value)
    {
        styxBuffer.push_back(static_cast<uint8_t>(value));
        return *this;
    }

    PacketOutputStream & PacketOutputStream::operator<<(const std::string & value)
    {
        assert(value.size() <= 65535u);
        uint16_t length = static_cast<uint16_t>(value.size());
        (*this) << length;
        write(value.c_str(), length);
        return *this;
    }

    void PacketOutputStream::write(const void *buffer, int32_t length)
    {
        const uint8_t * bytesBuffer = static_cast<const uint8_t*>(buffer);
        styxBuffer.insert(styxBuffer.end(), bytesBuffer, bytesBuffer + length);
    }

    uint32_t PacketOutputStream::finish(void)
    {
        uint32_t value = byteSwap(static_cast<uint32_t>(styxBuffer.size()));
        uint32_t *lengthPtr = reinterpret_cast<uint32_t*>(&styxBuffer[0]);
        *lengthPtr = value;
        return static_cast<uint32_t>(styxBuffer.size());
    }

    void PacketOutputStream::reset(uint8_t classId)
    {
        styxBuffer.clear();
        // Reserve space for length
        styxBuffer.resize(STYX_PACKET_LENGTH);

        (*this) << classId;
    }

    const StyxBuffer &PacketOutputStream::packet() const
    {
        return styxBuffer;
    }
}
