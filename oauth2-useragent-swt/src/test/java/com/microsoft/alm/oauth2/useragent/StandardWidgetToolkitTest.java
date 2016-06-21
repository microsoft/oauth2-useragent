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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;

public class StandardWidgetToolkitTest {

    private static final String HTTP_PROTOCOL = "http";
    private static final String HOST = "localhost";
    private static final String[] args = {UserAgentImpl.REQUEST_AUTHORIZATION_CODE};

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0, 0);

    private final AtomicReference<Exception> expectedExceptionRef = new AtomicReference<Exception>();
    private final AtomicReference<Exception> actualExceptionRef = new AtomicReference<Exception>();

    @Before
    public void setUp() throws Exception {
        StandardWidgetToolkit.RUNNABLE_FACTORY_OVERRIDE = null;
        expectedExceptionRef.set(null);
        actualExceptionRef.set(null);
    }

    @After
    public void tearDown() {
        StandardWidgetToolkit.RUNNABLE_FACTORY_OVERRIDE = null;
        final Exception actualException = this.actualExceptionRef.get();
        //noinspection ThrowableResultOfMethodCallIgnored
        final Exception expectedException = this.expectedExceptionRef.get();
        if (expectedException == null) {
            if (actualException != null) {
                if (actualException instanceof AuthorizationException) {
                    final AuthorizationException authorizationException = (AuthorizationException) actualException;
                    Assert.fail(authorizationException.getMessage() + UserAgentImpl.NEW_LINE + authorizationException.getDescription());
                }
                throw new Error(actualException);
            }
        }
        else {
            if (actualException == null) {
                final String template = "Expected exception '%1$s', got nothing.";
                final String message = String.format(template, expectedException.getClass().toString());
                Assert.fail(message);
            } else {
                assertEquals(expectedException.getClass(), actualException.getClass());
                assertEquals(expectedException.getMessage(), actualException.getMessage());
                if (actualException instanceof AuthorizationException) {
                    final AuthorizationException expectedAuthz = (AuthorizationException) expectedException;
                    final AuthorizationException actualAuthz = (AuthorizationException) actualException;
                    final String prefix = expectedAuthz.getDescription();
                    final String firstPart = actualAuthz.getDescription().substring(0, prefix.length());
                    assertEquals(actualAuthz.getDescription(), prefix, firstPart);
                }
            }
        }
    }

    @Category(IntegrationTests.class)
    @Test
    public void nullPointerException() {
        expectedExceptionRef.set(new NullPointerException());
        final String[] args = {UserAgentImpl.REQUEST_AUTHORIZATION_CODE};
        StandardWidgetToolkit.RUNNABLE_FACTORY_OVERRIDE = new RunnableFactory<StandardWidgetToolkit>() {
            public Runnable create(final StandardWidgetToolkit standardWidgetToolkit) {
                return new Runnable() {
                    public void run() {
                        try {
                            standardWidgetToolkit.requestAuthorizationCode(null, null);
                        }
                        catch (final Exception e) {
                            actualExceptionRef.set(e);
                        } finally {
                            standardWidgetToolkit.dispose();
                        }
                    }
                };
            }
        };

        StandardWidgetToolkit.main(args);
    }

    @Category(IntegrationTests.class)
    @Test
    public void happyPath_getCode() throws URISyntaxException, AuthorizationException,
            UnknownHostException {

        final int port = wireMockRule.port();

        final URI authorizationEndpoint = new URI(HTTP_PROTOCOL, null, HOST, port, "/oauth2/authorize",
                "response_type=code&client_id=main_wiremock&state=chicken", null);
        final URI authorizationConfirmation = new URI(HTTP_PROTOCOL, null, HOST, port, "/oauth2/confirm",
                "state=chicken", null);
        final String redirectingBody = String.format("<html><head><meta http-equiv='refresh' content='1; url=%1$s'>" +
                "</head><body>Redirecting to %1$s...</body></html>", authorizationConfirmation.toString());
        final URI redirectUri = new URI(HTTP_PROTOCOL, null, HOST, port, "/finished", "code=steak&state=chicken", null);

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

        StandardWidgetToolkit.RUNNABLE_FACTORY_OVERRIDE = new RunnableFactory<StandardWidgetToolkit>() {
            public Runnable create(final StandardWidgetToolkit standardWidgetToolkit) {
                return new Runnable() {
                    public void run() {
                        try {
                            final AuthorizationResponse response =
                                    standardWidgetToolkit.requestAuthorizationCode (authorizationEndpoint, redirectUri);

                            assertEquals("steak", response.getCode());
                            assertEquals("chicken", response.getState());
                        }
                        catch (final Exception e) {
                            actualExceptionRef.set(e);
                        } finally {
                            standardWidgetToolkit.dispose();
                        }
                    }
                };
            }
        };

        StandardWidgetToolkit.main(args);
    }
}
