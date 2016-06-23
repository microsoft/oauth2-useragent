// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent.utils;

public class StringHelper {

    public static boolean equal(final String s1, final String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        return s1.equals(s2);
    }

    public static boolean equalIgnoringCase(final String a, final String b) {
        //noinspection StringEquality
        if (a == b) {
            return true;
        }
        if (a != null && b != null) {
            int n = a.length();
            if (b.length() != n) {
                return false;
            }
            return a.regionMatches(true, 0, b, 0, n);
        }
        return false;
    }

    public static String join(final String separator, final String[] value) {
        if (value == null)
            throw new IllegalArgumentException("value is null");

        // "If separator is null, an empty string (String.Empty) is used instead."
        final String sep = separator == null ? "" : separator;

        final StringBuilder result = new StringBuilder();

        if (value.length > 0) {
            result.append(value[0] == null ? "" : value[0]);
            for (int i = 1; i < value.length; i++) {
                result.append(sep);
                result.append(value[i] == null ? "" : value[i]);
            }
        }

        return result.toString();
    }
}
