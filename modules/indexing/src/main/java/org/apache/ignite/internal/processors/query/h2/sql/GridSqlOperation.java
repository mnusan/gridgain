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

package org.apache.ignite.internal.processors.query.h2.sql;

import java.util.ArrayList;

import static org.apache.ignite.internal.processors.query.h2.sql.GridSqlOperationType.IN;

/**
 * Unary or binary operation.
 */
public class GridSqlOperation extends GridSqlElement {
    /** */
    private final GridSqlOperationType opType;

    /**
     * @param opType Operation type.
     */
    public GridSqlOperation(GridSqlOperationType opType) {
        super(opType == IN ? new ArrayList<GridSqlAst>() :
            new ArrayList<GridSqlAst>(opType.childrenCount()));

        this.opType = opType;
    }

    /**
     * @param opType Op type.
     * @param arg argument.
     */
    public GridSqlOperation(GridSqlOperationType opType, GridSqlElement arg) {
        this(opType);

        addChild(arg);
    }

    /**
     * @param opType Op type.
     * @param left Left.
     * @param right Right.
     */
    public GridSqlOperation(GridSqlOperationType opType, GridSqlAst left, GridSqlAst right) {
        this(opType);

        addChild(left);
        addChild(right);
    }

    /**
     * @return Operation type.
     */
    public GridSqlOperationType operationType() {
        return opType;
    }

    /** {@inheritDoc}  */
    @Override public String getSQL() {
        return opType.toSql(this);
    }
}