﻿/*
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

namespace Apache.Ignite.Core.Impl.Client.Cache.Query
{
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Diagnostics;
    using Apache.Ignite.Core.Binary;
    using Apache.Ignite.Core.Cache.Query;
    using Apache.Ignite.Core.Impl.Binary;
    using Apache.Ignite.Core.Impl.Binary.IO;

    /// <summary>
    /// Client fields cursor.
    /// </summary>
    internal class ClientFieldsQueryCursor : ClientQueryCursorBase<IList<object>>, IFieldsQueryCursor
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="ClientQueryCursor{TK, TV}" /> class.
        /// </summary>
        /// <param name="ignite">The ignite.</param>
        /// <param name="cursorId">The cursor identifier.</param>
        /// <param name="keepBinary">Keep binary flag.</param>
        /// <param name="initialBatchStream">Optional stream with initial batch.</param>
        /// <param name="getPageOp">The get page op.</param>
        /// <param name="columns">The columns.</param>
        public ClientFieldsQueryCursor(IgniteClient ignite, long cursorId, bool keepBinary,
            IBinaryStream initialBatchStream, ClientOp getPageOp, IList<string> columns)
            : base(ignite, cursorId, keepBinary, initialBatchStream, getPageOp,
                r =>
                {
                    var res = new List<object>(columns.Count);

                    for (var i = 0; i < columns.Count; i++)
                    {
                        res.Add(r.ReadObject<object>());
                    }

                    return res;
                })
        {
            Debug.Assert(columns != null);

            FieldNames = new ReadOnlyCollection<string>(columns);
        }

        /** <inheritdoc /> */
        public IList<string> FieldNames { get; private set; }

        /// <summary>
        /// Reads the columns.
        /// </summary>
        internal static List<string> ReadColumns(IBinaryRawReader reader)
        {
            return reader.ReadStringCollection();
        }
    }
}
