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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.cache.Cache;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.processors.affinity.AffinityTopologyVersion;
import org.apache.ignite.internal.processors.cache.GridCacheAffinityManager;
import org.apache.ignite.internal.processors.cache.GridCacheContext;
import org.apache.ignite.internal.processors.datastructures.GridCacheQueueHeaderKey;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.testframework.GridTestUtils;
import org.junit.Test;

import static org.apache.ignite.cache.CacheAtomicityMode.ATOMIC;
import static org.apache.ignite.cache.CacheMode.PARTITIONED;

/**
 * Queue failover test.
 */
public abstract class GridCacheAbstractQueueFailoverDataConsistencySelfTest extends IgniteCollectionAbstractTest {
    /** */
    private static final String QUEUE_NAME = "FailoverQueueTest";

    /** {@inheritDoc} */
    @Override protected int gridCount() {
        return 4;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override protected void afterTestsStopped() throws Exception {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        startGrids(gridCount());
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        stopAllGrids();
    }

    /** {@inheritDoc} */
    @Override protected long getTestTimeout() {
        return 5 * 60_000;
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        cfg.setMetricsLogFrequency(0);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected CacheMode collectionCacheMode() {
        return PARTITIONED;
    }

    /** {@inheritDoc} */
    @Override protected CollectionConfiguration collectionConfiguration() {
        CollectionConfiguration colCfg = super.collectionConfiguration();

        colCfg.setBackups(1);

        return colCfg;
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testAddFailover() throws Exception {
        testAddFailover(false);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testAddFailoverCollocated() throws Exception {
        testAddFailover(true);
    }

    /**
     * @param collocated Collocation flag.
     * @throws Exception If failed.
     */
    private void testAddFailover(boolean collocated) throws Exception {
        CollectionConfiguration colCfg = config(collocated);

        IgniteQueue<Integer> queue = grid(0).queue(QUEUE_NAME, 0, colCfg);

        assertNotNull(queue);
        assertEquals(0, queue.size());

        int primaryNode = primaryQueueNode(queue);

        int testNodeIdx = -1;

        for (int i = 0; i < gridCount(); i++) {
            if (i != primaryNode)
                testNodeIdx = i;
        }

        log.info("Test node: " + testNodeIdx) ;
        log.info("Header primary node: " + primaryNode) ;

        queue = grid(testNodeIdx).queue(QUEUE_NAME, 0, null);

        assertNotNull(queue);

        testAddFailover(queue, Arrays.asList(primaryNode)); // Kill queue header's primary node .

        List<Integer> killIdxs = new ArrayList<>();

        for (int i = 0; i < gridCount(); i++) {
            if (i != testNodeIdx)
                killIdxs.add(i);
        }

        testAddFailover(queue, killIdxs); // Kill random node.
    }

    /**
     * @param queue Queue.
     * @param killIdxs Indexes of nodes to kill.
     * @throws Exception If failed.
     */
    private void testAddFailover(IgniteQueue<Integer> queue, final List<Integer> killIdxs) throws Exception {
        assert !killIdxs.isEmpty();

        final AtomicBoolean stop = new AtomicBoolean();

        IgniteInternalFuture<?> fut = startNodeKiller(stop, new AtomicInteger(), killIdxs);

        final int ITEMS = (collectionCacheAtomicityMode() == ATOMIC) ? 10_000 : 3000;

        try {
            for (int i = 0; i < ITEMS; i++) {
                assertTrue(queue.add(i));

                if ((i + 1) % 500 == 0)
                    log.info("Added " + (i + 1) + " items.");
            }
        }
        finally {
            stop.set(true);
        }

        fut.get();

        log.info("Added all items.");

        for (int i = 0; i < ITEMS; i++) {
            assertEquals((Integer)i, queue.poll());

            if ((i + 1) % 500 == 0)
                log.info("Polled " + (i + 1) + " items.");
        }

        assertNull(queue.poll());
        assertEquals(0, queue.size());
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testPollFailover() throws Exception {
        testPollFailover(false);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testPollFailoverCollocated() throws Exception {
        testPollFailover(true);
    }

    /**
     * @param collocated Collocation flag.
     * @throws Exception If failed.
     */
    private void testPollFailover(boolean collocated) throws Exception {
        CollectionConfiguration colCfg = config(collocated);

        IgniteQueue<Integer> queue = grid(0).queue(QUEUE_NAME, 0, colCfg);

        assertNotNull(queue);
        assertEquals(0, queue.size());

        int primaryNode = primaryQueueNode(queue);

        int testNodeIdx = -1;

        for (int i = 0; i < gridCount(); i++) {
            if (i != primaryNode)
                testNodeIdx = i;
        }

        log.info("Test node: " + testNodeIdx) ;
        log.info("Primary node: " + primaryNode) ;

        queue = grid(testNodeIdx).queue(QUEUE_NAME, 0, null);

        assertNotNull(queue);

        testPollFailover(queue, Arrays.asList(primaryQueueNode(queue))); // Kill queue header's primary node .

        List<Integer> killIdxs = new ArrayList<>();

        for (int i = 0; i < gridCount(); i++) {
            if (i != testNodeIdx)
                killIdxs.add(i);
        }

        testPollFailover(queue, killIdxs); // Kill random node.
    }

    /**
     * @param queue Queue.
     * @param killIdxs Indexes of nodes to kill.
     * @throws Exception If failed.
     */
    private void testPollFailover(IgniteQueue<Integer> queue, final List<Integer> killIdxs) throws Exception {
        assert !killIdxs.isEmpty();

        final int ITEMS = collectionCacheAtomicityMode() == ATOMIC && !queue.collocated() ? 10_000 : 3000;

        for (int i = 0; i < ITEMS; i++) {
            assertTrue(queue.add(i));

            if ((i + 1) % 500 == 0)
                log.info("Added " + (i + 1) + " items.");
        }

        log.info("Added all items.");

        final AtomicBoolean stop = new AtomicBoolean();

        final AtomicInteger stopCnt = new AtomicInteger();

        IgniteInternalFuture<?> fut = startNodeKiller(stop, stopCnt, killIdxs);

        int err = 0;

        try {
            int pollNum = ITEMS;

            int exp = 0;

            for (int i = 0; i < pollNum; i++) {
                Integer e = queue.poll();

                if (collectionCacheAtomicityMode() == ATOMIC) {
                    if (e == null || e != exp) {
                        log.info("Unexpected data [expected=" + i + ", actual=" + e + ']');

                        err++;

                        pollNum--;

                        exp = e != null ? (e + 1) : (exp + 1);
                    }
                    else
                        exp++;
                }
                else
                    assertEquals((Integer)i, e);

                if ((i + 1) % 500 == 0)
                    log.info("Polled " + (i + 1) + " items.");
            }
        }
        finally {
            stop.set(true);
        }

        fut.get();

        if (collectionCacheAtomicityMode() == ATOMIC)
            assertTrue("Too many errors for atomic cache: " + err, err <= stopCnt.get());

        assertNull(queue.poll());
        assertEquals(0, queue.size());
    }

    /**
     * Starts thread restarting random node (node's index is chosen using given collection).
     *
     * @param stop Stop flag.
     * @param killCnt Counter incremented after node restart.
     * @param killIdxs Indexes of nodes to kill.
     * @return Future completing when thread finishes.
     */
    private IgniteInternalFuture<?> startNodeKiller(final AtomicBoolean stop,
        final AtomicInteger killCnt,
        final List<Integer> killIdxs) {
        return GridTestUtils.runAsync(new Callable<Void>() {
            @Override public Void call() throws Exception {
                ThreadLocalRandom rnd = ThreadLocalRandom.current();

                while (!stop.get()) {
                    int idx = killIdxs.get(rnd.nextInt(0, killIdxs.size()));

                    U.sleep(rnd.nextLong(500, 1000));

                    log.info("Killing node: " + idx);

                    stopGrid(idx);

                    U.sleep(rnd.nextLong(500, 1000));

                    startGrid(idx);

                    killCnt.incrementAndGet();
                }

                return null;
            }
        });
    }

    /**
     * @param queue Queue.
     * @return Primary node for queue's header.
     * @throws Exception If failed.
     */
    private int primaryQueueNode(IgniteQueue queue) throws Exception {
        GridCacheContext cctx = GridTestUtils.getFieldValue(queue, "cctx");

        GridCacheAffinityManager aff = cctx.affinity();

        CachePeekMode[] modes = new CachePeekMode[]{CachePeekMode.ALL};

        for (int i = 0; i < gridCount(); i++) {
            for (Cache.Entry e : grid(i).context().cache().internalCache(cctx.name()).localEntries(modes)) {
                Object key = e.getKey();

                if (aff.primaryByKey(grid(i).localNode(), key, AffinityTopologyVersion.NONE)
                    && key instanceof GridCacheQueueHeaderKey)
                    return i;
            }
        }

        fail("Failed to find primary node for queue header.");

        return -1;
    }
}
