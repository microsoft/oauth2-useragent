// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import com.microsoft.alm.oauth2.useragent.utils.PackageLocator;
import com.microsoft.alm.oauth2.useragent.utils.StringHelper;
import org.eclipse.swt.SWT;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StandardWidgetToolkitProvider extends Provider {

    public static final String SWT_RUNTIME_JAR_OVERRIDE = "SWT_RUNTIME_JAR_PATH";

    private static final File SWT_HOME = new File(USER_HOME, ".swt");
    private static final String SWT_JAR_NAME = getSwtRuntimeJarName();

    private static final File DEFAULT_SWT_RUNTIME_JAR = new File(SWT_HOME, SWT_JAR_NAME);

    private static final String JAVA_VERSION_REQUIREMENT = "Oracle Java SE or OpenJDK, version 6 and higher";
    private static final String ECLIPSE_SWT_RUNTIME_REQUIREMENT = "Standard Widget Toolkit Runtime at " +
            DEFAULT_SWT_RUNTIME_JAR;

    private static final String GUI_DESKTOP_ENVIRONMENT_REQUIREMENT = "A desktop environment.";

    private static final PackageLocator packageLocator = new PackageLocator();

    static PackageLocator PACKAGE_LOCATOR_OVERRIDE = null;

    static String getSwtRuntimeJarName() {
        final String swtRuntimeFormatter = "swt-%s.jar";
        final String swtArch = OS_ARCH.contains("64") ? "x86_64" : "x86";
        return String.format(swtRuntimeFormatter, swtArch);
    }

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

        if (getSwtRuntimeJar(DEFAULT_SWT_RUNTIME_JAR) == null) {
            requirements.add(ECLIPSE_SWT_RUNTIME_REQUIREMENT);
        }

        if (!hasDesktop(OS_NAME, ENV_DISPLAY)) {
            requirements.add(GUI_DESKTOP_ENVIRONMENT_REQUIREMENT);
        }

        return requirements;
    }

    @Override
    public void augmentProcessParameters(final List<String> command, final List<String> classPath) {
        final File swtJar = getSwtRuntimeJar(DEFAULT_SWT_RUNTIME_JAR);
        if (swtJar != null) {
            classPath.add(swtJar.getAbsolutePath());
        }

        relayProperties(command);

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

    static File getSwtRuntimeJar(final File defaultRuntimeJar) {
        // First we check for SWT override property
        File swtRuntime = null;
        final String overrideSwtJarPath = System.getProperty(SWT_RUNTIME_JAR_OVERRIDE);
        if (!StringHelper.isNullOrWhiteSpace(overrideSwtJarPath)) {
            final File overrideSwtJar = new File(overrideSwtJarPath);
            if (overrideSwtJar.isFile()) {
                swtRuntime = overrideSwtJar;
            }
        }

        // Then check for current classpath and try to locate SWT jar
        if (swtRuntime == null) {
            final PackageLocator locator = PACKAGE_LOCATOR_OVERRIDE == null
                    ? packageLocator
                    : PACKAGE_LOCATOR_OVERRIDE;
            try {
                final File swtClasspathJar = locator.locatePackage(SWT.class);
                swtRuntime = swtClasspathJar;
            } catch (NoClassDefFoundError e){
                //ignored, no SWT jar on classpath
            }
        }

        // Lastly fallback to default jar
        if (swtRuntime == null && defaultRuntimeJar.isFile()) {
            swtRuntime = defaultRuntimeJar;
        }

        return swtRuntime;
    }

    static void relayProperties(final List<String> command) {
        // https://www.eclipse.org/swt/faq.php#browserproxy
        if (isLinux(OS_NAME)) {
            relayNetworkProperties(command);
        }

        // https://www.eclipse.org/swt/faq.php#specifyxulrunner
        relayProperty(command, "org.eclipse.swt.browser.XULRunnerPath");

        // https://www.eclipse.org/swt/faq.php#specifyprofile
        relayProperty(command, "org.eclipse.swt.browser.MOZ_PROFILE_PATH");
    }

    static void relayNetworkProperties(final List<String> command) {
        // favor https proxy setting over http proxy setting, check for
        // regular Java property first so user don't have to change
        // their setting just for SWT
        final List<String> httpProxyHostProps =
                Arrays.asList("https.proxyHost", "http.proxyHost", "network.proxy_host");
        final String proxyHost = getFirstSetProperty(httpProxyHostProps);
        if (!StringHelper.isNullOrEmpty(proxyHost)) {
            command.add("-Dnetwork.proxy_host=" + proxyHost);
        }

        final List<String> httpProxyPortProps =
                Arrays.asList("https.proxyPort", "http.proxyPort", "network.proxy_port");
        final String proxyPort = getFirstSetProperty(httpProxyPortProps);
        if (!StringHelper.isNullOrEmpty(proxyPort)) {
            command.add("-Dnetwork.proxy_port=" + proxyPort);
        }
    }

    static void relayProperty(final List<String> command, final String propertyName) {
        final String value = System.getProperty(propertyName);
        if (!StringHelper.isNullOrEmpty(value)) {
            command.add(String.format("-D%s=%s", propertyName, value));
        }
    }

    static String getFirstSetProperty(final List<String> potentials) {
        String property = null;
        for (final String name : potentials) {
            final String value = System.getProperty(name);
            if (!StringHelper.isNullOrWhiteSpace(value)) {
                property = value;
                break;
            }
        }

        return property;
    }

}
