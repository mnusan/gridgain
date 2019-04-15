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

package org.apache.ignite.ml.math.primitives.vector.storage;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.ml.math.StorageConstants;
import org.apache.ignite.ml.math.primitives.vector.VectorStorage;

/**
 * Sparse, local, on-heap vector storage.
 */
public class SparseVectorStorage implements VectorStorage, StorageConstants {
    /** */
    private int size;

    /** Actual map storage. */
    private Map<Integer, Serializable> sto;

    /**
     *
     */
    public SparseVectorStorage() {
        // No-op.
    }

    /** */
    public SparseVectorStorage(Map<Integer, ? extends Serializable> map, boolean cp) {
        assert !map.isEmpty();

        this.size = map.size();
        sto = new HashMap<>(map);
    }

    /**
     * @param size Vector size.
     */
    public SparseVectorStorage(int size) {
        assert size > 0;

        this.size = size;
        this.sto = new HashMap<>();
    }

    /** {@inheritDoc} */
    @Override public int size() {
        return size;
    }

    /** {@inheritDoc} */
    @Override public double get(int i) {
        Serializable obj = sto.get(i);
        if (obj == null)
            return 0.0; //TODO: IGNITE-11664

        return ((Number)obj).doubleValue();
    }

    /** {@inheritDoc} */
    @Override public <T extends Serializable> T getRaw(int i) {
        return (T)sto.get(i);
    }

    /** {@inheritDoc} */
    @Override public void set(int i, double v) {
        if (v != 0.0)
            sto.put(i, v);
        else if (sto.containsKey(i)) //TODO: IGNITE-11664
            sto.remove(i);
    }

    /** {@inheritDoc} */
    @Override public void setRaw(int i, Serializable v) {
        if (v == null)
            sto.remove(i);
        else
            sto.put(i, v);
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size);
        out.writeObject(sto);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"unchecked"})
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        size = in.readInt();
        sto = (Map<Integer, Serializable>)in.readObject();
    }

    /** {@inheritDoc} */
    @Override public boolean isDense() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean isDistributed() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean isArrayBased() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean isNumeric() {
        return sto.values().stream().allMatch(v -> v instanceof Number) || sto.isEmpty();
    }

    /** {@inheritDoc} */
    @Override public double[] data() {
        if (!isNumeric())
            throw new ClassCastException("Vector has not only numeric values.");

        double[] data = new double[size];

        sto.forEach((idx, val) -> data[idx] = ((Number)val).doubleValue());

        return data;
    }

    /** {@inheritDoc} */
    @Override public Serializable[] rawData() {
        Serializable[] res = new Serializable[size];
        sto.forEach((i, v) -> res[i] = v);
        return res;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        SparseVectorStorage that = (SparseVectorStorage)o;

        return size == that.size && (sto != null ? sto.equals(that.sto) : that.sto == null);
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        int res = size;

        res = 31 * res + (sto != null ? sto.hashCode() : 0);

        return res;
    }

    /** */
    public IntSet indexes() {
        return new IntArraySet(sto.keySet());
    }
}
