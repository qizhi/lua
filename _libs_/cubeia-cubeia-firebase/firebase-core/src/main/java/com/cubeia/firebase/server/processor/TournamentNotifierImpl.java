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
package com.cubeia.firebase.server.processor;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.game.TournamentNotifier;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.util.executor.JmxExecutor;

public class TournamentNotifierImpl implements TournamentNotifier {

	private static transient Logger log = Logger.getLogger(TournamentNotifierImpl.class);
	
    private final Sender<MttEvent> mttRouter;
    
    /** The executor for sending asynchronous */
    private final static JmxExecutor executor;

    static {
    	// If you make this multi threaded then ordering of events must be addressed properly
        executor = new JmxExecutor(1, "Tournament-Notifier");
    }
	
	public TournamentNotifierImpl(Sender<MttEvent> mttRouter) {
		this.mttRouter = mttRouter;
		
	}
	
    public void sendToTournament(MttAction action) {
        MttEvent event = new MttEvent();
        event.setMttId(action.getMttId());
        event.setAction(action);
  
        sendEvent(event);    
    }
    
    /**
     * Send an event to the client router.
     * 
     * @param event
     */
    private void sendEvent(MttEvent event) {
        sendEventNonBlocking(event);
    }

    /**
     * Send event without blocking the calling thread (hand-off).
     * 
     * @param event
     */
    private void sendEventNonBlocking(final MttEvent event) {
        executor.submit(new Runnable() {
            public void run() {
                try {
                    mttRouter.dispatch(event);
                } catch (ChannelNotFoundException e) {
                    log.error("Event for non-existing mtt was discarded: " + event, e);
                }
            }
        });
    }    
}
