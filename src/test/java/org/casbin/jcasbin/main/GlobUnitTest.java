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

import org.casbin.jcasbin.util.BuiltInFunctions;
import org.casbin.jcasbin.util.Glob;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.testng.Assert.assertEquals;

public class GlobUnitTest {
    @DataProvider
    public Object[][] matches() {
        return new Object[][] {
            {"", "", true}, {"", "*", true}, {"/a/b", "/a/*", true},
            {"/a/b/c", "/a/*", false}, {"/a/b/c", "/a/**", true},
            {"a/b", "a?b", false}, {"a\\b", "a?b", true},
            {"a", "[abc]", true}, {"d", "[abc]", false},
            {"z", "[a-z]", true}, {"Z", "[a-z]", false},
            {"c", "[!ab]", true}, {"/", "[!ab]", false},
            {"^", "[^a]", true}, {"b", "[^a]", false},
            {"-", "[-ab]", true}, {"-", "[ab-]", true},
            {"&", "[&&]", true}, {"[", "[[]", true},
            {"\\", "[\\]", true}, {".", "[.-/]", true},
            {"/", "[.-/]", false},
            {"abc", "a{b,x}c", true}, {"axc", "a{b,x}c", true},
            {"ac", "a{b,x}c", false}, {"ac", "a{,x}c", true},
            {"", "{,}", true}, {"}", "}", true},
            {"*?[{", "\\*\\?\\[\\{", true},
            {".$+()|^", ".$+()|^", true},
            {"\n", "*", true}, {"\n", "**", false},
            {"\n", "***", true}, {"a\nb", "a?b", true},
            {"中文", "中文", true}, {"🙂", "🙂", true},
            {"🙂", "?", true}, {"🙂", "[🙂]", true},
            {"🙂", "\\🙂", true}, {"ab", "a", false}
        };
    }

    @Test(dataProvider = "matches")
    public void preservesPathGlobSemantics(String value, String glob, boolean expected) {
        assertEquals(BuiltInFunctions.globMatch(value, glob), expected,
                "pattern=" + glob + ", input=" + value);
    }

    @DataProvider
    public Object[][] malformed() {
        return new Object[][] {
            {"["}, {"[]"}, {"[!]"}, {"[z-a]"}, {"[a--]"}, {"[a-b-c]"},
            {"[^-]"}, {"[a/b]"}, {"{a"}, {"{a,{b,c}}"}, {"a\\"}
        };
    }

    @Test(dataProvider = "malformed", expectedExceptions = PatternSyntaxException.class)
    public void rejectsMalformedGlobs(String glob) {
        Pattern.compile(Glob.toRegexPattern(glob));
    }
}
