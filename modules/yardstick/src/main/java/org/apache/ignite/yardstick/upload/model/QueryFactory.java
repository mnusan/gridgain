/*
 *                   GridGain Community Edition Licensing
 *                   Copyright 2019 GridGain Systems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License") modified with Commons Clause
 * Restriction; you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * 
 * Commons Clause Restriction
 * 
 * The Software is provided to you by the Licensor under the License, as defined below, subject to
 * the following condition.
 * 
 * Without limiting other conditions in the License, the grant of rights under the License will not
 * include, and the License does not grant to you, the right to Sell the Software.
 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you
 * under the License to provide to third parties, for a fee or other consideration (including without
 * limitation fees for hosting or consulting/ support services related to the Software), a product or
 * service whose value derives, entirely or substantially, from the functionality of the Software.
 * Any license notice or attribution required by the License must also include this Commons Clause
 * License Condition notice.
 * 
 * For purposes of the clause above, the “Licensor” is Copyright 2019 GridGain Systems, Inc.,
 * the “License” is the Apache License, Version 2.0, and the Software is the GridGain Community
 * Edition software provided with this notice.
 */

package org.apache.ignite.yardstick.upload.model;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.yardstick.upload.StreamerParams;
import org.jetbrains.annotations.Nullable;

/**
 * Factory that hides all test data details: what query to use to create table or what random arguments to set in
 * prepared statement.
 */
public class QueryFactory {
    /** Name of the table upload data to. */
    public static final String UPLOAD_TABLE_NAME = "TEST_UPLOAD";

    /** Query to drop table if it exists. */
    public static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS test_upload;";

    /** Query to count table size. */
    public static final String COUNT = "SELECT COUNT(id) FROM test_upload;";

    /** Turns off Write Ahead Log. */
    public static final String TURN_OFF_WAL = "ALTER TABLE test_upload NOLOGGING";

    /** Turns on Write Ahead Log. */
    public static final String TURN_ON_WAL = "ALTER TABLE test_upload LOGGING";

    /** Turns off streaming mode. */
    public static final String TURN_OFF_STREAMING = "SET STREAMING OFF";

    /** Number of "values" fields in the test table (any field except primary key). */
    private int valFieldsCnt = 10;

    /** Parametrised query to insert new row. */
    private String insert = newInsertQuery();

    /** Atomicity mode of test table's cache. */
    private CacheAtomicityMode tabAtomicMode;

    /** */
    public QueryFactory(CacheAtomicityMode tabAtomicMode) {
        this.tabAtomicMode = tabAtomicMode;
    }

    /**
     * Create table with long primary key and number of long and varchar fields
     */
    public String createTable() {
        StringBuilder create = new StringBuilder("CREATE TABLE test_upload (id LONG PRIMARY KEY");

        for (int vi = 1; vi <= valFieldsCnt; vi++) {
            create.append(", val_").append(vi);

            if (vi % 2 == 1)
                create.append(" VARCHAR(255)");
            else
                create.append(" LONG");

        }

        create.append(')');

        StringBuilder withClause = new StringBuilder("key_type=Long, value_type=Values10");

        if (tabAtomicMode != null)
            withClause.append(", ATOMICITY=").append(tabAtomicMode.name());

        create.append(" WITH \"").append(withClause).append("\";");

        return create.toString();
    }

    /**
     * See {@link #insert}.
     */
    private String newInsertQuery() {
        StringBuilder insert = new StringBuilder("INSERT INTO test_upload VALUES (?");
        for (int vi = 1; vi <= valFieldsCnt; vi++)
            insert.append(", ?");

        insert.append(");");
        return insert.toString();
    }

    /**
     * See {@link #insert}.
     */
    public String insert() {
        return insert;
    }

