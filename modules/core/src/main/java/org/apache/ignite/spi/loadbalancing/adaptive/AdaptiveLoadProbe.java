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

package org.apache.ignite.spi.loadbalancing.adaptive;

import org.apache.ignite.cluster.ClusterNode;

/**
 * Pluggable implementation of node load probing. Implementations
 * of this can be configured to be used with {@link AdaptiveLoadBalancingSpi}
 * by setting {@link AdaptiveLoadBalancingSpi#setLoadProbe(AdaptiveLoadProbe)}
 * configuration parameter.
 * <p>
 * Note that if {@link #getLoad(org.apache.ignite.cluster.ClusterNode, int)} returns a value of {@code 0},
 * then implementation will assume that load value is simply not available and
 * will try to calculate an average of load values for other nodes. If such
 * average cannot be obtained (all node load values are {@code 0}), then a value
 * of {@code 1} will be used.
 * <p>
 * By default, {@link AdaptiveCpuLoadProbe} probing implementation is used.
 * <p>
 * <h1 class="header">Example</h1>
 * Here is an example of how probing can be implemented to use
 * number of active and waiting jobs as probing mechanism:
 * <pre name="code" class="java">
 * public class FooBarLoadProbe implements GridAdaptiveLoadProbe {
 *     // Flag indicating whether to use average value or current.
 *     private int useAvg = true;
 *
 *     public FooBarLoadProbe(boolean useAvg) {
 *         this.useAvg = useAvg;
 *     }
 *
 *     // Calculate load based on number of active and waiting jobs.
 *     public double getLoad(ClusterNode node, int jobsSentSinceLastUpdate) {
 *         GridNodeMetrics metrics = node.getMetrics();
 *
 *         if (useAvg) {
 *             double load = metrics.getAverageActiveJobs() + metrics.getAverageWaitingJobs();
 *
 *             if (load > 0) {
 *                 return load;
 *             }
 *         }
 *
 *         return metrics.getCurrentActiveJobs() + metrics.getCurrentWaitingJobs();
 *     }
 * }
 * </pre>
 * Below is an example of how a probe shown above would be configured with {@link AdaptiveLoadBalancingSpi}
 * SPI:
 * <pre name="code" class="xml">
 * &lt;property name="loadBalancingSpi"&gt;
 *     &lt;bean class="org.apache.ignite.spi.loadBalancing.adaptive.GridAdaptiveLoadBalancingSpi"&gt;
 *         &lt;property name="loadProbe"&gt;
 *             &lt;bean class="foo.bar.FooBarLoadProbe"&gt;
 *                 &lt;constructor-arg value="true"/&gt;
 *             &lt;/bean&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 * &lt;/property&gt;
 * </pre>
 */
public interface AdaptiveLoadProbe {
    /**
     * Calculates load value for a given node. Specific implementations would
     * usually take into account some of the values provided by
     * {@link org.apache.ignite.cluster.ClusterNode#metrics()} method. For example, load can be calculated
     * based on job execution time or number of active jobs, or CPU/Heap utilization.
     * <p>
     * Note that if this method returns a value of {@code 0},
     * then implementation will assume that load value is simply not available and
     * will try to calculate an average of load values for other nodes. If such
     * average cannot be obtained (all node load values are {@code 0}), then a value
     * of {@code 1} will be used.
     *
     * @param node Grid node to calculate load for.
     * @param jobsSentSinceLastUpdate Number of jobs sent to this node since
     *      last metrics update. This parameter may be useful when
     *      implementation takes into account the current job count on a node.
     * @return Non-negative load value for the node (zero and above).
     */
    public double getLoad(ClusterNode node, int jobsSentSinceLastUpdate);
}