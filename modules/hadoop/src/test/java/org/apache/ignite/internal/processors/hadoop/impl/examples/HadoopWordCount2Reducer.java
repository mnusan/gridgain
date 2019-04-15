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

package org.apache.ignite.internal.processors.hadoop.impl.examples;

import java.io.IOException;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.ignite.internal.processors.hadoop.impl.HadoopErrorSimulator;

/**
 * Combiner and Reducer phase of WordCount job.
 */
public class HadoopWordCount2Reducer extends Reducer<Text, IntWritable, Text, IntWritable> implements Configurable {
    /** Writable container for writing sum of word counts. */
    private IntWritable totalWordCnt = new IntWritable();

    /** Flag is to check that mapper was configured before run. */
    private boolean wasConfigured;

    /** Flag is to check that mapper was set up before run. */
    private boolean wasSetUp;

    /** {@inheritDoc} */
    @Override public void reduce(Text key, Iterable<IntWritable> values, Context ctx) throws IOException, InterruptedException {
        assert wasConfigured : "Reducer should be configured";
        assert wasSetUp : "Reducer should be set up";

        int wordCnt = 0;

        for (IntWritable value : values)
            wordCnt += value.get();

        totalWordCnt.set(wordCnt);

        ctx.write(key, totalWordCnt);

        reduceError();
    }

    /**
     * Simulates reduce error if needed.
     */
    protected void reduceError() throws IOException, InterruptedException {
        HadoopErrorSimulator.instance().onReduce();
    }

    /** {@inheritDoc} */
    @Override protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);

        wasSetUp = true;

        setupError();
    }

    /**
     * Simulates setup error if needed.
     */
    protected void setupError() throws IOException, InterruptedException {
        HadoopErrorSimulator.instance().onReduceSetup();
    }

    /** {@inheritDoc} */
    @Override protected void cleanup(Context context) throws IOException, InterruptedException {
        super.cleanup(context);

        cleanupError();
    }

    /**
     * Simulates cleanup error if needed.
     */
    protected void cleanupError() throws IOException, InterruptedException {
        HadoopErrorSimulator.instance().onReduceCleanup();
    }

    /** {@inheritDoc} */
    @Override public void setConf(Configuration conf) {
        wasConfigured = true;

        configError();
    }

    /**
     * Simulates configuration error if needed.
     */
    protected void configError() {
        HadoopErrorSimulator.instance().onReduceConfigure();
    }

    /** {@inheritDoc} */
    @Override public Configuration getConf() {
        return null;
    }
}