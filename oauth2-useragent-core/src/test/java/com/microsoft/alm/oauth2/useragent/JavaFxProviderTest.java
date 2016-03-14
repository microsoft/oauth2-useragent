// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class JavaFxProviderTest {

    private File fakeJarFile;
    private File nonExistentFile;

    @Before public void createTemporaryFiles() throws IOException {
        fakeJarFile = File.createTempFile("jfxrt", "jar");
        nonExistentFile = File.createTempFile("nowYouSeeMe", "nowYouDont");
        //noinspection ResultOfMethodCallIgnored
        nonExistentFile.delete();
    }

    private void test(final int expectedRequirements, final String version, final File potentialJavaFxJarFile, final String osName, final String osVersionString, final String display) {
        final File[] potentialJavaFxJarFiles = {
            potentialJavaFxJarFile
        };

        final ArrayList<String> actual = JavaFxProvider.checkRequirements(version, potentialJavaFxJarFiles, osName, osVersionString, display);

        Assert.assertEquals(expectedRequirements, actual.size());
    }

    @Test public void oracleJdk6OnWindows() {
        test(2, "1.6.0_20-b02", nonExistentFile, "Windows", null, null);
    }

    @Test public void oracleJdk8OnWindows() {
        test(0, "1.8.0_60-b27", fakeJarFile, "Windows", null, null);
    }

    @Test public void oracleJdk7OnMacOsX() {
        test(0, "1.7.0_71-b14", fakeJarFile, "Mac OS X", "10.10.5", "/private/tmp/com.apple.launchd.X5no1ibGbp/org.macosforge.xquartz:0");
    }

    @Test public void oracleJava7OnMacOsXElCapitan() {
        test(1, "1.7.0_71-b14", fakeJarFile, "Mac OS X", "10.11.0", "/private/tmp/com.apple.launchd.X5no1ibGbp/org.macosforge.xquartz:0");
    }

    @Test public void openJdk8OnFedoraViaSsh() {
        test(2, "1.8.0_60-b27", nonExistentFile, "Linux", null, null);
    }

    @Test public void openJdk8OnFedoraViaDesktop() {
        test(1, "1.8.0_60-b27", nonExistentFile, "Linux", null, ":1");
    }
}
