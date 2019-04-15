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

package org.apache.ignite.ml.genetic;

import java.util.ArrayList;
import java.util.List;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.ml.genetic.parameter.GAConfiguration;
import org.apache.ignite.ml.genetic.parameter.GAGridConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Calculate Fitness Test
 */
public class GAGridCalculateFitnessTest {
    /** Ignite instance */
    private Ignite ignite = null;

    /** GAGrid **/
    private GAGrid gaGrid = null;

    /** GAConfiguraton */
    private GAConfiguration gaCfg = null;

    /**
     * Setup test
     */
    @Before
    public void initialize() {

        try {

            // Create an Ignite instance as you would in any other use case.
            ignite = Ignition.start();

            // Create GAConfiguration
            gaCfg = new GAConfiguration();

            // set Gene Pool
            List<Gene> genes = this.getGenePool();
            gaCfg.setGenePool(genes);

            // set the Chromosome Length to '8' since password contains 8 characters.
            gaCfg.setChromosomeLen(8);

            // create and set Fitness function
            PasswordFitnessFunction function = new PasswordFitnessFunction();
            gaCfg.setFitnessFunction(function);

            gaGrid = new GAGrid(gaCfg, ignite);
            gaGrid.initializeGenePopulation();
            gaGrid.initializePopulation();

        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Test Calculate Fitness
     */

    @Test
    public void testCalculateFitness() {
        try {

            List<Long> chromosomeKeys = gaGrid.getPopulationKeys();

            Boolean boolVal = this.ignite.compute().execute(new FitnessTask(this.gaCfg), chromosomeKeys);

            IgniteCache<Long, Chromosome> populationCache = ignite.cache(GAGridConstants.POPULATION_CACHE);

            String sql = "select count(*) from Chromosome where fitnessScore>0";

            // Execute query to keys for ALL Chromosomes by fitnessScore
            QueryCursor<List<?>> cursor = populationCache.query(new SqlFieldsQuery(sql));

            List<List<?>> res = cursor.getAll();

            Long cnt = 0L;

            for (List row : res)
                cnt = (Long)row.get(0);

            assertEquals(500, cnt.longValue());
        }

        catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Helper routine to initialize Gene pool
     *
     * @return List of Genes
     */
    private List<Gene> getGenePool() {
        List<Gene> list = new ArrayList();

        char[] chars = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '!', '"', '#', '$', '%', '&', '(', ')', '*', '+', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', ']', '^'};

        for (int i = 0; i < chars.length; i++) {
            Gene gene = new Gene(new Character(chars[i]));
            list.add(gene);
        }
        return list;
    }

    @After
    public void tearDown() {

        Ignition.stop(true);
        ignite = null;
    }
}
