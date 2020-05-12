/*
 * Copyright 2020 GridGain Systems, Inc. and Contributors.
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
package org.apache.ignite.internal.processors.cache.persistence.tree.io;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.pagemem.PageUtils;
import org.apache.ignite.internal.util.GridStringBuilder;

/**
 * Marker page.
 */
public class MarkerPageIO extends PageIO {
    /** Offset for marker type value. */
    private static final int MARKER_TYPE_OFF = COMMON_HEADER_END;

    /** This type of marker is used to mark the end of pages stream. */
    public static final int MARKER_TYPE_TERMINATION = 1;

    /** */
    public static final IOVersions<MarkerPageIO> VERSIONS = new IOVersions<>(
        new MarkerPageIO(1)
    );

    /**
     * @param ver Page format version.
     */
    public MarkerPageIO(int ver) {
        super(T_MARKER_PAGE, ver);
    }
    /**
     * @param type Page type.
     * @param ver  Page format version.
     */
    protected MarkerPageIO(int type, int ver) {
        super(type, ver);
    }

    /** Type of a marker. */
    public int markerType(long pageAddr) {
        return PageUtils.getInt(pageAddr, MARKER_TYPE_OFF);
    }

    /** Sets marker type. */
    public void setMarkerType(long pageAddr, int markerType) {
        PageUtils.putInt(pageAddr, MARKER_TYPE_OFF, markerType);
    }

    /** {@inheritDoc} */
    @Override protected void printPage(long addr, int pageSize, GridStringBuilder sb) throws IgniteCheckedException {
        sb.a("MarkerPage [markerType=" + markerType(addr) + "]");
    }
}