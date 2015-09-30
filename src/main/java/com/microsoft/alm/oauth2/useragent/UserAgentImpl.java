// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.net.URI;

public class UserAgentImpl implements UserAgent {

    @Override
    public AuthorizationResponse requestAuthorizationCode(final URI authorizationEndpoint, final URI redirectUri)
            throws AuthorizationException {
        // TODO:
        // create sub-process with methodName as first argument
        // send parameters to stdout
        // wait for response from stdin
        // decode response
        return null;
    }

}
