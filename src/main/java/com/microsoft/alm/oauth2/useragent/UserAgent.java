// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.net.URI;

public interface UserAgent {
    AuthorizationResponse requestAuthorizationCode(final URI authorizationEndpoint, final URI redirectUri)
            throws AuthorizationException;
}
