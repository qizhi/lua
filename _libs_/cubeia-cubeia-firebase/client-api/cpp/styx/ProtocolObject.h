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

#ifndef STYX_PROTOCOLOBJECT_H
#define STYX_PROTOCOLOBJECT_H

#include "styx_support.h"

namespace styx
{

    class ProtocolObject {
    public:
        ProtocolObject() {}
        virtual ~ProtocolObject() {}

        virtual uint8_t classId() = 0;
        virtual void load(const StyxBuffer &buffer) = 0;
        virtual StyxBuffer save(void) const = 0;
    };
}

#endif // STYX_PROTOCOLOBJECT_H
