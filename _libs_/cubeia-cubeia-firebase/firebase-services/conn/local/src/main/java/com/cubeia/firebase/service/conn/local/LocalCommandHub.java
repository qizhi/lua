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
package com.cubeia.firebase.service.conn.local;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.service.conn.ClusterException;
import com.cubeia.firebase.service.conn.CommandDispatcher;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.conn.CommandReceiver;
import com.cubeia.firebase.service.conn.CommandResponse;
import com.cubeia.util.Lists;
import com.cubeia.util.threads.SafeRunnable;

/**
 * This is an in-memory multicaster of commands. It uses a cached
 * thread pool for hand-off. 
 * 
 * @author Lars J. Nilsson
 * @see LocalConnectionService
 */
public class LocalCommandHub implements CommandDispatcher, CommandReceiver {
	
	private static final long CAST_TIMEOUT = 5000;
	// private static final int LONG_PROCESSING_THRESHOLD = 500;
	// private static final int LONG_INDIVIDUAL_PROCESSING_THRESHOLD = 250;
	private static final Object NULL_KEY = new Object();

	private final ExecutorService exec = Executors.newCachedThreadPool();
	private final Map<Object, List<CommandListener>> listeners = new ConcurrentHashMap<Object, List<CommandListener>>();
	
	private final SocketAddress bindAddress;
	
	public LocalCommandHub(SocketAddress bindAddress) {
		this.bindAddress = bindAddress;
	}

	@Override
	public void dispatch(Command<?> com) throws ClusterException {
		dispatch(com, null);
	}

	@Override
	public void dispatch(Command<?> com, SocketAddress rec) throws ClusterException {
		// make sure this is only local
		checkLocalRecipient(rec);
		// create runnable and hand-off
		Handoff handoff = new Handoff(NULL_KEY, com);
		exec.submit(handoff);
	}

	@Override
	public CommandResponse[] send(String channel, Command<?> com) throws ClusterException {
		return doSend(channel, com, null, CAST_TIMEOUT);
	}

	@Override
	public CommandResponse[] send(String channel, Command<?> com, long timeout) throws ClusterException {
		return doSend(channel, com, null, (timeout <= 0 ? CAST_TIMEOUT : timeout));
	}

	@Override
	public CommandResponse send(String channel, Command<?> com, SocketAddress recipient) throws ClusterException {
		return send(channel, com, recipient, CAST_TIMEOUT);
	}

	@Override
	public CommandResponse send(String channel, Command<?> com, SocketAddress recipient, long timeout) throws ClusterException {
		return doSend(channel, com, recipient, (timeout <= 0 ? CAST_TIMEOUT : timeout))[0];
	}

	@Override
	public void addCommandListener(String channel, CommandListener listener) {
		Object key = getCheckKey(channel);
		List<CommandListener> list = listeners.get(key);
		if(list == null) list = createList(key);
		list.add(listener);
	}


	@Override
	public void addCommandListener(CommandListener list) {
		addCommandListener(null, list);
	}

	@Override
	public void removeCommandListener(String channel, CommandListener listener) {
		Object key = getCheckKey(channel);
		List<CommandListener> list = listeners.get(key);
		if(list != null) {
			list.remove(listener);
		}
	}

	@Override
	public void removeCommandListener(CommandListener list) {
		removeCommandListener(null, list);
	}
	
	public void stop() {
		exec.shutdown();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private CommandResponse[] doSend(String channel, Command<?> com, SocketAddress rec, long timeout) throws ClusterException {
		// local recipient only
		checkLocalRecipient(rec);
		// get channel key
		Object key = getCheckKey(channel);
		// create hand-off
		final Handoff handoff = new Handoff(key, com);
		// submit and wait for future
		Future<?> future = exec.submit(handoff);
		waitForExec(timeout, future);
		// return new response
		return new CommandResponse[] {
			new Response(Lists.toArray(handoff.result, Object.class))
		};
	}

	private void waitForExec(long timeout, Future<?> future) throws ClusterException {
		try {
			future.get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Logger.getLogger(getClass()).warn("Execution interrupted", e);
		} catch (ExecutionException e) {
			throw new ClusterException("Execution failure", e);
		} catch (TimeoutException e) { }
	}
	
	private void checkLocalRecipient(SocketAddress rec) throws ClusterException {
		if(rec != null && !rec.equals(bindAddress)) {
			throw new ClusterException("Unknown recipient: " + rec);
		}
	}
	
	private List<CommandListener> createList(Object key) {
		synchronized(NULL_KEY) {
			if(!listeners.containsKey(key)) {
				List<CommandListener> l = new CopyOnWriteArrayList<CommandListener>();
				listeners.put(key, l);
				return l;
			} else {
				return listeners.get(key);
			}
		}
	}
	
	private Object getCheckKey(String channel) {
		return (channel == null ? NULL_KEY : channel);
	}
	
	
	// --- INNER CLASSES --- //
	
	private class Response implements CommandResponse {
		
		private final Object[] objects;
		
		private Response(Object[] arr) {
			this.objects = arr;
		}
		
		@Override
		public Object[] getAnswer() {
			return objects;
		}
		
		@Override
		public SocketAddress getReceiver() {
			return bindAddress;
		}
		
		@Override
		public boolean isReceived() {
			return true;
		}
	}
	
	private class Handoff extends SafeRunnable {

		private final Object key;
		private final Command<?> comm;
		
		private final List<Object> result = new CopyOnWriteArrayList<Object>();
		
		private Handoff(Object key, Command<?> comm) {
			this.key = key;
			this.comm = comm;
		}
		
		@Override
		protected void innerRun() {
			List<CommandListener> list = listeners.get(key);
			if(list != null) {
				for (CommandListener l : list) {
					result.add(l.commandReceived(new CommandMessage(comm, bindAddress)));
				}
			}
		}		
	}
}
