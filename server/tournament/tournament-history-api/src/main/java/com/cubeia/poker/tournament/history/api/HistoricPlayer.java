/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.poker.tournament.history.api;

import com.google.code.morphia.annotations.Embedded;

import java.io.Serializable;

@SuppressWarnings("UnusedDeclaration")
@Embedded
public class HistoricPlayer implements Serializable {

    private int id;

    private String name;

    private String sessionId;

    HistoricPlayer() {

    }

    public HistoricPlayer(int playerId, String name, String sessionId) {
        this.id = playerId;
        this.name = name;
        this.sessionId = sessionId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "HistoricPlayer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
