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

import java.util.List;

import com.cubeia.firebase.api.service.sysstate.PublicSystemStateService;
import com.cubeia.firebase.guice.inject.Service;
import org.apache.log4j.Logger;

import com.cubeia.firebase.guice.inject.Log4j;
import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.joda.time.DateTime;

@Singleton
public class ActivatorTableManagerImpl implements ActivatorTableManager {

    @Inject
    private TableConfigTemplateProvider provider;

    @Inject
    private LobbyTableInspector inspector;

    @Inject
    private TableActionHandler handler;

    @Service
    private PublicSystemStateService systemStateService;

    @Log4j
    private Logger log;

    @Override
    public void run() {
        log.trace("Table manager executing.");
        List<TableConfigTemplate> templates = provider.getTemplates();
        log.trace("Found " + templates.size() + " templates.");
        List<TableModifierAction> actions = inspector.match(templates);
        log.trace("Inspector reports " + actions.size() + " actions.");
        for (TableModifierAction a : actions) {
            handler.handleAction(a);
        }
    }
}
