// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version
{
    private final static Pattern JAVA_RUNTIME_VERSION =
            Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)_(\\d+)-(?:.*-)?b(\\d+).*");
    private final static Pattern GENERIC_VERSION =
            Pattern.compile("[^0-9]*(\\d+)\\.(\\d+)\\.(\\d+).*");
    private final int major;
    private final int minor;
    private final int patch;
    private final int update;
    private final int build;

    public Version(int major, int minor, int patch, int update, int build)
    {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.update = update;
        this.build = build;
    }

    /**
     * Parses a Java Runtime version property string to find the details of the JDK version
     *
     * @param javaRuntimeVersion JDK version string
     * @return Version object containing JDK version details
     */
    public static Version parseJavaRuntimeVersion(final String javaRuntimeVersion)
    {
        Matcher matcher = getMatches(JAVA_RUNTIME_VERSION, javaRuntimeVersion);
        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = Integer.parseInt(matcher.group(3));
        int update = Integer.parseInt(matcher.group(4));
        int build = Integer.parseInt(matcher.group(5));

        return new Version(major, minor, patch, update, build);
    }

    /**
     * Parses a generic string that contains a version number in the format major.minor.patch
     *
     * @param version String containing the version number
     * @return Version object with version details
     */
    public static Version parseVersion(final String version)
    {
        Matcher matcher = getMatches(GENERIC_VERSION, version);
        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = Integer.parseInt(matcher.group(3));

        return new Version(major, minor, patch, 0, 0);
    }

    /**
     * Given a pattern and a string, a Matcher is returned if any matches are found else an exception is thrown
     *
     * @param pattern Version pattern to match against
     * @param version String containing the version
     * @return Matcher with matches to the version pattern
     */
    private static Matcher getMatches(final Pattern pattern, final String version)
    {
        final Matcher matcher = pattern.matcher(version);
        if (!matcher.matches())
        {
            final String template = "Unrecognized version string '%1$s'.";
            final String message = String.format(template, version);
            throw new IllegalArgumentException(message);
        }

        return matcher;
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
