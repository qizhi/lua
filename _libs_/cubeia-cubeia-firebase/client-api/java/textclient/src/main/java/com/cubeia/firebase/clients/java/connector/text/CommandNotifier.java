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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.ChannelChatPacket;
import com.cubeia.firebase.io.protocol.CreateTableRequestPacket;
import com.cubeia.firebase.io.protocol.FilteredJoinCancelRequestPacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableRequestPacket;
import com.cubeia.firebase.io.protocol.GameVersionPacket;
import com.cubeia.firebase.io.protocol.InvitePlayersRequestPacket;
import com.cubeia.firebase.io.protocol.JoinChatChannelRequestPacket;
import com.cubeia.firebase.io.protocol.JoinRequestPacket;
import com.cubeia.firebase.io.protocol.LeaveChatChannelPacket;
import com.cubeia.firebase.io.protocol.LeaveRequestPacket;
import com.cubeia.firebase.io.protocol.LobbyObjectSubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyObjectUnsubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyQueryPacket;
import com.cubeia.firebase.io.protocol.LobbySubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyUnsubscribePacket;
import com.cubeia.firebase.io.protocol.LocalServiceTransportPacket;
import com.cubeia.firebase.io.protocol.LoginRequestPacket;
import com.cubeia.firebase.io.protocol.LogoutPacket;
import com.cubeia.firebase.io.protocol.MttRegisterRequestPacket;
import com.cubeia.firebase.io.protocol.MttUnregisterRequestPacket;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.ParamFilter;
import com.cubeia.firebase.io.protocol.PlayerQueryRequestPacket;
import com.cubeia.firebase.io.protocol.ProbePacket;
import com.cubeia.firebase.io.protocol.ProbeStamp;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;
import com.cubeia.firebase.io.protocol.ServiceTransportPacket;
import com.cubeia.firebase.io.protocol.SystemInfoRequestPacket;
import com.cubeia.firebase.io.protocol.TableChatPacket;
import com.cubeia.firebase.io.protocol.TableQueryRequestPacket;
import com.cubeia.firebase.io.protocol.UnwatchRequestPacket;
import com.cubeia.firebase.io.protocol.VersionPacket;
import com.cubeia.firebase.io.protocol.WatchRequestPacket;
import com.cubeia.firebase.io.protocol.Enums.LobbyType;



/**
 * Parses and sends commands
 *
 */
public class CommandNotifier {

	private final IOContext context;

	private final boolean output;

	private final SimpleTextClient client;

	private int seq = 0;
	
	private int serviceSeq = 0;
	
    
    public CommandNotifier(IOContext context, SimpleTextClient client, boolean output) {
		this.context = context;
		this.client = client;
		this.output = output;
    }
    
    public void handleCommand(String command){
        // Divide string
        String[] args = command.split(" ");
        if (args.length <= 0) {
        	return;
        }
        
        
        if (!handleOpenCommand(command)) {
        	if (checkLogin()) {
	        	if (!handleLoggedInCommand(command)) {
	        		client.handleCommand(command);
	        	}
        	}
        }
    }
    
