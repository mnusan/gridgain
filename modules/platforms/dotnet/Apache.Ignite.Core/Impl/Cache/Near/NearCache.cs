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

namespace Apache.Ignite.Core.Impl.Cache.Near
{
    using System;
    using System.Collections.Concurrent;
    using System.Diagnostics;
    using Apache.Ignite.Core.Cache.Affinity;
    using Apache.Ignite.Core.Impl.Binary;
    using Apache.Ignite.Core.Impl.Binary.IO;

    /// <summary>
    /// Holds near cache data for a given cache, serves one or more <see cref="CacheImpl{TK,TV}"/> instances.
    /// </summary>
    internal sealed class NearCache<TK, TV> : INearCache
    {
        /** Affinity. */
        private readonly CacheAffinityImpl _affinity;
        
        /** Keep binary flag. */
        private readonly bool _keepBinary;

        /** Topology version func. Returns boxed <see cref="AffinityTopologyVersion"/>.
         * Boxed copy is passed directly to <see cref="NearCacheEntry{T}"/>, avoiding extra allocations.
         * This way for every unique <see cref="AffinityTopologyVersion"/> we only have one boxed copy,
         * and we can update <see cref="NearCacheEntry{T}.Version"/> atomically without locks. */
        private readonly Func<object> _affinityTopologyVersionFunc;

        /** Underlying map. */
        private readonly ConcurrentDictionary<TK, NearCacheEntry<TV>> _map = 
            new ConcurrentDictionary<TK, NearCacheEntry<TV>>();

        /** Stopped flag. */
        private volatile bool _stopped;

        /// <summary>
        /// Initializes a new instance of the <see cref="NearCache{TK, TV}"/> class.
        /// Called via reflection from <see cref="NearCacheManager.CreateNearCache"/>. 
        /// </summary>
        public NearCache(Func<object> affinityTopologyVersionFunc, CacheAffinityImpl affinity, bool keepBinary)
        {
            _affinityTopologyVersionFunc = affinityTopologyVersionFunc;
            _affinity = affinity;
            _keepBinary = keepBinary;
        }

        /** <inheritdoc /> */
        public bool IsStopped
        {
            get { return _stopped; }
        }

        public bool TryGetValue<TKey, TVal>(TKey key, out TVal val)
        {
            if (_stopped)
            {
                val = default(TVal);
                return false;
            }

            NearCacheEntry<TV> entry;
            var key0 = (TK) (object) key;
            
            if (_map.TryGetValue(key0, out entry))
            {
                if (IsValid(entry))
                {
                    val = (TVal) (object) entry.Value;
                    return true;
                }

                // Remove invalid entry to free up memory.
                // NOTE: We may end up removing a good entry that was inserted concurrently,
                // but this does not violate correctness, only causes a potential near cache miss.
                _map.TryRemove(key0, out entry);
            }

            val = default(TVal);
            return false;
        }

        /** <inheritdoc /> */
        public int GetSize()
        {
            if (_stopped)
            {
                return 0;
            }

            var count = 0;
            
            foreach (var e in _map)
            {
                if (IsValid(e.Value))
                {
                    count++;
                }
            }

            return count;
        }

        /** <inheritdoc /> */
        public void Update(IBinaryStream stream, Marshaller marshaller)
        {
            Debug.Assert(stream != null);
            Debug.Assert(marshaller != null);

            if (_stopped)
            {
                return;
            }

            var mode = _keepBinary ? BinaryMode.ForceBinary : BinaryMode.Deserialize;
            var reader = marshaller.StartUnmarshal(stream, mode);

            var key = reader.ReadObject<TK>();
            var hasVal = reader.ReadBoolean();

            var val = hasVal ? reader.ReadObject<TV>() : default(TV);
            var part = hasVal ? reader.ReadInt() : 0;
            var ver = hasVal
                ? new AffinityTopologyVersion(reader.ReadLong(), reader.ReadInt())
                : default(AffinityTopologyVersion);

            if (hasVal)
            {
                // Reuse existing boxed copy when possible to reduce allocations.
                var currentVerBoxed = _affinityTopologyVersionFunc();
                var verBoxed = (AffinityTopologyVersion) currentVerBoxed == ver ? currentVerBoxed : ver;

                _map[key] = new NearCacheEntry<TV>(val, verBoxed, part);
            }
            else
            {
                NearCacheEntry<TV> unused;
                _map.TryRemove(key, out unused);
            }
        }

        /** <inheritdoc /> */
        public void Stop()
        {
            _stopped = true;
            Clear();
        }
        
        /** <inheritdoc /> */
        public void Clear()
        {
            _map.Clear();
        }

        /// <summary>
        /// Checks whether specified cache entry is still valid, based on Affinity Topology Version.
        /// When primary node changes for a key, GridNearCacheEntry stops receiving updates for that key,
        /// because reader ("subscription") on new primary is not yet established.
        /// <para />
        /// This method is similar to GridNearCacheEntry.valid(). 
        /// </summary>
        /// <param name="entry">Entry to validate.</param>
        /// <typeparam name="TVal">Value type.</typeparam>
        /// <returns>True if entry is valid and can be returned to the user; false otherwise.</returns>
        private bool IsValid<TVal>(NearCacheEntry<TVal> entry)
        {
            // See comments on _affinityTopologyVersionFunc about boxed copy approach. 
            var currentVerBoxed = _affinityTopologyVersionFunc();
            var entryVerBoxed = entry.Version;
            
            Debug.Assert(currentVerBoxed != null);
            
            if (ReferenceEquals(currentVerBoxed, entryVerBoxed))
            {
                // Happy path: true on stable topology.
                return true;
            }

            if (entryVerBoxed == null)
            {
                return false;
            }

            var entryVer = (AffinityTopologyVersion) entryVerBoxed;
            var currentVer = (AffinityTopologyVersion) currentVerBoxed;

            if (entryVer >= currentVer)
            {
                return true;
            }

            var part = entry.Partition;
            var valid = _affinity.IsAssignmentValid(entryVer, part);

            // Update version or mark as invalid (null).
            entry.CompareExchangeVersion(valid ? currentVerBoxed : null, entryVerBoxed);

            return valid;
        }
    }
}
