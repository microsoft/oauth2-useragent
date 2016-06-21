// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import com.microsoft.alm.oauth2.useragent.utils.PackageLocator;
import org.eclipse.swt.SWT;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StandardWidgetToolkitProviderTest {

    private PackageLocator packageLocator = new PackageLocator();

    @Before
    public void setUp() throws Exception {
        StandardWidgetToolkitProvider.PACKAGE_LOCATOR_OVERRIDE = null;
        System.setProperty(StandardWidgetToolkitProvider.SWT_RUNTIME_JAR_OVERRIDE, "");
    }

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
    public void swtRuntimeRequirementMet_checkPropertyFirst() throws IOException {
        final File swtFromProperty = File.createTempFile("propertyPointedSwtJar", "unitTest");
        final File swtFromClasspath = packageLocator.locatePackage(SWT.class);
        final File swtDefaultJar = File.createTempFile("defaultFakeSwtJar", "unitTest");

        System.setProperty(StandardWidgetToolkitProvider.SWT_RUNTIME_JAR_OVERRIDE, swtFromProperty.getAbsolutePath());

        final File swtRuntime = StandardWidgetToolkitProvider.getSwtRuntimeJar(swtDefaultJar);
        assertNotNull(swtRuntime);
        assertEquals(swtFromProperty.getAbsolutePath(), swtRuntime.getAbsolutePath());
    }

    @Test
    public void swtRuntimeRequirementMet_loadFromClasspathBeforeGetDefault() throws IOException {
        final File swtFromClasspath = packageLocator.locatePackage(SWT.class);
        final File swtDefaultJar = File.createTempFile("defaultFakeSwtJar", "unitTest");

        final File swtRuntime = StandardWidgetToolkitProvider.getSwtRuntimeJar(swtDefaultJar);
        assertNotNull(swtRuntime);
        assertEquals(swtFromClasspath.getAbsolutePath(), swtRuntime.getAbsolutePath());
    }

    @Test
    public void swtRuntimeRequirementMet_loadDefaultSwtJarIfExists() throws IOException {
        final PackageLocator locatorMock = mock(PackageLocator.class);
        when(locatorMock.locatePackage(SWT.class)).thenThrow(new NoClassDefFoundError());

        StandardWidgetToolkitProvider.PACKAGE_LOCATOR_OVERRIDE = locatorMock;

        final File swtJar = File.createTempFile("eclipseSwtProviderExists", "unitTest");

        File actualJarFile = StandardWidgetToolkitProvider.getSwtRuntimeJar(swtJar);
        assertNotNull(actualJarFile);
        assertEquals(swtJar.getAbsolutePath(), actualJarFile.getAbsolutePath());

        //noinspection ResultOfMethodCallIgnored
        swtJar.delete();
        actualJarFile = StandardWidgetToolkitProvider.getSwtRuntimeJar(swtJar);
        assertNull(actualJarFile);
    }
}
