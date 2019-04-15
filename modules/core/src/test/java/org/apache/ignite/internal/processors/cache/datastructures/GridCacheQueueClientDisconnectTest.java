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

package org.apache.ignite.internal.processors.cache.datastructures;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteClientDisconnectedException;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

/**
 *
 */
public class GridCacheQueueClientDisconnectTest extends GridCommonAbstractTest {
    /** */
    private static final String IGNITE_QUEUE_NAME = "ignite-queue-client-reconnect-test";

    /** */
    private static final int FAILURE_DETECTION_TIMEOUT = 10_000;

    /** */
    private boolean clientMode;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        ((TcpDiscoverySpi)cfg.getDiscoverySpi()).setClientReconnectDisabled(false);

        cfg.setFailureDetectionTimeout(FAILURE_DETECTION_TIMEOUT);
        cfg.setClientFailureDetectionTimeout(FAILURE_DETECTION_TIMEOUT);

        if (clientMode)
            cfg.setClientMode(true);

        return cfg;
    }

    /**
     * @param cacheAtomicityMode Atomicity mode.
     * @return Configuration.
     */
    private static CollectionConfiguration collectionConfiguration(CacheAtomicityMode cacheAtomicityMode) {
        CollectionConfiguration colCfg = new CollectionConfiguration();

        colCfg.setAtomicityMode(cacheAtomicityMode);

        return colCfg;
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testClientDisconnect() throws Exception {
        try {
            Ignite server = startGrid(0);

            clientMode = true;

            Ignite client = startGrid(1);

            awaitPartitionMapExchange();

            final IgniteQueue queue = client.queue(
                IGNITE_QUEUE_NAME, 0, collectionConfiguration(CacheAtomicityMode.ATOMIC));

            final CountDownLatch latch = new CountDownLatch(1);

            GridTestUtils.runAsync(new Runnable() {
                @Override public void run() {
                    try {
                        Object value = queue.take();
                    }
                    catch (IgniteClientDisconnectedException icd) {
                        latch.countDown();
                    }
                    catch (Exception e) {
                    }
                }
            });

            U.sleep(5000);

            server.close();

            boolean countReachedZero = latch.await(FAILURE_DETECTION_TIMEOUT * 2, TimeUnit.MILLISECONDS);

            assertTrue("IgniteClientDisconnectedException was not thrown", countReachedZero);
        }
        finally {
            stopAllGrids();
        }
    }
}
