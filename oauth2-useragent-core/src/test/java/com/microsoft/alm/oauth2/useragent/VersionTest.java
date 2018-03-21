// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.junit.Assert;
import org.junit.Test;

public class VersionTest
{

    @Test public void openJdk7OnUbuntu() {
        testJdkParsing(1, 7, 0, 79, 14, "1.7.0_79-b14");
    }

    @Test public void openJdk7InternalOnUbuntu() {
        testJdkParsing(1, 7, 0, 79, 14, "1.7.0_79-internal-b14");
    }

    @Test public void appleJdk6OnMacOsX() {
        testJdkParsing(1, 6, 0, 65, 14, "1.6.0_65-b14-466.1-11M4716");
    }

    @Test public void oracleJdk7OnMacOsX() {
        testJdkParsing(1, 7, 0, 71, 14, "1.7.0_71-b14");
    }

    @Test public void oracleJdk6OnWindows() {
        testJdkParsing(1, 6, 0, 20, 2, "1.6.0_20-b02");
    }

    @Test public void oracleJdk8OnWindows() {
        testJdkParsing(1, 8, 0, 60, 27, "1.8.0_60-b27");
    }

    @Test public void oracleJdk8OnFedora() {
        testJdkParsing(1, 8, 0, 60, 27, "1.8.0_60-b27");
    }

    @Test public void openJdk8OnFedora() {
        testJdkParsing(1, 8, 0, 60, 27, "1.8.0_60-b27");
    }

    @Test public void javaWithBuildNoUpdate() {
        testJdkParsing(1, 8, 0, 0, 132, "1.8.0-b132");
    }

    @Test public void javaWithUpdateNoBuild() {
        testJdkParsing(1, 8, 0, 3, 0, "1.8.0_3");
    }

    @Test public void javaWithNoBuildNoUpdate() {
        testJdkParsing(1, 8, 0, 0, 0, "1.8.0");
    }

    @Test public void javaWithNoPatchNoUpdateNoBuild() {
        testJdkParsing(1, 8, 0, 0, 0, "1.8");
    }

    @Test public void java9MajorVersionFormat() {
        testJdkParsing(10, 0, 0, 0, 46, "10+46");
    }

    @Test public void java9MinorVersionFormat() {
        testJdkParsing(10, 1, 2, 0, 62, "10.1.2+62");
    }

    @Test public void java9EarlyAccessVersionFormat() {
        testJdkParsing(10, 0, 0, 0, 73, "10-ea+73");
    }

    @Test public void java9UbuntuVersionFormat() {
        testJdkParsing(9, 1, 0, 0, 0, "9-Ubuntu+0-9b181-4");
    }

    @Test public void gitVersion()
    {
        testGenericVersion(2, 4, 9, "git version 2.4.9 (Apple Git-60)");
    }

    @Test public void macOsVersion() {
        testGenericVersion(10, 10, 5, "10.10.5");
    }

    @Test public void macOsVersionWithoutPatch() {
        testGenericVersion(10, 11, 0, "10.11");
    }

    private void testGenericVersion(final int major, final int minor, final int patch, final String input) {
        final Version version = Version.parseVersion(input);
        Assert.assertEquals(major, version.getMajor());
        Assert.assertEquals(minor, version.getMinor());
        Assert.assertEquals(patch, version.getPatch());
    }

    private void testJdkParsing(final int major, final int minor, final int patch, final int update, final int build, final String input) {
        final Version version = Version.parseJavaRuntimeVersion(input);
        Assert.assertEquals(major, version.getMajor());
        Assert.assertEquals(minor, version.getMinor());
        Assert.assertEquals(patch, version.getPatch());
        Assert.assertEquals(update, version.getUpdate());
        Assert.assertEquals(build, version.getBuild());
    }
}
