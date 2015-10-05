// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Integration test with a local server.
 */
public class AppTest {

    private static final String PROTOCOL = "http";
    private static final String HOST = "localhost";
    private static final int PORT = 8089;

    @Rule public WireMockRule wireMockRule = new WireMockRule(PORT);

    @Category(IntegrationTests.class)
    @Test public void main_wiremock() throws URISyntaxException, AuthorizationException {
        final URI authorizationEndpoint = new URI(PROTOCOL, null, HOST, PORT, "/oauth2/authorize", "response_type=code&client_id=main_wiremock&state=chicken", null);
        final URI authorizationConfirmation = new URI(PROTOCOL, null, HOST, PORT, "/oauth2/confirm", "state=chicken", null);
        final String redirectingBody = String.format("<html><head><meta http-equiv='refresh' content='1; url=%1$s'></head><body>Redirecting to %1$s...</body></html>", authorizationConfirmation.toString());
        final URI redirectUri = new URI(PROTOCOL, null, HOST, PORT, "/finished", "code=steak&state=chicken", null);
        stubFor(get(urlEqualTo(authorizationEndpoint.getPath() + "?" + authorizationEndpoint.getQuery()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBody(redirectingBody)));
        stubFor(get(urlEqualTo(authorizationConfirmation.getPath() + "?" + authorizationConfirmation.getQuery()))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", redirectUri.toString())
                        .withBody(redirectingBody)));
        stubFor(get(urlEqualTo(redirectUri.getPath() + "?" + redirectUri.getQuery()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBody("Access granted, although you shouldn't see this message!")));
        final String[] args = {authorizationEndpoint.toString(), redirectUri.toString()};

        App.main(args);

        Assert.assertEquals("steak", App.code);
        Assert.assertEquals("chicken", App.state);

    }
}
