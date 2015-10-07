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

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final TestableProcessFactory processFactory;

    public UserAgentImpl() {
        this(new DefaultProcessFactory());
    }

    UserAgentImpl(final TestableProcessFactory processFactory) {
        this.processFactory = processFactory;
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
        final Provider provider = determineProvider(command, classPath);
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

    static Provider determineProvider(final List<String> command, final List<String> classPath) {

        // TODO: parse into Version-like object so we can do proper greater-than checks
        if (JAVA_VERSION_STRING.startsWith("1.7.0") || JAVA_VERSION_STRING.startsWith("1.8.0")) {
            // TODO: JavaFX only started shipping with 1.7.0 Update 6
            classPath.add(new File(JAVA_HOME, "/lib/jfxrt.jar").getAbsolutePath());
            return Provider.JAVA_FX;
        }
        // TODO: SWT needs the appropriate distribution
        // TODO: SWT on Mac needs to add "-XstartOnFirstThread" to command
        // TODO: SWT on RHEL 5 needs to download xulrunner, point to it and only on 32-bit VM
        throw new IllegalStateException("I don't support your platform yet.  Please send details about your operating system version, Java version, 32- vs. 64-bit, etc.");
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