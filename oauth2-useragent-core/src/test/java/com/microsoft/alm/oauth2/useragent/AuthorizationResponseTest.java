// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.StringContains.*;

public class AuthorizationResponseTest {

    @Test public void toString_withState() throws Exception {
        final AuthorizationResponse cut = new AuthorizationResponse("red", "green");

        final String actual = cut.toString();

        Assert.assertEquals("code=red&state=green", actual);
    }

    @Test public void toString_withoutState() throws Exception {
        final AuthorizationResponse cut = new AuthorizationResponse("red", null);

        final String actual = cut.toString();

        Assert.assertEquals("code=red", actual);
    }

    @Test public void fromString_null() throws Exception {
        final String input = null;

        try {
            AuthorizationResponse.fromString(input);
        } catch (final AuthorizationException actual) {
            Assert.assertEquals("error=unknown_error", actual.toString());
            return;
        }

        Assert.fail("Exception should have been thrown by fromString()");
    }

    @Test public void fromString_authorizationResponse() throws Exception {
        final String input = "code=red&state=green";

        final AuthorizationResponse actual = AuthorizationResponse.fromString(input);

        Assert.assertEquals("red", actual.getCode());
        Assert.assertEquals("green", actual.getState());
    }

    @Test public void fromString_authorizationResponseWithNewLine() throws Exception {
        final String input = "code=red&state=green\r\n";

        final AuthorizationResponse actual = AuthorizationResponse.fromString(input);

        Assert.assertEquals("red", actual.getCode());
        Assert.assertEquals("green", actual.getState());
    }

    @Test public void fromString_errorResponse() throws Exception {
        final String input = "error=invalid_request&error_description=Insufficient%20vespene%20gas&error_uri=https%3A%2F%2Fen.wikipedia.org%2Fw%2Findex.php%3Ftitle%3DVespene_gas";

        try {
            AuthorizationResponse.fromString(input);
        } catch (final AuthorizationException actual) {
            Assert.assertEquals("invalid_request", actual.getCode());
            Assert.assertEquals("Insufficient vespene gas", actual.getDescription());
            Assert.assertEquals("https://en.wikipedia.org/w/index.php?title=Vespene_gas", actual.getUri().toString());
            return;
        }

        Assert.fail("Exception should have been thrown by fromString()");
    }

    @Test public void fromString_erroneousNameValuePair() throws Exception {
        final String input = "codered&stategreen";

        try {
            AuthorizationResponse.fromString(input);
        } catch (final AuthorizationException actual) {
            Assert.assertEquals("parsing_error", actual.getCode());
            // no need to assert a hardcoded error description string
            return;
        }

        Assert.fail("Exception should have been thrown by fromString()");
    }

    @Test public void fromString_erroneousNameValuePairWithStdErr() throws Exception {
        final String input = "codered&stategreen";
        final String stdErr = "Insufficient vespene gas";

        try {
            AuthorizationResponse.fromString(input, stdErr);
        } catch (final AuthorizationException actual) {
            Assert.assertEquals("parsing_error", actual.getCode());
            Assert.assertThat(actual.getDescription(), containsString(input));
            Assert.assertThat(actual.getDescription(), containsString(stdErr));
            return;
        }

        Assert.fail("Exception should have been thrown by fromString()");
    }

}
