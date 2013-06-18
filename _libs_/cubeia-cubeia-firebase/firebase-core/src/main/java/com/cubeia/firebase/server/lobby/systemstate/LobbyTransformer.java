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
package com.cubeia.firebase.server.lobby.systemstate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.util.ParameterUtil;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
import com.cubeia.firebase.io.protocol.TournamentSnapshotPacket;
import com.cubeia.firebase.server.lobby.model.TableInfo;
import com.cubeia.firebase.server.lobby.model.TournamentInfo;

public class LobbyTransformer {
	
	private static transient Logger log = Logger.getLogger(LobbyTransformer.class);
	
	/**
     * Transforms a collection of table infos into a collection of
     * GameSummaryPackets.
     * 
     * @param tables
     * @return
     */
    public static Collection<ProtocolObject> transform(Collection<TableInfo> tables) {
        Collection<ProtocolObject> data = new ArrayList<ProtocolObject>(tables.size());
        for (TableInfo table : tables) {            
            data.add(transform(table));
        }
        return data;
    }
    
    /**
     * Transforms a table info into a GameSummaryPacket
     * 
     * FIXME unsafe cast below
     * 
     * @param table
     * @return
     */
    public static ProtocolObject transform(TableInfo table) {
        TableSnapshotPacket snapshot = new TableSnapshotPacket();
        snapshot.address = table.getFqn();
        snapshot.name = table.getName();
        snapshot.tableid = table.getId();
        snapshot.capacity = (short)table.getCapacity();
        snapshot.seated = (short)table.getSeated();
        // snapshot.params = new ArrayList<Param>();
        // Add all parameters
        snapshot.params = getProtocolParameters(table.getParams());
        
        return snapshot;
    }
	
	private static List<Param> getProtocolParameters(List<Parameter<?>> params) {
		List<Param> result = new ArrayList<Param>(params.size());
		
        // We need some classes defined, make sure you clear them between params
        // ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		// DataOutputStream dataStream = new DataOutputStream(byteStream);
		// PacketOutputStream writer = new PacketOutputStream(dataStream);
		
        for (Parameter<?> param : params) {
        	// byteStream.reset();
        	try {
        		Param p = ParameterUtil.convert(param);
        		if(p != null) {
        			result.add(p);
        		}
	        	/*Param p = new Param();
	        	p.key = param.getKey();
	        	p.type = BinaryData.intToByte(param.getType().ordinal());
	        	
	        	if (param.getType().equals(Type.STRING)) {
	        		p.value = writeString(param, byteStream, writer);
	        		result.add(p);
	        		
	        	} else if (param.getType().equals(Type.INT)) {
	        		p.value = writeInteger(param, byteStream, writer);
	        		result.add(p);
	        	} else if (param.getType().equals(Type.DATE)) {
	        		p.value = BinaryData.dateToBytes((Date)param.getValue());
	        		result.add(p);
	        	} else {
	        		log.warn("Unrecognized parameter type: "+param);
	        	}*/
        	} catch (Exception e) {
        		log.error("Could not serialize parameter: "+param);
        	}
        }
        return result;
	}
	

	
	public static ProtocolObject transform(TournamentInfo mtt) {
		TournamentSnapshotPacket snapshot = new TournamentSnapshotPacket();
        snapshot.address = mtt.getFqn();
        snapshot.mttid = mtt.getId();
        // snapshot.params = new ArrayList<Param>();
        
        List<Param> protocolParameters = getProtocolParameters(mtt.getParams());

        // Add all parameters
        snapshot.params = protocolParameters;
        
        return snapshot;
	}
	
	/*private static byte[] writeString(Parameter<String> param, ByteArrayOutputStream byteStream, PacketOutputStream writer) throws IOException {
		String sValue = String.valueOf(param.getValue());
		writer.saveString(sValue);
		return byteStream.toByteArray();
	}
	
	private static byte[] writeInteger(Parameter<Integer> param, ByteArrayOutputStream byteStream, PacketOutputStream writer) throws IOException {
		int iValue = param.getValue(); 
		writer.saveInt(iValue);
		byte[] bs = byteStream.toByteArray();
		return bs;
	}*/
}