    private boolean handleOpenCommand(String command) {
    	String[] args = command.split(" ");
    	boolean handled = true;
    	
    	if( args[0].equalsIgnoreCase( Commands.EXIT ) ){
            SimpleTextClient.shutDown = true;
            
        } else if( args[0].equalsIgnoreCase( Commands.HELP ) ){
            //---------------------
            // HELP -> Display commands
            //---------------------
            displayCommands();
           
            
        } else if( args[0].equalsIgnoreCase( Commands.LOGIN ) ){
            //---------------------
            // LOGIN
            //---------------------
            try{
                LoginRequestPacket packet = new LoginRequestPacket();
                packet.user = args[1];
                packet.password = args[2];
                if (args.length > 3) {
                	packet.operatorid = Integer.parseInt(args[3]);
                }
                context.setPlayer(new Player(packet.user, -1));
    
                sendPacket(packet);
                
            }catch(Exception ex){
            	ex.printStackTrace();
                System.out.println("login <nick> <pwd>");
            }
            
        } else if( args[0].equalsIgnoreCase( Commands.LIST ) ){
            //---------------------
            // LIST TABLES
            //---------------------
        	try {
        		LobbyQueryPacket packet = new LobbyQueryPacket();
        		int offset = 0;
        		if (args[1].equalsIgnoreCase("mtt")) {
        			packet.type = LobbyType.MTT;
        			offset++;
        		} else {
        		    packet.type = LobbyType.REGULAR;
        		}
        		
        		String address = "";
        		for (int i = 2+offset; i < args.length; i++) {
        			address += "/" + args[i];
        		}
	            
	            if (args.length > 1+offset) {
	            	packet.gameid = Integer.parseInt(args[1+offset]);
	            } else {
	            	packet.gameid = -1;
	            }
	            packet.address = address;
	            
		        output("Getting Lobby for domain: "+address);
		        output("---------------------------------------");
	            sendPacket(packet)
	            ;
        	 }catch(Exception ex){
                 System.out.println("list (mtt) (GID) (FQN)");
             }
            
        } else if( args[0].equalsIgnoreCase( Commands.SUBSCRIBE ) ){
            //---------------------
            // SUBSCRIBE TO LOBBY
            //---------------------
            try{
                LobbySubscribePacket packet = new LobbySubscribePacket();
                packet.gameid = Integer.parseInt(args[1]);
                
                if (args.length > 2) {
                    packet.address = args[2];
                }
                
                if (args.length > 3) {
                    if (args[3].equalsIgnoreCase("mtt")) {
                        packet.type = LobbyType.MTT;
                    }
                }
                
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("sub <GID> (FQN) (table|mtt)");
            }
            
        } else if( args[0].equalsIgnoreCase( Commands.UNSUBSCRIBE ) ){
            //---------------------
            // UNSUBSCRIBE TO LOBBY
            //---------------------
            try{
                LobbyUnsubscribePacket packet = new LobbyUnsubscribePacket();
                packet.gameid = Integer.parseInt(args[1]);
                
                if (args.length > 2) {
                    packet.address = args[2];
                }
                
                if (args.length > 3) {
                    if (args[3].equalsIgnoreCase("mtt")) {
                        packet.type = LobbyType.MTT;
                    }
                }
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("unsub <GID> (FQN) (table|mtt)");
            }
            
        }  else if( args[0].equalsIgnoreCase( Commands.SUBSCRIBE_OBJECT ) ){
            //---------------------------
            // SUBSCRIBE TO LOBBY OBJECT
            //---------------------------
            try{
                LobbyObjectSubscribePacket packet = new LobbyObjectSubscribePacket();
                packet.gameid = Integer.parseInt(args[1]);
                
                if (args.length > 2) {
                    packet.address = args[2];
                }
                
                packet.objectid = Integer.parseInt(args[3]);
                
                packet.type = LobbyType.REGULAR;
                if (args.length > 4) {
                    if (args[4].equalsIgnoreCase("mtt")) {
                        packet.type = LobbyType.MTT;
                    }
                }
                
                sendPacket(packet);
                
            }catch(Exception ex){
                ex.printStackTrace();
                System.out.println("subo <GID> <FQN> <id> (table|mtt)");
            }
            
        } else if( args[0].equalsIgnoreCase( Commands.UNSUBSCRIBE_OBJECT ) ){
            //-----------------------------
            // UNSUBSCRIBE TO LOBBY OBJECT
            //-----------------------------
            try{
                LobbyObjectUnsubscribePacket packet = new LobbyObjectUnsubscribePacket();
                packet.gameid = Integer.parseInt(args[1]);
                
                if (args.length > 2) {
                    packet.address = args[2];
                }
                
                packet.objectid = Integer.parseInt(args[3]);
                
                packet.type = LobbyType.REGULAR;
                if (args.length > 4) {
                    if (args[4].equalsIgnoreCase("mtt")) {
                        packet.type = LobbyType.MTT;
                    }
                }
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("unsubo <GID> <FQN> <id> (table|mtt)");
            }
            
        } else if( args[0].equalsIgnoreCase( Commands.VERSION ) ){
            //---------------------
            // VERSION
            //---------------------
            try{
                VersionPacket packet = new VersionPacket();
                packet.game = 99;
                packet.operatorid = 0;
                packet.protocol = new ProtocolObjectFactory().version();
                
                sendPacket(packet);
                
            }catch(Exception ex){
            	ex.printStackTrace();
                System.out.println("version");
            }
            
        }  else if( args[0].equalsIgnoreCase( Commands.GAME_VERSION ) ){
            //---------------------
            // GAME VERSION
            //---------------------
            try{
               GameVersionPacket packet = new GameVersionPacket();
               packet.game = 99;
               packet.operatorid = 0;
               packet.version = "0";
                
               sendPacket(packet);
            	
            }catch(Exception ex){
            	ex.printStackTrace();
                System.out.println("gversion <GID>");
            }
            
        }  else if( args[0].equalsIgnoreCase( Commands.LOCAL_SERVICE ) ){
            //---------------------
            // LOCAL SERVICE TEST 
            //---------------------
            try{
                LocalServiceTransportPacket packet = new LocalServiceTransportPacket();
                packet.servicedata = args[1].getBytes();   
                packet.seq = serviceSeq++;
                System.out.println("Sending local service with seq: "+packet.seq);
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("loc <string>");
            }
           
        } else if( args[0].equalsIgnoreCase( Commands.SYSTEM_INFO ) ){
            //---------------------
            // SYSTEM INFO REQUEST
            //---------------------
            try{
                SystemInfoRequestPacket packet = new SystemInfoRequestPacket();
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("sysinfo");
            }
           
        } else {
        	handled = false;
        }
    	
    	return handled;
	}
    
    
    
    

