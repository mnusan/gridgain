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

package org.apache.ignite.internal.processors.query.h2.twostep.messages;

import java.nio.ByteBuffer;
import org.apache.ignite.cache.query.QueryCancelledException;
import org.apache.ignite.cache.query.QueryRetryException;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.plugin.extensions.communication.Message;
import org.apache.ignite.plugin.extensions.communication.MessageReader;
import org.apache.ignite.plugin.extensions.communication.MessageWriter;

/**
 * Error message.
 */
public class GridQueryFailResponse implements Message {
    /** General error failure type. */
    public static final byte GENERAL_ERROR = 0;

    /** Cancelled by originator failure type. */
    public static final byte CANCELLED_BY_ORIGINATOR = 1;

    /** Execution error. Query should be retried. */
    public static final byte RETRY_QUERY = 2;

    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private long qryReqId;

    /** */
    private String errMsg;

    /** */
    private byte failCode;

    /**
     * Default constructor.
     */
    public GridQueryFailResponse() {
        // No-op.
    }

    /**
     * @param qryReqId Query request ID.
     * @param err Error.
     */
    public GridQueryFailResponse(long qryReqId, Throwable err) {
        this.qryReqId = qryReqId;
        this.errMsg = err.getMessage();

        if (err instanceof QueryCancelledException)
            this.failCode = CANCELLED_BY_ORIGINATOR;
        else if (err instanceof QueryRetryException)
            this.failCode = RETRY_QUERY;
        else
            this.failCode = GENERAL_ERROR;
    }

    /**
     * @return Query request ID.
     */
    public long queryRequestId() {
        return qryReqId;
    }

    /**
     * @return Error.
     */
    public String error() {
        return errMsg;
    }

    /**
     * @return Fail code.
     */
    public byte failCode() {
        return failCode;
    }

    /** {@inheritDoc} */
    @Override public void onAckReceived() {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridQueryFailResponse.class, this);
    }

    /** {@inheritDoc} */
    @Override public boolean writeTo(ByteBuffer buf, MessageWriter writer) {
        writer.setBuffer(buf);

        if (!writer.isHeaderWritten()) {
            if (!writer.writeHeader(directType(), fieldsCount()))
                return false;

            writer.onHeaderWritten();
        }

        switch (writer.state()) {
            case 0:
                if (!writer.writeString("errMsg", errMsg))
                    return false;

                writer.incrementState();

            case 1:
                if (!writer.writeByte("failCode", failCode))
                    return false;

                writer.incrementState();

            case 2:
                if (!writer.writeLong("qryReqId", qryReqId))
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

        switch (reader.state()) {
            case 0:
                errMsg = reader.readString("errMsg");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 1:
                failCode = reader.readByte("failCode");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 2:
                qryReqId = reader.readLong("qryReqId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

        }

        return reader.afterMessageRead(GridQueryFailResponse.class);
    }

    /** {@inheritDoc} */
    @Override public short directType() {
        return 107;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 3;
    }
}
