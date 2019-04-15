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

namespace Apache.Ignite.Core.Impl.Binary.Structure
{
    using System;
    using System.Diagnostics;

    /// <summary>
    /// Jump table.
    /// </summary>
    internal class BinaryStructureJumpTable
    {
        /** Names. */
        private readonly string[] _names;

        /** Path indexes. */
        private readonly int[] _pathIdxs;

        /// <summary>
        /// Create minimal jump table with two entries.
        /// </summary>
        /// <param name="firstName">First name.</param>
        /// <param name="firstPathIdx">First path index.</param>
        /// <param name="secondName">Second name.</param>
        /// <param name="secondPathIdx">Second path index.</param>
        public BinaryStructureJumpTable(string firstName, int firstPathIdx, 
            string secondName, int secondPathIdx)
        {
            _names = new[] { firstName, secondName };
            _pathIdxs = new[] { firstPathIdx, secondPathIdx };
        }

        /// <summary>
        /// Constructor.
        /// </summary>
        /// <param name="names">Field names.</param>
        /// <param name="pathIdxs">Path indexes.</param>
        private BinaryStructureJumpTable(string[] names, int[] pathIdxs)
        {
            Debug.Assert(names.Length > 1);
            Debug.Assert(names.Length == pathIdxs.Length);
            
            _names = names;
            _pathIdxs = pathIdxs;
        }

        /// <summary>
        /// Get path index for the given field.
        /// </summary>
        /// <param name="fieldName">Field name.</param>
        /// <returns>Path index.</returns>
        public int GetPathIndex(string fieldName)
        {
            Debug.Assert(fieldName != null);
            
            // Optimistically assume that field name is a literal.
            for (var i = 0; i < _names.Length; i++)
            {
                if (ReferenceEquals(fieldName, _names[i]))
                    return _pathIdxs[i];
            }

            // Fallback to slow-path with normal string comparison.
            for (var i = 0; i < _names.Length; i++)
            {
                if (fieldName.Equals(_names[i]))
                    return _pathIdxs[i];
            }

            // No path found for the field.
            return -1;
        }

        /// <summary>
        /// Copy jump table.
        /// </summary>
        /// <returns>New jump table.</returns>
        public BinaryStructureJumpTable Copy()
        {
            return new BinaryStructureJumpTable(_names, _pathIdxs);
        }

        /// <summary>
        /// Copy jump table with additional jump.
        /// </summary>
        /// <param name="name">Field name.</param>
        /// <param name="pathIdx">Path index.</param>
        /// <returns>New jump table.</returns>
        public BinaryStructureJumpTable CopyAndAdd(string name, int pathIdx)
        {
            var newNames = new string[_names.Length + 1];
            var newPathIdxs = new int[_pathIdxs.Length + 1];

            Array.Copy(_names, newNames, _names.Length);
            Array.Copy(_pathIdxs, newPathIdxs, _pathIdxs.Length);

            newNames[newNames.Length - 1] = name;
            newPathIdxs[newPathIdxs.Length - 1] = pathIdx;

            return new BinaryStructureJumpTable(newNames, newPathIdxs);
        }
    }
}
