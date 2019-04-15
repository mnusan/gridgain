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

package org.apache.ignite.ml.util.generators;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.ml.dataset.feature.extractor.impl.LabeledDummyVectorizer;
import org.apache.ignite.ml.dataset.impl.cache.CacheBasedDataset;
import org.apache.ignite.ml.dataset.impl.cache.CacheBasedDatasetBuilder;
import org.apache.ignite.ml.dataset.primitive.builder.context.EmptyContextBuilder;
import org.apache.ignite.ml.dataset.primitive.builder.data.SimpleDatasetDataBuilder;
import org.apache.ignite.ml.dataset.primitive.context.EmptyContext;
import org.apache.ignite.ml.dataset.primitive.data.SimpleDatasetData;
import org.apache.ignite.ml.environment.LearningEnvironmentBuilder;
import org.apache.ignite.ml.structures.LabeledVector;
import org.apache.ignite.ml.util.generators.primitives.scalar.GaussRandomProducer;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.stream.DoubleStream;

/**
 * Test for {@link DataStreamGenerator} cache filling.
 */
public class DataStreamGeneratorFillCacheTest extends GridCommonAbstractTest {
    /** */
    private Ignite ignite;

    /** */
    @Before
    public void before() throws Exception {
        ignite = startGrid();
    }

    /** */
    @After
    public void after() throws Exception {
        ignite.close();
    }

    /** */
    @Test
    public void testCacheFilling() {
        IgniteConfiguration configuration = new IgniteConfiguration().setPeerClassLoadingEnabled(true);
        String cacheName = "TEST_CACHE";
        CacheConfiguration<UUID, LabeledVector<Double>> cacheConfiguration =
            new CacheConfiguration<UUID, LabeledVector<Double>>(cacheName)
                .setAffinity(new RendezvousAffinityFunction(false, 10));
        int datasetSize = 5000;

        try (Ignite ignite = Ignition.start(configuration)) {
            IgniteCache<UUID, LabeledVector<Double>> cache = ignite.getOrCreateCache(cacheConfiguration);
            DataStreamGenerator generator = new GaussRandomProducer(0).vectorize(1).asDataStream();
            generator.fillCacheWithVecUUIDAsKey(datasetSize, cache);

            LabeledDummyVectorizer<UUID, Double> vectorizer = new LabeledDummyVectorizer<>();
            CacheBasedDatasetBuilder<UUID, LabeledVector<Double>> datasetBuilder = new CacheBasedDatasetBuilder<>(ignite, cache);
            try (CacheBasedDataset<UUID, LabeledVector<Double>, EmptyContext, SimpleDatasetData> dataset =
                     datasetBuilder.build(LearningEnvironmentBuilder.defaultBuilder(),
                         new EmptyContextBuilder<>(), new SimpleDatasetDataBuilder<>(vectorizer))) {

                StatPair result = dataset.compute(
                    data -> new StatPair(DoubleStream.of(data.getFeatures()).sum(), data.getRows()),
                    StatPair::sum
                );

                assertEquals(datasetSize, result.countOfRows);
                assertEquals(0.0, result.elementsSum / result.countOfRows, 1e-2);
            }

            ignite.destroyCache(cacheName);
        }
    }

    /** */
    static class StatPair {
        /** */
        private double elementsSum;

        /** */
        private int countOfRows;

        /** */
        public StatPair(double elementsSum, int countOfRows) {
            this.elementsSum = elementsSum;
            this.countOfRows = countOfRows;
        }

        /** */
        static StatPair sum(StatPair left, StatPair right) {
            if (left == null && right == null)
                return new StatPair(0, 0);
            else if (left == null)
                return right;
            else if (right == null)
                return left;
            else
                return new StatPair(
                    right.elementsSum + left.elementsSum,
                    right.countOfRows + left.countOfRows
                );
        }
    }
}
