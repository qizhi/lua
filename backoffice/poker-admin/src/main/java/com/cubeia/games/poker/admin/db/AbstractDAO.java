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

import com.cubeia.games.poker.entity.TableConfigTemplate;
import com.cubeia.games.poker.tournament.configuration.ScheduledTournamentConfiguration;
import com.cubeia.games.poker.tournament.configuration.SitAndGoConfiguration;
import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructure;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.timing.TimingProfile;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructure;
import org.apache.log4j.Logger;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Base class for handling persistence.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
@Transactional
@SuppressWarnings("deprecation")
public class AbstractDAO extends JpaDaoSupport implements AdminDAO {

    @SuppressWarnings("unused")
    private static transient Logger log = Logger.getLogger(AbstractDAO.class);

    /* (non-Javadoc)
     * @see com.cubeia.games.poker.admin.db.AdminDAO#getItem(java.lang.Class, java.lang.Integer)
     */
    public <T> T getItem(Class<T> class1, Integer id) {
        return getJpaTemplate().find(class1, id);
    }

    @Override
    public <T> void removeItem(Class<T> class1, int id) throws org.springframework.dao.DataAccessException {
        T item = getItem(class1, id);
        if (item != null) {
            getJpaTemplate().remove(item);
        }
    }

    /* (non-Javadoc)
    * @see com.cubeia.games.poker.admin.db.AdminDAO#persist(java.lang.Object)
    */
    public void persist(Object entity) {
        getJpaTemplate().persist(entity);
    }

    public void save(Object entity) {
        getJpaTemplate().merge(entity);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public List<SitAndGoConfiguration> getSitAndGoConfigurations() {
        return getJpaTemplate().find("from SitAndGoConfiguration where configuration.archived is false");
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public List<ScheduledTournamentConfiguration> getScheduledTournamentConfigurations() {
        return getJpaTemplate().find("from ScheduledTournamentConfiguration where configuration.archived is false");
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public List<TableConfigTemplate> getTableConfigTemplates() {
        return getJpaTemplate().find("from TableConfigTemplate");
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public List<TimingProfile> getTimingProfiles() {
        return getJpaTemplate().find("from TimingProfile where archived is false");
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public List<RakeSettings> getRakeSettings() {
        return getJpaTemplate().find("from RakeSettings where archived is false");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BlindsStructure> getBlindsStructures() {
        return getJpaTemplate().find("from BlindsStructure");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PayoutStructure> getPayoutStructures() {
        return getJpaTemplate().find("from PayoutStructure");
    }
}
