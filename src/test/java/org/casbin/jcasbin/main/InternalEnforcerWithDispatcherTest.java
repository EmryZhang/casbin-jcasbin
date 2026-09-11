// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements. See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership. The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License. You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

package org.casbin.jcasbin.main;

import org.casbin.jcasbin.persist.Dispatcher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class InternalEnforcerWithDispatcherTest {

    private final static String SEC = "expected-sec";

    private final static String PTYPE = "expected-ptype";

    private final static List<String> RULE = asList("expected-new-rule-1", "expected-new-rule-2");

    private final static List<String> OLD_RULE = asList("expected-old-rule-1", "expected-old-rule-2");

    private final static int FIELD_INDEX = 0;

    private final static String[] FIELD_VALUES = new String[1];

    private InternalEnforcer enforcer;

    @BeforeMethod
    public void setUp() {
        this.enforcer = new InternalEnforcer();
        this.enforcer.setDispatcher(new CustomDispatcher());
        this.enforcer.setAutoNotifyDispatcher(true);
    }

    @Test
    public void testAddPolicy() {
        boolean result = enforcer.addPolicy(SEC, PTYPE, RULE);
        assertTrue(result);
    }

    @Test
    public void testAddPolicies() {
        boolean result = enforcer.addPolicies(SEC, PTYPE, singletonList(RULE), false);
        assertTrue(result);
    }

    @Test
    public void testRemovePolicy() {
        boolean result = enforcer.removePolicy(SEC, PTYPE, RULE);
        assertTrue(result);
    }

    @Test
    public void testRemovePolicies() {
        boolean result = enforcer.removePolicies(SEC, PTYPE, singletonList(RULE));
        assertTrue(result);
    }

    @Test
    public void testRemoveFilteredPolicy() {
        boolean result = enforcer.removeFilteredPolicy(SEC, PTYPE, FIELD_INDEX, FIELD_VALUES);
        assertTrue(result);
    }

    @Test
    public void testUpdatePolicy() {
        boolean result = enforcer.updatePolicy(SEC, PTYPE, OLD_RULE, RULE);
        assertTrue(result);
    }

    private static class CustomDispatcher implements Dispatcher {

        @Override
        public void addPolicies(
            final String sec,
            final String ptype,
            final List<List<String>> rules
        ) {
            assertEquals(SEC, sec);
            assertEquals(PTYPE, ptype);
            assertEquals(singletonList(RULE), rules);
        }

        @Override
        public void removePolicies(
            final String sec,
            final String ptype,
            final List<List<String>> rules
        ) {
            assertEquals(SEC, sec);
            assertEquals(PTYPE, ptype);
            assertEquals(singletonList(RULE), rules);
        }

        @Override
        public void removeFilteredPolicy(
            final String sec,
            final String ptype,
            final int fieldIndex,
            final String... fieldValues
        ) {
            assertEquals(SEC, sec);
            assertEquals(PTYPE, ptype);
            assertEquals(FIELD_INDEX, fieldIndex);
            assertEquals(FIELD_VALUES, fieldValues);
        }

        @Override
        public void clearPolicy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updatePolicy(
            final String sec,
            final String ptype,
            final List<String> oldRule,
            final List<String> newRule
        ) {
            assertEquals(SEC, sec);
            assertEquals(PTYPE, ptype);
            assertEquals(OLD_RULE, oldRule);
            assertEquals(RULE, newRule);
        }
    }
}
