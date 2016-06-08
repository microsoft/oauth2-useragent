// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StandardWidgetToolkitProviderTest {

    @Test
    public void jreRequirementMet_WithJava678() throws Exception {
        final Version jre16 = new Version(1, 6, 0, 0, 0);
        boolean javaRequirementMet = StandardWidgetToolkitProvider.isJavaVersion6AndUp(jre16);
        assertTrue(javaRequirementMet);

        final Version jre17 = new Version(1, 7, 45, 0, 0);
        javaRequirementMet = StandardWidgetToolkitProvider.isJavaVersion6AndUp(jre17);
        assertTrue(javaRequirementMet);

        final Version jre18 = new Version(1, 8, 10, 0, 0);
        javaRequirementMet = StandardWidgetToolkitProvider.isJavaVersion6AndUp(jre18);
        assertTrue(javaRequirementMet);
    }

    @Test
    public void jreRequirementMet_WithFutureJavaVersion_9andUp() throws Exception {
        final Version jre9 = new Version(1, 9, 0, 0, 0);
        boolean javaRequirementMet = StandardWidgetToolkitProvider.isJavaVersion6AndUp(jre9);
        assertTrue(javaRequirementMet);

        final Version jre1_10 = new Version(1, 10, 0, 0, 0);
        javaRequirementMet = StandardWidgetToolkitProvider.isJavaVersion6AndUp(jre1_10);
        assertTrue(javaRequirementMet);

        final Version jre2_0 = new Version(2, 0, 0, 0, 0);
        javaRequirementMet = StandardWidgetToolkitProvider.isJavaVersion6AndUp(jre2_0);
        assertTrue(javaRequirementMet);
    }

    @Test
    public void jreRequirementNotMet_WithJre45_or_imaginaryMajorVersion0() {
        final Version jre4 = new Version(1, 4, 0, 0, 0);
        boolean javaRequirementMet = StandardWidgetToolkitProvider.isJavaVersion6AndUp(jre4);
        assertFalse(javaRequirementMet);

        final Version jre5 = new Version(1, 5, 0, 0, 0);
        javaRequirementMet = StandardWidgetToolkitProvider.isJavaVersion6AndUp(jre5);
        assertFalse(javaRequirementMet);

        final Version jre0 = new Version(0, 100, 0, 0, 0);
        javaRequirementMet = StandardWidgetToolkitProvider.isJavaVersion6AndUp(jre0);
        assertFalse(javaRequirementMet);
    }

    @Test
    public void swtRuntimeRequirementMet_ifSwtJarExists() throws IOException {
        final File[] swtJar = new File[] {
            File.createTempFile("eclipseSwtProviderExists", "unitTest")
        };

        final File swtRuntime = StandardWidgetToolkitProvider.getSwtRuntimeJar(swtJar);
        assertNotNull(swtRuntime);
    }

    @Test
    public void swtRuntimeRequirementNotMet_ifSwtJarDoesNotExists() throws IOException {
        final File[] swtJar = new File[] {
            File.createTempFile("eclipseSwtProviderDoesNotExist", "unitTest")
        };
        //noinspection ResultOfMethodCallIgnored
        swtJar[0].delete();

        final File swtRuntime = StandardWidgetToolkitProvider.getSwtRuntimeJar(swtJar);
        assertNull(swtRuntime);
    }
}
