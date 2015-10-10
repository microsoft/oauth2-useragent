// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

public class AuthorizationExceptionTest {

    private static final String NEW_LINE = System.getProperty("line.separator");

    @Test public void toString_simpleRoundTrip() throws Exception {
        final String code = "code";
        final String description = "I am a verbose description of the error.";
        final URI uri = URI.create("http://example.com/oauth2/error/code");

        final String actual = AuthorizationException.toString(code, description, uri);

        try {
            AuthorizationResponse.fromString(actual);
        }
        catch (final AuthorizationException e) {
            Assert.assertEquals(code, e.getCode());
            Assert.assertEquals(description, e.getDescription());
            Assert.assertEquals(uri, e.getUri());
            return;
        }
        Assert.fail("An exception should have been thrown.");
    }

    @Test public void toString_throwableRoundTrip() throws Exception {
        final String code = "code";
        final String message = "A strange game. The only winning move is not to play.";
        final URI uri = URI.create("http://example.com/oauth2/error/code");
        try {
            throw new IllegalStateException(message);
        }
        catch (final IllegalStateException e) {
            final String actual = AuthorizationException.toString(code, e, uri);
            try {
                AuthorizationResponse.fromString(actual);
            }
            catch (final AuthorizationException ae) {
                Assert.assertEquals(code, ae.getCode());
                final String prefix = "java.lang.IllegalStateException: " +
                        "A strange game. The only winning move is not to play." + NEW_LINE +
                        "\tat com.microsoft.alm.oauth2.useragent.AuthorizationExceptionTest.toString_throwableRoundTrip";
                final String firstPart = ae.getDescription().substring(0, prefix.length());
                Assert.assertEquals(prefix, firstPart);
                Assert.assertEquals(uri, ae.getUri());
                return;
            }
            Assert.fail("An exception should have been thrown.");
        }
    }

}
