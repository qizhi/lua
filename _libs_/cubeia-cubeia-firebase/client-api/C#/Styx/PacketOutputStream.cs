using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Net;

namespace Styx
{
    /**
     * Handles the serialization of the Styx wire format.
     */
    public class PacketOutputStream
    {
        /** maximum string legnth in bytes */
        public static int STRING_MAX_BYTES = 0xffff;

        private BinaryWriter writer;

        public PacketOutputStream(BinaryWriter writer)
        {
            this.writer = writer;
        }


        public void saveByte(byte val)
        {
            writer.Write(val);
        }

        public void saveUnsignedByte(byte val)
        {
            writer.Write(val);
        }

        public void saveUnsignedShort(ushort val)
        {
            writer.Write(System.Net.IPAddress.HostToNetworkOrder((short)val));
        }

        public void saveShort(short val)
        {
            writer.Write(System.Net.IPAddress.HostToNetworkOrder(val));
        }

        public void saveInt(int val)
        {
            writer.Write(System.Net.IPAddress.HostToNetworkOrder(val));
        }

        public void saveUnsignedInt(uint val)
        {
            writer.Write(System.Net.IPAddress.HostToNetworkOrder(val));
        }

        public void saveLong(long val)
        {
            writer.Write(System.Net.IPAddress.HostToNetworkOrder(val));
        }

        public void saveBool(bool val) 
        {
            writer.Write(val);
        }

        /**
         * Save the given string. 
         * An {@link IOException} will be thrown if the number of bytes of the string encoded in 
         * UTF-8 is greater than {@link #STRING_MAX_BYTES}.
         * @param val the string
         * @if the number of UTF-8 bytes of the string is >= {@link #STRING_MAX_BYTES}
         */
        public void saveString(String val)
        {
            if (val == null)
                val = "";

            // Create a UTF-8 encoding.
            UTF8Encoding utf8 = new UTF8Encoding();


            if (utf8.GetByteCount(val) > STRING_MAX_BYTES)
            {
                throw new IOException("String byte length is too long: bytes = " + utf8.GetByteCount(val)+ ", max allowed = " + 0xffff);
            }

            byte[] utf8Bytes = utf8.GetBytes(val);

            saveShort((short)utf8Bytes.Length);
            saveArray(utf8Bytes);
        }

        public void saveArray(byte[] gamedata)
        {
            writer.Write(gamedata);
        }

        public void saveArray(int[] data)
        {
            foreach (int val in data)
            {
                saveInt(val);
            }
        }

        public void saveArray(String[] removedParams)
        {
            foreach (String name in removedParams)
            {
                saveString(name);
            }
        }
    }
}
