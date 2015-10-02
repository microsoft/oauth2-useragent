// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class UserAgentImpl implements UserAgent {

    static final String REQUEST_AUTHORIZATION_CODE = "requestAuthorizationCode";

    @Override
    public AuthorizationResponse requestAuthorizationCode(final URI authorizationEndpoint, final URI redirectUri)
            throws AuthorizationException {
        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("authorizationEndpoint", authorizationEndpoint.toString());
        parameters.put("redirectUri", redirectUri.toString());
        return encode(REQUEST_AUTHORIZATION_CODE, parameters);
    }

    AuthorizationResponse encode(final String methodName, final Map<String, String> parameters) {
        // assemble args, adding methodName last
        // create mockable process from args
        // emit parameters to process outputStream
        // read result from process inputStream
        // wait for process to exit
        // construct result object from string result
        return null;
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