	@SuppressWarnings("unchecked")
	private boolean handleLoggedInCommand(String command) {
		boolean handled = true;
		String[] args = command.split(" ");
		
		if( args[0].equalsIgnoreCase( Commands.WATCH_TABLE ) ){
            //---------------------
            // WATCH TABLE
            //---------------------
            try{
                WatchRequestPacket packet = new WatchRequestPacket();
                packet.tableid = Integer.parseInt( args[1] );               
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("watch <TID>");
            }
            
        } else if( args[0].equalsIgnoreCase( Commands.SIT_AT_TABLE ) ){
            //---------------------
            // JOIN TABLE
            //---------------------
            try{
                JoinRequestPacket packet = new JoinRequestPacket();
                packet.tableid = Integer.parseInt( args[1] );
                packet.seat = Byte.parseByte( args[2] );
                packet.params = Collections.EMPTY_LIST;
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("join <TID> <seat#>");
            }
            
        } else if( args[0].equalsIgnoreCase( Commands.FILTERED_JOIN_TABLE ) ){
            //---------------------
            // FILTERED JOIN TABLE
            //---------------------
            try{
                FilteredJoinTableRequestPacket packet = new FilteredJoinTableRequestPacket();
                packet.gameid = Integer.parseInt( args[1] );
                packet.address = args[2];
               
                if (args.length > 3) {
		            // Get parameters
		            String params = command.substring(args[0].length() + args[1].length() + args[2].length() + 2);
		            List<ParamFilter> filter = TableParameterParser.getFilteredParams(params);
		            packet.params = filter;
	            }
                packet.seq = seq ++;
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("fjoin <GID> <FQN> (FPM1) (FPM2) ...");
            }
            
        } else if( args[0].equalsIgnoreCase( Commands.CANCEL_FILTERED_JOIN_TABLE) ){
            //---------------------
            // CANCEL FILTERED JOIN TABLE
            //---------------------
            try{
                FilteredJoinCancelRequestPacket packet = new FilteredJoinCancelRequestPacket();
                packet.seq = Integer.parseInt( args[1] );

                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("cfjoin <SEQ>");
            }
            
        } else if( args[0].equalsIgnoreCase( Commands.LEAVE_TABLE ) ){
            //---------------------
            // LEAVE TABLE
            //---------------------
            try{
                LeaveRequestPacket packet = new LeaveRequestPacket();
                packet.tableid = Integer.parseInt( args[1] );
               
                output("Leaving table: "+packet.tableid);
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("leave <TID>");
            }
            
        } else if( args[0].equalsIgnoreCase( Commands.LOGOUT ) ){
            //---------------------
            // LOGOUT
            //---------------------
            try{
                LogoutPacket packet = new LogoutPacket();
                boolean leaveTables = false;
                if (args.length >= 2 && args[1].equalsIgnoreCase("true")) {
                    leaveTables = true;
                }
                packet.leaveTables = leaveTables;
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("logout (true)");
            }
            
        } else if( args[0].equalsIgnoreCase( Commands.STOP_WATCH_TABLE ) ){
            //---------------------
            // STOP WATCHING TABLE
            //---------------------
            try{
                UnwatchRequestPacket packet = new UnwatchRequestPacket();
                packet.tableid = Integer.parseInt( args[1] );
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("unwatch <TID>");
            }
            
        } else if( args[0].equalsIgnoreCase( Commands.PROBE ) ){
            //---------------------
            // PROBE
            //---------------------
            try{
            	ProbeStamp stamp = new ProbeStamp();
            	stamp.clazz = getClass().toString();
            	stamp.timestamp = System.currentTimeMillis();
            	
                ProbePacket packet = new ProbePacket();
                packet.tableid = Integer.parseInt( args[1] );
                packet.stamps = new ArrayList<ProbeStamp>();
                packet.stamps.add(stamp);
                //output("Probe table: "+packet.tableid);
                sendPacket(packet);
                
            }catch(Exception ex){
            	ex.printStackTrace();
                System.out.println("probe <TID>");
            }
           
        }else if( args[0].equalsIgnoreCase( Commands.JOIN_CHAT_CHANNEL ) ){
            //---------------------
            // JOIN CHAT CHANNEL
            //---------------------
            try{
                JoinChatChannelRequestPacket packet = new JoinChatChannelRequestPacket();
                packet.channelid = Integer.parseInt( args[1] );
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("joinchat <CID>");
            }
           
        }else if( args[0].equalsIgnoreCase( Commands.LEAVE_CHAT_CHANNEL	 ) ){
            //---------------------
            // LEAVE CHAT CHANNEL
            //---------------------
            try{
                LeaveChatChannelPacket packet = new LeaveChatChannelPacket();
                packet.channelid = Integer.parseInt( args[1] );
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("leavechat <CID>");
            }
           
        }else if( args[0].equalsIgnoreCase( Commands.CHANNEL_CHAT ) ){
            //---------------------
            // CHANNEL CHAT
            //---------------------
            try{
                ChannelChatPacket packet = new ChannelChatPacket();
                packet.channelid = Integer.parseInt( args[1] );
                packet.message = command.substring(args[0].length() + args[1].length()+2);
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("chat <CID> <msg>");
            }
           
        }else if( args[0].equalsIgnoreCase( Commands.TABLE_CHAT ) ){
            //---------------------
            // TABLE CHAT
            //---------------------
            try{
                TableChatPacket packet = new TableChatPacket();
                packet.tableid = Integer.parseInt( args[1] );
                packet.message = command.substring(args[0].length() + args[1].length()+2);
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("say <TID> <msg>");
            }
           
        } else if( args[0].equalsIgnoreCase( Commands.SERVICE ) ){
            //---------------------
            // SERVICE TRANSPORT
            //---------------------
            try{
                ServiceTransportPacket packet = new ServiceTransportPacket();
                packet.service = args[1];
                packet.servicedata = command.substring(args[0].length() + args[1].length()+2).getBytes();
                packet.seq = serviceSeq++;
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("service <SID> <msg>");
            }
           
        } else if( args[0].equalsIgnoreCase( Commands.PLAYER_QUERY ) ){
            //---------------------
            // PLAYER QUERY
            //---------------------
            try{
                PlayerQueryRequestPacket packet = new PlayerQueryRequestPacket();
                packet.pid = Integer.parseInt( args[1] );
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("pq <PID>");
            }
           
        } else if( args[0].equalsIgnoreCase( Commands.TABLE_QUERY ) ){
            //---------------------
            // TABLE QUERY
            //---------------------
            try {
                TableQueryRequestPacket packet = new TableQueryRequestPacket();
                packet.tableid = Integer.parseInt(args[1]);
                sendPacket(packet);
            } catch(Exception ex) {
                System.out.println("tq <TID>");
            }
           
        } else if( args[0].equalsIgnoreCase( Commands.MTT_REG ) ){
            //---------------------
            // MTT REGISTER
            //---------------------
            try{
            	MttRegisterRequestPacket packet = new MttRegisterRequestPacket();
                packet.mttid = Integer.parseInt( args[1] );
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("mttreg <MID>");
            }
           
        } else if( args[0].equalsIgnoreCase( Commands.MTT_UNREG ) ){
            //---------------------
            // MTT UNREGISTER
            //---------------------
            try{
            	MttUnregisterRequestPacket packet = new MttUnregisterRequestPacket();
                packet.mttid = Integer.parseInt( args[1] );
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("mttunreg <MID>");
            }
            
        }  else if( args[0].equalsIgnoreCase( Commands.CREATE_TABLE ) ){
            //---------------------
            // CREATE TABLE
            //---------------------
            try{
            	CreateTableRequestPacket packet = new CreateTableRequestPacket();
            	packet.gameid = Integer.parseInt(args[1]);
            	packet.seats = (byte) Integer.parseInt(args[2]);
            	packet.seq = seq++;
            	
            	int[] inv = new int[args.length - 3];
            	for (int i = 3; i < args.length; i++) {
            		inv[i-3] = Integer.parseInt(args[i]);
            	}
            	packet.invitees = inv;
            	packet.params = new ArrayList<Param>();
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("create <GID> <seats> (pid)*");
            }
            
        }  else if( args[0].equalsIgnoreCase( Commands.INVITE ) ){
            //---------------------
            // INVITE PLAYERS
            //---------------------
            try{
            	InvitePlayersRequestPacket packet = new InvitePlayersRequestPacket();
            	packet.tableid = Integer.parseInt(args[1]);
            	
            	
            	int[] inv = new int[args.length - 2];
            	for (int i = 2; i < args.length; i++) {
            		inv[i-2] = Integer.parseInt(args[i]);
            	}
            	packet.invitees = inv;
                sendPacket(packet);
                
            }catch(Exception ex){
                System.out.println("invite <TID> (pid)*");
            }
            
        } else {
        	handled = false;
        }
		return handled;
	}

