// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@link InterceptingBrowser}.
 */
public class InterceptingBrowserTest {

    @Test
    public void matchesRedirection_typical() throws Exception {
        final String redirectUriString = "https://auth.example.com/success";
        final String actualUriString = "https://auth.example.com/success?code=steak&state=chicken";

        final boolean actual = InterceptingBrowser.matchesRedirection(redirectUriString, actualUriString);

        Assert.assertEquals(true, actual);
    }

    @Test
    public void matchesRedirection_hostIsCaseInsensitive() throws Exception {
        final String redirectUriString = "https://Auth.example.com/success";
        final String actualUriString = "https://auth.example.com/success?code=steak&state=chicken";

        final boolean actual = InterceptingBrowser.matchesRedirection(redirectUriString, actualUriString);

        Assert.assertEquals(true, actual);
    }

    @Test
    public void matchesRedirection_pathIsCaseSensitive() throws Exception {
        final String redirectUriString = "https://auth.example.com/success";
        final String actualUriString = "https://auth.example.com/Success?code=steak&state=chicken";

        final boolean actual = InterceptingBrowser.matchesRedirection(redirectUriString, actualUriString);

        Assert.assertEquals(false, actual);
    }

    @Test
    public void matchesRedirection_expectedContainsQuery() throws Exception {
        final String redirectUriString = "https://auth.example.com/success?state=chicken";
        final String actualUriString = "https://auth.example.com/success?code=steak&state=chicken";

        final boolean actual = InterceptingBrowser.matchesRedirection(redirectUriString, actualUriString);

        Assert.assertEquals(true, actual);
    }

    @Test
    public void matchesRedirection_pathWithoutSlash() throws Exception {
        final String redirectUriString = "https://auth.example.com";
        final String actualUriString = "https://auth.example.com/";

        final boolean actual = InterceptingBrowser.matchesRedirection(redirectUriString, actualUriString);

        Assert.assertEquals(true, actual);
    }

    @Test
    public void matchesRedirection_urnSchemeWithNativeAppRedirect() throws Exception {
        final String redirectUriString = "urn:ietf:wg:oauth:2.0:oob";
        final String actualUriString = "urn:ietf:wg:oauth:2.0:oob?code=abc&stat=123";

        final boolean actual = InterceptingBrowser.matchesRedirection(redirectUriString, actualUriString);

        Assert.assertEquals(true, actual);
    }


    @Test
    public void matchesRedirection_urnSchemeCaseSensitive() throws Exception {
        final String redirectUriString = "urn:ietf:wg:oauth:2.0:oob";
        final String actualUriString = "urn:IETF:wg:oauth:2.0:oob?code=abc&stat=123";

        final boolean actual = InterceptingBrowser.matchesRedirection(redirectUriString, actualUriString);

        Assert.assertEquals(false, actual);
    }
}
