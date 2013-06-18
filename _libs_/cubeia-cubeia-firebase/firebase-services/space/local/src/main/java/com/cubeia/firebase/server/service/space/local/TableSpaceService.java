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
package com.cubeia.firebase.server.service.space.local;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.game.table.TableData;
import com.cubeia.firebase.mtt.state.MttStateData;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.space.SpaceConfig;
import com.cubeia.space.handler.mtt.MTTHandler;
import com.cubeia.space.handler.table.TableHandler;
import com.cubeia.space.service.AbstractBaseService;

public class TableSpaceService extends AbstractBaseService {
	
	// --- SERVICE METHODS --- //
	
	@Override
	public void destroy() { }

	@Override
	public void start() { 
		tableCache.start();
		mttCache.start();
	}

	@Override
	public void stop() { 
		tableCache.stop();
		mttCache.stop();
	}

	
	// --- SPACE SERVICE --- //
	
	@Override
	protected void initCaches() throws SystemException {
		tableCache = new TableHandler(new SpaceImpl<TableData>("TableSpace", getSpaceConfig(TABLE_SPACE_NAMESPACE), EventType.GAME, getMBus(), con.getMBeanServer(), getTransactionManager()), con.getParentRegistry(), tableFactory);
		mttCache = new MTTHandler(new SpaceImpl<MttStateData>("MttSpace", getSpaceConfig(MTT_SPACE_NAMESPACE), EventType.MTT, getMBus(), con.getMBeanServer(), getTransactionManager()), con.getParentRegistry(), mttFactory);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private SpaceConfig getSpaceConfig(String ns) throws SystemCoreException {
		ClusterConfigProviderContract serv = con.getParentRegistry().getServiceInstance(ClusterConfigProviderContract.class);
		return serv.getConfiguration(SpaceConfig.class, new Namespace(ns));
	}
	
    private MBusContract getMBus() {
		return con.getParentRegistry().getServiceInstance(MBusContract.class);
	}
}
