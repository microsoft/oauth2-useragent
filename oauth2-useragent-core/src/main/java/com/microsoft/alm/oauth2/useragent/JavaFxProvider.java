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
        "A desktop environment.",
        "Oracle Java SE 7 is not supported on Mac OS X 10.11 and greater.  Please upgrade to Java 8."
    ));
    private final File[] potentialJavaFxJarLocations = new File[]{
        new File(JAVA_HOME, "lib/jfxrt.jar"),
        new File(JAVA_HOME, "lib/ext/jfxrt.jar"),
    };

    protected JavaFxProvider() {
        super("JavaFx");
    }

    @Override public List<String> checkRequirements() {

        return checkRequirements(JAVA_RUNTIME_VERSION, potentialJavaFxJarLocations, OS_NAME, OS_VERSION, ENV_DISPLAY);
    }

    static ArrayList<String> checkRequirements(final String javaRuntimeVersionString, final File[] potentialJavaFxJarLocations, final String osName, final String osVersionString, final String displayVariable) {
        final ArrayList<String> requirements = new ArrayList<String>();
        final Version javaVersion = Version.parseJavaRuntimeVersion(javaRuntimeVersionString);
        boolean hasSupportedJava = false;
        // TODO: what about 1.9 or 2.x?
        final int javaMajorVersion = javaVersion.getMajor();
        final int javaMinorVersion = javaVersion.getMinor();
        final int javaUpdateVersion = javaVersion.getUpdate();
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
        else {
            if (isMac(osName)) {
                if (javaMajorVersion == 1 && javaMinorVersion == 7) {
                    final Version osVersion = Version.parseVersion(osVersionString);
                    if (osVersion.getMajor() == 10)
                    {
                        if (osVersion.getMinor() >= 11)
                        {
                            requirements.add(REQUIREMENTS.get(3));
                        }
                    }
                }
            }
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

        if (!hasDesktop(osName, displayVariable)) {
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
