/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
 *
 * This program is licensed under a Firebase Enterprise Edition
 * License. You should have received a copy of the Firebase Enterprise
 * Edition License along with this program. If not, contact info@cubeia.com.
 */
package com.cubeia.firebase.service.persistence;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.service.persistence.jpa.JPAPersistenceManager;

public class PersistenceService implements Service, PersistenceServiceContract {
	
	// --- INSTANCE MEMBERS --- //

	private PersistenceManager man;
	
	public void destroy() {
		man = null;
	}

	public void init(ServiceContext con) throws IllegalStateException, SystemException {
		man = new JPAPersistenceManager(con.getParentRegistry());
	}
	
	public void start() {
		man.start();
	}
	
	public PersistenceManager getPersistenceManager() {
		return man;
	}
	
	public void stop() {
		man.stop();
	}
}