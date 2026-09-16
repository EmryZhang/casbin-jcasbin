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

import com.google.gson.Gson;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;

public class FrontendUnitTest {

  @Test
  public void testCasbinJsGetPermissionForUser() throws IOException {
    SyncedEnforcer e = new SyncedEnforcer("examples/rbac_model.conf", "examples/rbac_with_hierarchy_policy.csv");
    HashMap<String, Object> received = new Gson().fromJson(Frontend.casbinJsGetPermissionForUser(e, "alice"), HashMap.class);
    String expectedModelStr = readFixtureWithoutComments("examples/rbac_model.conf");
    assertEquals(received.get("m"), expectedModelStr);

    String expectedPolicyStr = readFixtureWithoutComments("examples/rbac_with_hierarchy_policy.csv");
    expectedPolicyStr = Pattern.compile("\n+").matcher(expectedPolicyStr).replaceAll("\n");
    String[] expectedPolicyItem = expectedPolicyStr.split(",|\n");
    int i = 0;
    for (List<String> sArr : (List<List<String>>) received.get("p")) {
      for (String s : sArr) {
        assertEquals(expectedPolicyItem[i].trim(), s.trim());
        i++;
      }
    }
    for (List<String> sArr : (List<List<String>>) received.get("g")) {
      for (String s : sArr) {
        assertEquals(expectedPolicyItem[i].trim(), s.trim());
        i++;
      }
    }
  }

  // Model and policy export contains definitions, not file license comments.
  private String readFixtureWithoutComments(String path) throws IOException {
    String text = new String(Files.readAllBytes(Paths.get(path)), java.nio.charset.StandardCharsets.UTF_8);
    return text.replaceAll("(?m)^#.*(?:\\r?\\n|$)", "").replaceFirst("^\\s*\\r?\\n", "");
  }
}
