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
package com.cubeia.firebase.service.ctransaction.local;

import junit.framework.TestCase;

import com.cubeia.firebase.transaction.ContextType;
import com.cubeia.firebase.transaction.CoreResource;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.ResourceException;
import com.cubeia.firebase.transaction.TransactionException;

public class LocalCoreManagerTest extends TestCase {

	private LocalCoreManager manager;

	@Override
	protected void setUp() throws Exception {
		this.manager = new LocalCoreManager(null);
	}
	
	public void testCommit() throws Exception {
		CoreTransaction tr = manager.create(ContextType.GAME_EVENT);
		Resource r1 = new Resource();
		Resource r2 = new Resource();
		tr.attach(r1);
		tr.attach(r2);
		tr.commit();
		assertTrue(r1.committed);
		assertTrue(r2.committed);
	}
	
	public void testFailPrep() throws Exception {
		CoreTransaction tr = manager.create(ContextType.GAME_EVENT);
		FailPrepResource r2 = new FailPrepResource();
		Resource r1 = new Resource();
		tr.attach(r1);
		tr.attach(r2);
		try {
			tr.commit();
			fail("Should have failed");
		} catch(TransactionException e) {
			assertEquals(3, r2.done);
			/*
			 * None should be committed, r1 should be
			 * rolled back
			 */
			assertFalse(r1.committed);
			assertTrue(r1.rollbacked);
			assertFalse(r2.committed);
			assertFalse(r2.rollbacked);
		}
	}
	
	public void testFailCommit() throws Exception {
		CoreTransaction tr = manager.create(ContextType.GAME_EVENT);
		Resource r1 = new Resource();
		FailComResource r2 = new FailComResource();
		Resource r3 = new Resource();
		tr.attach(r1);
		tr.attach(r2);
		tr.attach(r3);
		try {
			tr.commit();
			fail("Should have aborted");
		} catch(TransactionException e) {
			/*
			 * r1 & r3 should be committed, and there should 
			 * be no rollback, r2 should be neither committed 
			 * nor rollback
			 */
			assertTrue(r1.committed);
			assertFalse(r1.rollbacked);
			assertFalse(r2.committed);
			assertFalse(r2.rollbacked);
			assertTrue(r3.committed);
			assertFalse(r3.rollbacked);
		}
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class Resource implements CoreResource {

		// private UUID id;
		boolean committed, rollbacked;

		public Resource() {
			// this.id = UUID.randomUUID();
		}
		
		@Override
		public void commit() throws ResourceException {
			committed = true;
		}

		@Override
		public void prepare() throws ResourceException { }

		@Override
		public void rollback() { 
			rollbacked = true;
		}
	}

	private class FailPrepResource extends Resource {
		
		int done = 0;
		
		@Override
		public void prepare() throws ResourceException {
			done++;
			throw new ResourceException("Test Error", 2);
		}
	}
		
	private class FailComResource extends Resource {
			
		@Override
		public void commit() throws ResourceException {
			throw new ResourceException("Test Error", 2);
		}
	}
}
