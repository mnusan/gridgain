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

package org.apache.ignite.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.configuration.IgniteReflectionFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link IgniteReflectionFactory} class.
 */
public class IgniteReflectionFactorySelfTest {
    /** */
    @Test
    public void testByteMethod() {
        byte    expByteVal    = 42;
        short   expShortVal   = 42;
        int     expIntVal     = 42;
        long    expLongVal    = 42L;
        float   expFloatVal   = 42.0f;
        double  expDoubleVal  = 42.0d;
        char    expCharVal    = 'z';
        boolean expBooleanVal = true;

        Map<String, Serializable> props = new HashMap<>();

        props.put("byteField", expByteVal);
        props.put("shortField", expShortVal);
        props.put("intField", expIntVal);
        props.put("longField", expLongVal);
        props.put("floatField", expFloatVal);
        props.put("doubleField", expDoubleVal);
        props.put("charField", expCharVal);
        props.put("booleanField", expBooleanVal);

        IgniteReflectionFactory<TestClass> factory = new IgniteReflectionFactory<>(TestClass.class);

        factory.setProperties(props);

        TestClass instance = factory.create();

        assertEquals(expByteVal, instance.getByteField());
        assertEquals(expShortVal, instance.getShortField());
        assertEquals(expIntVal, instance.getIntField());
        assertEquals(expLongVal, instance.getLongField());
        assertEquals(expFloatVal, instance.getFloatField(), 0);
        assertEquals(expDoubleVal, instance.getDoubleField(), 0);
        assertEquals(expCharVal, instance.getCharField());
        assertEquals(expBooleanVal, instance.getBooleanField());
    }

    /**
     * Test class that is created by {@link IgniteReflectionFactory}.
     */
    public static class TestClass {
        /** */
        private byte byteField;
        /** */
        private short shortField;
        /** */
        private int iField;
        /** */
        private long longField;
        /** */
        private float floatField;
        /** */
        private double doubleField;
        /** */
        private char cField;
        /** */
        private boolean booleanField;

        /**
         * Returns byte field.
         *
         * @return Byte field.
         */
        public byte getByteField() {
            return byteField;
        }

        /**
         * Sets byte field.
         *
         * @param byteField New field value.
         */
        public void setByteField(byte byteField) {
            this.byteField = byteField;
        }

        /**
         * Returns short field.
         *
         * @return Short field.
         */
        public short getShortField() {
            return shortField;
        }

        /**
         * Sets short field.
         *
         * @param shortField New field value.
         */
        public void setShortField(short shortField) {
            this.shortField = shortField;
        }

        /**
         * Returns long field.
         *
         * @return Long field.
         */
        public long getLongField() {
            return longField;
        }

        /**
         * Sets long field.
         *
         * @param longField New field value.
         */
        public void setLongField(long longField) {
            this.longField = longField;
        }

        /**
         * Returns float field.
         *
         * @return Float field.
         */
        public float getFloatField() {
            return floatField;
        }

        /**
         * Sets float field.
         *
         * @param floatField New field value.
         */
        public void setFloatField(float floatField) {
            this.floatField = floatField;
        }

        /**
         * Returns double field.
         *
         * @return Double field.
         */
        public double getDoubleField() {
            return doubleField;
        }

        /**
         * Sets double field.
         *
         * @param doubleField New field value.
         */
        public void setDoubleField(double doubleField) {
            this.doubleField = doubleField;
        }

        /**
         * Returns char field.
         *
         * @return Char field.
         */
        public char getCharField() {
            return cField;
        }

        /**
         * Sets char field.
         *
         * @param cField New field value.
         */
        public void setCharField(char cField) {
            this.cField = cField;
        }

        /**
         * Returns boolean field.
         *
         * @return Boolean field.
         */
        public boolean getBooleanField() {
            return booleanField;
        }

        /**
         * Sets boolean field.
         *
         * @param booleanField New field value.
         */
        public void setBooleanField(boolean booleanField) {
            this.booleanField = booleanField;
        }

        /**
         * Returns int field.
         *
         * @return Int field.
         */
        public int getIntField() {
            return iField;
        }

        /**
         * Sets int field.
         *
         * @param iField New field value.
         */
        public void setIntField(int iField) {
            this.iField = iField;
        }
    }
}
