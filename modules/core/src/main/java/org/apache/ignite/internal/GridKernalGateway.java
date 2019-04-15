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

import org.apache.ignite.internal.util.future.GridFutureAdapter;
import org.apache.ignite.internal.util.tostring.GridToStringExclude;
import org.jetbrains.annotations.Nullable;

/**
 * This interface guards access to implementations of public methods that access kernal
 * functionality from the following main API interfaces:
 * <ul>
 * <li>{@link org.apache.ignite.cluster.ClusterGroup}</li>
 * </ul>
 * Note that this kernal gateway <b>should not</b> be used to guard against method from
 * the following non-rich interfaces since their implementations are already managed
 * by their respective implementing classes:
 * <ul>
 * <li>{@link org.apache.ignite.Ignite}</li>
 * <li>{@link org.apache.ignite.cluster.ClusterNode}</li>
 * </ul>
 * Kernal gateway is also responsible for notifying various futures about the change in
 * kernal state so that issued futures could properly interrupt themselves when kernal
 * becomes unavailable while future is held externally by the user.
 */
@GridToStringExclude
public interface GridKernalGateway {
    /**
     * Should be called on entering every kernal related call
     * <b>originated directly or indirectly via public API</b>.
     * <p>
     * This method essentially acquires a read lock and multiple threads
     * can enter the call without blocking.
     *
     * @throws IllegalStateException Thrown in case when no kernal calls are allowed.
     * @see #readUnlock()
     */
    public void readLock() throws IllegalStateException;

    /**
     * Same as {@link #readLock()} but doesn't throw IllegalStateException if grid stop.
     */
    public void readLockAnyway();

    /**
     * Sets kernal state. Various kernal states drive the logic inside of the gateway.
     *
     * @param state Kernal state to set.
     */
    public void setState(GridKernalState state);

    /**
     * Gets current kernal state.
     *
     * @return Kernal state.
     */
    public GridKernalState getState();

    /**
     * Should be called on leaving every kernal related call
     * <b>originated directly or indirectly via public API</b>.
     * <p>
     * This method essentially releases the internal read-lock acquired previously
     * by {@link #readLock()} method.
     *
     * @see #readLock()
     */
    public void readUnlock();

    /**
     * This method waits for all current calls to exit and blocks any further
     * {@link #readLock()} calls until {@link #writeUnlock()} method is called.
     * <p>
     * This method essentially acquires the internal write lock.
     */
    public void writeLock();

    /**
     * This method unblocks {@link #writeLock()}.
     * <p>
     * This method essentially releases internal write lock previously acquired
     * by {@link #writeLock()} method.
     */
    public void writeUnlock();

    /**
     * Gets user stack trace through the first call of grid public API.
     *
     * @return User stack trace.
     */
    public String userStackTrace();

    /**
     * @param timeout Timeout.
     * @return {@code True} if write lock has been acquired.
     * @throws InterruptedException If interrupted.
     */
    public boolean tryWriteLock(long timeout) throws InterruptedException;

    /**
     * Disconnected callback.
     *
     * @return Reconnect future.
     */
    @Nullable public GridFutureAdapter<?> onDisconnected();

    /**
     * Reconnected callback.
     */
    public void onReconnected();
}
