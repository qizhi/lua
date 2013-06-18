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
package com.cubeia.firebase.service.jndi.impl;

import java.util.Collections;

import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.sql.DataSource;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.cubeia.firebase.api.service.datasource.DatasourceListener;
import com.cubeia.firebase.api.service.datasource.DatasourceServiceContract;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;
import com.cubeia.firebase.util.InvocationFacade;

public class JndiContextTest extends TestCase {

	private DataSource ds;
	private JndiContext con;
	
	private DatasourceListener listener; // captured via mockito
	
	private DatasourceServiceContract man; // mock
	
	@Override
	protected void setUp() throws Exception {
		System.setProperty("java.naming.factory.initial", "com.cubeia.firebase.api.jndi.FirebaseContextFactory");
		System.setProperty("java.naming.factory.url.pkgs", "com.cubeia.firebase.api.jndi");
		man = Mockito.mock(DatasourceServiceContract.class);
		Mockito.when(man.getDatasources()).thenReturn(Collections.singletonList("kalle"));
		ds = Mockito.mock(DataSource.class); 
		Mockito.when(man.getDatasource(Mockito.eq("kalle"))).thenReturn(ds);
		con = new JndiContext(man, Mockito.mock(TransactionManagerProvider.class));
		ArgumentCaptor<DatasourceListener> capt = ArgumentCaptor.forClass(DatasourceListener.class);
		Mockito.verify(man).addDatasourceListener(capt.capture());
		listener = capt.getValue();
	}	 
	
	public void testJndi() throws Exception {
		con.invokeWithJndi(new InvocationFacade<Exception>() {
			@Override
			public Object invoke() throws Exception {
				InitialContext con = new InitialContext();
				Name name = con.getNameParser("").parse("java:comp/env/jdbc/kalle");
				DataSource test = (DataSource) con.lookup(name);
				// Assert.assertTrue(test instanceof DataSourceAdapter);
				Assert.assertTrue(ds == test);
				return null;
			}
		});
	}
	
	public void testRemoveDs() throws Exception {
		listener.datasourceRemoved("kalle");
		con.invokeWithJndi(new InvocationFacade<Exception>() {
			@Override
			public Object invoke() throws Exception {
				InitialContext con = new InitialContext();
				try {
					con.lookup("java:comp/env/jdbc/kalle");
					Assert.fail();
				} catch(NameNotFoundException e) {
					// This is expected
				}
				return null;
			}
		});
	}
	
	public void testAddDs() throws Exception {
		DataSource ds = Mockito.mock(DataSource.class);
		Mockito.when(man.getDatasource(Mockito.eq("olle"))).thenReturn(ds);
		listener.datasourceAdded("olle");
		con.invokeWithJndi(new InvocationFacade<Exception>() {
			@Override
			public Object invoke() throws Exception {
				InitialContext con = new InitialContext();
				DataSource test = (DataSource) con.lookup("java:comp/env/jdbc/olle");
				Assert.assertNotNull(test);
				return null;
			}
		});
	}
}
