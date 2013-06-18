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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.Status;
import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.defined.Parameter.Type;
import com.cubeia.firebase.io.protocol.BadPacket;
import com.cubeia.firebase.io.protocol.CreateTableResponsePacket;
import com.cubeia.firebase.io.protocol.EncryptedTransportPacket;
import com.cubeia.firebase.io.protocol.Enums;
import com.cubeia.firebase.io.protocol.FilteredJoinCancelResponsePacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableAvailablePacket;
import com.cubeia.firebase.io.protocol.FilteredJoinTableResponsePacket;
import com.cubeia.firebase.io.protocol.ForcedLogoutPacket;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.io.protocol.GameVersionPacket;
import com.cubeia.firebase.io.protocol.GoodPacket;
import com.cubeia.firebase.io.protocol.JoinChatChannelResponsePacket;
import com.cubeia.firebase.io.protocol.JoinResponsePacket;
import com.cubeia.firebase.io.protocol.KickPlayerPacket;
import com.cubeia.firebase.io.protocol.LeaveResponsePacket;
import com.cubeia.firebase.io.protocol.LobbySubscribePacket;
import com.cubeia.firebase.io.protocol.LobbyUnsubscribePacket;
import com.cubeia.firebase.io.protocol.LocalServiceTransportPacket;
import com.cubeia.firebase.io.protocol.LoginResponsePacket;
import com.cubeia.firebase.io.protocol.MttPickedUpPacket;
import com.cubeia.firebase.io.protocol.MttRegisterResponsePacket;
import com.cubeia.firebase.io.protocol.MttSeatedPacket;
import com.cubeia.firebase.io.protocol.MttTransportPacket;
import com.cubeia.firebase.io.protocol.MttUnregisterResponsePacket;
import com.cubeia.firebase.io.protocol.NotifyChannelChatPacket;
import com.cubeia.firebase.io.protocol.NotifyInvitedPacket;
import com.cubeia.firebase.io.protocol.NotifyJoinPacket;
import com.cubeia.firebase.io.protocol.NotifyLeavePacket;
import com.cubeia.firebase.io.protocol.NotifyRegisteredPacket;
import com.cubeia.firebase.io.protocol.NotifySeatedPacket;
import com.cubeia.firebase.io.protocol.NotifyWatchingPacket;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.PingPacket;
import com.cubeia.firebase.io.protocol.PlayerInfoPacket;
import com.cubeia.firebase.io.protocol.PlayerQueryResponsePacket;
import com.cubeia.firebase.io.protocol.ProbePacket;
import com.cubeia.firebase.io.protocol.ProbeStamp;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;
import com.cubeia.firebase.io.protocol.SeatInfoPacket;
import com.cubeia.firebase.io.protocol.ServiceTransportPacket;
import com.cubeia.firebase.io.protocol.SystemInfoResponsePacket;
import com.cubeia.firebase.io.protocol.SystemMessagePacket;
import com.cubeia.firebase.io.protocol.TableChatPacket;
import com.cubeia.firebase.io.protocol.TableQueryResponsePacket;
import com.cubeia.firebase.io.protocol.TableRemovedPacket;
import com.cubeia.firebase.io.protocol.TableSnapshotListPacket;
import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
import com.cubeia.firebase.io.protocol.TableUpdateListPacket;
import com.cubeia.firebase.io.protocol.TableUpdatePacket;
import com.cubeia.firebase.io.protocol.TournamentRemovedPacket;
import com.cubeia.firebase.io.protocol.TournamentSnapshotListPacket;
import com.cubeia.firebase.io.protocol.TournamentSnapshotPacket;
import com.cubeia.firebase.io.protocol.TournamentUpdateListPacket;
import com.cubeia.firebase.io.protocol.TournamentUpdatePacket;
import com.cubeia.firebase.io.protocol.UnwatchResponsePacket;
import com.cubeia.firebase.io.protocol.VersionPacket;
import com.cubeia.firebase.io.protocol.WatchResponsePacket;
import com.cubeia.firebase.io.protocol.Enums.ResponseStatus;
import com.cubeia.firebase.io.protocol.Enums.TournamentAttributes;
import com.cubeia.firebase.io.protocol.Enums.WatchResponseStatus;

public class ManualPacketHandler extends AbstractClientPacketHandler {
    
	private Logger log = Logger.getLogger(ManualPacketHandler.class);
    
    private IOContext context;
    
    private boolean output = true;
    
    public ManualPacketHandler(IOContext context) {
    	this.context = context;
    }
    
    public ManualPacketHandler(IOContext context, boolean output) {
    	this.context = context;
    	this.output = output;
    }

    @Override
    public void visit(GoodPacket packet) {
    	if (output) System.out.println("Command "+packet.cmd+" accepted by server (Good)");
    }
    
