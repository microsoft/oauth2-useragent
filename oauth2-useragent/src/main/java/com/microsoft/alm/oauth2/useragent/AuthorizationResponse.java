// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class AuthorizationResponse {


    private static final String UTF_8 = "UTF-8";
    private static final Pattern PAIR_SEPARATOR = Pattern.compile("&");
    private static final Pattern NAME_VALUE_SEPARATOR = Pattern.compile("=");

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

    @Override public String toString() {
        try {
            final StringBuilder sb = new StringBuilder();
            sb.append("code=").append(URLEncoder.encode(code, UTF_8));
            if (state != null) {
                sb.append('&');
                sb.append("state=").append(URLEncoder.encode(state, UTF_8));
            }
            return sb.toString();
        }
        catch (final UnsupportedEncodingException ignored) {
            return null;
        }
    }

    public static AuthorizationResponse fromString(final String s) throws AuthorizationException {
        String code = null;
        String state = null;
        String error = null;
        String errorDescription = null;
        String errorUriString = null;
        if (s != null) {
            final String[] pairs = PAIR_SEPARATOR.split(s);

            for (final String pair : pairs) {
                final String[] nameAndValue = NAME_VALUE_SEPARATOR.split(pair, 2);
                try {
                    final String name = URLDecoder.decode(nameAndValue[0], UTF_8);
                    final String value = URLDecoder.decode(nameAndValue[1], UTF_8);
                    if ("code".equals(name)) {
                        code = value;
                    } else if ("state".equals(name)) {
                        state = value;
                    } else if ("error".equals(name)) {
                        error = value;
                    } else if ("error_description".equals(name)) {
                        errorDescription = value;
                    } else if ("error_uri".equals(name)) {
                        errorUriString = value;
                    }
                }
                catch (final UnsupportedEncodingException ignored) {
                }
            }
        }

        if (code != null) {
            return new AuthorizationResponse(code, state);
        }
        URI errorUri = null;
        if (errorUriString != null) {
            try {
                errorUri = new URI(errorUriString);
            }
            catch (final URISyntaxException ignored) {
            }
        }
        throw new AuthorizationException(error, errorDescription, errorUri, null);
    }
}
