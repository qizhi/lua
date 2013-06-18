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
package com.cubeia.firebase.api.mtt.support;

import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.model.MttRegistrationRequest;
import com.cubeia.firebase.api.mtt.support.registry.PlayerInterceptor;
import com.cubeia.firebase.api.mtt.support.registry.PlayerListener;

/**
 * <b>NB: </b> This class is within the public API because of build reasons,
 * it should only be used for testing! It will be moved shortly. See Trac issue
 * #417.
 */
//FIXME: Move to test, if you can get Maven to support it, see Trac issue #417
public class MttSupportAdapter extends MTTSupport {

	@Override
	public PlayerInterceptor getPlayerInterceptor(MTTStateSupport state) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void tournamentDestroyed(MttInstance mttInstance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PlayerListener getPlayerListener(MTTStateSupport state) {
		return new PlayerListener() {

			public void playerRegistered(MttInstance instance, MttRegistrationRequest request) {
				// TODO Auto-generated method stub
				
			}

			public void playerUnregistered(MttInstance instance, int pid) {
				// TODO Auto-generated method stub
				
			}
			
		};
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

    @Override
    public void process(MttRoundReportAction action, MttInstance mttInstance) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void process(MttTablesCreatedAction action, MttInstance instance) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void process(MttObjectAction action, MttInstance instance) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void tournamentCreated(MttInstance mttInstance) {
    }

}
