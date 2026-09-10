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

import org.casbin.jcasbin.util.ExpressionValidator;

public class TestAviatorValidation {
    public static void main(String[] args) {
        System.out.println("Testing Expression Validator:\n");
        
        // Test 1: Valid Casbin expression
        try {
            ExpressionValidator.validateExpression("r.age > 18 && r.role == 'admin'");
            System.out.println("✅ PASS: Standard Casbin expression allowed");
        } catch (Exception e) {
            System.out.println("❌ FAIL: " + e.getMessage());
        }
        
        // Test 2: Invalid - seq.list()
        try {
            ExpressionValidator.validateExpression("seq.list('A', 'B', 'C')");
            System.out.println("❌ FAIL: seq.list() should be blocked");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ PASS: seq.list() blocked - " + e.getMessage());
        }
        
        // Test 3: Invalid - string.startsWith()
        try {
            ExpressionValidator.validateExpression("string.startsWith(r.path, '/admin')");
            System.out.println("❌ FAIL: string.startsWith() should be blocked");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ PASS: string.startsWith() blocked - " + e.getMessage());
        }
        
        // Test 4: Valid - custom function
        try {
            ExpressionValidator.validateExpression("myCustomFunc(r.value)");
            System.out.println("✅ PASS: Custom functions allowed");
        } catch (Exception e) {
            System.out.println("❌ FAIL: " + e.getMessage());
        }
        
        // Test 5: Invalid - lambda
        try {
            ExpressionValidator.validateExpression("lambda(x) -> x * 2");
            System.out.println("❌ FAIL: lambda should be blocked");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ PASS: lambda blocked - " + e.getMessage());
        }
    }
}
