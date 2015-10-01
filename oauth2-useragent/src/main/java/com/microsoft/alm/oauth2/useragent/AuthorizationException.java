// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.net.URI;

public class AuthorizationException extends Exception {

    private final String code;
    private final String description;
    private final URI uri;

    public AuthorizationException(final String code) {
        this(code, null, null, null);
    }

    public AuthorizationException(final String code, final String description, final URI uri, final Throwable cause) {
        super(code, cause);
        this.code = code;
        this.description = description;
        this.uri = uri;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public URI getUri() {
        return uri;
    }
}
