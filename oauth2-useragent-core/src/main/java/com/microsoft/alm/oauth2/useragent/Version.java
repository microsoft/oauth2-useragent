// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version
{
    private final static Pattern JAVA_LEGACY_RUNTIME_VERSION =
            Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:_(\\d+))?(?:-(?:.*-)?b(\\d+))?.*");
    private final static Pattern GENERIC_VERSION =
            Pattern.compile("[^0-9]*(\\d+)\\.(\\d+)(?:\\.(\\d+))?.*");

    /**
     * Java 9 and later: Version can be a single integer or in the format of x.x.x
     */
    private final static String VNUM = "(?<VNUM>[1-9][0-9]*(?:(?:\\.0)*\\.[1-9][0-9]*)*)";

    /**
     * Java 9 and later: Pre-release identifier can be string or integer
     */
    private final static String PRE = "(?:-(?<PRE>[a-zA-Z0-9]+))?";

    /**
     * Java 9 and later: Promoted build number as an integer.
     */
    private final static String BUILD = "(?:(?<PLUS>\\+)(?<BUILD>0|[1-9][0-9]*)?)?";

    /**
     * Java 9 and later: Optional build information as a string or integer.
     */
    private final static String OPT = "(?:-(?<OPT>[-a-zA-Z0-9.]+))?";

    /**
     * Pattern used for Java versions 9 and later.
     */
    private final static Pattern JAVA_9_PLUS_VERSION = Pattern.compile(VNUM + PRE + BUILD + OPT);

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
        Matcher matcher = JAVA_9_PLUS_VERSION.matcher(javaRuntimeVersion);

        if (matcher.matches()) {
            String[] version = matcher.group(1).split("\\.");

            int feature = Integer.parseInt(version[0]);
            int interim = (version.length > 1 ? Integer.parseInt(version[1]) : 0);
            int update = (version.length > 2 ? Integer.parseInt(version[2]) : 0);
            int patch = (version.length > 3 ? Integer.parseInt(version[3]) : 0);

            int build = integerOrZero(matcher.group(4));

            return new Version(feature, interim, update, patch, build);
        } else {
            matcher = JAVA_LEGACY_RUNTIME_VERSION.matcher(javaRuntimeVersion);

            if (matcher.matches()) {
                int major = Integer.parseInt(matcher.group(1));
                int minor = Integer.parseInt(matcher.group(2));
                int patch = integerOrZero(matcher.group(3));
                int update = integerOrZero(matcher.group(4));
                int build = integerOrZero(matcher.group(5));

                return new Version(major, minor, patch, update, build);
            } else {
                throw new IllegalArgumentException(getUnrecognizedVersionErrorMessage(javaRuntimeVersion));
            }
        }
    }

    /**
     * Parses a generic string that contains a version number in the format
     * major.minor.patch
     * or
     * major.minor
     *
     * @param version String containing the version number
     * @return Version object with version details
     */
    public static Version parseVersion(final String version)
    {
        Matcher matcher = GENERIC_VERSION.matcher(version);
     
        if (matcher.matches()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int patch = integerOrZero(matcher.group(3));

            return new Version(major, minor, patch, 0, 0);
        } else {
            throw new IllegalArgumentException(getUnrecognizedVersionErrorMessage(version));
        }
    }

    static int integerOrZero(final String input) {
        return input != null ? Integer.parseInt(input) : 0;
    }

    /**
     * Returns an error message including the unrecognized version
     *
     * @param version String containing the unrecognized version
     * @return Error message for the unrecognized version
     */
    private static String getUnrecognizedVersionErrorMessage(final String version) {
        final String template = "Unrecognized version string '%1$s'.";        
        return String.format(template, version);
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
