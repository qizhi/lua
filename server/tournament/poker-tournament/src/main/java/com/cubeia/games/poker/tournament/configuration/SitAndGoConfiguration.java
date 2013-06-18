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

package com.cubeia.games.poker.tournament.configuration;

import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructureFactory;
import com.cubeia.poker.timing.TimingFactory;
import com.cubeia.poker.timing.TimingProfile;
import org.apache.log4j.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity
public class SitAndGoConfiguration implements Serializable {

    private static final Logger log = Logger.getLogger(SitAndGoConfiguration.class);

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(cascade = {CascadeType.ALL})
    private TournamentConfiguration configuration = new TournamentConfiguration();

    public SitAndGoConfiguration() {
    }

    public TournamentConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(TournamentConfiguration configuration) {
        this.configuration = configuration;
    }

    public SitAndGoConfiguration(String name, int capacity, TimingProfile timings) {
        configuration = new TournamentConfiguration();
        configuration.setName(name);
        configuration.setMinPlayers(capacity);
        configuration.setMaxPlayers(capacity);
        configuration.setSeatsPerTable(10);
        configuration.setTimingType(timings);
        configuration.setBlindsStructure(BlindsStructureFactory.createDefaultBlindsStructure());
    }

    public SitAndGoConfiguration(String name, int capacity) {
        this(name, capacity, TimingFactory.getRegistry().getDefaultTimingProfile());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
