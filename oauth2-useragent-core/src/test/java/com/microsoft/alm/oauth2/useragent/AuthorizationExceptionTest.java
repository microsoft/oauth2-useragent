// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

public class AuthorizationExceptionTest {

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

}
