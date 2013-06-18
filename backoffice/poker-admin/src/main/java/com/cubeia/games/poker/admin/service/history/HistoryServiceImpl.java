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

package com.cubeia.games.poker.admin.service.history;

import com.cubeia.poker.handhistory.api.HistoricHand;
import com.cubeia.poker.tournament.history.api.HistoricTournament;
import com.google.code.morphia.query.Query;
import com.mongodb.BasicDBObject;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class HistoryServiceImpl implements HistoryService {

    private static final Logger log = Logger.getLogger(HistoryServiceImpl.class);

    private MongoStorage mongoStorage;

    @Autowired
    public HistoryServiceImpl(@Qualifier("mongoStorage") MongoStorage mongoStorage) {
        this.mongoStorage = mongoStorage;
    }

    @Override
    public List<HistoricHand> findHandHistory(Integer playerId, String tableId, Date fromDate, Date toDate) {
        log.info("Finding hand histories by query: playerId = " + playerId + " tableId = " + tableId + " from: " + fromDate + " to: " + toDate);
        Query query = mongoStorage.createQuery(HistoricHand.class);
        if (tableId != null) query.field("table.tableIntegrationId").equal(tableId);
        /*
         * Note, we are searching based on when the hand started, so it's not a bug that startTime is used for both from and to.
         * That is we are searching for hands for which fromDate < startTime < toDate.
         */
        if (fromDate != null) query.field("startTime").greaterThanOrEq(fromDate.getTime());
        if (toDate != null) query.field("startTime").lessThanOrEq(toDate.getTime());
        if (playerId != null) query.filter("seats elem", new BasicDBObject("playerId", playerId));
        return query.order("-startTime").asList();
    }

    @Override
    public HistoricHand findHandById(String handId) {
        Query query = mongoStorage.createQuery(HistoricHand.class);
        query.field("_id").equal(handId);
        return (HistoricHand)query.get();
    }

    @Override
    public List<HistoricTournament> findTournaments(Date fromDate, Date toDate) {
        log.info("Finding tournaments by query: from: " + fromDate + " to: " + toDate);
        Query query = mongoStorage.createQuery(HistoricTournament.class);
        if (fromDate != null) query.field("startTime").greaterThanOrEq(fromDate.getTime());
        if (toDate != null) query.field("startTime").lessThanOrEq(toDate.getTime());
        return query.order("-startTime").asList();
    }

    @Override
    public HistoricTournament findTournamentByHistoricId(String id) {
        Query query = mongoStorage.createQuery(HistoricTournament.class);
        query.field("id").equal(new ObjectId(id));
        return (HistoricTournament)query.get();
    }

    @Override
    protected void finalize() throws Throwable {
        mongoStorage.disconnect();
        super.finalize();
    }
}