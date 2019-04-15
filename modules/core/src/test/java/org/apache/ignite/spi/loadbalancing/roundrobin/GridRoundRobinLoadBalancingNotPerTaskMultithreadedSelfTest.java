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

package org.apache.ignite.spi.loadbalancing.roundrobin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.ignite.GridTestJob;
import org.apache.ignite.GridTestTaskSession;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.compute.ComputeTaskSession;
import org.apache.ignite.lang.IgniteUuid;
import org.apache.ignite.testframework.GridSpiTestContext;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.spi.GridSpiAbstractTest;
import org.apache.ignite.testframework.junits.spi.GridSpiTest;
import org.apache.ignite.testframework.junits.spi.GridSpiTestConfig;
import org.junit.Test;

/**
 * Multithreaded tests for global load balancer.
 */
@GridSpiTest(spi = RoundRobinLoadBalancingSpi.class, group = "Load Balancing SPI")
public class GridRoundRobinLoadBalancingNotPerTaskMultithreadedSelfTest
    extends GridSpiAbstractTest<RoundRobinLoadBalancingSpi> {
    /** Thread count. */
    public static final int THREAD_CNT = 8;

    /** Per-thread iteration count. */
    public static final int ITER_CNT = 4_000_000;

    /**
     * @return Per-task configuration parameter.
     */
    @GridSpiTestConfig
    public boolean getPerTask() {
        return false;
    }

    /** {@inheritDoc} */
    @Override protected GridSpiTestContext initSpiContext() throws Exception {
        GridSpiTestContext spiCtx = super.initSpiContext();

        spiCtx.createLocalNode();
        spiCtx.createRemoteNodes(10);

        return spiCtx;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        assert !getSpi().isPerTask() : "Invalid SPI configuration.";
    }

    /**
     *
     * @throws Exception If failed.
     */
    @Test
    public void testMultipleTaskSessionsMultithreaded() throws Exception {
        final RoundRobinLoadBalancingSpi spi = getSpi();

        final List<ClusterNode> allNodes = (List<ClusterNode>)getSpiContext().nodes();

        GridTestUtils.runMultiThreaded(new Callable<Object>() {
            @Override public Object call() throws Exception {
                ComputeTaskSession ses = new GridTestTaskSession(IgniteUuid.randomUuid());

                Map<UUID, AtomicInteger> nodeCnts = new HashMap<>();

                for (int i = 1; i <= ITER_CNT; i++) {
                    ClusterNode node = spi.getBalancedNode(ses, allNodes, new GridTestJob());

                    if (!nodeCnts.containsKey(node.id()))
                        nodeCnts.put(node.id(), new AtomicInteger(1));
                    else
                        nodeCnts.get(node.id()).incrementAndGet();
                }

                int predictCnt = ITER_CNT / allNodes.size();

                // Consider +-20% is permissible spread for single node measure.
                int floor = (int)(predictCnt * 0.8);

                double avgSpread = 0;

                for (ClusterNode n : allNodes) {
                    int curCnt = nodeCnts.get(n.id()).intValue();

                    avgSpread += Math.abs(predictCnt - curCnt);

                    String msg = "Node stats [id=" + n.id() + ", cnt=" + curCnt + ", floor=" + floor +
                        ", predictCnt=" + predictCnt + ']';

                    info(msg);

                    assertTrue(msg, curCnt >= floor);
                }

                avgSpread /= allNodes.size();

                avgSpread = 100.0 * avgSpread / predictCnt;

                info("Average spread for " + allNodes.size() + " nodes is " + avgSpread + " percents");

                // Consider +-10% is permissible average spread for all nodes.
                assertTrue("Average spread is too big: " + avgSpread, avgSpread <= 10);

                return null;
            }
        }, THREAD_CNT, "balancer-test-worker");
    }
}
