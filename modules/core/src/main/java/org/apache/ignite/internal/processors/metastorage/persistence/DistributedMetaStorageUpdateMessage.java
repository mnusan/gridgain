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

package org.apache.ignite.internal.processors.metastorage.persistence;

import java.util.UUID;
import org.apache.ignite.internal.managers.discovery.DiscoCache;
import org.apache.ignite.internal.managers.discovery.DiscoveryCustomMessage;
import org.apache.ignite.internal.managers.discovery.GridDiscoveryManager;
import org.apache.ignite.internal.processors.affinity.AffinityTopologyVersion;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.lang.IgniteUuid;
import org.jetbrains.annotations.Nullable;

/** */
class DistributedMetaStorageUpdateMessage implements DiscoveryCustomMessage {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private final IgniteUuid id = IgniteUuid.randomUuid();

    /** Request ID. */
    private final UUID reqId;

    /** */
    private final String key;

    /** */
    private final byte[] valBytes;

    /** */
    private String errorMsg;

    /** */
    public DistributedMetaStorageUpdateMessage(UUID reqId, String key, byte[] valBytes) {
        this.reqId = reqId;
        this.key = key;
        this.valBytes = valBytes;
    }

    /** {@inheritDoc} */
    @Override public IgniteUuid id() {
        return id;
    }

    /** */
    public UUID requestId() {
        return reqId;
    }

    /** */
    public String key() {
        return key;
    }

    /** */
    public byte[] value() {
        return valBytes;
    }

    /** */
    public boolean isAckMessage() {
        return false;
    }

    /** */
    public void errorMessage(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /** */
    protected String errorMessage() {
        return errorMsg;
    }

    /** {@inheritDoc} */
    @Override @Nullable public DiscoveryCustomMessage ackMessage() {
        return new DistributedMetaStorageUpdateAckMessage(reqId, errorMsg);
    }

    /** {@inheritDoc} */
    @Override public boolean isMutable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean stopProcess() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public DiscoCache createDiscoCache(
        GridDiscoveryManager mgr,
        AffinityTopologyVersion topVer,
        DiscoCache discoCache
    ) {
        throw new UnsupportedOperationException("createDiscoCache");
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(DistributedMetaStorageUpdateMessage.class, this);
    }
}
