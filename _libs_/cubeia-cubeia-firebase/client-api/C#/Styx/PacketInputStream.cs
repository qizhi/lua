using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Net;


namespace Styx
{
    /**
     * Handles the deserialization of the Styx wire format.
     */
    public class PacketInputStream
    {
        private BinaryReader reader;

        public PacketInputStream(BinaryReader reader)
        {
            this.reader = reader;
        }

        public byte loadByte()
        {
            return reader.ReadByte();
        }

        public byte loadUnsignedByte()
        {
            return reader.ReadByte();
        }

        public ushort loadUnsignedShort()
        {
            return (ushort)System.Net.IPAddress.NetworkToHostOrder((short)reader.ReadUInt16());
        }

        public short loadShort()
        {
            return System.Net.IPAddress.NetworkToHostOrder(reader.ReadInt16());
        }

        public int loadInt()
        {
            return System.Net.IPAddress.NetworkToHostOrder(reader.ReadInt32());
        }

        public uint loadUnsignedInt()
        {
            return (uint)System.Net.IPAddress.NetworkToHostOrder(reader.ReadUInt32());
        }

        public long loadLong()
        {
            return System.Net.IPAddress.NetworkToHostOrder(reader.ReadInt64());
        }

        public bool loadBool()
        {
            return (reader.ReadByte() != 0);
        }

        public unsafe String loadString()
        {
            ushort length = loadUnsignedShort();
            byte[] utf8 = reader.ReadBytes(length);

            UTF8Encoding encoding = new UTF8Encoding(true, true);


            // Instruct the Garbage Collector not to move the memory
            fixed (void* pAsciiChars = utf8)
            {
                String utf8String = new String((sbyte*)pAsciiChars, 0, utf8.Length, encoding);
                return utf8String;
            }
        }


        public void loadByteArray(byte[] arg0)
        {
            reader.Read(arg0, 0, (int) reader.BaseStream.Length);
        }

        public void loadIntArray(int[] data)
        {
            for (int i = 0; i < data.Length; i++)
            {
                data[i] = reader.ReadInt32();
            }
        }

        public void loadStringArray(String[] removedParams)
        {
            for (int i = 0; i < removedParams.Length; i++)
            {
                removedParams[i] = loadString();
            }
        }
    }
}

