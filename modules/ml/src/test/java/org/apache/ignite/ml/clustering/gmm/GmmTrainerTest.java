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

package org.apache.ignite.ml.clustering.gmm;

import org.apache.ignite.ml.common.TrainerTest;
import org.apache.ignite.ml.dataset.feature.extractor.Vectorizer;
import org.apache.ignite.ml.dataset.feature.extractor.impl.ArraysVectorizer;
import org.apache.ignite.ml.dataset.feature.extractor.impl.FeatureLabelExtractorWrapper;
import org.apache.ignite.ml.dataset.impl.local.LocalDatasetBuilder;
import org.apache.ignite.ml.math.primitives.vector.Vector;
import org.apache.ignite.ml.math.primitives.vector.VectorUtils;
import org.apache.ignite.ml.math.primitives.vector.impl.DenseVector;
import org.apache.ignite.ml.structures.LabeledVector;
import org.apache.ignite.ml.trainers.FeatureLabelExtractor;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for GMM trainer.
 */
public class GmmTrainerTest extends TrainerTest {
    /** Data. */
    private static final Map<Integer, double[]> data = new HashMap<>();

    static {
        data.put(0, new double[] {1.0, 1.0, 1.0});
        data.put(1, new double[] {1.0, 2.0, 1.0});
        data.put(2, new double[] {2.0, 1.0, 1.0});
        data.put(3, new double[] {-1.0, -1.0, 2.0});
        data.put(4, new double[] {-1.0, -2.0, 2.0});
        data.put(5, new double[] {-2.0, -1.0, 2.0});
    }

    /** */
    @Test
    public void testFit() {
        GmmTrainer trainer = new GmmTrainer(2, 1)
            .withInitialMeans(Arrays.asList(
                VectorUtils.of(1.0, 2.0),
                VectorUtils.of(-1.0, -2.0)));
        GmmModel model = trainer.fit(
            new LocalDatasetBuilder<>(data, parts),
            new ArraysVectorizer<Integer>().labeled(Vectorizer.LabelCoordinate.LAST)
        );

        Assert.assertEquals(2, model.countOfComponents());
        Assert.assertEquals(2, model.dimension());
        Assert.assertArrayEquals(new double[] {1.33, 1.33}, model.distributions().get(0).mean().asArray(), 1e-2);
        Assert.assertArrayEquals(new double[] {-1.33, -1.33}, model.distributions().get(1).mean().asArray(), 1e-2);
    }

    /** */
    @Test(expected = IllegalArgumentException.class)
    public void testOnEmptyPartition() throws Throwable {
        GmmTrainer trainer = new GmmTrainer(2, 1)
            .withInitialMeans(Arrays.asList(VectorUtils.of(1.0, 2.0), VectorUtils.of(-1.0, -2.0)));

        try {
            trainer.fit(
                new LocalDatasetBuilder<>(new HashMap<>(), parts),
                FeatureLabelExtractorWrapper.wrap((k, v) -> new DenseVector(2), (k, v) -> 1.0)
            );
        }
        catch (RuntimeException e) {
            throw e.getCause();
        }
    }

    /** */
    @Test
    public void testUpdateOnEmptyDataset() {
        GmmTrainer trainer = new GmmTrainer(2, 1)
            .withInitialMeans(Arrays.asList(
                VectorUtils.of(1.0, 2.0),
                VectorUtils.of(-1.0, -2.0)));
        GmmModel model = trainer.fit(
            new LocalDatasetBuilder<>(data, parts),
            new ArraysVectorizer<Integer>().labeled(Vectorizer.LabelCoordinate.LAST)
        );

        model = trainer.updateModel(model,
            new LocalDatasetBuilder<>(new HashMap<>(), parts),
            new FeatureLabelExtractorWrapper<>(new FeatureLabelExtractor<Double, Vector, Double>() {
                private static final long serialVersionUID = -7245682432641745217L;

                @Override public LabeledVector<Double> extract(Double aDouble, Vector vector) {
                    return new LabeledVector<>(new DenseVector(2), 1.0);
                }
            })
        );

        Assert.assertEquals(2, model.countOfComponents());
        Assert.assertEquals(2, model.dimension());
        Assert.assertArrayEquals(new double[] {1.33, 1.33}, model.distributions().get(0).mean().asArray(), 1e-2);
        Assert.assertArrayEquals(new double[] {-1.33, -1.33}, model.distributions().get(1).mean().asArray(), 1e-2);
    }
}
