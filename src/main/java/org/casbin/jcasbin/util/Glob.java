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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Converts Casbin path globs into regular expressions, using '/' on every OS. */
public class Glob {

    /**
     * Translates literals, escapes, *, **, ?, character sets and brace alternatives.
     * A single wildcard cannot cross '/', while ** can. Alternatives cannot nest.
     *
     * @param globPattern the path pattern
     * @return a regular expression matching the complete input
     * @throws PatternSyntaxException when the glob is malformed
     */
    public static String toRegexPattern(String globPattern) {
        return "\\A" + new Translator(globPattern).sequence(false) + "\\z";
    }

    private static final class Translator {
        private final String input;
        private int cursor;

        private Translator(String input) {
            this.input = input;
        }

        private PatternSyntaxException error(String message, int position) {
            return new PatternSyntaxException(message, input, position);
        }

        private String sequence(boolean alternative) {
            List<String> pieces = new ArrayList<>();
            while (cursor < input.length()) {
                char token = input.charAt(cursor);
                if (alternative && (token == ',' || token == '}')) {
                    break;
                }
                int position = cursor++;
                if (token == '\\') {
                    if (cursor == input.length()) {
                        throw error("Escape requires a following character", position);
                    }
                    int escaped = cursor;
                    cursor += Character.charCount(input.codePointAt(cursor));
                    pieces.add(Pattern.quote(input.substring(escaped, cursor)));
                } else if (token == '*') {
                    boolean recursive = cursor < input.length() && input.charAt(cursor) == '*';
                    if (recursive) {
                        cursor++;
                    }
                    pieces.add(recursive ? ".*" : "[^/]*");
                } else if (token == '?') {
                    pieces.add("[^/]");
                } else if (token == '[') {
                    pieces.add(characterSet(position));
                } else if (token == '{') {
                    if (alternative) {
                        throw error("Nested alternatives are not supported", position);
                    }
                    pieces.add(alternatives(position));
                } else {
                    if (Character.isHighSurrogate(token) && cursor < input.length()
                            && Character.isLowSurrogate(input.charAt(cursor))) {
                        cursor++;
                    }
                    pieces.add(Pattern.quote(input.substring(position, cursor)));
                }
            }
            return String.join("", pieces);
        }

        private String alternatives(int start) {
            List<String> branches = new ArrayList<>();
            while (true) {
                branches.add(sequence(true));
                if (cursor == input.length()) {
                    throw error("Alternative list is not closed", start);
                }
                if (input.charAt(cursor++) == '}') {
                    return "(?:" + String.join("|", branches) + ")";
                }
            }
        }

        private String characterSet(int start) {
            int close = input.indexOf(']', cursor);
            if (close < 0) {
                throw error("Character set is not closed", start);
            }
            String members = input.substring(cursor, close);
            int offset = cursor;
            cursor = close + 1;
            boolean exclude = members.startsWith("!");
            int index = exclude ? 1 : 0;
            StringBuilder contents = new StringBuilder();
            // An initial caret is a literal, not a negation marker.
            if (index == 0 && members.startsWith("^")) {
                contents.append(Pattern.quote("^"));
                index++;
            } else if (index < members.length() && members.charAt(index) == '-') {
                contents.append(Pattern.quote("-"));
                index++;
            }
            while (index < members.length()) {
                int lower = members.codePointAt(index);
                index += Character.charCount(lower);
                if (lower == '/' || lower == '-') {
                    throw error("Invalid character-set member", offset + index - 1);
                }
                contents.append(Pattern.quote(new String(Character.toChars(lower))));
                if (index < members.length() && members.charAt(index) == '-') {
                    index++;
                    if (index == members.length()) {
                        contents.append(Pattern.quote("-"));
                    } else {
                        int upper = members.codePointAt(index);
                        index += Character.charCount(upper);
                        if (upper < lower) {
                            throw error("Invalid character-set range", offset + index - 1);
                        }
                        contents.append('-').append(Pattern.quote(new String(Character.toChars(upper))));
                    }
                }
            }
            if (contents.length() == 0) {
                throw error("Character set must not be empty", start);
            }
            return exclude ? "[^/" + contents + "]" : "(?!/)[" + contents + "]";
        }
    }
}
