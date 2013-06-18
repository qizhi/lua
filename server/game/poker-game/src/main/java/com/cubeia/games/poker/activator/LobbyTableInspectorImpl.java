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
import com.cubeia.firebase.guice.inject.Log4j;
import com.cubeia.firebase.guice.inject.Service;
import com.cubeia.game.poker.config.api.PokerConfigurationService;
import com.cubeia.games.poker.common.lobby.PokerLobbyAttributes;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.cubeia.poker.shutdown.api.ShutdownServiceContract;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.cubeia.firebase.api.game.lobby.DefaultTableAttributes._LAST_MODIFIED;
import static com.cubeia.firebase.api.game.lobby.DefaultTableAttributes._MTT_ID;
import static com.cubeia.firebase.api.game.lobby.DefaultTableAttributes._SEATED;
import static com.cubeia.games.poker.common.lobby.PokerLobbyAttributes.TABLE_TEMPLATE;
import static java.util.Collections.addAll;

@Singleton
public class LobbyTableInspectorImpl implements LobbyTableInspector {

    @Log4j
    private Logger log;

    @Inject
    private TableFactory factory;

    @Inject
    private SystemTime time;

    @Service
    private PokerConfigurationService config;

    @Service
    ShutdownServiceContract shutdownService;

    @Override
    public List<TableModifierAction> match(List<TableConfigTemplate> templates) {
        List<TableModifierAction> result = new ArrayList<TableModifierAction>();
        List<LobbyTable> allTables = getAllTables();
        // first pass: check for destruction
        checkDestruction(allTables, result);
        // divide into groups by template
        Map<Integer, List<LobbyTable>> tables = partitionTables(allTables);
        log.trace("Found " + allTables.size() + " tables in " + tables.size() + " templates");
        // now check all templates for closure or creation
        checkTemplates(templates, tables, result);
        // now check if any templates are missing
        checkMissingTemplates(templates, tables, result, shuttingDown());
        return result;
    }

    private boolean shuttingDown() {
        return shutdownService.isSystemShuttingDown();
    }

    // --- PRIVATE METHODS --- //

    private List<LobbyTable> getAllTables() {
        LobbyTable[] arr = factory.listTables();
        List<LobbyTable> list = new LinkedList<LobbyTable>();
        addAll(list, arr);
        return list;
    }

    private void checkMissingTemplates(List<TableConfigTemplate> templates, Map<Integer, List<LobbyTable>> tables,
            List<TableModifierAction> result, boolean shuttingDown) {
        Set<Integer> templateIds = collectTemplateIds(templates);
        for (Entry<Integer, List<LobbyTable>> entry : tables.entrySet()) {
            if (templateMissing(templateIds, entry) || shuttingDown) {
                // missing template, check if empty and close immediately if so
                for (LobbyTable table : entry.getValue()) {
                    boolean empty = isEmpty(table);
                    log.debug("Table[" + table.getTableId() + "] is registered on missing template " + entry.getKey() + ", will close if empty: " + empty);
                    if (empty) {
                        result.add(TableModifierAction.close(table.getTableId()));
                    }
                }
            }
        }
    }

    private boolean templateMissing(Set<Integer> templateIds, Entry<Integer, List<LobbyTable>> entry) {
        return !templateIds.contains(entry.getKey());
    }

    private Set<Integer> collectTemplateIds(List<TableConfigTemplate> templates) {
        Set<Integer> templateIds = new HashSet<Integer>(templates.size());
        for (TableConfigTemplate t : templates) {
            templateIds.add(t.getId());
        }
        return templateIds;
    }

    private void checkTemplates(List<TableConfigTemplate> templates, Map<Integer, List<LobbyTable>> tables, List<TableModifierAction> result) {
        if (shuttingDown()) {
            log.debug("System is shutting down, won't check templates.");
            return;
        }

        // for each template
        for (TableConfigTemplate config : templates) {
            List<LobbyTable> list = tables.get(config.getId());
            // create a list if it doesn't exist
            if (list == null) {
                list = new ArrayList<LobbyTable>();
                tables.put(config.getId(), list);
            }
            /*
             * Now, if "all" is less than minimum, add. If "empty
             * is less then minimum empty, add. Otherwise, check for
             * closure.
             */
            int all = list.size();
            int empty = countEmptyTables(list);
            int min = config.getMinTables();
            int minEmpty = config.getMinEmptyTables();
            if(log.isTraceEnabled()) {
            	log.trace("Vars: All: " + all + "; Empty: " + empty + "; Config min: " + min + "; Config min empty: " + minEmpty);
            }
            if (all < min) {
            	int create = min - all;
            	log.debug("We don't have enough tables, will create " + create);
                createTables(config, result, create);
            } else if (empty < minEmpty) {
            	int create = minEmpty - empty;
            	log.debug("We don't have enough empty tables, will create " + create);
                createTables(config, result, create);
            } else {
            	if (empty > minEmpty && all > min) {
                    int destroy = empty - minEmpty;
                    if(all - destroy  < min) {
                    	destroy = all - min;
                    }
            		log.debug("We have too many empty tables; will close " + (all - minEmpty));
                    checkClosure(config, list, result, destroy);
                } else {
                	if(log.isTraceEnabled()) {
                    	log.trace("Table are just right, won't do anything.");
                    }
                }
            }
        }
    }

