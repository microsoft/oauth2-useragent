// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent.utils;

import com.microsoft.alm.oauth2.useragent.Provider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PackageLocatorTest {

    private Class testClazz;
    private PackageLocator.ClassPropertyAccessor classPropertyAccessorMock;
    private PackageLocator underTest;

    @Before public void setup() {
        testClazz = this.getClass();

        classPropertyAccessorMock = mock(PackageLocator.ClassPropertyAccessor.class);
        when(classPropertyAccessorMock.getSimpleName(testClazz)).thenReturn(testClazz.getSimpleName());

        underTest = new PackageLocator(classPropertyAccessorMock);
    }

    @Test public void stripSchemes_zeroSchemes() throws Exception {
        final URI uri = URI.create("/PATH.xyz.jar");

        final URI actual = PackageLocator.stripSchemes(uri);

        Assert.assertEquals(URI.create("/PATH.xyz.jar"), actual);
    }

    @Test public void stripSchemes_oneScheme() throws Exception {
        final URI uri = URI.create("file:/PATH.xyz.jar");

        final URI actual = PackageLocator.stripSchemes(uri);

        Assert.assertEquals(URI.create("/PATH.xyz.jar"), actual);
    }

    @Test public void stripSchemes_oneSchemeWithJarBoundary() throws Exception {
        final URI uri = URI.create("file:/PATH.xyz.jar!/classpath.class");

        final URI actual = PackageLocator.stripSchemes(uri);

        Assert.assertEquals(URI.create("/PATH.xyz.jar!/classpath.class"), actual);
    }

    @Test public void stripSchemes_twoSchemes() throws Exception {
        final URI uri = URI.create("jar:file:/PATH.xyz.jar!/classpath.class");

        final URI actual = PackageLocator.stripSchemes(uri);

        Assert.assertEquals(URI.create("/PATH.xyz.jar!/classpath.class"), actual);
    }

    /**
     * IntelliJ IDEA plugins return {@code null} when asked for {@link CodeSource#getLocation()}
     */
    @Test public void locatePackage_fallbackOnNullCodeSourceLocation() throws Exception {
        final URL url
                = new URL("jar:file:/abc/def.jar!/com/microsoft/alm/oauth2/useragent/utils/PackageLocatorTest.class");
        final CodeSource codesource = new CodeSource(null, (Certificate[])null);
        final ProtectionDomain protectionDomain = new ProtectionDomain(codesource, null);

        when(classPropertyAccessorMock.getProtectionDomain(testClazz)).thenReturn(protectionDomain);
        when(classPropertyAccessorMock.getResource(testClazz, testClazz.getSimpleName() + ".class")).thenReturn(url);
        when(classPropertyAccessorMock.getCanonicalName(testClazz)).thenReturn(testClazz.getCanonicalName());

        final File actual = underTest.locatePackage(testClazz);

        assertEquals("/abc/def.jar", actual);

    }

    @Test public void locatePackage_fallbackToJarResourceOnSecurityException() throws Exception {
        final URL url
                = new URL("jar:file:/abc/def.jar!/com/microsoft/alm/oauth2/useragent/utils/PackageLocatorTest.class");

        when(classPropertyAccessorMock.getProtectionDomain(testClazz)).thenThrow(new SecurityException());
        when(classPropertyAccessorMock.getResource(testClazz, testClazz.getSimpleName() + ".class")).thenReturn(url);
        when(classPropertyAccessorMock.getCanonicalName(testClazz)).thenReturn(testClazz.getCanonicalName());

        final File actual = underTest.locatePackage(testClazz);

        assertEquals("/abc/def.jar", actual);
    }

    @Test public void locatePackage_fallbackToWindowsResourcePathOnSecurityException() throws Exception {
        final URL url
                = new URL("file:/E:/windowsPath/Test/com/microsoft/alm/oauth2/useragent/utils/PackageLocatorTest.class");

        when(classPropertyAccessorMock.getProtectionDomain(testClazz)).thenThrow(new SecurityException());
        when(classPropertyAccessorMock.getResource(testClazz, testClazz.getSimpleName() + ".class")).thenReturn(url);
        when(classPropertyAccessorMock.getCanonicalName(testClazz)).thenReturn(testClazz.getCanonicalName());

        final File actual = underTest.locatePackage(testClazz);

        assertEquals("/E:/windowsPath/Test", actual);
    }

    @Test public void locatePackage_favorProtectionDomain() throws Exception {
        when(classPropertyAccessorMock.getProtectionDomain(testClazz)).thenReturn(testClazz.getProtectionDomain());
        when(classPropertyAccessorMock.getCanonicalName(testClazz)).thenReturn(testClazz.getCanonicalName());

        final File actual = underTest.locatePackage(testClazz);

        // Unit tests are compiled into a "test-classes" folder
        Assert.assertTrue(actual.getAbsolutePath().endsWith("test-classes"));
    }

    @Test public void getClasspathFromUrl_doesNotContainClass() throws Exception {
        final URL url
                = new URL("file:/E:/a%20b/c%26d/");
        final String canonicalName = "com.microsoft.alm.oauth2.useragent.utils.PackageLocatorTest";

        final File actual = PackageLocator.getClasspathFromUrl(url, canonicalName);

        assertEquals("/E:/a b/c&d", actual);
    }

    @Test public void getClasspathFromUrl_encodedCharactersInURL() throws Exception {
        final URL url
            = new URL("file:/E:/a%20b/c%26d/com/microsoft/alm/oauth2/useragent/utils/PackageLocatorTest.class");
        final String canonicalName = "com.microsoft.alm.oauth2.useragent.utils.PackageLocatorTest";

        final File actual = PackageLocator.getClasspathFromUrl(url, canonicalName);

        assertEquals("/E:/a b/c&d", actual);
    }

    @Test public void getClasspathFromUrl_spaceAndPlusInURL() throws Exception {
        final URL url
            = new URL("file:///c:/Documents and+Settings/User/com/microsoft/alm/oauth2/useragent/utils/PackageLocatorTest.class");
        final String canonicalName = "com.microsoft.alm.oauth2.useragent.utils.PackageLocatorTest";

        final File actual = PackageLocator.getClasspathFromUrl(url, canonicalName);

        assertEquals("/c:/Documents and+Settings/User", actual);
    }

    private void assertEquals(final String expectedPath, final File actual) {
        final String expectedOsSpecificPath = toOSSpecific(expectedPath);
        final String actualPath = actual.getAbsolutePath();

        Assert.assertEquals(expectedOsSpecificPath, actualPath);
    }

    private String toOSSpecific(final String unixFormattedName) {
        String massaged = unixFormattedName;

        if (Provider.isWindows(System.getProperty("os.name"))) {
            // On windows, we need to remove the starting / and convert slashes
            if (massaged.startsWith("/")) {
                massaged = massaged.substring(1); // remove starting /
            }

            // Add the current drive letter if there is no drive
            if (massaged.charAt(1) != ':') {
                massaged = new File("/").getAbsolutePath() + massaged;
            }

            return massaged.replace("/", "\\");

        }

        return unixFormattedName;
    }
}
