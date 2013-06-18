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
package com.cubeia.firebase.server.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.mockito.Mockito;

public class InvocationHandlerAdapterTest extends TestCase {

	public void testAdapter() throws Throwable {
		InvocationHandler rec = mock(InvocationHandler.class);
		InvocationHandlerAdapter adapter = new InvocationHandlerAdapter(rec);
		adapter.invoke(null, null, null);
		verify(rec).invoke(null, null, null);
	}
	
	public void testRecevier() throws Throwable {
		String kalle = "kalle";
		Class<? extends String> cl = kalle.getClass();
		Method method = cl.getMethod("toString", (Class<?>[])null);
		InvocationHandlerAdapter adapter = new InvocationHandlerAdapter(kalle);
		String s = (String) adapter.invoke(null, method, null);
		assertEquals(kalle, s);
	}
	
	public void testRethrow() throws Throwable {
		Call run = mock(Call.class);
		Mockito.when(run.call()).thenThrow(new IOException("kkk"));
		Class<? extends Call> cl = run.getClass();
		Method method = cl.getMethod("call", (Class<?>[])null);
		InvocationHandlerAdapter adapter = new InvocationHandlerAdapter(run);
		try {
			adapter.invoke(null, method, null);
			Assert.fail();
		} catch(InvocationTargetException e) {
			Assert.fail();
		} catch(IOException e) {
			Assert.assertEquals("kkk", e.getMessage());
		}
	}
	
	public void testNonrethrow() throws Throwable {
		Call run = mock(Call.class);
		Mockito.when(run.call()).thenThrow(new IllegalStateException("kkk"));
		Class<? extends Call> cl = run.getClass();
		Method method = cl.getMethod("call", (Class<?>[])null);
		InvocationHandlerAdapter adapter = new InvocationHandlerAdapter(run);
		try {
			adapter.invoke(null, method, null);
			Assert.fail();
		} catch(InvocationTargetException e) {
			Throwable cause = e.getCause();
			Assert.assertEquals("kkk", cause.getMessage());
		} catch(IOException e) {
			Assert.fail();
		}
	}
	
	private static interface Call {
		
		public Object call() throws IOException;
		
	}
}
