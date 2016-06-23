// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Integration test with a local server.
 */
public class AppTest {

    private static final String PROTOCOL = "http";
    private static final InetSocketAddress ALL_INTERFACES_AUTOMATIC_PORT =
        new InetSocketAddress("0.0.0.0" /* all interfaces */, 0 /* automatic port */);

    @Rule public WireMockRule wireMockRule = new WireMockRule(0);

    private final LoggingFiltersSourceAdapter adapter = new LoggingFiltersSourceAdapter();

    private Properties oldProperties;
    private String localHostName;
    private int wireMockPort;
    private HttpProxyServer proxyServer;
    private String proxyPort;

    @Before
    public void setUp() throws Exception {
        adapter.reset();
        proxyServer = DefaultHttpProxyServer
            .bootstrap()
            .withAddress(ALL_INTERFACES_AUTOMATIC_PORT)
            .withFiltersSource(adapter)
            .start();
        final InetSocketAddress proxyAddress = proxyServer.getListenAddress();
        proxyPort = Integer.toString(proxyAddress.getPort(), 10);

        oldProperties = System.getProperties();
        final Properties tempProperties = new Properties(oldProperties);
        System.setProperties(tempProperties);
        final InetAddress localHostAddress = InetAddress.getLocalHost();
        localHostName = localHostAddress.getHostName();
        wireMockPort = wireMockRule.port();
    }

    @After
    public void tearDown() throws Exception {
        System.setProperties(oldProperties);
        proxyServer.stop();
    }

    @Category(IntegrationTests.class)
    @Test public void main_wiremock() throws URISyntaxException, AuthorizationException, UnknownHostException {
        final URI authorizationEndpoint = new URI(PROTOCOL, null, localHostName, wireMockPort, "/oauth2/authorize", "response_type=code&client_id=main_wiremock&state=chicken", null);
        final URI authorizationConfirmation = new URI(PROTOCOL, null, localHostName, wireMockPort, "/oauth2/confirm", "state=chicken", null);
        final String redirectingBody = String.format("<html><head><meta http-equiv='refresh' content='1; url=%1$s'></head><body>Redirecting to %1$s...</body></html>", authorizationConfirmation.toString());
        final URI redirectUri = new URI(PROTOCOL, null, localHostName, wireMockPort, "/finished", "code=steak&state=chicken", null);
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

        try {
            App.main(args);
        }
        catch (final AuthorizationException e) {
            Assert.fail(e.getMessage() + UserAgentImpl.NEW_LINE + e.getDescription());
        }

        Assert.assertEquals("steak", App.code);
        Assert.assertEquals("chicken", App.state);

    }

    @Category(IntegrationTests.class)
    @Test public void main_withProxyServerEnabled() throws URISyntaxException, AuthorizationException, UnknownHostException {

        final Properties tempProperties = new Properties(oldProperties);
        tempProperties.setProperty("http.proxyHost", localHostName);
        tempProperties.setProperty("http.proxyPort", proxyPort);
        System.setProperties(tempProperties);

        main_wiremock();

        Assert.assertTrue(adapter.proxyWasUsed());
    }

    @Category(IntegrationTests.class)
    @Test public void main_withProxyServerTunnellingTLS() throws URISyntaxException, AuthorizationException, UnknownHostException {

        final Properties tempProperties = new Properties(oldProperties);
        tempProperties.setProperty("https.proxyHost", localHostName);
        tempProperties.setProperty("http.proxyHost", localHostName);
        tempProperties.setProperty("https.proxyPort", proxyPort);
        tempProperties.setProperty("http.proxyPort", proxyPort);
        System.setProperties(tempProperties);

        final String[] args = {"https://visualstudio.com", "https://www.visualstudio.com"};

        boolean exceptionWasThrown = false;
        try {
            App.main(args);
        }
        catch (final AuthorizationException ignored) {
            // we can't distinguish failure to connect from a successful redirect!
            exceptionWasThrown = true;
        }

        Assert.assertTrue(exceptionWasThrown);
        Assert.assertTrue(adapter.proxyWasUsed());
    }
}