	@Override
    public void visit(BadPacket packet) {
		if (output) System.out.println("Command "+packet.cmd+" denied by server (Bad).");
    }
	
    @Override
    public void visit(PlayerInfoPacket packet) {
    	if (output) System.out.println("Player: "+packet.nick+" ("+packet.pid+")");
    }

    public void visit(NotifyJoinPacket packet) {
    	if (output) System.out.println("[Table-"+packet.tableid+"] Player "+packet.pid+" joined at seat: "+packet.seat);
    }

    public void visit(NotifyLeavePacket packet) {
    	if (output) System.out.println("[Table-"+packet.tableid+"] Player "+packet.pid+" has left the table.");
    }
    
    public void visit(PingPacket packet) {
    	context.getConnector().sendPacket(packet);
    }

    public void visit(LoginResponsePacket packet) {
    	context.setPlayerId(packet.pid);
    	if (output) {
    		if (packet.status == ResponseStatus.OK) {
    			System.out.println("Logged in with screenname: "+packet.screenname+" id "+packet.pid);
    		} else {
    			System.out.println("Login failed. Status: "+packet.status+" Msg: "+packet.message);
    		}
    	}
    }

    public void visit(ProbePacket packet) {
    	ProbeStamp stampThis = new ProbeStamp();
    	stampThis.clazz = getClass().toString();
    	stampThis.timestamp = System.currentTimeMillis();
    	
    	packet.stamps.add(stampThis);
    	if (output) {
	        System.out.println("[Table-"+packet.tableid+"] Probe recieved:");
	        List<ProbeStamp> stamps = packet.stamps;
	        long base = -1;
	        for (ProbeStamp stamp : stamps) {
				if(base == -1) base = stamp.timestamp;
				long curr = stamp.timestamp;
				System.out.println(" - " + stamp.clazz.substring(stamp.clazz.lastIndexOf('.') + 1) + " - " + curr + " - " + (curr - base));
			}
    	}
    }

	@Override
    public void visit(TableChatPacket packet) {
		if (output) System.out.println("[Table-"+packet.tableid+"] Player "+packet.pid+" says: "+packet.message);
	}
	
	public void visit(NotifyChannelChatPacket packet) {
		if (output) System.out.println("[Chat-"+packet.channelid+"] "+packet.nick+"("+packet.pid+"): "+packet.message);
	}
	
	public void visit(JoinResponsePacket packet) {
		if (packet.status == Enums.JoinResponseStatus.OK) {
			if (output) System.out.println("You have joined table: "+packet.tableid+"   code["+packet.code+"]");
		} else if (packet.status == Enums.JoinResponseStatus.DENIED) {
			if (output) System.out.println("Join table denied by server  - code["+packet.code+"]");
		} else {
			if (output) System.out.println("Join Response : Table["+packet.tableid+"] seat["+packet.seat+"] status["+packet.status+"] code["+packet.code+"]");
		}
	}

	public void visit(WatchResponsePacket packet) {
		if (packet.status == WatchResponseStatus.OK) {
			if (output) System.out.println("You are now watching table: "+packet.tableid + ": " + packet);
		} else if (packet.status == WatchResponseStatus.DENIED || packet.status == WatchResponseStatus.DENIED_ALREADY_SEATED) {
			if (output) System.out.println("Watch table denied by server");
		} else {
			if (output) System.out.println("Watch Response : Table["+packet.tableid+"] status["+packet.status+"]");
		}
	}

	public void visit(UnwatchResponsePacket packet) {
		if (output) System.out.println("Unwatch Response : Table["+packet.tableid+"] status["+packet.status+"]");
	}

	public void visit(LeaveResponsePacket packet) {
		if (output) System.out.println("Leave Response : Table["+packet.tableid+"] status["+packet.status+"] code["+packet.code+"]");
	}

	public void visit(JoinChatChannelResponsePacket packet) {
		if (output) System.out.println("Join Chat Response : Channel["+packet.channelid+"] status["+packet.status+"]");
	}
	
	public void visit(FilteredJoinTableResponsePacket packet) {
		System.out.println("Filtered Join Response : seq["+packet.seq+"] gid["+packet.gameid+"] Address["+packet.address+"] status["+packet.status+"]");
	}
	
	public void visit(FilteredJoinCancelResponsePacket packet) {
		if (packet.status == Enums.ResponseStatus.OK) {
			System.out.println("Filtered Request ["+packet.seq+"] cancelled.");
		} else {
			System.out.println("Failed to cancel Filtered Request ["+packet.seq+"] Status: "+packet.status);
		}
	}

