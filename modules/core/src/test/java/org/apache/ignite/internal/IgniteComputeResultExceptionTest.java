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

package org.apache.ignite.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.IgniteException;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeJobResultPolicy;
import org.apache.ignite.compute.ComputeTask;
import org.apache.ignite.compute.ComputeTaskFuture;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

/**
 * Testing that if {@link ComputeTask#result(ComputeJobResult, List)} throws an {@link IgniteException}
 * then that exception is thrown as the execution result.
 */
public class IgniteComputeResultExceptionTest extends GridCommonAbstractTest {
    /** */
    @Test
    public void testIgniteExceptionExecute() throws Exception {
        checkExecuteException(new IgniteException());
    }

    /** */
    @Test
    public void testIgniteExceptionWithCauseExecute() throws Exception {
        checkExecuteException(new IgniteException(new Exception()));
    }

    /** */
    @Test
    public void testIgniteExceptionWithCauseChainExecute() throws Exception {
        checkExecuteException(new IgniteException(new Exception(new Throwable())));
    }

    /** */
    @Test
    public void testCustomExceptionExecute() throws Exception {
        checkExecuteException(new TaskException());
    }

    /** */
    @Test
    public void testCustomExceptionWithCauseExecute() throws Exception {
        checkExecuteException(new TaskException(new Exception()));
    }

    /** */
    @Test
    public void testCustomExceptionWithCauseChainExecute() throws Exception {
        checkExecuteException(new TaskException(new Exception(new Throwable())));
    }

    /** */
    private void checkExecuteException(IgniteException resE) throws Exception {
        try (Ignite ignite = startGrid()) {
            IgniteCompute compute = ignite.compute();
            try {
                compute.execute(new ResultExceptionTask(resE), null);
            } catch (IgniteException e) {
                assertSame(resE, e);
            }
        }
    }

    /** */
    @Test
    public void testIgniteExceptionExecuteAsync() throws Exception {
        checkExecuteAsyncException(new IgniteException());
    }

    /** */
    @Test
    public void testIgniteExceptionWithCauseExecuteAsync() throws Exception {
        checkExecuteAsyncException(new IgniteException(new Exception()));
    }

    /** */
    @Test
    public void testIgniteExceptionWithCauseChainExecuteAsync() throws Exception {
        checkExecuteAsyncException(new IgniteException(new Exception(new Throwable())));
    }


    /** */
    @Test
    public void testCustomExceptionExecuteAsync() throws Exception {
        checkExecuteAsyncException(new TaskException());
    }

    /** */
    @Test
    public void testCustomExceptionWithCauseExecuteAsync() throws Exception {
        checkExecuteAsyncException(new TaskException(new Exception()));
    }

    /** */
    @Test
    public void testCustomExceptionWithCauseChainExecuteAsync() throws Exception {
        checkExecuteAsyncException(new TaskException(new Exception(new Throwable())));
    }

    /** */
    private void checkExecuteAsyncException(IgniteException resE) throws Exception {
        try (Ignite ignite = startGrid()) {
            IgniteCompute compute = ignite.compute();
            ComputeTaskFuture<Object> fut = compute.executeAsync(new ResultExceptionTask(resE), null);
            try {
                fut.get();
            } catch (IgniteException e) {
                assertSame(resE, e);
            }
        }
    }

    /** */
    private static class TaskException extends IgniteException {
        /** */
        public TaskException() {
            // No-op.
        }

        /** */
        public TaskException(Throwable cause) {
            super(cause);
        }
    }

    /** */
    private static class NoopJob implements ComputeJob {
        /** */
        @Override public void cancel() {
            // No-op.
        }

        /** */
        @Override public Object execute() throws IgniteException {
            return null;
        }
    }

    /** */
    private static class ResultExceptionTask implements ComputeTask<Object, Object> {
        /** */
        private final IgniteException resE;

        /**
         * @param resE Exception to be rethrown by the
         */
        ResultExceptionTask(IgniteException resE) {
            this.resE = resE;
        }

        /** */
        @Override public Map<? extends ComputeJob, ClusterNode> map(List<ClusterNode> subgrid,
            @Nullable Object arg) throws IgniteException {
            Map<ComputeJob, ClusterNode> jobs = new HashMap<>();

            for (ClusterNode node : subgrid)
                jobs.put(new NoopJob(), node);

            return jobs;
        }

        /** */
        @Override
        public ComputeJobResultPolicy result(ComputeJobResult res, List<ComputeJobResult> rcvd) throws IgniteException {
            throw resE;
        }

        /** */
        @Nullable @Override public Object reduce(List<ComputeJobResult> results) throws IgniteException {
            return null;
        }
    }
}
