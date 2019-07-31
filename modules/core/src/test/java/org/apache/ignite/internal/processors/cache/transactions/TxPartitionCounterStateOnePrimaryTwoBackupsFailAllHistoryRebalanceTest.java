/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.cache.transactions;

import org.apache.ignite.internal.processors.cache.persistence.db.wal.IgniteWalRebalanceTest;

import static org.apache.ignite.IgniteSystemProperties.IGNITE_PDS_WAL_REBALANCE_THRESHOLD;

/**
 * Tests partition consistency recovery in case then all owners are lost in the middle of transaction.
 */
public class TxPartitionCounterStateOnePrimaryTwoBackupsFailAllHistoryRebalanceTest extends
    TxPartitionCounterStateOnePrimaryTwoBackupsFailAllTest {
    /** */
    private boolean skipCheck;

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        skipCheck = false;

        System.setProperty(IGNITE_PDS_WAL_REBALANCE_THRESHOLD, "0");
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        int histRebCnt = IgniteWalRebalanceTest.WalRebalanceCheckingCommunicationSpi.allRebalances().size();

        IgniteWalRebalanceTest.WalRebalanceCheckingCommunicationSpi.cleanup();

        super.afterTest();

        // Expecting only one historical rebalance for test scenario.
        if (!skipCheck)
            assertEquals("WAL rebalance must happen exactly 1 time", 1, histRebCnt);

        System.clearProperty(IGNITE_PDS_WAL_REBALANCE_THRESHOLD);

        skipCheck = true;
    }

    /** {@inheritDoc} */
    @Override public void testRestartAllOwnersAfterPartialCommit_2tx_1() throws Exception {
        // Rebalance will not be triggered because counters are same.
        skipCheck = true;
    }

    /** {@inheritDoc} */
    @Override public void testRestartAllOwnersAfterPartialCommit_2tx_2() throws Exception {
        // Rebalance will not be triggered because counters are same.
        skipCheck = true;
    }

    /** {@inheritDoc} */
    @Override public void testRestartAllOwnersAfterPartialCommit_2tx_3() throws Exception {
        // Rebalance will not be triggered because counters are same.
        skipCheck = true;
    }

    /** {@inheritDoc} */
    @Override public void testRestartAllOwnersAfterPartialCommit_2tx_4() throws Exception {
        // Rebalance will not be triggered because counters are same.
        skipCheck = true;
    }

    /** {@inheritDoc} */
    @Override public void testStopAllOwnersWithPartialCommit_3tx_1_1() throws Exception {
        // Rebalance will not be triggered because counters are same.
        skipCheck = true;
    }

    /** {@inheritDoc} */
    @Override public void testStopAllOwnersWithPartialCommit_3tx_1_2() throws Exception {
        // Rebalance will not be triggered because counters are same.
        skipCheck = true;
    }

    /** {@inheritDoc} */
    @Override public void testStopAllOwnersWithPartialCommit_3tx_2_1() throws Exception {
        // Rebalance will not be triggered because counters are same.
        skipCheck = true;
    }

    /** {@inheritDoc} */
    @Override public void testStopAllOwnersWithPartialCommit_3tx_2_2() throws Exception {
        // Rebalance will not be triggered because counters are same.
        skipCheck = true;
    }
}
