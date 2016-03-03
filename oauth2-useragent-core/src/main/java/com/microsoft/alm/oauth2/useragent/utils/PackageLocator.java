// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.regex.Pattern;

public class PackageLocator {

    private static final Pattern PATTERN_DOT = Pattern.compile("\\.");

    private final ClassPropertyAccessor classPropertyAccessor;

    public PackageLocator() {
        this(new ClassPropertyAccessor());
    }

    PackageLocator(final ClassPropertyAccessor classPropertyAccessor) {
        this.classPropertyAccessor = classPropertyAccessor;
    }

    /**
     * Determines the path to the package that contains the specified class.
     *
     * @param  clazz the class to be located
     * @return a {@link File} representing the archive (jar or zip) or folder that contains this class
     */
    public File locatePackage(final Class clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz was null");
        }

        // Inspired by http://stackoverflow.com/a/12733172
        File classFilePath = getClasspathFromProtectionDomain(clazz);
        if (classFilePath == null) {
            classFilePath = getClasspathFromResource(clazz);
        }
        return classFilePath;
    }

    private File getClasspathFromProtectionDomain(final Class clazz) throws SecurityException {
        ProtectionDomain protectionDomain;
        try {
            protectionDomain = classPropertyAccessor.getProtectionDomain(clazz);
        }
        catch (final SecurityException ignored) {
            return null;
        }
        if (protectionDomain == null) {
            throw new Error("Unable to determine the ProtectionDomain for the specified class");
        }
        final CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) {
            throw new Error("ProtectionDomain returned a null CodeSource");
        }
        final URL resourceUrl = codeSource.getLocation();
        if (resourceUrl == null) {
            return null;
        }

        final String canonicalName = classPropertyAccessor.getCanonicalName(clazz);

        final File result = getClasspathFromUrl(resourceUrl, canonicalName);

        return result;
    }

    private File getClasspathFromResource(final Class clazz) {

        final String name = classPropertyAccessor.getSimpleName(clazz) + ".class";
        final URL resourceUrl = classPropertyAccessor.getResource(clazz, name);
        if (resourceUrl == null) {
            throw new Error("A null resource URL was returned by getResource");
        }

        final String canonicalName = classPropertyAccessor.getCanonicalName(clazz);

        final File result = getClasspathFromUrl(resourceUrl, canonicalName);

        return result;
    }

    static File getClasspathFromUrl(final URL resourceUrl, final String canonicalName) {
        if (resourceUrl == null) {
            throw new IllegalArgumentException("resourceUrl must not be null");
        }
        if (canonicalName == null) {
            throw new IllegalArgumentException("canonicalName must not be null");
        }

        String resourcePath;
        try {
            final URI resourceUri = resourceUrl.toURI();
            final URI schemeStrippedUri = stripSchemes(resourceUri);
            resourcePath = schemeStrippedUri.getPath();
        }
        catch (final URISyntaxException ignored) {
            // inspired by Kohsuke's blog post:
            // https://community.oracle.com/blogs/kohsuke/2007/04/25/how-convert-javaneturl-javaiofile
            resourcePath = resourceUrl.getPath();
        }

        final String slashedName = PATTERN_DOT.matcher(canonicalName).replaceAll("/");
        final String pathToClassFile = "/" + slashedName + ".class";
        final int lastIndexOfClassName = resourcePath.lastIndexOf(pathToClassFile);

        final File result;
        if (lastIndexOfClassName > 0) {
            // remove the trailing ! as well
            final int mark = resourcePath.charAt(lastIndexOfClassName - 1) == '!'
                    ? lastIndexOfClassName - 1
                    : lastIndexOfClassName;
            final String resourcePathMinusSuffix = resourcePath.substring(0, mark);
            result = new File(resourcePathMinusSuffix);
        }
        else {
            result = new File(resourcePath);
        }

        return result;
    }

    /**
     * Removes all the schemes from the specified {@link URI}.
     *
     * For example, given {@code jar:file:/PATH/xyz.jar!/classpath.class}, the output should be
     * {@code /PATH/xyz.jar!/classpath.class}.
     *
     * @param uri a {@link URI} which has zero or more schemes
     * @return a {@link URI} which has zero schemes
     */
    static URI stripSchemes(final URI uri) {
        URI schemeStrippedUri = uri;
        while (schemeStrippedUri.getScheme() != null) {
            schemeStrippedUri = URI.create(schemeStrippedUri.getRawSchemeSpecificPart());
        }
        return schemeStrippedUri;
    }

    static class ClassPropertyAccessor {
        public ProtectionDomain getProtectionDomain(final Class clazz) throws SecurityException {
            return clazz.getProtectionDomain();
        }

        public URL getResource(final Class clazz, final String resourceName) {
            return clazz.getResource(resourceName);
        }

        public String getCanonicalName(final Class clazz) {
            return clazz.getCanonicalName();
        }

        public String getSimpleName(final Class clazz) {
            return clazz.getSimpleName();
        }
    }
}
