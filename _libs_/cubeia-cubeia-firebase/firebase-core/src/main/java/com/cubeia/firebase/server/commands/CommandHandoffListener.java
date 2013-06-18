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
package com.cubeia.firebase.server.commands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.util.threads.SafeRunnable;

/**
 * This class uses a thread pool to do a hand-off to a 
 * wrapped listener. You need to {@link #destroy()} this
 * object in order to close its contained thread pool.
 * 
 * @author Larsan
 */
public class CommandHandoffListener implements CommandListener {

	private final ExecutorService exec;
	private final CommandListener wrapped;
	
	public CommandHandoffListener(int threads, CommandListener wrapped) {
		Arguments.notNull(wrapped, "wrapped");
		this.wrapped = wrapped;
		if(threads > 0) {
			exec = Executors.newFixedThreadPool(threads);
		} else {
			exec = Executors.newSingleThreadExecutor();
		}
	}
	
	public CommandHandoffListener(CommandListener wrapped) {
		this(-1, wrapped);
	}
	
	
	public Object commandReceived(final CommandMessage c) {
		exec.execute(new SafeRunnable() {
		
			public void innerRun() {
				wrapped.commandReceived(c);
			}
		});
		return null;
	}

	public void destroy() {
		exec.shutdown();
	}
}
