using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Net;
namespace Styx
{
    /**
     * The styx wire protocol serializer/deserializer. This class is the
     * primary interface to be used by users of the Styx wire protocol.
     */
    public class StyxSerializer
    {
        public static int HEADER_SIZE = 4;
        private ObjectFactory factory;

        /**
         * Initialize this class with the automatically generated
         * ObjectFactory
         */
        public StyxSerializer(ObjectFactory factory)
        {
            this.factory = factory;
        }

        /**
         * Unpack a byte sequence into a concrete ProtocolObject.
         */
        public ProtocolObject unpack(byte[] inBuffer)
        {

            MemoryStream memStream = new MemoryStream(inBuffer);
            BinaryReader reader = new BinaryReader(memStream);
            
            int payloadLength = System.Net.IPAddress.NetworkToHostOrder(reader.ReadInt32());

            // Styx by default uses length exclusive from the length header
            if ((memStream.Length - memStream.Position) < payloadLength - HEADER_SIZE)
                throw new DataMisalignedException("Packet not fully read! Want " + payloadLength + " bytes, available: " + (int)(memStream.Length - memStream.Position));

            byte classId = reader.ReadByte();
            ProtocolObject po = factory.create(classId);
            po.load(new PacketInputStream(reader));

            return po;
        }


        public byte[] pack(ProtocolObject obj)
        { 
            byte[] outBuffer = packObject(obj);
            MemoryStream memStream = new MemoryStream();
            BinaryWriter writer = new BinaryWriter(memStream);

            writer.Write(System.Net.IPAddress.HostToNetworkOrder(outBuffer.Length + 5));
            writer.Write(obj.classId());
            writer.Write(outBuffer);
            byte[] outBuf = new byte[memStream.Length];
            memStream.Position = 0;
            memStream.Read(outBuf, 0, (int)memStream.Length);
            return outBuf;
        }

        private byte[] packObject(ProtocolObject obj)
        {
            MemoryStream memStream = new MemoryStream();
            BinaryWriter writer = new BinaryWriter(memStream);
            PacketOutputStream pos = new PacketOutputStream(writer);
            obj.save(pos);
            byte[] outBuf = new byte[memStream.Length];
            memStream.Position = 0;
            memStream.Read(outBuf, 0, (int)memStream.Length);
            return outBuf;
        }
    }
}




