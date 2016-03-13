// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
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

    @Rule public WireMockRule wireMockRule = new WireMockRule(0);

    @Category(IntegrationTests.class)
    @Test public void main_wiremock() throws URISyntaxException, AuthorizationException, UnknownHostException {
        final int port = wireMockRule.port();
        final InetAddress localHostAddress = InetAddress.getLocalHost();
        final String host = localHostAddress.getHostName();
        final URI authorizationEndpoint = new URI(PROTOCOL, null, host, port, "/oauth2/authorize", "response_type=code&client_id=main_wiremock&state=chicken", null);
        final URI authorizationConfirmation = new URI(PROTOCOL, null, host, port, "/oauth2/confirm", "state=chicken", null);
        final String redirectingBody = String.format("<html><head><meta http-equiv='refresh' content='1; url=%1$s'></head><body>Redirecting to %1$s...</body></html>", authorizationConfirmation.toString());
        final URI redirectUri = new URI(PROTOCOL, null, host, port, "/finished", "code=steak&state=chicken", null);
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
        final Properties oldProperties = System.getProperties();
        final LoggingFiltersSourceAdapter adapter = new LoggingFiltersSourceAdapter();

        final String listenAddress = "0.0.0.0" /* all interfaces */;
        final int listenPort = 0 /* automatic port */;
        final InetSocketAddress requestedAddress = new InetSocketAddress(listenAddress, listenPort);
        final HttpProxyServer proxyServer =
            DefaultHttpProxyServer
                .bootstrap()
                .withAddress(requestedAddress)
                .withFiltersSource(adapter)
                .start();

        try {
            final Properties tempProperties = new Properties(oldProperties);
            final InetAddress localHost = InetAddress.getLocalHost();
            tempProperties.setProperty("http.proxyHost", localHost.getHostName());
            final InetSocketAddress proxyAddress = proxyServer.getListenAddress();
            tempProperties.setProperty("http.proxyPort", Integer.toString(proxyAddress.getPort(), 10));
            System.setProperties(tempProperties);

            main_wiremock();

            Assert.assertTrue(adapter.proxyWasUsed());
        }
        finally {
            proxyServer.stop();
            System.setProperties(oldProperties);
        }
    }

    @Category(IntegrationTests.class)
    @Test public void main_withProxyServerTunnellingTLS() throws URISyntaxException, AuthorizationException, UnknownHostException {
        final Properties oldProperties = System.getProperties();
        final LoggingFiltersSourceAdapter adapter = new LoggingFiltersSourceAdapter();

        final String listenAddress = "0.0.0.0" /* all interfaces */;
        final int listenPort = 0 /* automatic port */;
        final InetSocketAddress requestedAddress = new InetSocketAddress(listenAddress, listenPort);
        final HttpProxyServer proxyServer =
                DefaultHttpProxyServer
                        .bootstrap()
                        .withAddress(requestedAddress)
                        .withFiltersSource(adapter)
                        .start();

        try {
            final Properties tempProperties = new Properties(oldProperties);
            final InetAddress localHost = InetAddress.getLocalHost();
            tempProperties.setProperty("https.proxyHost", localHost.getHostName());
            tempProperties.setProperty("http.proxyHost", localHost.getHostName());
            final InetSocketAddress proxyAddress = proxyServer.getListenAddress();
            final String proxyPort = Integer.toString(proxyAddress.getPort(), 10);
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
        finally {
            proxyServer.stop();
            System.setProperties(oldProperties);
        }
    }
}
