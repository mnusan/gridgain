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

package org.apache.ignite.testsuites;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.ignite.internal.processors.cache.GridCachePreloadingEvictionsSelfTest;
import org.apache.ignite.internal.processors.cache.distributed.near.GridCacheAtomicNearEvictionSelfTest;
import org.apache.ignite.internal.processors.cache.distributed.near.GridCacheNearEvictionSelfTest;
import org.apache.ignite.internal.processors.cache.eviction.DhtAndNearEvictionTest;
import org.apache.ignite.internal.processors.cache.eviction.GridCacheConcurrentEvictionConsistencySelfTest;
import org.apache.ignite.internal.processors.cache.eviction.GridCacheConcurrentEvictionsSelfTest;
import org.apache.ignite.internal.processors.cache.eviction.GridCacheEmptyEntriesLocalSelfTest;
import org.apache.ignite.internal.processors.cache.eviction.GridCacheEmptyEntriesPartitionedSelfTest;
import org.apache.ignite.internal.processors.cache.eviction.GridCacheEvictableEntryEqualsSelfTest;
import org.apache.ignite.internal.processors.cache.eviction.GridCacheEvictionFilterSelfTest;
import org.apache.ignite.internal.processors.cache.eviction.GridCacheEvictionLockUnlockSelfTest;
import org.apache.ignite.internal.processors.cache.eviction.GridCacheEvictionTouchSelfTest;
import org.apache.ignite.internal.processors.cache.eviction.fifo.FifoEvictionPolicyFactorySelfTest;
import org.apache.ignite.internal.processors.cache.eviction.fifo.FifoEvictionPolicySelfTest;
import org.apache.ignite.internal.processors.cache.eviction.lru.LruEvictionPolicyFactorySelfTest;
import org.apache.ignite.internal.processors.cache.eviction.lru.LruEvictionPolicySelfTest;
import org.apache.ignite.internal.processors.cache.eviction.lru.LruNearEvictionPolicySelfTest;
import org.apache.ignite.internal.processors.cache.eviction.lru.LruNearOnlyNearEvictionPolicySelfTest;
import org.apache.ignite.internal.processors.cache.eviction.paged.PageEvictionDataStreamerTest;
import org.apache.ignite.internal.processors.cache.eviction.paged.PageEvictionMetricTest;
import org.apache.ignite.internal.processors.cache.eviction.paged.PageEvictionPagesRecyclingAndReusingTest;
import org.apache.ignite.internal.processors.cache.eviction.paged.PageEvictionReadThroughTest;
import org.apache.ignite.internal.processors.cache.eviction.paged.PageEvictionTouchOrderTest;
import org.apache.ignite.internal.processors.cache.eviction.paged.Random2LruNearEnabledPageEvictionMultinodeTest;
import org.apache.ignite.internal.processors.cache.eviction.paged.Random2LruPageEvictionMultinodeTest;
import org.apache.ignite.internal.processors.cache.eviction.paged.Random2LruPageEvictionWithRebalanceTest;
import org.apache.ignite.internal.processors.cache.eviction.paged.RandomLruNearEnabledPageEvictionMultinodeTest;
import org.apache.ignite.internal.processors.cache.eviction.paged.RandomLruPageEvictionMultinodeTest;
import org.apache.ignite.internal.processors.cache.eviction.paged.RandomLruPageEvictionWithRebalanceTest;
import org.apache.ignite.internal.processors.cache.eviction.sorted.SortedEvictionPolicyFactorySelfTest;
import org.apache.ignite.internal.processors.cache.eviction.sorted.SortedEvictionPolicySelfTest;
import org.apache.ignite.testframework.GridTestUtils;

/**
 * Test suite for cache eviction.
 */
public class IgniteCacheEvictionSelfTestSuite {
    /**
     * @param ignoredTests Ignored tests.
     * @return Cache eviction test suite.
     */
    public static List<Class<?>> suite(Collection<Class> ignoredTests) {
        List<Class<?>> suite = new ArrayList<>();

        GridTestUtils.addTestIfNeeded(suite, FifoEvictionPolicySelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, SortedEvictionPolicySelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, LruEvictionPolicySelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, FifoEvictionPolicyFactorySelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, SortedEvictionPolicyFactorySelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, LruEvictionPolicyFactorySelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, LruNearEvictionPolicySelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, LruNearOnlyNearEvictionPolicySelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, GridCacheNearEvictionSelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, GridCacheAtomicNearEvictionSelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, GridCacheEvictionFilterSelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, GridCacheConcurrentEvictionsSelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, GridCacheConcurrentEvictionConsistencySelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, GridCacheEvictionTouchSelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, GridCacheEvictionLockUnlockSelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, GridCachePreloadingEvictionsSelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, GridCacheEmptyEntriesPartitionedSelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, GridCacheEmptyEntriesLocalSelfTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, GridCacheEvictableEntryEqualsSelfTest.class, ignoredTests);

        GridTestUtils.addTestIfNeeded(suite, RandomLruPageEvictionMultinodeTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, RandomLruNearEnabledPageEvictionMultinodeTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, Random2LruPageEvictionMultinodeTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, Random2LruNearEnabledPageEvictionMultinodeTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, RandomLruPageEvictionWithRebalanceTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, Random2LruPageEvictionWithRebalanceTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, PageEvictionTouchOrderTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, PageEvictionReadThroughTest.class, ignoredTests);
        GridTestUtils.addTestIfNeeded(suite, PageEvictionDataStreamerTest.class, ignoredTests);

        GridTestUtils.addTestIfNeeded(suite, PageEvictionMetricTest.class, ignoredTests);

        GridTestUtils.addTestIfNeeded(suite, PageEvictionPagesRecyclingAndReusingTest.class, ignoredTests);

        GridTestUtils.addTestIfNeeded(suite, DhtAndNearEvictionTest.class, ignoredTests);

        return suite;
    }
}
