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

package org.apache.ignite.internal.util.lang.gridfunc;

import java.util.Collection;
import java.util.Iterator;
import org.apache.ignite.internal.util.GridSerializableCollection;
import org.apache.ignite.internal.util.lang.GridFunc;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.lang.IgnitePredicate;
import org.jetbrains.annotations.NotNull;

/**
 * Light-weight view on given col with provided predicate.
 *
 * @param <T> Type of the col.
 */
public class PredicateCollectionView<T> extends GridSerializableCollection<T> {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private final Collection<T> col;

    /** */
    private final IgnitePredicate<? super T>[] preds;

    /**
     * @param col Input col that serves as a base for the view.
     * @param preds Optional preds. If preds are not provided - all elements will be in the view.
     */
    @SafeVarargs
    public PredicateCollectionView(Collection<T> col, IgnitePredicate<? super T>... preds) {
        this.col = col;
        this.preds = preds;
    }

    /** {@inheritDoc} */
    @Override public boolean add(T e) {
        // Pass through (will fail for readonly).
        return GridFunc.isAll(e, preds) && col.add(e);
    }

    /** {@inheritDoc} */
    @NotNull @Override public Iterator<T> iterator() {
        return F.iterator0(col, false, preds);
    }

    /** {@inheritDoc} */
    @Override public int size() {
        return F.size(col, preds);
    }

    /** {@inheritDoc} */
    @Override public boolean isEmpty() {
        return F.isEmpty(preds) ? col.isEmpty() : !iterator().hasNext();
    }
}
