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
package com.cubeia.test.systest.game.tests;

import java.util.List;

import com.cubeia.firebase.api.action.Attribute;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.test.systest.game.SystestService;
import com.cubeia.test.systest.game.TestProcessor;
import com.cubeia.test.systest.game.api.ProxyService;
import com.cubeia.test.systest.io.protocol.ProtocolObjectFactory;

public abstract class ProcessorBase implements TestProcessor {

	private static final long serialVersionUID = 4070894324469530965L;
	
	protected transient GameContext con;

	public void setGameContext(GameContext con) {
		this.con = con;
	}

	public void handle(GameDataAction action, Table table) { 
		checkServiceAccess();
		// try {
			ProtocolObject proto = new StyxSerializer(new ProtocolObjectFactory()).unpack(action.getData());
			if(action.getAttributes().size() > 0) {
				doTranslatedHandle(proto, action.getPlayerId(), table, action.getAttributes());
			} else {
				doTranslatedHandle(proto, action.getPlayerId(), table);
			}
		/*} catch (IOException e) {
			Logger.getLogger(getClass()).error("Failed to deserialize action", e);
		}*/
	}

	private void checkServiceAccess() {
		this.con.getServices().getServiceInstance(SystestService.class).checkContextClassLoader();
		this.con.getServices().getServiceInstance(ProxyService.class).checkProxyAccess();
	}

	protected void doTranslatedHandle(ProtocolObject gamePacket, int playerId, Table table, List<Attribute> attributes) { }
	
	protected void doTranslatedHandle(ProtocolObject gamePacket, int playerId, Table table) { }
	
	protected void doHandle(Object objectAction, Table table) { }
	
	protected GameObjectAction createGameObjectAction(int tableId, Object packet) {
		GameObjectAction act = new GameObjectAction(tableId);
		act.setAttachment(packet);
		return act;
	}
	
	protected GameDataAction createDataAction(int playerId, int tableId, ProtocolObject packet) {
		GameDataAction act = new GameDataAction(playerId, tableId);
		// try {
			act.setData(new StyxSerializer(new ProtocolObjectFactory()).pack(packet));
		/*} catch (IOException e) {
			Logger.getLogger(getClass()).error("Failed to serialize action", e);
		}*/
		return act;
	}

	public void handle(GameObjectAction action, Table table) {  
		doHandle(action.getAttachment(), table);
	}
}