	public void visit(VersionPacket packet) {
		int local = new ProtocolObjectFactory().version();
		int remote = packet.protocol;
		if (local == remote) {
			System.out.println("Protocol version verified, version is: " + packet.protocol);
		} else {
			System.out.println("Protocol Version mismatch. Local:"+local+" Server:"+remote);
		}
	}

	public void visit(GameVersionPacket packet) {
		System.out.println("Remote Game Version: "+packet.version);
	}
	
	public void visit(SystemMessagePacket packet) {
		System.out.println("System Message: "+packet.message);
	}
	
	public void visit(NotifySeatedPacket packet) {
		System.out.println("I am sitting at table ["+packet.tableid+"] seat ["+packet.seat+"] mtt ["+packet.mttid+"] table inf["+packet.snapshot+"]");
	}
	
	public void visit(NotifyRegisteredPacket packet) {
		for (int id : packet.tournaments) {
			System.out.println("I am registered in tournament ["+id+"]");
		}
	}
	
	public void visit(NotifyWatchingPacket packet) {
		System.out.println("I was watching table ["+packet.tableid+"]");
	}
	
	@Override
    public void visit(LobbySubscribePacket arg0) {}

	@Override
    public void visit(LobbyUnsubscribePacket arg0) {}

	public void visit(TableSnapshotListPacket packet) {
		for (TableSnapshotPacket p : packet.snapshots) {
			visit(p);
		}
	}
	
