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

package org.apache.ignite.ml.math.primitives.matrix.storage;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.apache.ignite.ml.math.primitives.matrix.MatrixStorage;

/**
 * {@link MatrixStorage} implementation that delegates to parent matrix.
 */
public class ViewMatrixStorage implements MatrixStorage {
    /** Parent matrix storage. */
    private MatrixStorage dlg;

    /** Row offset in the parent matrix. */
    private int rowOff;
    /** Column offset in the parent matrix. */
    private int colOff;

    /** Amount of rows in the matrix. */
    private int rows;
    /** Amount of columns in the matrix. */
    private int cols;

    /**
     *
     */
    public ViewMatrixStorage() {
        // No-op.
    }

    /**
     * @param dlg Backing parent storage.
     * @param rowOff Row offset to parent matrix.
     * @param colOff Column offset to parent matrix.
     * @param rows Amount of rows in the view.
     * @param cols Amount of columns in the view.
     */
    public ViewMatrixStorage(MatrixStorage dlg, int rowOff, int colOff, int rows, int cols) {
        assert dlg != null;
        assert rowOff >= 0;
        assert colOff >= 0;
        assert rows > 0;
        assert cols > 0;

        this.dlg = dlg;

        this.rowOff = rowOff;
        this.colOff = colOff;

        this.rows = rows;
        this.cols = cols;
    }

    /**
     *
     */
    public MatrixStorage delegate() {
        return dlg;
    }

    /**
     *
     */
    public int rowOffset() {
        return rowOff;
    }

    /**
     *
     */
    public int columnOffset() {
        return colOff;
    }

    /**
     *
     */
    public int rowsLength() {
        return rows;
    }

    /**
     *
     */
    public int columnsLength() {
        return cols;
    }

    /** {@inheritDoc} */
    @Override public double get(int x, int y) {
        return dlg.get(rowOff + x, colOff + y);
    }

    /** {@inheritDoc} */
    @Override public void set(int x, int y, double v) {
        dlg.set(rowOff + x, colOff + y, v);
    }

    /** {@inheritDoc} */
    @Override public int columnSize() {
        return cols;
    }

    /** {@inheritDoc} */
    @Override public int rowSize() {
        return rows;
    }

    /** {@inheritDoc} */
    @Override public int storageMode() {
        return dlg.storageMode();
    }

    /** {@inheritDoc} */
    @Override public int accessMode() {
        return dlg.accessMode();
    }

    /** {@inheritDoc} */
    @Override public boolean isArrayBased() {
        return dlg.isArrayBased() && rowOff == 0 && colOff == 0;
    }

    /** {@inheritDoc} */
    @Override public boolean isDense() {
        return dlg.isDense();
    }

    /** {@inheritDoc} */
    @Override public boolean isDistributed() {
        return dlg.isDistributed();
    }

    /** {@inheritDoc} */
    @Override public double[] data() {
        return dlg.data();
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(dlg);

        out.writeInt(rowOff);
        out.writeInt(colOff);

        out.writeInt(rows);
        out.writeInt(cols);
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        dlg = (MatrixStorage)in.readObject();

        rowOff = in.readInt();
        colOff = in.readInt();

        rows = in.readInt();
        cols = in.readInt();
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        int res = 1;

        res = res * 37 + rows;
        res = res * 37 + cols;
        res = res * 37 + rowOff;
        res = res * 37 + colOff;
        res = res * 37 + dlg.hashCode();

        return res;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        ViewMatrixStorage that = (ViewMatrixStorage)o;

        return rows == that.rows && cols == that.cols && rowOff == that.rowOff && colOff == that.colOff &&
            (dlg != null ? dlg.equals(that.dlg) : that.dlg == null);
    }
}
