// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.junit.Assert;
import org.junit.Test;

public class UserAgentImplTest {

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

}
