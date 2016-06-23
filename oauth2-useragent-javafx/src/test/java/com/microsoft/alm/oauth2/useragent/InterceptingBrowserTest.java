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
        final String redirectUriString = "http://auth.example.com/success";
        final String actualUriString = "http://auth.example.com/success?code=steak&state=chicken";

        final boolean actual = InterceptingBrowser.matchesRedirection(redirectUriString, actualUriString);

        Assert.assertEquals(true, actual);
    }

    @Test
    public void matchesRedirection_hostIsCaseInsensitive() throws Exception {
        final String redirectUriString = "http://Auth.example.com/success";
        final String actualUriString = "http://auth.example.com/success?code=steak&state=chicken";

        final boolean actual = InterceptingBrowser.matchesRedirection(redirectUriString, actualUriString);

        Assert.assertEquals(true, actual);
    }

}
