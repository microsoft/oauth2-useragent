// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class UserAgentImpl implements UserAgent {

    static final String REQUEST_AUTHORIZATION_CODE = "requestAuthorizationCode";
    static final String JAVA_VERSION_STRING = System.getProperty("java.version");
    static final String JAVA_HOME = System.getProperty("java.home");
    static final String PATH_SEPARATOR = System.getProperty("path.separator");
    static final String NEW_LINE = System.getProperty("line.separator");

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final TestableProcessFactory processFactory;
    private final Provider provider;

    public UserAgentImpl() {
        this(new DefaultProcessFactory(), determineProvider(System.getProperty("userAgentProvider")));
    }

    UserAgentImpl(final TestableProcessFactory processFactory, final Provider provider) {
        this.processFactory = processFactory;
        this.provider = provider;
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
        provider.augmentProcessParameters(command, classPath);
        // TODO: is this the best way to add our JAR?
        classPath.add(System.getProperty("java.class.path"));
        addClassPathToCommand(classPath, command, PATH_SEPARATOR);
        command.add("com.microsoft.alm.oauth2.useragent." + provider.getClassName());
        command.add(methodName);
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        final String[] args = command.toArray(EMPTY_STRING_ARRAY);
        try {
            final TestableProcess process = processFactory.create(args);
            final OutputStream outputStream = process.getOutputStream();
            final PrintStream printStream = new PrintStream(outputStream);
            for (final String parameter : parameters) {
                printStream.println(parameter);
            }
            printStream.flush();

            final InputStream inputStream = process.getInputStream();
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            final String response = bufferedReader.readLine();

            process.waitFor();

            return AuthorizationResponse.fromString(response);
        }
        catch (final IOException e) {
            throw new AuthorizationException("io_exception", e.getMessage(), null, e);
        }
        catch (final InterruptedException e) {
            throw new AuthorizationException("interrupted_exception", e.getMessage(), null, e);
        }
    }

    void addClassPathToCommand(final List<String> classPath, final List<String> command, final String pathSeparator) {
        command.add("-classpath");
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        final String[] classPathComponents = classPath.toArray(EMPTY_STRING_ARRAY);
        final String classPathString = join(pathSeparator, classPathComponents);
        command.add(classPathString);
    }

    static String join(final String separator, final String[] value)
    {
        if (value == null)
            throw new IllegalArgumentException("value is null");

        // "If separator is null, an empty string (String.Empty) is used instead."
        final String sep = separator == null ? "" : separator;

        final StringBuilder result = new StringBuilder();

        if (value.length > 0) {
            result.append(value[0] == null ? "" : value[0]);
            for (int i = 1; i < value.length; i++) {
                result.append(sep);
                result.append(value[i] == null ? "" : value[i]);
            }
        }

        return result.toString();
    }

    // TODO: this method could be more testable if it accepted a list of providers as a parameter
    static Provider determineProvider(final String userAgentProvider) {

        if (userAgentProvider != null) {
            for (final Provider provider : Provider.PROVIDERS) {
                if (provider.getClassName().equals(userAgentProvider)) {
                    return provider;
                }
            }
        }
        final StringBuilder sb = new StringBuilder("I don't support your platform yet.  Please send details about your operating system version, Java version, 32- vs. 64-bit, etc.");
        for (final Provider provider : Provider.PROVIDERS) {
            final List<String> requirements = provider.checkRequirements();
            if (requirements.size() == 0) {
                return provider;
            }
            sb.append(NEW_LINE);
            sb.append("Unmet requirements for the '").append(provider.getClassName()).append("' provider:").append(NEW_LINE);
            for (final String requirement : requirements) {
                sb.append(" - ").append(requirement).append(NEW_LINE);
            }
        }
        throw new IllegalStateException(sb.toString());
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
            printStream.println(e.toString());
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
        final URI uri = URI.create(redirectedUri);
        return uri.getQuery();
    }
}
