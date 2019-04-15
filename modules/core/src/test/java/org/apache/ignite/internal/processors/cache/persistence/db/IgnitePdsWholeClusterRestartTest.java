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

package org.apache.ignite.internal.processors.cache.persistence.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.WALMode;
import org.apache.ignite.spi.checkpoint.noop.NoopCheckpointSpi;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

/**
 *
 */
public class IgnitePdsWholeClusterRestartTest extends GridCommonAbstractTest {
    /** */
    private static final int GRID_CNT = 5;

    /** */
    private static final int ENTRIES_COUNT = 1_000;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        DataStorageConfiguration memCfg = new DataStorageConfiguration()
            .setDefaultDataRegionConfiguration(
                new DataRegionConfiguration().setMaxSize(100L * 1024 * 1024).setPersistenceEnabled(true))
            .setWalMode(WALMode.LOG_ONLY);

        cfg.setDataStorageConfiguration(memCfg);

        CacheConfiguration ccfg1 = defaultCacheConfiguration();

        ccfg1.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        ccfg1.setRebalanceMode(CacheRebalanceMode.SYNC);
        ccfg1.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
        ccfg1.setAffinity(new RendezvousAffinityFunction(false, 32));
        ccfg1.setBackups(2);

        cfg.setActiveOnStart(false);

        // To avoid hostname lookup on start.
        cfg.setCheckpointSpi(new NoopCheckpointSpi());

        cfg.setCacheConfiguration(ccfg1);

        cfg.setConsistentId(gridName);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        stopAllGrids();

        cleanPersistenceDir();
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        stopAllGrids();

        cleanPersistenceDir();
    }

        /**
     * @throws Exception if failed.
     */
    @Test
    public void testRestarts() throws Exception {
        startGrids(GRID_CNT);

        ignite(0).active(true);

        awaitPartitionMapExchange();

        try (IgniteDataStreamer<Object, Object> ds = ignite(0).dataStreamer(DEFAULT_CACHE_NAME)) {
            for (int i = 0; i < ENTRIES_COUNT; i++)
                ds.addData(i, i);
        }

        stopAllGrids();

        List<Integer> idxs = new ArrayList<>();

        for (int i = 0; i < GRID_CNT; i++)
            idxs.add(i);

        for (int r = 0; r < 10; r++) {
            Collections.shuffle(idxs);

            info("Will start in the following order: " + idxs);

            for (Integer idx : idxs)
                startGrid(idx);

            try {
                ignite(0).active(true);

                for (int g = 0; g < GRID_CNT; g++) {
                    Ignite ig = ignite(g);

                    for (int k = 0; k < ENTRIES_COUNT; k++)
                        assertEquals("Failed to read [g=" + g + ", part=" + ig.affinity(DEFAULT_CACHE_NAME).partition(k) +
                            ", nodes=" + ig.affinity(DEFAULT_CACHE_NAME).mapKeyToPrimaryAndBackups(k) + ']',
                            k, ig.cache(DEFAULT_CACHE_NAME).get(k));
                }
            }
            finally {
                stopAllGrids();
            }
        }
    }
}