    private void createTables(TableConfigTemplate config, List<TableModifierAction> result, int num) {
        for (int i = 0; i < num; i++) {
            log.trace("Adding new table for config: " + config.getId());
            result.add(TableModifierAction.create(config));
        }
    }

    private int countEmptyTables(List<LobbyTable> list) {
        int i = 0;
        for (LobbyTable t : list) {
            if (isEmpty(t)) {
                i++;
            }
        }
        return i;
    }

    /*
     * Check for tables to be destroyed, regardless of template, and return all other tables
     */
    private void checkDestruction(List<LobbyTable> allTables, List<TableModifierAction> result) {
        for (Iterator<LobbyTable> it = allTables.iterator(); it.hasNext(); ) {
            LobbyTable table = it.next();
            if (isClosed(table)) {
                log.debug("Table[" + table.getTableId() + "] is closed, will be destroyed.");
                result.add(TableModifierAction.destroy(table.getTableId()));
                it.remove();
            }
        }
    }

    private boolean isClosed(LobbyTable table) {
        AttributeValue val = table.getAttributes().get(PokerLobbyAttributes.TABLE_READY_FOR_CLOSE.name());
        boolean b = (val != null && val.getIntValue() > 0);
        if (log.isTraceEnabled()) {
            log.trace("Table " + table.getTableId() + " is closed: " + b + " (attribute: " + (val == null ? "null" : val.getIntValue()) + ")");
        }
        return b;
    }

    private void checkClosure(TableConfigTemplate config, List<LobbyTable> list, List<TableModifierAction> result, int maxRemove) {
        if (maxRemove < 1) {
            return; // NOTHING TO DO
        }
        int check = 0;
        for (Iterator<LobbyTable> it = list.iterator(); it.hasNext(); ) {
            LobbyTable table = it.next();
            if (isEmpty(table) && isStale(table, config)) {
                check++;
                log.debug("Table[" + table.getTableId() + "] is empty, will send close request.");
                result.add(TableModifierAction.close(table.getTableId()));
                it.remove(); // do not do anything else on table
                if (check == maxRemove) {
                    log.debug("Short-cutting the closure process, have marked " + check + " tables");
                    break;
                }
            }
        }
    }

    private boolean isStale(LobbyTable table, TableConfigTemplate templ) {
        String tmp = table.getAttributes().get(_LAST_MODIFIED.name()).getStringValue();
        long timestamp = Long.valueOf(tmp);
        long ttl = templ.getTTL();
        if (ttl <= 0) {
            // fall back on configuration
            ttl = config.getActivatorConfig().getDefaultTableTTL();
        }
        boolean b = (time.now() - timestamp) > ttl;
        if (log.isTraceEnabled()) {
            log.trace("Table " + table.getTableId() + " is stale: " + b);
        }
        return b;
    }

    private boolean isEmpty(LobbyTable table) {
        boolean b = table.getAttributes().get(_SEATED.name()).getIntValue() == 0;
        if (log.isTraceEnabled()) {
            log.trace("Table " + table.getTableId() + " is empty: " + b);
        }
        return b;
    }

    private Map<Integer, List<LobbyTable>> partitionTables(List<LobbyTable> tables) {
        Map<Integer, List<LobbyTable>> map = new HashMap<Integer, List<LobbyTable>>();
        for (LobbyTable t : tables) {
            if (t.getAttributes().containsKey(_MTT_ID.name())) {
                // Skip tournament tables.
                continue;
            }
            int template = getTemplateId(t);
            List<LobbyTable> list = map.get(template);
            if (list == null) {
                list = new ArrayList<LobbyTable>();
                map.put(template, list);
            }
            list.add(t);
        }
        return map;
    }

    private int getTemplateId(LobbyTable t) {
        Map<String, AttributeValue> map = t.getAttributes();
        return (map.containsKey(TABLE_TEMPLATE.name()) ? map.get(TABLE_TEMPLATE.name()).getIntValue() : -1);
    }

}
