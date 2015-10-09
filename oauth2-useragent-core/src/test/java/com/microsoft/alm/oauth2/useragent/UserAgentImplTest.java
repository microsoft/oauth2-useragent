// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;

public class UserAgentImplTest {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String NEW_LINE = System.getProperty("line.separator");

    @Test public void extractResponseFromRedirectUri_Typical() {
        final String redirectedUri = "https://msopentech.com/" +
                "?code=AAABAAAAiL9Kn2Z27UubvWFPbm0gLSXKVsoCQ5SqteFtDHVxXA8fd44gIaK71" +
                "juLqGyAA&session_state=10f521b6-41a9-41ba-8faa-8645e74d5123";

        final String actual = UserAgentImpl.extractResponseFromRedirectUri(redirectedUri);

        final String expected =
                "code=AAABAAAAiL9Kn2Z27UubvWFPbm0gLSXKVsoCQ5SqteFtDHVxXA8fd44gIaK71" +
                "juLqGyAA&session_state=10f521b6-41a9-41ba-8faa-8645e74d5123";
        Assert.assertEquals(expected, actual);
    }

    @Test public void decode_requestAuthorizationCode() throws AuthorizationException, UnsupportedEncodingException {
        final String authorizationEndpoint = "https://login.microsoftonline.com/common/oauth2/authorize?resource=foo&client_id=bar&response_type=code&redirect_uri=https%3A//redirect.example.com";
        final String redirectUri = "https://redirect.example.com";
        final UserAgent mockUserAgent = Mockito.mock(UserAgent.class);
        Mockito.when(mockUserAgent.requestAuthorizationCode(URI.create(authorizationEndpoint), URI.create(redirectUri))).thenReturn(new AuthorizationResponse("red", null));
        final StringBuilder sb = new StringBuilder();
        sb.append(authorizationEndpoint).append(NEW_LINE);
        sb.append(redirectUri).append(NEW_LINE);
        final String stdout = sb.toString();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(stdout.getBytes(UTF_8));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        UserAgentImpl.decode(mockUserAgent, new String[]{UserAgentImpl.REQUEST_AUTHORIZATION_CODE}, inputStream, outputStream);

        Mockito.verify(mockUserAgent).requestAuthorizationCode(Matchers.isA(URI.class), Matchers.isA(URI.class));
        final String actual = outputStream.toString(UTF_8.name());
        Assert.assertEquals("code=red", actual.trim());
    }

    @Test public void encode_requestAuthorizationCode() throws AuthorizationException, IOException {
        final String authorizationEndpoint = "https://login.microsoftonline.com/common/oauth2/authorize?resource=foo&client_id=bar&response_type=code&redirect_uri=https%3A//redirect.example.com";
        final String redirectUri = "https://redirect.example.com";
        final TestProcess process = new TestProcess("code=red");
        final TestableProcessFactory processFactory = new TestableProcessFactory() {
            @Override public TestableProcess create(final String... args) throws IOException {
                return process;
            }
        };
        final UserAgentImpl cut = new UserAgentImpl(processFactory, TestProvider.INSTANCE);

        final AuthorizationResponse actual = cut.encode(UserAgentImpl.REQUEST_AUTHORIZATION_CODE, authorizationEndpoint, redirectUri);

        Assert.assertEquals("red", actual.getCode());
        final String actualStdout = process.getOutput();
        Assert.assertEquals(authorizationEndpoint + NEW_LINE + redirectUri + NEW_LINE, actualStdout);

    }

    @Test public void encode_programmingError() throws AuthorizationException, IOException {
        final String authorizationEndpoint = "https://login.microsoftonline.com/common/oauth2/authorize?resource=foo&client_id=bar&response_type=code&redirect_uri=https%3A//redirect.example.com";
        final String redirectUri = "https://redirect.example.com";
        final String stackTrace = "Exception in Application start method";
        final TestProcess process = new TestProcess("", stackTrace);
        final TestableProcessFactory processFactory = new TestableProcessFactory() {
            @Override public TestableProcess create(final String... args) throws IOException {
                return process;
            }
        };
        final UserAgentImpl cut = new UserAgentImpl(processFactory, TestProvider.INSTANCE);

        try {
            cut.encode(UserAgentImpl.REQUEST_AUTHORIZATION_CODE, authorizationEndpoint, redirectUri);
        }
        catch (final AuthorizationException e) {
            Assert.assertEquals(stackTrace, e.getDescription().trim());
            Assert.assertEquals("subprocess_error", e.getCode());
            return;
        }
        Assert.fail("An AuthorizationException should have been thrown.");
    }
}
