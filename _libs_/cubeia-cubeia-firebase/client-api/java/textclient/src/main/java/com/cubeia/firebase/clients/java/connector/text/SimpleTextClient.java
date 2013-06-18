/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cubeia.firebase.clients.java.connector.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import com.cubeia.firebase.clients.java.connector.Connector;
import com.cubeia.firebase.clients.java.connector.Encryption;
import com.cubeia.firebase.clients.java.connector.PacketListener;
import com.cubeia.firebase.clients.java.connector.SocketConnector;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.io.protocol.PacketVisitor;

/**
 * This is the client that should be used for manual input,
 * i.e. text based commands.
 * 
 * @author Fredrik
 *
 */
public class SimpleTextClient {

    // Shutdown all connections? yes|no
    public static volatile boolean shutDown = false;
	
    protected IOContext context;
    
    protected CommandNotifier commandNotifier;
    
    /**
     * Create a Text Client for the supplied connection.
     * 
     * @param context
     */
    public SimpleTextClient(String host, int port, boolean output) {
    	try {
    		Connection connector = new ConnectionImpl(host, port); // NioConnectorImpl(host, port);
	        context = new IOContext();
	        context.setConnector(connector);
	        
			commandNotifier = new CommandNotifier(context, this, true);
			
			// The Text Client's packet handler 
	        ManualPacketHandler packetHandler = new ManualPacketHandler(context, output);
	        // Register packer handler
	        this.context.getConnector().addPacketHandler(packetHandler);
	        
	        // Send a version packet
	        commandNotifier.handleCommand("version"); 
	        
    	} catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    /**
     * Create a Text Client for the supplied connection.
     * 
     * @param context
     */
    public SimpleTextClient(String host, int port) {
    	this(host, port, true);
    }
    
    protected void addPacketHandler(PacketVisitor handler) {
    	this.context.getConnector().addPacketHandler(handler);
    }
    
    /**
     * Override this in your implementation to handle
     * client specific commands.
     * 
     * @param command
     */
    public void handleCommand(String command) {
    	System.out.println("Unknown command: " + command.split(" ")[0]);
	}
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
            System.err.println("Usage: java SimpleTextClient [port] host \nEx.: " +
            		"\n\t java TextClient localhost" +
            		"\n\t java TextClient 4123 localhost");
            return;
        }
		

        int hostIndex = 0;
        int port = 4123; // Default
        
        // If the first argument is a string of digits then we take that
        // to be the port number to use
        if (Pattern.matches("[0-9]+", args[0])) {
            port = Integer.parseInt(args[0]);            
            hostIndex = 1;
        }
       
        SimpleTextClient client = new SimpleTextClient(args[hostIndex], port);
        client.run();


	}

    /**
     * Reads from prompt
     * 
     * @return
     */
    public static String getCommand(){
        String command = "";
        //  open up standard input 
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
        try { 
            command = br.readLine(); 
        
           // br.close();
        } catch (IOException ioe) { 
            System.out.println("IO error trying to read command"); 
            System.exit(1); 
         } 
         return command;
    }
    
    /**
     *  Run
     *
     */
    public void run() {
        try{
            while( !shutDown ){
                String command = getCommand();
                commandNotifier.handleCommand(command); 
                Thread.sleep(1000);
            }
        } catch (Exception e) { 
            System.out.println("Error in RUN: "+e); 
            System.exit(1); 
         } 
    }
    
    
    // --- PRIVATE CLASSES --- //
    
    private static class ConnectionImpl implements Connection {

    	private final Connector conn;
    	private final java.util.List<PacketVisitor> list = new CopyOnWriteArrayList<PacketVisitor>();
    	
    	private ConnectionImpl(String host, int port) throws IOException, GeneralSecurityException {
    		System.out.println("Connecting with encrytpion " + getEncryption() + " to " + host + ":" + port);
    		if(Constants.USE_HANDSHAKE) {
    			System.out.println("Connecting with handshake: " + Constants.HANDHAKE);
    		}
    		conn = new SocketConnector(host, port, null, getEncryption(), Constants.USE_HANDSHAKE, Constants.HANDHAKE);
    		conn.addListener(new PacketListener() {
				
				@Override
				public void packetRecieved(ProtocolObject packet) {
					for (PacketVisitor v : list) {
						try {
		    				packet.accept(v);
		    			} catch (Throwable th) {
		    				System.err.println("Packet handler: "+v+" could not handle packet: "+packet);
		    				th.printStackTrace();
		    			}
					}
				}
			});
    		conn.connect();
    	}
    	
		@Override
		public void addPacketHandler(PacketVisitor handler) {
			list.add(handler);
		}

		@Override
		public void removePacketHandler(PacketVisitor handler) {
			list.remove(handler);
		}

		@Override
		public void sendDataPacket(int table, int player, ByteBuffer data) {
			GameTransportPacket packet = new GameTransportPacket();
			packet.tableid = table;
			packet.pid = player;
			packet.gamedata = data.array();
			sendPacket(packet);
		}

		@Override
		public void sendPacket(ProtocolObject packet) {
			conn.send(packet);
		}

		@Override
		public void shutdown() {
			conn.disconnect();
		}
		
		private Encryption getEncryption() {
			if(Constants.USE_NAIVE_SSL) {
				return Encryption.NAIVE_SSL;
			} else if(Constants.USE_SSL) {
				return Encryption.SSL;
			} else if(Constants.USE_NATIVE) {
				return Encryption.FIREBASE_NATIVE;
			} else {
				return Encryption.NONE;
			}
		}
    }
}
