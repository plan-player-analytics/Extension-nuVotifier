/*
    Copyright(c) 2019 Risto Lahtela (Rsl1122)

    The MIT License(MIT)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files(the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions :
    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/
package com.djrapitops.extension;

import com.djrapitops.plan.query.QueryService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class NuVotifierStorage {

    private final QueryService queryService;

    public NuVotifierStorage() {
        queryService = QueryService.getInstance();
        createTable();
        queryService.subscribeDataClearEvent(this::recreateTable);
        queryService.subscribeToPlayerRemoveEvent(this::removePlayer);
    }

    private void createTable() {
        String dbType = queryService.getDBType();
        boolean sqlite = dbType.equalsIgnoreCase("SQLITE");

        String sql = "CREATE TABLE IF NOT EXISTS plan_votes (" +
                "id int " + (sqlite ? "PRIMARY KEY" : "NOT NULL AUTO_INCREMENT") + ',' +
                "user_name varchar(36) NOT NULL," +
                "service varchar(150) NOT NULL," +
                "votes int NOT NULL" +
                (sqlite ? "" : ",PRIMARY KEY (id)") +
                ')';

        queryService.execute(sql, PreparedStatement::execute);
    }

    private void dropTable() {
        queryService.execute("DROP TABLE IF EXISTS plan_votes", PreparedStatement::execute);
    }

    private void recreateTable() {
        dropTable();
        createTable();
    }

    private void removePlayer(UUID playerUUID) {
        Optional<String> userName = queryService.getCommonQueries()
                .fetchNameOf(playerUUID);
        if (!userName.isPresent()) return;
        queryService.execute(
                "REMOVE FROM plan_votes WHERE user_name=?",
                statement -> {
                    statement.setString(1, userName.get());
                    statement.execute();
                }
        );
    }

    public void storeVote(String userName, String service) throws ExecutionException {
        String update = "UPDATE plan_votes SET votes=votes+1 WHERE service=? AND user_name=?";
        String insert = "INSERT INTO plan_votes (votes, service, user_name) VALUES (1, ?, ?)";

        AtomicBoolean updated = new AtomicBoolean(false);
        try {
            queryService.execute(update, statement -> {
                statement.setString(1, service);
                statement.setString(2, userName);
                updated.set(statement.executeUpdate() > 0);
            }).get(); // Wait
            if (!updated.get()) {
                queryService.execute(insert, statement -> {
                    statement.setString(1, service);
                    statement.setString(2, userName);
                    statement.execute();
                });
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public int getVoteCount(String userName) {
        String sql = "SELECT sum(votes) as count FROM plan_votes WHERE user_name=?";

        return queryService.query(sql, statement -> {
            statement.setString(1, userName);
            try (ResultSet set = statement.executeQuery()) {
                return set.next() ? set.getInt("count") : -1;
            }
        });
    }

    public Map<String, Integer> getVotesPerService() {
        final String sql = "SELECT service, sum(votes) as count" +
                " FROM plan_votes" +
                " GROUP BY service";
        return queryService.query(sql, statement -> {
            try (ResultSet set = statement.executeQuery()) {
                Map<String, Integer> votesByService = new HashMap<>();
                while (set.next()) {
                    votesByService.put(set.getString("service"), set.getInt("count"));
                }
                return votesByService;
            }
        });
    }
}
