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

package org.casbin.jcasbin.util;

import java.util.regex.PatternSyntaxException;

/**
 * Glob translates a path-style glob expression into a Java regular expression.
 *
 * <p>Supported syntax:
 * <ul>
 *   <li>{@code *} matches any run of characters except {@code /}</li>
 *   <li>{@code **} matches any run of characters including {@code /}</li>
 *   <li>{@code ?} matches exactly one character except {@code /}</li>
 *   <li>{@code [abc]}, {@code [a-z]} match one character from the set; {@code [!abc]} negates the set.
 *       A leading {@code ^} or {@code -} is literal. The set never matches {@code /}.</li>
 *   <li>{@code {a,b}} matches either alternative; groups cannot be nested</li>
 *   <li>{@code \x} matches the character {@code x} literally</li>
 * </ul>
 * The separator is always {@code /}, regardless of the operating system.
 */
public class Glob {

    private Glob() {
    }

    /**
     * Creates a regex pattern from the given glob expression.
     *
     * @param globPattern the given glob expression
     * @return the regex pattern, anchored to match the whole input
     * @throws PatternSyntaxException if the glob expression is malformed
     */
    public static String toRegexPattern(String globPattern) {
        StringBuilder regex = new StringBuilder(globPattern.length() * 2 + 2).append('^');
        int groupStart = -1;
        int i = 0;
        while (i < globPattern.length()) {
            char c = globPattern.charAt(i);
            switch (c) {
                case '\\':
                    if (i + 1 >= globPattern.length()) {
                        throw error("Trailing escape character", globPattern, i);
                    }
                    appendLiteral(regex, globPattern.charAt(i + 1));
                    i += 2;
                    break;
                case '*':
                    if (i + 1 < globPattern.length() && globPattern.charAt(i + 1) == '*') {
                        regex.append(".*");
                        i += 2;
                    } else {
                        regex.append("[^/]*");
                        i++;
                    }
                    break;
                case '?':
                    regex.append("[^/]");
                    i++;
                    break;
                case '[':
                    i = appendCharacterSet(regex, globPattern, i);
                    break;
                case '{':
                    if (groupStart >= 0) {
                        throw error("Nested groups are not supported", globPattern, i);
                    }
                    groupStart = i;
                    regex.append("(?:");
                    i++;
                    break;
                case ',':
                    if (groupStart >= 0) {
                        regex.append('|');
                    } else {
                        appendLiteral(regex, c);
                    }
                    i++;
                    break;
                case '}':
                    if (groupStart >= 0) {
                        groupStart = -1;
                        regex.append(')');
                    } else {
                        appendLiteral(regex, c);
                    }
                    i++;
                    break;
                default:
                    appendLiteral(regex, c);
                    i++;
            }
        }
        if (groupStart >= 0) {
            throw error("Unclosed group", globPattern, groupStart);
        }
        return regex.append('$').toString();
    }

    /**
     * Translates the character set starting at {@code start} (which must be {@code [}).
     *
     * @return the index just past the closing {@code ]}
     */
    private static int appendCharacterSet(StringBuilder regex, String glob, int start) {
        int i = start + 1;
        boolean negated = i < glob.length() && glob.charAt(i) == '!';
        if (negated) {
            i++;
        }
        // Exclude the separator up front; a negated set is simply [^/...].
        regex.append(negated ? "[^/" : "(?!/)[");
        int members = 0;

        // A leading '^' (when not negated) or '-' is an ordinary member.
        if (i < glob.length() && (glob.charAt(i) == '-' || (!negated && glob.charAt(i) == '^'))) {
            appendLiteral(regex, glob.charAt(i));
            members++;
            i++;
        }

        while (true) {
            if (i >= glob.length()) {
                throw error("Unclosed character set", glob, start);
            }
            char c = glob.charAt(i);
            if (c == ']') {
                break;
            }
            if (c == '/') {
                throw error("Separator is not allowed in a character set", glob, i);
            }
            if (c == '-') {
                throw error("Range has no start", glob, i);
            }
            appendLiteral(regex, c);
            members++;
            i++;
            if (i < glob.length() && glob.charAt(i) == '-') {
                i++;
                if (i >= glob.length()) {
                    throw error("Unclosed character set", glob, start);
                }
                char upper = glob.charAt(i);
                if (upper == ']') {
                    // "[ab-]": a dash right before the closing bracket is literal.
                    appendLiteral(regex, '-');
                    break;
                }
                if (upper < c) {
                    throw error("Range end is lower than range start", glob, i);
                }
                regex.append('-');
                appendLiteral(regex, upper);
                i++;
            }
        }
        if (members == 0) {
            throw error("Empty character set", glob, start);
        }
        regex.append(']');
        return i + 1;
    }

    /**
     * Appends {@code c} so that it matches itself, both inside and outside a character class.
     * Escaping every non-alphanumeric character is always safe in java.util.regex.
     */
    private static void appendLiteral(StringBuilder regex, char c) {
        if (!Character.isLetterOrDigit(c) && c < 128) {
            regex.append('\\');
        }
        regex.append(c);
    }

    private static PatternSyntaxException error(String description, String glob, int index) {
        return new PatternSyntaxException(description, glob, index);
    }
}
