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

package com.cubeia.games.poker.admin.db;

import java.util.List;

import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.cubeia.games.poker.tournament.configuration.ScheduledTournamentConfiguration;
import com.cubeia.games.poker.tournament.configuration.SitAndGoConfiguration;
import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructure;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.timing.TimingProfile;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructure;

public interface AdminDAO {

    public abstract <T> T getItem(Class<T> class1, Integer id);

    public abstract void persist(Object entity);

    public abstract void save(Object entity);

    public List<SitAndGoConfiguration> getSitAndGoConfigurations();

    public List<ScheduledTournamentConfiguration> getScheduledTournamentConfigurations();
    
    public List<TableConfigTemplate> getTableConfigTemplates();

    public List<TimingProfile> getTimingProfiles();

    public List<RakeSettings> getRakeSettings();

    public List<BlindsStructure> getBlindsStructures();

    List<PayoutStructure> getPayoutStructures();

    public <T> void removeItem(Class<T> class1, int templateId) throws org.springframework.dao.DataAccessException;

}
