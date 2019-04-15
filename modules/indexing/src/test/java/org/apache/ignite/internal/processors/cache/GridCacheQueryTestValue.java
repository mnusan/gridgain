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

package org.apache.ignite.internal.processors.cache;

import java.io.Serializable;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.cache.query.annotations.QueryTextField;

/**
 * Query test value.
 */
@SuppressWarnings("unused")
public class GridCacheQueryTestValue implements Serializable {
    /** */
    @QueryTextField
    @QuerySqlField(name = "fieldname")
    private String field1;

    /** */
    @QuerySqlField
    private int field2;

    /** */
    @QuerySqlField(index = true)
    private long field3;

    /** */
    @QuerySqlField(orderedGroups = {
        @QuerySqlField.Group(name = "grp1", order = 1),
        @QuerySqlField.Group(name = "grp2", order = 2)})
    private long field4;

    /** */
    @QuerySqlField(orderedGroups = {@QuerySqlField.Group(name = "grp1", order = 2)})
    private long field5;

    /** */
    @QuerySqlField(orderedGroups = {@QuerySqlField.Group(name = "grp1", order = 3)})
    private GridCacheQueryEmbeddedValue field6 = new GridCacheQueryEmbeddedValue();

    /**
     *
     * @return Field.
     */
    public String getField1() {
        return field1;
    }

    /**
     *
     * @param field1 Field.
     */
    public void setField1(String field1) {
        this.field1 = field1;
    }

    /**
     *
     * @return Field.
     */
    public int getField2() {
        return field2;
    }

    /**
     *
     * @param field2 Field.
     */
    public void setField2(int field2) {
        this.field2 = field2;
    }

    /**
     *
     * @return Field.
     */
    public long getField3() {
        return field3;
    }

    /**
     *
     * @param field3 Field.
     */
    public void setField3(long field3) {
        this.field3 = field3;
    }

    /**
     *
     * @return Field.
     */
    public long getField4() {
        return field4;
    }

    /**
     *
     * @param field4 Field.
     */
    public void setField4(long field4) {
        this.field4 = field4;
    }

    /**
     * @return Field.
     */
    public long getField5() {
        return field5;
    }

    /**
     * @param field5 Field.
     */
    public void setField5(long field5) {
        this.field5 = field5;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"RedundantIfStatement"})
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GridCacheQueryTestValue that = (GridCacheQueryTestValue)o;

        if (field2 != that.field2)
            return false;

        if (field3 != that.field3)
            return false;

        if (field1 != null ? !field1.equals(that.field1) : that.field1 != null)
            return false;

        return true;
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        int res = (field1 != null ? field1.hashCode() : 0);

        res = 31 * res + field2;
        res = 31 * res + (int)(field3 ^ (field3 >>> 32));

        return res;
    }

    /**
     * @param field6 Embedded value.
     */
    public void setField6(GridCacheQueryEmbeddedValue field6) {
        this.field6 = field6;
    }
}