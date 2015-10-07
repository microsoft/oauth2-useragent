// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {
    private final static Pattern JAVA_RUNTIME_VERSION =
            Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)_(\\d+)-b(\\d+).*");
    private final int major;
    private final int minor;
    private final int patch;
    private final int update;
    private final int build;

    public Version(final String javaRuntimeVersion) {
        final Matcher matcher = JAVA_RUNTIME_VERSION.matcher(javaRuntimeVersion);
        if (!matcher.matches()) {
            final String template = "Unrecognized version string '%1$s'.";
            final String message = String.format(template, javaRuntimeVersion);
            throw new IllegalArgumentException(message);
        }
        major = Integer.parseInt(matcher.group(1));
        minor = Integer.parseInt(matcher.group(2));
        patch = Integer.parseInt(matcher.group(3));
        update = Integer.parseInt(matcher.group(4));
        build = Integer.parseInt(matcher.group(5));
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public int getUpdate() {
        return update;
    }

    public int getBuild() {
        return build;
    }

}
