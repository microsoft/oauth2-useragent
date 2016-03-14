// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Provider {

    static final String JAVA_RUNTIME_VERSION = System.getProperty("java.runtime.version");
    static final String JAVA_VM_NAME = System.getProperty("java.vm.name");
    static final String JAVA_HOME = System.getProperty("java.home");
    static final String OS_NAME = System.getProperty("os.name");
    static final String OS_VERSION = System.getProperty("os.version");
    static final String ENV_DISPLAY = System.getenv("DISPLAY");

    public static final Provider JAVA_FX = new JavaFxProvider();

    public static final List<Provider> PROVIDERS = Collections.unmodifiableList(Arrays.asList(
            JAVA_FX
    ));

    private final String className;

    protected Provider(final String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    /**
     * Compares the specified string against a known value for the Mac OS X operating system.
     *
     * @param osName a string representation of the operating system name,
     *               usually obtained from the {@code os.name} system property.
     *
     * @return {@code true} if the specified string represents the Mac OS X operating system;
     *          {@code false} otherwise
     */
    public static boolean isMac(final String osName) {
        return osName.equals("Mac OS X");
    }

    /**
     * Compares the specified string against a known value for the GNU/Linux family of operating systems.
     *
     * @param osName a string representation of the operating system name,
     *               usually obtained from the {@code os.name} system property.
     *
     * @return {@code true} if the specified string represents a GNU/Linux-based operating system;
     *          {@code false} otherwise
     */
    public static boolean isLinux(final String osName) {
        return osName.equals("Linux");
    }

    /**
     * Compares the specified string against a known value for the Windows family of operating systems.
     *
     * @param osName a string representation of the operating system name,
     *               usually obtained from the {@code os.name} system property.
     *
     * @return {@code true} if the specified string represents a Windows operating system;
     *          {@code false} otherwise
     */
    public static boolean isWindows(final String osName) {
        return osName.startsWith("Windows");
    }

    public abstract List<String> checkRequirements();

    public abstract void augmentProcessParameters(final List<String> command, final List<String> classPath);
}
