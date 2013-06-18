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

package com.cubeia.games.poker.activator;

import com.cubeia.firebase.api.game.activator.ActivatorContext;
import com.cubeia.firebase.api.game.activator.GameActivator;
import com.cubeia.firebase.api.game.activator.MttAwareActivator;
import com.cubeia.firebase.api.game.activator.TableFactory;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.guice.inject.Log4j;
import com.cubeia.game.poker.config.api.PokerConfigurationService;
import com.cubeia.games.poker.common.guice.JpaInitializer;
import com.cubeia.games.poker.common.jmx.JmxUtil;
import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.cubeia.poker.PokerGuiceModule;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.timing.TimingFactory;
import com.cubeia.poker.variant.PokerVariant;
import com.cubeia.util.threads.SafeRunnable;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledExecutorService;

import static com.cubeia.poker.settings.RakeSettings.createDefaultRakeSettings;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Override the default game activator in order to provide my own
 * specific implementations.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class PokerActivator implements GameActivator, MttAwareActivator, PokerActivatorMBean {

    private static final String JMX_BIND_NAME = "com.cubeia.poker:type=PokerActivator";

    @Log4j
    private Logger log;

    @Inject
    private ActivatorTableManager tableManager;

    @Inject
    @Named("activatorThreads")
    private ScheduledExecutorService executor;

    @Inject
    @Named("activatorInterval")
    private long interval;

    @Inject
    private MttTableCreationHandler mttTables;

    @Inject
    private ParticipantFactory participantFactory;

    @Inject
    private TableFactory tableFactory;

    @Override
    public void init(ActivatorContext context) throws SystemException {
        new JmxUtil().mountBean(JMX_BIND_NAME, this);
        boolean useDatabase = !useMockIntegrations(context);
        Injector inj = Guice.createInjector(
                new ActivatorGuiceModule(context, useDatabase),
                new PokerGuiceModule());
        inj.injectMembers(this);
        log.debug("Init called.");
        if (useDatabase) {
            log.debug("Initializing JPA.");
            inj.getInstance(JpaInitializer.class);
        }
    }

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(new SafeRunnable() {

            @Override
            protected void innerRun() {
                tableManager.run();
            }
        }, interval, interval, MILLISECONDS);
    }

    @Override
    public void createTable(String domain, int seats, BigDecimal anteLevel, PokerVariant variant) {
        TableConfigTemplate t = createNewTemplate(seats, anteLevel, variant);
        PokerParticipant p = participantFactory.createParticipantFor(t);
        tableFactory.createTable(seats, p);
    }

    @Override
    public void destroyTable(int id) {
        tableFactory.destroyTable(id, true);
    }

    @Override
    public void mttTableCreated(Table table, int mttId, Object commandAttachment, LobbyAttributeAccessor acc) {
        log.debug("Created poker tournament table: " + table.getId());
        mttTables.tableCreated(table, mttId, commandAttachment, acc);
    }

    @Override
    public void stop() {
        executor.shutdown();
    }

    @Override
    public void destroy() {
        new JmxUtil().unmountBean(JMX_BIND_NAME);
    }

    // --- PRIVATE METHODS ---- //

    private boolean useMockIntegrations(ActivatorContext context) {
        return context.getServices().getServiceInstance(PokerConfigurationService.class).getActivatorConfig().useMockIntegrations();
    }

    private TableConfigTemplate createNewTemplate(int seats, BigDecimal anteLevel, PokerVariant variant) {
        TableConfigTemplate t = new TableConfigTemplate();
        t.setAnte(anteLevel);
        t.setSeats(seats);
        t.setVariant(variant);
        t.setRakeSettings(createDefaultRakeSettings(BigDecimal.valueOf(0.02)));
        t.setBetStrategy(BetStrategyType.NO_LIMIT);
        t.setTiming(TimingFactory.getRegistry().getDefaultTimingProfile());
        return t;
    }
}
