/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 * 
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.cache.distributed.dht.atomic;

import java.io.Externalizable;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.internal.GridDirectCollection;
import org.apache.ignite.internal.GridDirectTransient;
import org.apache.ignite.internal.processors.cache.GridCacheContext;
import org.apache.ignite.internal.processors.cache.GridCacheDeployable;
import org.apache.ignite.internal.processors.cache.GridCacheIdMessage;
import org.apache.ignite.internal.processors.cache.GridCacheSharedContext;
import org.apache.ignite.internal.processors.cache.KeyCacheObject;
import org.apache.ignite.internal.util.tostring.GridToStringInclude;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.plugin.extensions.communication.MessageCollectionItemType;
import org.apache.ignite.plugin.extensions.communication.MessageReader;
import org.apache.ignite.plugin.extensions.communication.MessageWriter;
import org.apache.ignite.plugin.extensions.communication.TimeLoggableResponse;

/**
 * DHT atomic cache backup update response.
 */
public class GridDhtAtomicUpdateResponse extends GridCacheIdMessage implements GridCacheDeployable, TimeLoggableResponse {
    /** */
    private static final long serialVersionUID = 0L;

    /** Message index. */
    public static final int CACHE_MSG_IDX = nextIndexId();

    /** Future version. */
    private long futId;

    /** */
    private UpdateErrors errs;

    /** Evicted readers. */
    @GridToStringInclude
    @GridDirectCollection(KeyCacheObject.class)
    private List<KeyCacheObject> nearEvicted;

    /** */
    private int partId;

    /** @see TimeLoggableResponse#reqSentTimestamp(). */
    @GridDirectTransient
    private long reqSendTimestamp = INVALID_TIMESTAMP;

    /** @see TimeLoggableResponse#reqReceivedTimestamp(). */
    @GridDirectTransient
    private long reqReceivedTimestamp = INVALID_TIMESTAMP;

    /** @see TimeLoggableResponse#respSendTimestamp(). */
    private long responseSendTimestamp = INVALID_TIMESTAMP;

    /**
     * Empty constructor required by {@link Externalizable}.
     */
    public GridDhtAtomicUpdateResponse() {
        // No-op.
    }

    /**
     * @param cacheId Cache ID.
     * @param partId Partition.
     * @param futId Future ID.
     * @param addDepInfo Deployment info.
     */
    public GridDhtAtomicUpdateResponse(int cacheId, int partId, long futId, boolean addDepInfo) {
        this.cacheId = cacheId;
        this.partId = partId;
        this.futId = futId;
        this.addDepInfo = addDepInfo;
    }

    /** {@inheritDoc} */
    @Override public int lookupIndex() {
        return CACHE_MSG_IDX;
    }

    /**
     * @return Future version.
     */
    public long futureId() {
        return futId;
    }

    /**
     * Sets update error.
     *
     * @param err Error.
     */
    public void onError(IgniteCheckedException err){
        if (errs == null)
            errs = new UpdateErrors();

        errs.onError(err);
    }

    /** {@inheritDoc} */
    @Override public IgniteCheckedException error() {
        return errs != null ? errs.error() : null;
    }

    /**
     * @return Evicted readers.
     */
    Collection<KeyCacheObject> nearEvicted() {
        return nearEvicted;
    }

    /**
     * @param nearEvicted Evicted near cache keys.
     */
    public void nearEvicted(List<KeyCacheObject> nearEvicted) {
        this.nearEvicted = nearEvicted;
    }

    /** {@inheritDoc} */
    @Override public int partition() {
        return partId;
    }

    /** {@inheritDoc} */
    @Override public void prepareMarshal(GridCacheSharedContext ctx) throws IgniteCheckedException {
        super.prepareMarshal(ctx);

        GridCacheContext cctx = ctx.cacheContext(cacheId);

        // Can be null if client near cache was removed, in this case assume do not need prepareMarshal.
        if (cctx != null) {
            prepareMarshalCacheObjects(nearEvicted, cctx);

            if (errs != null)
                errs.prepareMarshal(this, cctx);
        }
    }

    /** {@inheritDoc} */
    @Override public void finishUnmarshal(GridCacheSharedContext ctx, ClassLoader ldr) throws IgniteCheckedException {
        super.finishUnmarshal(ctx, ldr);

        GridCacheContext cctx = ctx.cacheContext(cacheId);

        finishUnmarshalCacheObjects(nearEvicted, cctx, ldr);

        if (errs != null)
            errs.finishUnmarshal(this, cctx, ldr);
    }

    /** {@inheritDoc} */
    @Override public boolean addDeploymentInfo() {
        return addDepInfo;
    }

    /** {@inheritDoc} */
    @Override public IgniteLogger messageLogger(GridCacheSharedContext ctx) {
        return ctx.atomicMessageLogger();
    }

    /** {@inheritDoc} */
    @Override public void reqSendTimestamp(long reqSendTimestamp) {
        this.reqSendTimestamp = reqSendTimestamp;
    }

    /** {@inheritDoc} */
    @Override public long reqSentTimestamp() {
        return reqSendTimestamp;
    }

    /** {@inheritDoc} */
    @Override public void reqReceivedTimestamp(long reqReceivedTimestamp) {
        this.reqReceivedTimestamp = reqReceivedTimestamp;
    }

    /** {@inheritDoc} */
    @Override public long reqReceivedTimestamp() {
        return reqReceivedTimestamp;
    }

    /** {@inheritDoc} */
    @Override public void respSendTimestamp(long responseSendTimestamp) {
        this.responseSendTimestamp = responseSendTimestamp;
    }

    /** {@inheritDoc} */
    @Override public long respSendTimestamp() {
        return responseSendTimestamp;
    }

    /** {@inheritDoc} */
    @Override public boolean writeTo(ByteBuffer buf, MessageWriter writer) {
        writer.setBuffer(buf);

        if (!super.writeTo(buf, writer))
            return false;

        if (!writer.isHeaderWritten()) {
            if (!writer.writeHeader(directType(), fieldsCount()))
                return false;

            writer.onHeaderWritten();
        }

        switch (writer.state()) {
            case 4:
                if (!writer.writeMessage("errs", errs))
                    return false;

                writer.incrementState();

            case 5:
                if (!writer.writeLong("futId", futId))
                    return false;

                writer.incrementState();

            case 6:
                if (!writer.writeCollection("nearEvicted", nearEvicted, MessageCollectionItemType.MSG))
                    return false;

                writer.incrementState();

            case 7:
                if (!writer.writeInt("partId", partId))
                    return false;

                writer.incrementState();

            case 8:
                if (!writer.writeLong("responseSendTimestamp", responseSendTimestamp))
                    return false;

                writer.incrementState();

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean readFrom(ByteBuffer buf, MessageReader reader) {
        reader.setBuffer(buf);

        if (!reader.beforeMessageRead())
            return false;

        if (!super.readFrom(buf, reader))
            return false;

        switch (reader.state()) {
            case 4:
                errs = reader.readMessage("errs");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 5:
                futId = reader.readLong("futId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 6:
                nearEvicted = reader.readCollection("nearEvicted", MessageCollectionItemType.MSG);

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 7:
                partId = reader.readInt("partId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 8:
                responseSendTimestamp = reader.readLong("responseSendTimestamp");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

        }

        return reader.afterMessageRead(GridDhtAtomicUpdateResponse.class);
    }

    /** {@inheritDoc} */
    @Override public short directType() {
        return 39;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 9;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridDhtAtomicUpdateResponse.class, this);
    }
}
