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

package org.apache.ignite.spi.indexing;

import java.util.Collection;
import java.util.Iterator;
import javax.cache.Cache;
import org.apache.ignite.spi.IgniteSpi;
import org.apache.ignite.spi.IgniteSpiException;
import org.jetbrains.annotations.Nullable;

/**
 * Indexing SPI allows user to index cache content. Using indexing SPI user can index data in cache and run queries.
 * <p>
 * <b>NOTE:</b> this SPI (i.e. methods in this interface) should never be used directly. SPIs provide
 * internal view on the subsystem and is used internally by Ignite kernal. In rare use cases when
 * access to a specific implementation of this SPI is required - an instance of this SPI can be obtained
 * via {@link org.apache.ignite.Ignite#configuration()} method to check its configuration properties or call other non-SPI
 * methods. Note again that calling methods from this interface on the obtained instance can lead
 * to undefined behavior and explicitly not supported.
 *
 * <b>NOTE:</b> Key and value arguments of IgniteSpi methods can be {@link org.apache.ignite.binary.BinaryObject} instances.
 * BinaryObjects can be deserialized manually if original objects needed.
 *
 * Here is a Java example on how to configure SPI.
 * <pre name="code" class="java">
 * IndexingSpi spi = new MyIndexingSpi();
 *
 * IgniteConfiguration cfg = new IgniteConfiguration();
 *
 * // Overrides default indexing SPI.
 * cfg.setIndexingSpi(spi);
 *
 * // Starts grid.
 * Ignition.start(cfg);
 * </pre>
 * Here is an example of how to configure SPI from Spring XML configuration file.
 * <pre name="code" class="xml">
 * &lt;property name=&quot;indexingSpi&quot;&gt;
 *     &lt;bean class=&quot;com.example.MyIndexingSpi&quot;&gt;
 *     &lt;/bean&gt;
 * &lt;/property&gt;
 * </pre>
 * <p>
 * <img src="http://ignite.apache.org/images/spring-small.png">
 * <br>
 * For information about Spring framework visit <a href="http://www.springframework.org/">www.springframework.org</a>
 */
public interface IndexingSpi extends IgniteSpi {
    /**
     * Executes query.
     *
     * @param cacheName Cache name.
     * @param params Query parameters.
     * @param filters System filters.
     * @return Query result. If the iterator implements {@link AutoCloseable} it will be correctly closed.
     * @throws IgniteSpiException If failed.
     */
    public Iterator<Cache.Entry<?,?>> query(@Nullable String cacheName, Collection<Object> params,
        @Nullable IndexingQueryFilter filters) throws IgniteSpiException;

    /**
     * Updates index. Note that key is unique for cache, so if cache contains multiple indexes
     * the key should be removed from indexes other than one being updated.
     *
     * @param cacheName Cache name.
     * @param key Key.
     * @param val Value.
     * @param expirationTime Expiration time or 0 if never expires.
     * @throws IgniteSpiException If failed.
     */
    public void store(@Nullable String cacheName, Object key, Object val, long expirationTime) throws IgniteSpiException;

    /**
     * Removes index entry by key.
     *
     * @param cacheName Cache name.
     * @param key Key.
     * @throws IgniteSpiException If failed.
     */
    public void remove(@Nullable String cacheName, Object key) throws IgniteSpiException;
}
