// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StandardWidgetToolkitProvider extends Provider {
    private static final String JAVA_VERSION_REQUIREMENT = "Oracle Java SE or OpenJDK, version 6 and higher";
    private static final String ECLIPSE_SWT_RUNTIME_REQUIREMENT = "Standard Widget Toolkit Runtime JAR in $(HOME)/.swt/swt.jar.";
    private static final String GUI_DESKTOP_ENVIRONMENT_REQUIREMENT = "A desktop environment.";

    private static final File[] potentialSwtJarLocations = new File[]{
            new File(USER_HOME, ".swt/swt.jar"),
            new File(JAVA_IO_TMPDIR, "swt.jar"),
    };

    protected StandardWidgetToolkitProvider() {
        super("StandardWidgetToolkit");
    }

    @Override
    public List<String> checkRequirements() {
        final List<String> requirements = new ArrayList<String>();
        final Version javaVersion = Version.parseJavaRuntimeVersion(JAVA_RUNTIME_VERSION);
        if (!isJavaVersion6AndUp(javaVersion)) {
            requirements.add(JAVA_VERSION_REQUIREMENT);
        }

        if (getSwtRuntimeJar(potentialSwtJarLocations) == null) {
            requirements.add(ECLIPSE_SWT_RUNTIME_REQUIREMENT);
        }

        if (!hasDesktop(OS_NAME, ENV_DISPLAY)) {
            requirements.add(GUI_DESKTOP_ENVIRONMENT_REQUIREMENT);
        }

        return requirements;
    }

    @Override
    public void augmentProcessParameters(final List<String> command, final List<String> classPath) {
        final File swtJar = getSwtRuntimeJar(potentialSwtJarLocations);
        if (swtJar != null) {
            classPath.add(swtJar.getAbsolutePath());
        }

        if (isMac(OS_NAME)) {
            command.add("-XstartOnFirstThread");
        }
    }

    static boolean isJavaVersion6AndUp(final Version javaVersion) {
        final int javaMajorVersion = javaVersion.getMajor();
        final int javaMinorVersion = javaVersion.getMinor();

        return javaMajorVersion > 1
                || (javaMajorVersion == 1 && javaMinorVersion >= 6);
    }

    static File getSwtRuntimeJar(final File[] possibleSwtJars) {
        for (final File potentialSwtJar : possibleSwtJars) {
            if (potentialSwtJar.isFile()) {
                return potentialSwtJar;
            }
        }

        return null;
    }
}