    /**
     * @param csvFilePath path to csv file.
     * @param packetSize if not null, add packet_size query option.
     * @return sql query that inserts data from specified csv file.
     */
    public String copyFrom(String csvFilePath, @Nullable Long packetSize) {
        String pSizeExpr = "";

        if (packetSize != null)
            pSizeExpr = " packet_size " + packetSize;

        return "COPY FROM '" + csvFilePath + "' " +
            "INTO test_upload " + attributes() + " " +
            "FORMAT CSV" + pSizeExpr + ";";
    }

    /**
     * Creates string - comma-separated attributes of test table, surrounded with braces Is used as a part of sql
     * statement.
     *
     * @return attributes list of test table as part of sql statement.
     */
    private String attributes() {
        StringBuilder attrs = new StringBuilder("(id");

        for (int vi = 1; vi <= valFieldsCnt; vi++)
            attrs.append(", val_").append(vi);

        attrs.append(')');

        return attrs.toString();
    }

    /**
     * Fills specified prepared statement with random values and specified id (primary key).
     *
     * @param stmt prepared statement, built from {@link #insert} query.
     * @param id id in the test table.
     * @throws SQLException if statement is not correct.
     */
    public void setRandomInsertArgs(PreparedStatement stmt, long id) throws SQLException {
        stmt.setLong(1, id);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int vi = 1; vi <= valFieldsCnt; vi++) {
            // vi is value index (among all values), but we also have "id" which is primary key
            // so index in query is value index shifted by 1.
            int qryIdx = vi + 1;

            long nextVal = rnd.nextLong();

            if (vi % 2 == 1)
                stmt.setLong(qryIdx, nextVal);
            else
                stmt.setString(qryIdx, String.valueOf(nextVal));
        }
    }

    /**
     * Generates CSV line containing specified id and random values. This line corresponds 1 row of the test table,
     * which will be inserted in the end.
     *
     * @param id key in the test table.
     * @return generated comma-separated line.
     */
    public String randomCsvLine(long id) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        StringBuilder line = new StringBuilder().append(id);

        for (int vi = 1; vi <= valFieldsCnt; vi++) {
            line.append(',');

            if (vi % 2 == 1)
                line.append(rnd.nextLong());
            else
                line.append('"').append(rnd.nextLong()).append('"');
        }

        return line.toString();
    }

    /**
     * Sql command that turns on streaming with specified parameters.
     *
     * @param p - POJO containing parameters for streamer.
     * @return - sql command to turn on streaming.
     */
    @SuppressWarnings("ConstantConditions")
    public String turnOnStreaming(StreamerParams p) {
        StringBuilder cmd = new StringBuilder("SET STREAMING ON");

        if (p.streamerLocalBatchSize() != null)
            cmd.append(" BATCH_SIZE ").append(p.streamerLocalBatchSize());

        if (p.streamerAllowOverwrite() != null) {
            String val = p.streamerAllowOverwrite() ? "ON" : "OFF";

            cmd.append(" ALLOW_OVERWRITE ").append(val);
        }

        if (p.streamerPerNodeParallelOperations() != null)
            cmd.append(" PER_NODE_PARALLEL_OPERATIONS ").append(p.streamerPerNodeParallelOperations());

        if (p.streamerPerNodeBufferSize() != null)
            cmd.append(" PER_NODE_BUFFER_SIZE ").append(p.streamerPerNodeBufferSize());

        if (p.streamerOrdered())
            cmd.append(" ORDERED");

        return cmd.append(';').toString();
    }

    /**
     * @param valIdx index of value to create index on. Should in range [1..10].
     * @return query that creates index on n-th value field.
     */
    public String createIndex(int valIdx) {
        if (valIdx > 10 || valIdx < 1)
            throw new IllegalArgumentException("Incorrect value index [" + valIdx + "]." +
                " Value index should be in range [1..10].");

        String colName = "val_" + valIdx;
        String idxName = colName + "_idx";

        return "CREATE INDEX " + idxName + " ON test_upload (" + colName + ");";
    }
}
