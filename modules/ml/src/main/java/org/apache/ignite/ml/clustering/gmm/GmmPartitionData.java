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

import org.apache.ignite.internal.util.typedef.internal.A;
import org.apache.ignite.ml.dataset.Dataset;
import org.apache.ignite.ml.dataset.PartitionDataBuilder;
import org.apache.ignite.ml.dataset.UpstreamEntry;
import org.apache.ignite.ml.dataset.feature.extractor.Vectorizer;
import org.apache.ignite.ml.dataset.primitive.context.EmptyContext;
import org.apache.ignite.ml.environment.LearningEnvironment;
import org.apache.ignite.ml.math.primitives.vector.Vector;
import org.apache.ignite.ml.math.stat.MultivariateGaussianDistribution;
import org.apache.ignite.ml.structures.LabeledVector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Partition data for GMM algorithm. Unlike partition data for other algorithms this class aggregate probabilities of
 * each cluster of gaussians mixture (see {@link #pcxi}) for each vector in dataset.
 */
class GmmPartitionData implements AutoCloseable {
    /** Dataset vectors. */
    private List<LabeledVector<Double>> xs;

    /** P(cluster|xi) where second idx is a cluster and first is a index of point. */
    private double[][] pcxi;

    /**
     * Creates an instance of GmmPartitionData.
     *
     * @param xs Dataset.
     * @param pcxi P(cluster|xi) per cluster.
     */
    GmmPartitionData(List<LabeledVector<Double>> xs, double[][] pcxi) {
        A.ensure(xs.size() == pcxi.length, "xs.size() == pcxi.length");

        this.xs = xs;
        this.pcxi = pcxi;
    }

    /**
     * @param i Index of vector in partition.
     * @return Vector.
     */
    public Vector getX(int i) {
        return xs.get(i).features();
    }

    /**
     * Updates P(c|xi) values in partitions and compute dataset likelihood.
     *
     * @param dataset Dataset.
     * @param clusterProbs Component probabilities.
     * @param components Components.
     * @return Dataset likelihood.
     */
    static double updatePcxiAndComputeLikelihood(Dataset<EmptyContext, GmmPartitionData> dataset, Vector clusterProbs,
        List<MultivariateGaussianDistribution> components) {

        return dataset.compute(
            data -> updatePcxi(data, clusterProbs, components),
            (left, right) -> asPrimitive(left) + asPrimitive(right)
        );
    }

    /**
     * @param cluster Cluster id.
     * @param i Vector id.
     * @return P(cluster | xi) value.
     */
    public double pcxi(int cluster, int i) {
        return pcxi[i][cluster];
    }

    /**
     * @param cluster Cluster id.
     * @param i Vector id.
     * @param value P(cluster|xi) value.
     */
    public void setPcxi(int cluster, int i, double value) {
        pcxi[i][cluster] = value;
    }

    /**
     * @return All vectors from partition.
     */
    public List<LabeledVector<Double>> getAllXs() {
        return Collections.unmodifiableList(xs);
    }

    /** {@inheritDoc} */
    @Override public void close() throws Exception {
        //NOP
    }

    /**
     * Builder for GMM partition data.
     */
    public static class Builder<K, V, C extends Serializable> implements PartitionDataBuilder<K, V, EmptyContext, GmmPartitionData> {
        /** Serial version uid. */
        private static final long serialVersionUID = 1847063348042022561L;

        /** Upsteam vectorizer. */
        private final Vectorizer<K, V, C, Double> extractor;

        /** Count of components of mixture. */
        private final int countOfComponents;

        /**
         * Creates an instance of Builder.
         *
         * @param extractor Extractor.
         * @param countOfComponents Count of components.
         */
        public Builder(Vectorizer<K, V, C, Double> extractor, int countOfComponents) {
            this.extractor = extractor;
            this.countOfComponents = countOfComponents;
        }

        /** {@inheritDoc} */
        @Override public GmmPartitionData build(LearningEnvironment env, Iterator<UpstreamEntry<K, V>> upstreamData,
            long upstreamDataSize, EmptyContext ctx) {

            int rowsCount = Math.toIntExact(upstreamDataSize);
            List<LabeledVector<Double>> xs = new ArrayList<>(rowsCount);
            double[][] pcxi = new double[rowsCount][countOfComponents];

            while (upstreamData.hasNext()) {
                UpstreamEntry<K, V> entry = upstreamData.next();
                LabeledVector<Double> x = extractor.extract(entry.getKey(), entry.getValue());
                xs.add(x);
            }

            return new GmmPartitionData(xs, pcxi);
        }
    }

    /**
     * Sets P(c|xi) = 1 for closest cluster "c" for each vector in partition data using initial means as cluster centers
     * (like in k-means).
     *
     * @param initMeans Initial means.
     */
    static void estimateLikelihoodClusters(GmmPartitionData data, Vector[] initMeans) {
        for (int i = 0; i < data.size(); i++) {
            int closestClusterId = -1;
            double minSquaredDist = Double.MAX_VALUE;

            Vector x = data.getX(i);
            for (int c = 0; c < initMeans.length; c++) {
                data.setPcxi(c, i, 0.0);
                double distance = initMeans[c].getDistanceSquared(x);
                if (distance < minSquaredDist) {
                    closestClusterId = c;
                    minSquaredDist = distance;
                }
            }

            data.setPcxi(closestClusterId, i, 1.);
        }
    }

    /**
     * @return Size of dataset partition.
     */
    public int size() {
        return pcxi.length;
    }

    /**
     * Updates P(c|xi) values in partitions given components probabilities and components of GMM.
     *
     * @param clusterProbs Component probabilities.
     * @param components Components.
     */
    static double updatePcxi(GmmPartitionData data, Vector clusterProbs,
        List<MultivariateGaussianDistribution> components) {

        GmmModel model = new GmmModel(clusterProbs, components);
        double maxProb = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < data.size(); i++) {
            Vector x = data.getX(i);
            double xProb = model.prob(x);
            if(xProb > maxProb)
                maxProb = xProb;

            double normalizer = 0.0;
            for (int c = 0; c < clusterProbs.size(); c++)
                normalizer += components.get(c).prob(x) * clusterProbs.get(c);

            for (int c = 0; c < clusterProbs.size(); c++)
                data.pcxi[i][c] = (components.get(c).prob(x) * clusterProbs.get(c)) / normalizer;
        }

        return maxProb;
    }

    /**
     * @param val Value.
     * @return 0 if Value == null and simplified value in terms of type otherwise.
     */
    private static double asPrimitive(Double val) {
        return val == null ? 0.0 : val;
    }
}
