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

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.game.activator.TableFactory;
import com.cubeia.firebase.api.game.lobby.LobbyTable;
import com.cubeia.firebase.api.routing.ActivatorRouter;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.game.poker.config.api.PokerActivatorConfig;
import com.cubeia.game.poker.config.api.PokerConfigurationService;
import com.cubeia.games.poker.common.SystemTestTime;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.cubeia.poker.shutdown.api.ShutdownServiceContract;
import com.cubeia.poker.timing.TimingFactory;
import com.google.inject.Guice;
import com.google.inject.Inject;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cubeia.firebase.api.game.lobby.DefaultTableAttributes._LAST_MODIFIED;
import static com.cubeia.firebase.api.game.lobby.DefaultTableAttributes._SEATED;
import static com.cubeia.games.poker.activator.TableModifierActionType.CLOSE;
import static com.cubeia.games.poker.activator.TableModifierActionType.CREATE;
import static com.cubeia.games.poker.activator.TableModifierActionType.DESTROY;
import static com.cubeia.games.poker.common.lobby.PokerLobbyAttributes.TABLE_READY_FOR_CLOSE;
import static com.cubeia.games.poker.common.lobby.PokerLobbyAttributes.TABLE_TEMPLATE;
import static com.cubeia.poker.variant.PokerVariant.TELESINA;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class LobbyTableInspectorImplTest {

    @Inject
    private LobbyTableInspector handler;

    @Inject
    private SystemTestTime time;

    @Inject
    private TableFactory factory;

    @Inject
    private ServiceRegistry services;

    @Mock
    private ShutdownServiceContract shutdownService;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        Guice.createInjector(new TestActivatorModule() {

            @Override
            protected void configure() {
                bind(TableFactory.class).toInstance(Mockito.mock(TableFactory.class));
                bind(ActivatorRouter.class).toInstance(Mockito.mock(ActivatorRouter.class));
                bind(TableActionHandler.class).to(TableActionHandlerImpl.class);
                bind(LobbyTableInspector.class).to(LobbyTableInspectorImpl.class);
                bind(SystemTime.class).to(SystemTestTime.class);
                bind(ShutdownServiceContract.class).toInstance(shutdownService);
                super.configure();
            }
        }).injectMembers(this);
        Mockito.when(services.getServiceInstance(PokerConfigurationService.class).getActivatorConfig()).thenReturn(new PokerActivatorConfig() {

            @Override
            public boolean useMockIntegrations() {
                return true;
            }

            @Override
            public long getDefaultTableTTL() {
                return 60000;
            }

            @Override
            public long getActivatorInterval() {
                return 30;
            }
        });
        time.set(0);
    }

    @Test
    public void trivialClose() {
        /*
         * One table which is old should be closed
         */
        TableConfigTemplate templ = createTemplate(0, 0);
        LobbyTable table = mockTableForTempl(templ, 666, 0, 10);
        Mockito.when(factory.listTables()).thenReturn(new LobbyTable[]{table});
        time.set(70000); // 1 min 10 secs
        List<TableModifierAction> result = handler.match(Collections.singletonList(templ));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(CLOSE, result.get(0).getType());
    }

    @Test
    public void trivialSkipClose() {
        /*
           * One table which is old should not be closed
           * if "min tables" is equal or higher then number of
           * empty tables
           */
        TableConfigTemplate templ = createTemplate(1, 1);
        LobbyTable table = mockTableForTempl(templ, 666, 0, 10);
        Mockito.when(factory.listTables()).thenReturn(new LobbyTable[]{table});
        time.set(70000); // 1 min 10 secs
        List<TableModifierAction> result = handler.match(Collections.singletonList(templ));
        Assert.assertEquals(0, result.size()); // although table is old is shouldn't be removed
    }

    @Test
    public void trivialDestroy() {
        /*
           * One table which is old should be closed
           */
        TableConfigTemplate templ = createTemplate(0, 0);
        LobbyTable table = mockTableForTempl(templ, 666, 0, 10);
        table.getAttributes().put(TABLE_READY_FOR_CLOSE.name(), new AttributeValue(1)); // MARK CLOSE
        Mockito.when(factory.listTables()).thenReturn(new LobbyTable[]{table});
        List<TableModifierAction> result = handler.match(Collections.singletonList(templ));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(DESTROY, result.get(0).getType());
    }

    @Test
    public void trivialDestroyAndCreate() {
        /*
           * One table which is old should be closed, but at least
           * one table so expect a CREATE as well
           */
        TableConfigTemplate templ = createTemplate(1, 1); // AT LEAST ONE
        LobbyTable table = mockTableForTempl(templ, 666, 0, 10);
        table.getAttributes().put(TABLE_READY_FOR_CLOSE.name(), AttributeValue.wrap(1)); // MARK CLOSE
        Mockito.when(factory.listTables()).thenReturn(new LobbyTable[]{table});
        List<TableModifierAction> result = handler.match(Collections.singletonList(templ));
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(DESTROY, result.get(0).getType());
        Assert.assertEquals(CREATE, result.get(1).getType());
    }

    @Test
    public void trivialCreate() {
        /*
           * No table exists, min 1 = create one
           */
        TableConfigTemplate templ = createTemplate(1, 1);
        Mockito.when(factory.listTables()).thenReturn(new LobbyTable[]{});
        List<TableModifierAction> result = handler.match(Collections.singletonList(templ));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(CREATE, result.get(0).getType());
    }

    @Test
    public void trivialMissingTemplate() {
        /*
           * If a template is missing and no-one is sitting
           * the table should be marked close
           */
        TableConfigTemplate templ = createTemplate(1, 1);
        TableConfigTemplate templ2 = createTemplate(1, 1);
        templ2.setId(666); // this is the missing template
        LobbyTable table1 = mockTableForTempl(templ, 666, 0, 10);
        LobbyTable table2 = mockTableForTempl(templ2, 666, 0, 10);
        Mockito.when(factory.listTables()).thenReturn(new LobbyTable[]{table1, table2});
        List<TableModifierAction> result = handler.match(Collections.singletonList(templ)); // missing templ2
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(CLOSE, result.get(0).getType());
    }

    @Test
    public void tenInitialAndDoNotClose() {
        /*
           * Create min of 10 tables and check that they are not
           * closed even though they get stale
           */
        TableConfigTemplate templ = createTemplate(10, 5);
        Mockito.when(factory.listTables()).thenReturn(new LobbyTable[]{});
        List<TableModifierAction> result = handler.match(Collections.singletonList(templ));
        Assert.assertEquals(10, result.size());
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(CREATE, result.get(0).getType());
        }
        LobbyTable[] tables = mockTablesForTempl(10, templ, 0, 10);
        Mockito.when(factory.listTables()).thenReturn(tables);
        time.set(70000); // 1 min 10 secs
        result = handler.match(Collections.singletonList(templ));
        Assert.assertEquals(0, result.size()); // tables are stale, but also on min
    }

    @Test
    public void twelweTablesAndDoNotClose() {
        /*
           * With min=10, and minEmtpy=5, create 12 tables and check that 2 are
           * closed even though all are stale
           */
        TableConfigTemplate templ = createTemplate(10, 5);
        LobbyTable[] tables = mockTablesForTempl(12, templ, 0, 10);
        Mockito.when(factory.listTables()).thenReturn(tables);
        time.set(70000); // 1 min 10 secs
        List<TableModifierAction> result = handler.match(Collections.singletonList(templ));
        Assert.assertEquals(2, result.size()); // tables are stale, but also within (min + minEmpty)
    }

    @Test
    public void twelweNonEmtpyAndSevenEmpty() {
        /*
           * With min=10, and minEmtpy=5, create 19 tables of which 12 are non-empty
           */
        TableConfigTemplate templ = createTemplate(10, 5);
        LobbyTable[] tables = mockTablesForTempl(19, templ, 0, 10);
        setNonEmptyTables(tables, 12);
        Mockito.when(factory.listTables()).thenReturn(tables);
        time.set(500); // NOT STALE YET
        List<TableModifierAction> result = handler.match(Collections.singletonList(templ));
        Assert.assertEquals(0, result.size()); // no stale tables
        time.set(70000); // STALE
        result = handler.match(Collections.singletonList(templ));
        Assert.assertEquals(2, result.size()); // 7 stale, but min 5 tables (7 - 5)
    }
    
    @Test
    public void twoNonEmtpyAndTenEmpty() {
        /*
           * With min=10, and minEmtpy=5, create 12 tables of which 2 are non-empty
           */
        TableConfigTemplate templ = createTemplate(10, 5);
        LobbyTable[] tables = mockTablesForTempl(12, templ, 0, 10);
        setNonEmptyTables(tables, 2);
        Mockito.when(factory.listTables()).thenReturn(tables);
        time.set(500); // NOT STALE YET
        List<TableModifierAction> result = handler.match(Collections.singletonList(templ));
        Assert.assertEquals(0, result.size()); // no stale tables
        time.set(70000); // STALE
        result = handler.match(Collections.singletonList(templ));
        Assert.assertEquals(2, result.size()); // 10 stale, but min 10 tables
    }


    // --- PRIVATE METHODS --- //

    private void setNonEmptyTables(LobbyTable[] tables, int num) {
        for (int i = 0; i < num; i++) {
            tables[i].getAttributes().put(_SEATED.name(), AttributeValue.wrap(1));
        }
    }

    private LobbyTable[] mockTablesForTempl(int num, TableConfigTemplate templ, int seated, int lastModified) {
        LobbyTable[] arr = new LobbyTable[num];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = mockTableForTempl(templ, i, seated, lastModified);
        }
        return arr;
    }

    private TableConfigTemplate createTemplate(int min, int minEmpty) {
        TableConfigTemplate templ = new TableConfigTemplate();
        templ.setAnte(BigDecimal.TEN);
        templ.setSeats(6);
        templ.setTiming(TimingFactory.getRegistry().getTimingProfile("EXPRESS"));
        templ.setVariant(TELESINA);
        templ.setMinEmptyTables(minEmpty);
        templ.setMinTables(min);
        templ.setTTL(60000);
        return templ;
    }

    private LobbyTable mockTableForTempl(final TableConfigTemplate templ, final int tableId, int seated, int lastModified) {
        final Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
        setSeated(map, seated);
        setLastModified(map, seated);
        setTemplate(map, templ.getId());
        return new LobbyTable() {

            private static final long serialVersionUID = 42404196469420079L;

            @Override
            public int getObjectId() {
                return tableId;
            }

            @Override
            public Map<String, AttributeValue> getAttributes() {
                return map;
            }

            @Override
            public int getTableId() {
                return tableId;
            }
        };
    }

    private void setTemplate(Map<String, AttributeValue> map, int id) {
        map.put(TABLE_TEMPLATE.name(), AttributeValue.wrap(id));
    }

    private void setSeated(Map<String, AttributeValue> map, int seated) {
        map.put(_SEATED.name(), AttributeValue.wrap(seated));
    }

    private void setLastModified(Map<String, AttributeValue> map, int lastMod) {
        map.put(_LAST_MODIFIED.name(), AttributeValue.wrap(String.valueOf(lastMod)));
    }
}