	private boolean checkLogin() {
    	if (! (context.getPlayer() != null && context.getPlayerId() > 0)) {
    		output("You need to login first!");
    		return false;
    	} else {
    		return true;
    	}
    }
    

	private void displayCommands() {
		String cmdList = 
			"\n-----------------------------\n"+
			"Generic Commands: \n"+
			"\t help \n"+
			"\t exit \n"+
			"\t version \n"+
			"\t gversion <GID> \n"+
			"\t sysinfo \n"+
			"\t login <nick> <pwd> \n"+
			"\t logout (true) -Logout. Leave all tables if true\n"+
			"\t list (GID) (FQN) \n"+
			"\t sub <GID> (FQN) (table|mtt)\n"+
			"\t unsub <GID> (FQN) (table|mtt)\n"+
	        "\t subo <GID> <FQN> <id> (table|mtt)\n"+
	        "\t unsubo <GID> <FQN> <id> (table|mtt)\n"+
			"\t watch <TID> \n"+
			"\t unwatch <TID> \n"+
			"\t join <TID> <seat#> \n"+
			"\t fjoin <GID> <FQN> (FPM)*  -Filtered Join\n"+
			"\t cfjoin <SEQ> \n"+
			"\t leave <TID> \n"+
			"\t probe <TID> \n"+
			"\t joinchat <CID> \n"+
			"\t leavechat <CID> \n"+
			"\t chat <CID> <msg> \n"+
			"\t say <TID> <msg> \n"+
			"\t service <SID> <msg> \n"+
			"\t pq <PID>  -Player Query\n"+
			"\t tq <TID>  -Table Query\n"+
			"\t loc <msg>  -Local Service\n"+
			"\t create <GID> <seats> (pid)* -Create Table\n"+
			"\t invite <TID> (pid)* -Invite Player to a table\n"+
			
			"\nMTT Commands:\n"+
			"\t mttreg <MID>  -MTT Register\n"+
			"\t mttunreg <MID>  -MTT Unregister\n"+
			
			"\n"+
			
			"-----------------------------\n\n"+
			
			"<x> = Needed parameter \n"+
			"(x) = Optional parameter \n"+
			"*   = Multiple instances allowed\n"+
			"PID = Player id \n"+
			"TID = Table id \n"+
			"GID = Game id \n"+
			"CID = Chat Channel id \n"+
			"FQN = Lobby path (e.g. /99/a/b)\n"+
			"FPM = Filter parameter (e.g. apa=banan, seated>3)\n"+
			"SEQ = Sequential number (packet id)\n"+
			"\n\n";
		
			System.out.println(cmdList);
		
	}

    
    
    public void sendPacket(ProtocolObject packet){
        context.getConnector().sendPacket(packet);
    }
    
    
    
    private void output(String text) {
    	if (output) {
    		System.out.println(text);
    	}
    }
}
