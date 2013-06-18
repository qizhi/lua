/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.network.example.rps;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import com.cubeia.firebase.clients.java.connector.text.SimpleTextClient;
import com.cubeia.firebase.io.protocol.GameTransportPacket;

public class Client extends SimpleTextClient {

	public static final long GAME_ID = 1337L;
	private Integer tableId = null;
	
	public Client(String host, int port) {
		super(host, port);
	    CommandHandler handler = new CommandHandler(this);
	    addPacketHandler(handler);
	}
	
	@Override
	public void handleCommand(String command) {
	    try {
	       String[] args = command.split(" ");

	       if (args.length != 2) {
	           reportBadCommand("size="+args.length+", should be 2");
	           return;
	       }
			
	       int tableid = Integer.parseInt(args[0]);
	       ByteBuffer buf = ByteBuffer.wrap(args[1].getBytes());
				
	       // Sends data wrapped in a GameTransportPacket
	       // the context attribute is supplied by the super class
	       context.getConnector().sendDataPacket(tableid, context.getPlayerId(), buf);
				
	    } catch (Exception e) {
	       reportBadCommand(e.toString());
	    }
	}

	private void reportBadCommand(String error) {
	    System.err.println("Invalid command ("+error+") Format: TID cmd");
	}	
	
	public static void main(String[] args) {
	    if (args.length < 1) {
	    	System.err.println("Usage: java TextClient [port] host \nEx.: " +
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
       
        Client client = new Client(args[hostIndex], port);
        
//        client.handleLogin();
        
        client.run();
	}
	
	@SuppressWarnings("unused")
    private void handleLogin() {
        System.out.println("Username: ");
        String userName = getCommand();
        
        System.out.println("Password: ");
        String password = getCommand();
        
        commandNotifier.handleCommand("login " + userName + " " + password);
        
	}
	
	
	@Override
	public void run() {
//		commandNotifier.handleCommand("list 1337");
		
	    try{
	        while(!shutDown){
	            String command = getCommand();
	            
	            System.err.println("cmd: " + command);
	            System.err.println("isLoggedIn() == " + isLoggedIn());
	            
	            if (isLoggedIn()  &&  command.startsWith("play ")) {
	            	GameTransportPacket gtp = new GameTransportPacket(tableId, context.getPlayerId(), command.getBytes(), null);
	            	context.getConnector().sendPacket(gtp);
	            } else {
	            	commandNotifier.handleCommand(command); 
	            }

	            Thread.sleep(1000);
	        }
	        
	        System.err.println("going down...");
	        context.getConnector().shutdown();
	    } catch (Exception e) { 
	        System.out.println("Error in RUN: "+e); 
	        System.exit(1); 
	    } 
	}
	
	private boolean isLoggedIn() {
		return context.getPlayer() != null  &&  context.getPlayerId() > 0;
    }
	
	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}
}