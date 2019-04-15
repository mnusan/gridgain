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

#ifndef _IGNITE_IMPL_TRANSACTIONS_TRANSACTIONS_IMPL
#define _IGNITE_IMPL_TRANSACTIONS_TRANSACTIONS_IMPL

#include <ignite/common/concurrent.h>
#include <ignite/jni/java.h>
#include <ignite/transactions/transaction_consts.h>
#include <ignite/transactions/transaction_metrics.h>

#include <ignite/impl/interop/interop_target.h>

namespace ignite 
{
    namespace impl
    {
        namespace transactions
        {
            /**
             * Transactions implementation.
             */
            class IGNITE_FRIEND_EXPORT TransactionsImpl : private interop::InteropTarget
            {
                typedef ignite::common::concurrent::SharedPointer<ignite::impl::IgniteEnvironment> SP_IgniteEnvironment;
                typedef ignite::transactions::TransactionState TransactionState;
            public:
                /**
                 * Constructor used to create new instance.
                 *
                 * @param env Environment.
                 * @param javaRef Reference to java object.
                 */
                TransactionsImpl(SP_IgniteEnvironment env, jobject javaRef);

                /**
                 * Destructor.
                 */
                ~TransactionsImpl();

                /**
                 * Start new transaction.
                 *
                 * @param concurrency Concurrency.
                 * @param isolation Isolation.
                 * @param timeout Timeout in milliseconds. Zero if for infinite timeout.
                 * @param txSize Number of entries participating in transaction (may be approximate).
                 * @param err Error.
                 * @return Transaction ID on success.
                 */
                int64_t TxStart(int concurrency, int isolation, int64_t timeout,
                    int32_t txSize, IgniteError& err);

                /**
                 * Commit Transaction.
                 *
                 * @param id Transaction ID.
                 * @param err Error.
                 * @return Resulting state.
                 */
                TransactionState::Type TxCommit(int64_t id, IgniteError& err);

                /**
                 * Rollback Transaction.
                 *
                 * @param id Transaction ID.
                 * @param err Error.
                 * @return Resulting state.
                 */
                TransactionState::Type TxRollback(int64_t id, IgniteError& err);

                /**
                 * Close Transaction.
                 *
                 * @param id Transaction ID.
                 * @param err Error.
                 * @return Resulting state.
                 */
                TransactionState::Type TxClose(int64_t id, IgniteError& err);

                /**
                 * Make transaction into rollback-only.
                 *
                 * After transaction have been marked as rollback-only it may
                 * only be rolled back. Error occurs if such transaction is
                 * being commited.
                 *
                 * @param err Error.
                 * @return True if rollback-only has been set.
                 */
                bool TxSetRollbackOnly(int64_t id, IgniteError& err);

                /**
                 * Get Transaction state.
                 *
                 * @param id Transaction ID.
                 * @return Resulting state.
                 */
                TransactionState::Type TxState(int64_t id, IgniteError& err);

                /**
                 * Get metrics.
                 *
                 * @param err Error.
                 * @return Metrics instance.
                 */
                ignite::transactions::TransactionMetrics GetMetrics(IgniteError& err);

            private:
                /**
                 * Convert integer state constant to that of the TransactionState.
                 *
                 * @param state Integer constant state.
                 * @return TransactionState constant.
                 */
                TransactionState::Type ToTransactionState(int state);

                IGNITE_NO_COPY_ASSIGNMENT(TransactionsImpl)
            };
        }
    }
}

#endif //_IGNITE_IMPL_TRANSACTIONS_TRANSACTIONS_IMPL