    public void visit(TableSnapshotPacket packet) {
		try {
			String info = "Table ["+packet.tableid+"]\t  "+packet.name+"\t "+packet.seated+"/"+packet.capacity+"\t  "+ "domain: "+packet.address;
			// info += printTableParamters(packet.params);
			if (output) {
				System.out.println(info);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    public void visit(TableUpdateListPacket packet) {
    	for (TableUpdatePacket p : packet.updates) {
    		visit(p);
    	}
    }
	
	public void visit(TableUpdatePacket packet) {
		String info = "Table["+packet.tableid+"] Updated: seated["+packet.seated+"]";
		info += printParameters(packet.params);
		if (output) {
			System.out.println(info);
		}
	}
	
	public void visit(TableRemovedPacket packet) {
		if (output) {
			System.out.println("Table ["+packet.tableid+"]\tWas removed");
		}
	}

	public void visit(ForcedLogoutPacket packet) {
		if (output) System.out.println("I was forced to logout. Code["+packet.code+"] Msg["+packet.message+"]");
	}
	
	public void visit(FilteredJoinTableAvailablePacket packet) {
		System.out.println("Table available: seq["+packet.seq+"] Table["+packet.tableid+"] seat["+packet.seat+"]");		
	}
	
	public void visit(ServiceTransportPacket packet) {
		if (output) System.out.println("Service transport. Seq["+packet.seq+"] Data: "+new String(packet.servicedata));
	}

    @Override
    public void visit(GameTransportPacket packet) {}

	public void visit(KickPlayerPacket packet) {
		System.out.println("You were kicked from table: "+packet.tableid);
	}

	public void visit(SeatInfoPacket packet) {
		if (output) {
			PlayerInfoPacket p = packet.player;
			Enums.PlayerStatus status = packet.status;
			
			String info = "Seat Info for table ["+packet.tableid+"]\n";
			info += "\t Seat  : "+packet.seat+"\n";
			info += "\t Player: "+p.nick+"("+p.pid+")\n";
			info += "\t Status: "+status+"\n";
			
			System.out.println(info);
		}
	}

	public void visit(PlayerQueryResponsePacket packet) {
		if (output) System.out.println(packet);
	}

	public void visit(TableQueryResponsePacket packet) {
		String info = "TableQueryResponse: tableId[" + packet.tableid + "] status["+packet.status+"] seatinfo[ ";
		for (SeatInfoPacket s : packet.seats) {
			info += " seat[" + s.seat + "]" + " status[" + s.status + " playerinfo[";
			info += " nick[" + s.player.nick + "] pid["+ s.player.pid + "] details[";
			info += printParameters(s.player.details) + " ]]";
			info += "\n";
		}
		log.debug(info);
	}
	
	private String printParameters(List<Param> params) {
		String result = " Params : ";
		for (Param p : params) {
			Parameter<?> param = convertParamToParameter(p);
			result += " ["+param.getKey()+":"+param.getValue()+"]";
		}
		return result;
	}

	public static List<Parameter<?>> convertParamsToParameters(List<Param> params) {
		List<Parameter<?>> parameters = new ArrayList<Parameter<?>>();
		for (Param p : params) {
			parameters.add(convertParamToParameter(p));
		}
		return parameters;
	}
	
	public static Parameter<?> convertParamToParameter(Param p) {
		Parameter<?> param;
		if (p.type == Type.STRING.ordinal()) {
			param = convertParamToStringParameter(p);
		} else if (p.type == Type.INT.ordinal()) {
			param = convertParamToByteParameter(p);
		} else {
			throw new IllegalArgumentException("Unknown type: " + p.type);
		}
		return param;
	}
	
	public static Parameter<Integer> convertParamToByteParameter(Param p) {
		int i = DataUtil.byteArrayToInt(p.value);
		Parameter<Integer> param = new Parameter<Integer>(p.key, i, Type.STRING);
		return param;
	}

	public static Parameter<String> convertParamToStringParameter(Param p) {
		if (p.value.length < 3) return null; // Not enough data
		byte[] bs = new byte[p.value.length-1];
		ByteBuffer buf = ByteBuffer.wrap(p.value);
		buf.get();
		buf.get(bs);
		Parameter<String> param = new Parameter<String>(p.key, new String(bs), Type.STRING);
		return param;
	}
	
	public void visit(MttRegisterResponsePacket packet) {
		if (output) System.out.println("MTT Register: "+packet);
	}

	public void visit(MttUnregisterResponsePacket packet) {
		if (output) System.out.println("MTT Unregister: "+packet);
	}

	public void visit(LocalServiceTransportPacket packet) {
		if (output) System.out.println("Local Service: Seq:"+packet.seq+" Data:"+packet.servicedata.length+" bytes");
	}

	public void visit(MttTransportPacket packet) {}
	
	public void visit(TournamentSnapshotListPacket packet) {
		for (TournamentSnapshotPacket p : packet.snapshots) {
			visit(p);
		}
	}

	public void visit(TournamentSnapshotPacket packet) {
		try {
			String name = "n/a";
			int players = 0;
			int regged = 0;
			int cap = 0;
			for (Param p : packet.params) {
				if (p.key.equals(TournamentAttributes.NAME.toString())) name = convertParamToStringParameter(p).getValue();
				if (p.key.equals(TournamentAttributes.REGISTERED.toString())) regged = DataUtil.byteArrayToInt(p.value);
				if (p.key.equals(TournamentAttributes.CAPACITY.toString())) cap = DataUtil.byteArrayToInt(p.value);
				if (p.key.equals(TournamentAttributes.ACTIVE_PLAYERS.toString())) players = DataUtil.byteArrayToInt(p.value);
			}
			
			
			String info = "MTT ["+packet.mttid+"]\t  "+name+"\t ("+players+") "+regged+"/"+cap+"\t  "+
							"domain: "+packet.address;
			// info += printParameters(packet.params);
			System.out.println(info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void visit(TournamentUpdateListPacket packet) {
		for (TournamentUpdatePacket p : packet.updates) {
			visit(p);
		}
	}

	public void visit(TournamentUpdatePacket packet) {
		String info = "MTT["+packet.mttid+"] Updated: ";
		info += printParameters(packet.params);
		System.out.println(info);
	}

	public void visit(TournamentRemovedPacket packet) {
		System.out.println("Tournament removed: "+packet.mttid);
	}
	
	public void visit(MttSeatedPacket packet) {
		if (output) System.out.println("I was seated in the tournament["+packet.mttid+"] at table["+packet.tableid+"] and seat["+packet.seat+"]");
	}
	
	public void visit(MttPickedUpPacket packet) {
		if (output) System.out.println("I was picked up in the tournament["+packet.mttid+"] from table["+packet.tableid+"]");
	}

	public void visit(EncryptedTransportPacket packet) {
		if (output) System.out.println("Encrypted packet received: "+packet);
	}

	public void visit(CreateTableResponsePacket packet) {
		if (output) {
			
			Enums.ResponseStatus status = packet.status;
			
			String out = "Table Create Response:\n";
			out += "\t Status : "+status.name()+"\n";
			if (!status.equals(Enums.ResponseStatus.OK)) 
				out += "\t Code : "+packet.code+"\n";
			out += "\t Seq  : "+packet.seq+"\n";
			out += "\t Table: "+packet.tableid+"\n";
			out += "\t Seat : "+packet.seat+"\n";			
			System.out.println(out);
		}
	}

	public void visit(NotifyInvitedPacket packet) {
		if (output) {
			String out = "I was invited to join a table:\n";
			out += "\t Inviter: "+packet.screenname+" ("+packet.inviter+")\n";
			out += "\t Table  : "+packet.tableid+"\n";
			out += "\t Seat   : "+packet.seat+"\n";	
			System.out.println(out);
		}
	}

    public void visit(SystemInfoResponsePacket packet) {
        if (output) {
            System.out.println("Players online: "+packet.players);
            System.out.println(printParameters(packet.params));
        }
    }
	
}