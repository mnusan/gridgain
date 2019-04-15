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

package org.apache.ignite.examples.ml.inference.spark.modelparser;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.examples.ml.tutorial.TitanicUtils;
import org.apache.ignite.ml.clustering.kmeans.KMeansModel;
import org.apache.ignite.ml.math.functions.IgniteBiFunction;
import org.apache.ignite.ml.math.primitives.vector.Vector;
import org.apache.ignite.ml.math.primitives.vector.VectorUtils;
import org.apache.ignite.ml.sparkmodelparser.SparkModelParser;
import org.apache.ignite.ml.sparkmodelparser.SupportedSparkModels;

import javax.cache.Cache;
import java.io.FileNotFoundException;

/**
 * Run KMeans model loaded from snappy.parquet file.
 * The snappy.parquet file was generated by Spark MLLib model.write.overwrite().save(..) operator.
 * <p>
 * You can change the test data used in this example and re-run it to explore this algorithm further.</p>
 */
public class KMeansFromSparkExample {
    /** Path to Spark KMeans model. */
    public static final String SPARK_MDL_PATH = "examples/src/main/resources/models/spark/serialized/kmeans";

    /** Run example. */
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println();
        System.out.println(">>> K-means model loaded from Spark through serialization over partitioned dataset usage example started.");
        // Start ignite grid.
        try (Ignite ignite = Ignition.start("examples/config/example-ignite.xml")) {
            System.out.println(">>> Ignite grid started.");

            IgniteCache<Integer, Object[]> dataCache = null;
            try {
                dataCache = TitanicUtils.readPassengers(ignite);

                IgniteBiFunction<Integer, Object[], Vector> featureExtractor = (k, v) -> {
                    double[] data = new double[] {(double)v[0], (double)v[5], (double)v[6], (double)v[4]};
                    data[0] = Double.isNaN(data[0]) ? 0 : data[0];
                    data[1] = Double.isNaN(data[1]) ? 0 : data[1];
                    data[2] = Double.isNaN(data[2]) ? 0 : data[2];
                    data[3] = Double.isNaN(data[3]) ? 0 : data[3];
                    return VectorUtils.of(data);
                };

                IgniteBiFunction<Integer, Object[], Double> lbExtractor = (k, v) -> (double)v[1];

                KMeansModel mdl = (KMeansModel)SparkModelParser.parse(
                    SPARK_MDL_PATH,
                    SupportedSparkModels.KMEANS
                );

                System.out.println(">>> K-Means model: " + mdl);
                System.out.println(">>> ------------------------------------");
                System.out.println(">>> | Predicted cluster\t| Is survived\t|");
                System.out.println(">>> ------------------------------------");

                try (QueryCursor<Cache.Entry<Integer, Object[]>> observations = dataCache.query(new ScanQuery<>())) {
                    for (Cache.Entry<Integer, Object[]> observation : observations) {
                        Vector inputs = featureExtractor.apply(observation.getKey(), observation.getValue());
                        double isSurvived = lbExtractor.apply(observation.getKey(), observation.getValue());
                        double clusterId = mdl.predict(inputs);

                        System.out.printf(">>> | %.4f\t\t| %.4f\t\t|\n", clusterId, isSurvived);
                    }
                }

                System.out.println(">>> ---------------------------------");
            } finally {
                dataCache.destroy();
            }
        }
    }
}
