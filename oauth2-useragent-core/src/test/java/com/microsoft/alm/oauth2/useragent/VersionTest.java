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

    @Test public void genericVersion() {
        final Version version = Version.parseVersion("git version 2.4.9 (Apple Git-60)");
        Assert.assertEquals(2, version.getMajor());
        Assert.assertEquals(4, version.getMinor());
        Assert.assertEquals(9, version.getPatch());
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
