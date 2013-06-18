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


#ifndef STYX_SUPPORT_H
#define STYX_SUPPORT_H

#ifndef _MSC_VER
    #include <stdint.h>
#else
    #include <msc_stdint.h>
#endif

#include <vector>
#include <string.h>
#include <string>

#ifndef LITTLE_ENDIAN
    #define LITTLE_ENDIAN	0
#endif

#ifndef BIG_ENDIAN
    #define BIG_ENDIAN		1
#endif

#define STYX_PACKET_LENGTH      4

#define STYX_UNUSED(x) (void)x;

namespace styx
{
    typedef std::vector<uint8_t> StyxBuffer;

    /**
    * detectEndian.
    * Detect edianness of the current platform.
    *
    * @return LITLLE_ENDIAN or BIG_ENDIAN
    */
    static int detectEndian(void)
    {
        int32_t testInteger = 1;
        char *test = reinterpret_cast<char *>(&testInteger);
        return *test == 1 ? LITTLE_ENDIAN : BIG_ENDIAN;
    }

    // Initialize once
    static int byteOrder = detectEndian();

    /**
    * byteSwap.
    * Swaps byte order if necessary.
    */
    template<class T>
    T byteSwap(T val)
    {
        if ( byteOrder == LITTLE_ENDIAN ) {
                T res;
                unsigned char * ip = reinterpret_cast<unsigned char*>(&val);
                unsigned char * op = reinterpret_cast<unsigned char*>(&res);
                for(unsigned i = 0; i != sizeof(T); ++i) {
                    op[sizeof(T)-i-1] = ip[i];
                }
                return res;
        } else {
            return val;
        }
    }
}

#endif // STYX_SUPPORT_H
