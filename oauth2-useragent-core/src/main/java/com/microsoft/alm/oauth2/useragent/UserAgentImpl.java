// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import com.microsoft.alm.oauth2.useragent.subprocess.DefaultProcessFactory;
import com.microsoft.alm.oauth2.useragent.subprocess.ProcessCoordinator;
import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcess;
import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcessFactory;
import com.microsoft.alm.oauth2.useragent.utils.PackageLocator;
import com.microsoft.alm.oauth2.useragent.utils.StringHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class UserAgentImpl implements UserAgent, ProviderScanner {

    static final String REQUEST_AUTHORIZATION_CODE = "requestAuthorizationCode";
    static final String JAVA_VERSION_STRING = System.getProperty("java.version");
    static final String JAVA_HOME = System.getProperty("java.home");
    static final String PATH_SEPARATOR = System.getProperty("path.separator");
    static final String NEW_LINE = System.getProperty("line.separator");
    static final String UTF_8 = "UTF-8";
    static final String USER_AGENT_PROVIDER_PROPERTY_NAME = "userAgentProvider";

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Set<String> NETWORKING_PROPERTY_NAMES;
    private static final Map<String, String> SAFE_REPLACEMENTS;

    static {
        // https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html
        final Set<String> networkingPropertyNames = new HashSet<String>();
        // HTTP
        networkingPropertyNames.add("http.proxyHost");
        networkingPropertyNames.add("http.proxyPort");
        networkingPropertyNames.add("http.nonProxyHosts");

        // HTTPS
        networkingPropertyNames.add("https.proxyHost");
        networkingPropertyNames.add("https.proxyPort");

        // SOCKS
        networkingPropertyNames.add("socksProxyHost");
        networkingPropertyNames.add("socksProxyPort");
        networkingPropertyNames.add("socksProxyVersion");
        networkingPropertyNames.add("java.net.socks.username");
        networkingPropertyNames.add("java.net.socks.password");

        networkingPropertyNames.add("java.net.useSystemProxies");

        // Misc HTTP properties
        networkingPropertyNames.add("http.auth.ntlm.domain");
        NETWORKING_PROPERTY_NAMES = Collections.unmodifiableSet(networkingPropertyNames);

        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("+", " ");
        map.put("%28", "(");
        map.put("%29", ")");
        map.put("%2F", "/");
        map.put("%3A", ":");
        map.put("%3B", ";");
        map.put("%5C", "\\");
        map.put("%7C", "|");
        SAFE_REPLACEMENTS = Collections.unmodifiableMap(map);
    }

    private final TestableProcessFactory processFactory;
    private final List<Provider> candidateProviders;
    private final LinkedHashMap<Provider, List<String>> requirementsByProvider = new LinkedHashMap<Provider, List<String>>();
    private Provider provider;
    private boolean hasScannedAtLeastOnce = false;
    private String userAgentProvider = null;

    public UserAgentImpl() {
        this(new DefaultProcessFactory(), null, Provider.PROVIDERS);
    }

    UserAgentImpl(final TestableProcessFactory processFactory, final Provider provider, final List<Provider> candidateProviders) {
        this.processFactory = processFactory;
        this.candidateProviders = candidateProviders;
        this.provider = provider;
    }

    @Override
    public Provider findCompatibleProvider() {
        final String userAgentProvider = System.getProperty(USER_AGENT_PROVIDER_PROPERTY_NAME);
        return findCompatibleProvider(userAgentProvider, true);
    }

    @Override
    public Provider findCompatibleProvider(final String userAgentProvider) {
        return findCompatibleProvider(userAgentProvider, true);
    }

    Provider findCompatibleProvider(final String userAgentProvider, final boolean checkOverrideIsCompatible) {
        if (provider == null || !StringHelper.equal(this.userAgentProvider, userAgentProvider)) {
            requirementsByProvider.clear();
            provider = scanProviders(userAgentProvider, candidateProviders, requirementsByProvider, checkOverrideIsCompatible);
            hasScannedAtLeastOnce = true;
            this.userAgentProvider = userAgentProvider;
        }
        return provider;
    }

    @Override
    public Map<Provider, List<String>> getUnmetProviderRequirements() {
        if (!hasScannedAtLeastOnce) {
            findCompatibleProvider();
        }

        final Map<Provider, List<String>> copy = new LinkedHashMap<Provider, List<String>>(requirementsByProvider);
        final Map<Provider, List<String>> result = Collections.unmodifiableMap(copy);
        return result;
    }

    @Override
    public boolean hasCompatibleProvider() {
        findCompatibleProvider();
        return provider != null;
    }

    @Override
    public boolean hasCompatibleProvider(final String userAgentProvider) {
        findCompatibleProvider(userAgentProvider);
        return provider != null;
    }

    @Override
    public AuthorizationResponse requestAuthorizationCode(final URI authorizationEndpoint, final URI redirectUri)
            throws AuthorizationException {
        return encode(REQUEST_AUTHORIZATION_CODE, authorizationEndpoint.toString(), redirectUri.toString());
    }

    AuthorizationResponse encode(final String methodName, final String... parameters)
            throws AuthorizationException {
        final ArrayList<String> command = new ArrayList<String>();
        final ArrayList<String> classPath = new ArrayList<String>();
        // TODO: should we append ".exe" on Windows?
        command.add(new File(JAVA_HOME, "bin/java").getAbsolutePath());

        command.add("-Djava.protocol.handler.pkgs=com.microsoft.alm.oauth2.useragent");

        final String userAgentProvider = System.getProperty(USER_AGENT_PROVIDER_PROPERTY_NAME);
        findCompatibleProvider(userAgentProvider, false);
        if (provider == null) {
            throwUnsupported(requirementsByProvider);
        }
        provider.augmentProcessParameters(command, classPath);

        relayProperties(System.getProperties(), NETWORKING_PROPERTY_NAMES, command);

        // Locate our class to add it to the classPath
        final PackageLocator locator = new PackageLocator();
        final File classPathEntryFile = locator.locatePackage(UserAgentImpl.class);
        final String classPathEntry = classPathEntryFile.getAbsolutePath();
        classPath.add(classPathEntry);

        addClassPathToCommand(classPath, command, PATH_SEPARATOR);
        command.add("com.microsoft.alm.oauth2.useragent." + provider.getClassName());
        command.add(methodName);
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        final String[] args = command.toArray(EMPTY_STRING_ARRAY);

        try {
            final TestableProcess process = processFactory.create(args);
            final ProcessCoordinator coordinator = new ProcessCoordinator(process);
            for (final String parameter : parameters) {
                coordinator.println(parameter);
            }
            coordinator.waitFor();

            final String response = coordinator.getStdOut();
            final String errorContents = coordinator.getStdErr();
            return AuthorizationResponse.fromString(response, errorContents);
        }
        catch (final IOException e) {
            throw new AuthorizationException("io_exception", e.getMessage(), null, e);
        }
        catch (final InterruptedException e) {
            throw new AuthorizationException("interrupted_exception", e.getMessage(), null, e);
        }
    }

    static void relayProperties(final Properties properties, final Set<String> propertyNames, final List<String> destinationCommand) {
        for (final String propertyName : propertyNames) {
            final String propertyValue = properties.getProperty(propertyName);
            if (propertyValue != null) {
                destinationCommand.add("-D" + propertyName + "=" + propertyValue);
            }
        }
    }

    void addClassPathToCommand(final List<String> classPath, final List<String> command, final String pathSeparator) {
        command.add("-classpath");
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        final String[] classPathComponents = classPath.toArray(EMPTY_STRING_ARRAY);
        final String classPathString = StringHelper.join(pathSeparator, classPathComponents);
        command.add(classPathString);
    }

    static void throwUnsupported(final Map<Provider, List<String>> unmetRequirements) {
        final StringBuilder sb = buildUnsupportedMessage(unmetRequirements);

        throw new IllegalStateException(sb.toString());
    }

    public static StringBuilder buildUnsupportedMessage(Map<Provider, List<String>> unmetRequirements) {
        final StringBuilder sb = new StringBuilder("I don't support your platform yet.");
        describeUnmetRequirements(unmetRequirements, sb);
        sb.append(NEW_LINE);
        sb.append("Please send details about your operating system version, Java version, 32- vs. 64-bit, etc.");
        sb.append(NEW_LINE);
        sb.append("The following System Properties and Environment Variables would be very useful.");
        sb.append(NEW_LINE);

        final Properties properties = System.getProperties();
        appendProperties(properties, sb);
        sb.append(NEW_LINE);

        final Map<String, String> variables = System.getenv();
        appendVariables(variables, sb);
        return sb;
    }

    static Provider scanProviders(final String userAgentProvider, final List<Provider> providers, final Map<Provider, List<String>> destinationUnmetRequirements, final boolean checkOverrideIsCompatible) {

        Provider result = null;

        if (userAgentProvider != null) {
            for (final Provider provider : providers) {
                if (provider.getClassName().equals(userAgentProvider)) {
                    if (checkOverrideIsCompatible) {
                        final List<String> requirements = provider.checkRequirements();
                        if (requirements == null || requirements.size() == 0) {
                            result = provider;
                        }
                    }
                    else {
                        result = provider;
                    }
                    break;
                }
            }
        }

        for (final Provider provider : providers) {
            final List<String> requirements = provider.checkRequirements();
            if (requirements == null || requirements.size() == 0) {
                if (result == null) {
                    result = provider;
                }
            }
            else {
                destinationUnmetRequirements.put(provider, requirements);
            }
        }

        return result;
    }

    public static void describeUnmetRequirements(final Map<Provider, List<String>> unmetRequirements, final StringBuilder destination) {
        for (final Map.Entry<Provider, List<String>> pair: unmetRequirements.entrySet()) {
            final Provider provider = pair.getKey();
            final List<String> requirements = pair.getValue();
            if (requirements != null && requirements.size() > 0) {
                destination.append(NEW_LINE);
                destination.append("Unmet requirements for the '").append(provider.getClassName()).append("' provider:").append(NEW_LINE);
                for (final String requirement : requirements) {
                    destination.append(" - ").append(requirement).append(NEW_LINE);
                }
            }
        }
    }

    static void appendProperties(final Properties properties, final StringBuilder destination) {
        final String header = "# --- BEGIN SYSTEM PROPERTIES ---";
        final String footer = "# ---- END SYSTEM PROPERTIES ----";
        final Set<String> keys = properties.stringPropertyNames();

        appendPairs(keys, properties, destination, header, footer);
    }

    static void appendVariables(final Map<String, String> variables, final StringBuilder destination) {
        final String header = "# --- BEGIN ENVIRONMENT VARIABLES ---";
        final String footer = "# ---- END ENVIRONMENT VARIABLES ----";
        final Set<String> keys = variables.keySet();

        appendPairs(keys, variables, destination, header, footer);
    }

    static void appendPairs(final Set<String> keys, final Map pairs, final StringBuilder destination, final String header, final String footer) {
        destination.append(header).append(NEW_LINE).append(NEW_LINE);
        final String[] keyArray = new String[keys.size()];
        keys.toArray(keyArray);
        Arrays.sort(keyArray);
        for (final String key : keyArray) {
            final String encodedKey = sortOfUrlEncode(key);

            final String value = (String) pairs.get(key);
            final String encodedValue = value == null ? "" : sortOfUrlEncode(value);

            destination.append(encodedKey).append('=').append(encodedValue).append(NEW_LINE);
        }
        destination.append(NEW_LINE).append(footer).append(NEW_LINE);
    }

    static String sortOfUrlEncode(final String s) {
        try {
            //noinspection UnnecessaryLocalVariable
            final String encoded = URLEncoder.encode(s, UTF_8);
            // encode() goes too far for our purposes, so undo some common ones for easier raw inspection
            String result = encoded;
            for (final Map.Entry<String, String> entry : SAFE_REPLACEMENTS.entrySet()) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
            return result;
        }
        catch (final UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    static void decode(final UserAgent target, final String[] args, final InputStream inputStream, final OutputStream outputStream) {
        final PrintStream printStream = new PrintStream(outputStream);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        try {
            final String methodName = args[0];

            if (REQUEST_AUTHORIZATION_CODE.equals(methodName)) {
                final URI authorizationEndpoint = new URI(bufferedReader.readLine());
                final URI redirectUri = new URI(bufferedReader.readLine());

                final AuthorizationResponse result = target.requestAuthorizationCode(authorizationEndpoint, redirectUri);

                printStream.println(result.toString());
            }
        }
        catch (final AuthorizationException e) {
            printStream.println(AuthorizationException.toString(e.getCode(), e.getDescription(), e.getUri()));
        }
        catch (final IOException e) {
            printStream.println(AuthorizationException.toString("io_exception", e.getMessage(), null));
        }
        catch (final URISyntaxException e) {
            printStream.println(AuthorizationException.toString("uri_syntax_exception", e.getMessage(), null));
        }
        printStream.flush();
    }

    static String extractResponseFromRedirectUri(final String redirectedUri) {
        final URL uri;
        try {
            uri = new URL(redirectedUri);
            return uri.getQuery();
        } catch (MalformedURLException e) {
            //ignored
        }

        return null;
    }
}
