// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Very simple program demonstrating the use of the oauth2-useragent library.
 */
public class App 
{
    static String code = null;
    static String state;

    public static void main(final String[] args) throws AuthorizationException, URISyntaxException {

        final URI authorizationEndpoint = new URI(args[0]);
        final URI redirectUri = new URI(args[1]);
        final String providerName = args.length >= 3 ? args[2] : null;

        final UserAgent userAgent = new UserAgentImpl();
        if (providerName != null) {
            final ProviderScanner providerScanner = (ProviderScanner) userAgent;
            final Provider provider = providerScanner.findCompatibleProvider(providerName);
            if (provider == null) {
                final String template = "The '%s' provider is not available!";
                final String message = String.format(template, providerName);
                throw new UnsupportedOperationException(message);
            }
        }

        final AuthorizationResponse authorizationResponse = userAgent.requestAuthorizationCode(authorizationEndpoint, redirectUri);

        code = authorizationResponse.getCode();
        state = authorizationResponse.getState();
        System.out.print("Authorization Code: ");
        System.out.println(code);
    }
}
