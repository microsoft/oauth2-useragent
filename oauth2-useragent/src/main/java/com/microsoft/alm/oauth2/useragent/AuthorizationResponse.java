// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

public class AuthorizationResponse {

    private final String code;
    private final String state;

    public AuthorizationResponse(final String code, final String state) {
        this.code = code;
        this.state = state;
    }

    public String getCode() {
        return code;
    }
    public String getState() {
        return state;
    }

}
