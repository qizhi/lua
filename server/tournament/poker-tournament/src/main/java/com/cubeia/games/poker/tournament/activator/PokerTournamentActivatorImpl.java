/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.tournament.activator;

import com.cubeia.firebase.api.mtt.MttFactory;
import com.cubeia.firebase.api.mtt.activator.ActivatorContext;
import com.cubeia.firebase.api.mtt.activator.MttActivator;
import com.cubeia.firebase.api.server.Startable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.game.poker.config.api.PokerConfigurationService;
import com.cubeia.games.poker.common.guice.JpaInitializer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.log4j.Logger;

/**
 * Base class for the tournament activator.
 * <p/>
 * This class will start an activator depending on the availability of a database
 * (i.e. presence of poker-ds.xml).
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class PokerTournamentActivatorImpl implements MttActivator, Startable, PokerTournamentActivator {

    public static final int POKER_GAME_ID = 1;

    private static transient Logger log = Logger.getLogger(PokerTournamentActivatorImpl.class);

    private PokerActivator activator;

    private MttFactory factory;

    private Injector injector;

    private JMXActivator jmxInterface;

    private ActivatorContext context;

    public PokerTournamentActivatorImpl() {

    }

    /*------------------------------------------------

       EXTERNAL INTERFACE METHODS

       Methods that will be called from remote
       interface.

    ------------------------------------------------*/

    public void checkInstancesNow() {
        log.warn("Check Instances Now called");
        activator.checkTournamentsNow();
    }

    public void shutdownTournament(int mttInstanceId) {
        log.warn("Shutdown Tournament [" + mttInstanceId + "] called");
    }

    public void startTournament(int mttInstanceId) {
        log.warn("Start Tournament [" + mttInstanceId + "] called");
    }

    public void destroyTournament(int mttInstanceId) {
        log.warn("Destroy Tournament [" + mttInstanceId + "] called");
        factory.destroyMtt(POKER_GAME_ID, mttInstanceId);
    }

    /*------------------------------------------------

       ACTIVATOR AGGREGATED METHODS

       Lifecycle methods passed on the the
       activator implementation.

    ------------------------------------------------*/

    /**
     * The factory will be injected to the activator used
     * when it is created.
     */
    public void setMttFactory(MttFactory factory) {
        this.factory = factory;
    }

    public void destroy() {
        jmxInterface.destroy();
        activator.destroy();
    }

    public void init(ActivatorContext context) throws SystemException {
        this.context = context;
        if (useMockIntegrations()) {
            log.warn("Using mock activator module.");
            injector = Guice.createInjector(new MockActivatorModule());
        } else {
            log.info("Creating database based activator module.");
            injector = Guice.createInjector(new ActivatorModule());
            // This will initialize JPA.
            injector.getInstance(JpaInitializer.class);
        }
        activator = injector.getInstance(PokerActivator.class);
        activator.init(context);
    }

    public void start() {
        activator.setMttFactory(factory);
        activator.start();
        jmxInterface = new JMXActivator(this);
    }

    public void stop() {
        activator.stop();
    }


    // --- PRIVATE METHODS --- //

    private boolean useMockIntegrations() {
        return context.getServices().getServiceInstance(PokerConfigurationService.class).getActivatorConfig().useMockIntegrations();
    }
}
