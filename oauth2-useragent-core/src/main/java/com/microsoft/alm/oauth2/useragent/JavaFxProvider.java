// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class JavaFxProvider extends Provider {

    private static final List<String> REQUIREMENTS = Collections.unmodifiableList(Arrays.asList(
        "Oracle Java SE 7 update 6 or higher, Oracle Java SE 8, OR OpenJDK 8.",
        "JavaFX or OpenJFX runtime JAR.",
        "A desktop environment."
    ));
    private final File[] potentialJavaFxJarLocations = new File[]{
        new File(JAVA_HOME, "/lib/jfxrt.jar"),
        new File(JAVA_HOME, "/lib/ext/jfxrt.jar"),
    };

    protected JavaFxProvider() {
        super("JavaFx");
    }

    @Override public List<String> checkRequirements() {

        return checkRequirements(JAVA_RUNTIME_VERSION, potentialJavaFxJarLocations, OS_NAME, ENV_DISPLAY);
    }

    static ArrayList<String> checkRequirements(final String javaRuntimeVersion, final File[] potentialJavaFxJarLocations, final String osName, final String displayVariable) {
        final ArrayList<String> requirements = new ArrayList<String>();
        final Version version = Version.parseJavaRuntimeVersion(javaRuntimeVersion);
        boolean hasSupportedJava = false;
        // TODO: what about 1.9 or 2.x?
        final int javaMajorVersion = version.getMajor();
        final int javaMinorVersion = version.getMinor();
        final int javaUpdateVersion = version.getUpdate();
        switch (javaMajorVersion) {
            case 1:
                switch (javaMinorVersion) {
                    case 8:
                        hasSupportedJava = true;
                        break;
                    case 7:
                        if (javaUpdateVersion >= 6) {
                            hasSupportedJava = true;
                        }
                        break;
                }
                break;
        }
        if (!hasSupportedJava) {
            requirements.add(REQUIREMENTS.get(0));
        }

        boolean hasJavaFx = false;
        for (final File potentialJavaFxJar : potentialJavaFxJarLocations) {
            if (potentialJavaFxJar.isFile()) {
                hasJavaFx = true;
                break;
            }
        }
        if (!hasJavaFx) {
            requirements.add(REQUIREMENTS.get(1));
        }

        boolean hasDesktop = false;
        if (isWindows(osName)) {
            // TODO: There's still a chance the user is connected via SSH or on Server Core...
            hasDesktop = true;
        }
        else if (isMac(osName)) {
            // TODO: There's still a chance the user is connected via SSH...
            hasDesktop = true;
        }
        else if (isLinux(osName)) {
            if (displayVariable != null) {
                hasDesktop = true;
            }
        }
        if (!hasDesktop) {
            requirements.add(REQUIREMENTS.get(2));
        }

        return requirements;
    }

    @Override public void augmentProcessParameters(final List<String> command, final List<String> classPath) {
        File javaFxJar = null;
        for (final File potentialJavaFxJar : potentialJavaFxJarLocations) {
            if (potentialJavaFxJar.isFile()) {
                javaFxJar = potentialJavaFxJar;
                break;
            }
        }
        if (javaFxJar != null) {
            classPath.add(javaFxJar.getAbsolutePath());
        }
    }
}
