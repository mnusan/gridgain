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

package org.apache.ignite.internal.processors.cache.mvcc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.cache.CacheException;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.TransactionDuplicateKeyException;
import org.junit.Test;

import static java.util.Arrays.asList;

/**
 *
 */
public class CacheMvccDmlSimpleTest extends CacheMvccAbstractTest {
    /** */
    private IgniteCache<?, ?> cache;

    /** {@inheritDoc} */
    @Override protected CacheMode cacheMode() {
        return CacheMode.PARTITIONED;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        cache = startGrid(0).getOrCreateCache(
            new CacheConfiguration<>("test")
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL_SNAPSHOT)
                .setSqlSchema("PUBLIC")
                .setIndexedTypes(Integer.class, Integer.class)
        );
    }

    /**
     * @throws Exception if failed.
     */
    @Test
    public void testInsert() throws Exception {
        int cnt = update("insert into Integer(_key, _val) values(1, 1),(2, 2)");

        assertEquals(2, cnt);

        assertEquals(asSet(asList(1, 1), asList(2, 2)), query("select * from Integer"));

        try {
            update("insert into Integer(_key, _val) values(3, 3),(1, 1)");
        } catch (CacheException e) {
            assertTrue(e.getCause() instanceof TransactionDuplicateKeyException);
            assertTrue(e.getMessage().startsWith("Duplicate key during INSERT ["));
        }

        assertEquals(asSet(asList(1, 1), asList(2, 2)), query("select * from Integer"));
    }

    /**
     * @throws Exception if failed.
     */
    @Test
    public void testMerge() throws Exception {
        {
            int cnt = update("merge into Integer(_key, _val) values(1, 1),(2, 2)");

            assertEquals(2, cnt);
            assertEquals(asSet(asList(1, 1), asList(2, 2)), query("select * from Integer"));
        }

        {
            int cnt = update("merge into Integer(_key, _val) values(3, 3),(1, 1)");

            assertEquals(2, cnt);
            assertEquals(asSet(asList(1, 1), asList(2, 2), asList(3, 3)), query("select * from Integer"));
        }
    }

    /**
     * @throws Exception if failed.
     */
    @Test
    public void testUpdate() throws Exception {
        {
            int cnt = update("update Integer set _val = 42 where _key = 42");

            assertEquals(0, cnt);
            assertTrue(query("select * from Integer").isEmpty());
        }

        update("insert into Integer(_key, _val) values(1, 1),(2, 2)");

        {
            int cnt = update("update Integer set _val = 42 where _key = 42");

            assertEquals(0, cnt);
            assertEquals(asSet(asList(1, 1), asList(2, 2)), query("select * from Integer"));
        }

        {
            int cnt = update("update Integer set _val = 42 where _key >= 42");

            assertEquals(0, cnt);
            assertEquals(asSet(asList(1, 1), asList(2, 2)), query("select * from Integer"));
        }

        {
            int cnt = update("update Integer set _val = 11 where _key = 1");

            assertEquals(1, cnt);
            assertEquals(asSet(asList(1, 11), asList(2, 2)), query("select * from Integer"));
        }

        {
            int cnt = update("update Integer set _val = 12 where _key <= 2");

            assertEquals(asSet(asList(1, 12), asList(2, 12)), query("select * from Integer"));
            assertEquals(2, cnt);
        }
    }

    /**
     * @throws Exception if failed.
     */
    @Test
    public void testDelete() throws Exception {
        {
            int cnt = update("delete from Integer where _key = 42");

            assertEquals(0, cnt);
        }

        update("insert into Integer(_key, _val) values(1, 1),(2, 2)");

        {
            int cnt = update("delete from Integer where _key = 42");

            assertEquals(0, cnt);
            assertEquals(asSet(asList(1, 1), asList(2, 2)), query("select * from Integer"));
        }

        {
            int cnt = update("delete from Integer where _key >= 42");

            assertEquals(0, cnt);
            assertEquals(asSet(asList(1, 1), asList(2, 2)), query("select * from Integer"));
        }

        {
            int cnt = update("delete from Integer where _key = 1");

            assertEquals(1, cnt);
            assertEquals(asSet(asList(2, 2)), query("select * from Integer"));
        }

        {
            int cnt = update("delete from Integer where _key <= 2");

            assertTrue(query("select * from Integer").isEmpty());
            assertEquals(1, cnt);
        }
    }

    /**
     * @param q Query.
     * @return Row set.
     */
    private Set<List<?>> query(String q) {
        return new HashSet<>(cache.query(new SqlFieldsQuery(q)).getAll());
    }

    /**
     * @param q Query.
     * @return Updated rows count.
     */
    private int update(String q) {
        return Integer.parseInt(cache.query(new SqlFieldsQuery(q)).getAll().get(0).get(0).toString());
    }

    /** */
    private Set<List<?>> asSet(List<?>... ls) {
        return new HashSet<>(asList(ls));
    }
}
