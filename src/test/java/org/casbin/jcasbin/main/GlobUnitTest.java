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

import org.casbin.jcasbin.util.Glob;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.testng.Assert.assertEquals;

public class GlobUnitTest {

    @DataProvider
    public Object[][] matches() {
        return new Object[][]{
            // literals and anchoring
            {"", "", true},
            {"abc", "abc", true},
            {"ab", "abc", false},
            {"abcd", "abc", false},
            {"a.b", "a.b", true},
            {"axb", "a.b", false},
            {".$+()|^", ".$+()|^", true},
            {"中文/路径", "中文/路径", true},
            {"🙂", "🙂", true},

            // single wildcard never crosses '/'
            {"", "*", true},
            {"/a/b", "/a/*", true},
            {"/a/b/c", "/a/*", false},
            {"/a/", "/a/*", true},
            {"a\nb", "*", true},

            // double wildcard crosses '/'
            {"/a/b/c", "/a/**", true},
            {"/a/", "/a/**", true},
            {"/x/y/z", "**/z", true},
            {"/x/y/z", "***", true},

            // single character
            {"a", "?", true},
            {"/", "?", false},
            {"🙂", "?", true},
            {"a/b", "a?b", false},
            {"a\\b", "a?b", true},

            // character sets
            {"a", "[abc]", true},
            {"d", "[abc]", false},
            {"z", "[a-z]", true},
            {"Z", "[a-z]", false},
            {"m", "[a-cx-z]", false},
            {"c", "[!ab]", true},
            {"a", "[!ab]", false},
            {"/", "[!ab]", false},
            {"^", "[^a]", true},
            {"b", "[^a]", false},
            {"-", "[-ab]", true},
            {"-", "[!-ab]", false},
            {"-", "[ab-]", true},
            {"-", "[-]", true},
            {"&", "[&&]", true},
            {"[", "[[]", true},
            {"\\", "[\\]", true},
            {".", "[.-/]", true},
            {"/", "[.-/]", false},
            {"🙂", "[🙂]", true},

            // alternatives
            {"abc", "a{b,x}c", true},
            {"axc", "a{b,x}c", true},
            {"ac", "a{b,x}c", false},
            {"ac", "a{,x}c", true},
            {"", "{,}", true},
            {"a/b", "{a/b,c}", true},
            {"a,b", "a,b", true},
            {"}", "}", true},
            {"a}", "a}", true},

            // escapes
            {"*?[{", "\\*\\?\\[\\{", true},
            {"a*", "a\\*", true},
            {"ab", "a\\*", false},
            {"n", "\\n", true},
            {"🙂", "\\🙂", true},
        };
    }

    @Test(dataProvider = "matches")
    public void testToRegexPattern(String input, String glob, boolean expected) {
        assertEquals(Pattern.matches(Glob.toRegexPattern(glob), input), expected,
            "glob=" + glob + " input=" + input);
    }

    @DataProvider
    public Object[][] malformed() {
        return new Object[][]{
            {"["}, {"[a"}, {"[a-"}, {"[]"}, {"[!]"}, {"[/]"}, {"[a/b]"},
            {"[z-a]"}, {"[a--]"}, {"[a-b-c]"}, {"[^-]"}, {"[--]"},
            {"{a"}, {"{a,{b,c}}"}, {"a\\"},
        };
    }

    @Test(dataProvider = "malformed", expectedExceptions = PatternSyntaxException.class)
    public void testMalformedGlob(String glob) {
        Glob.toRegexPattern(glob);
    }
}
