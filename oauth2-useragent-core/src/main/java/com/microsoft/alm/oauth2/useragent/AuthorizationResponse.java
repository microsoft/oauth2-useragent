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

    static final String RESPONSE_CODE = "code";
    static final String RESPONSE_STATE = "state";

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
            sb.append(RESPONSE_CODE).append('=').append(URLEncoder.encode(code, UTF_8));
            if (state != null) {
                sb.append('&');
                sb.append(RESPONSE_STATE).append('=').append(URLEncoder.encode(state, UTF_8));
            }
            return sb.toString();
        }
        catch (final UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public static AuthorizationResponse fromString(final String s) throws AuthorizationException {
        return fromString(s, null);
    }

    public static AuthorizationResponse fromString(final String s, final String potentialDescription) throws AuthorizationException {
        String code = null;
        String state = null;
        String error = "unknown_error";
        String errorDescription = potentialDescription;
        String errorUriString = null;
        if (s != null && s.length() > 0) {
            final String trimmed = s.trim();
            final String[] pairs = PAIR_SEPARATOR.split(trimmed);

            for (final String pair : pairs) {
                final String[] nameAndValue = NAME_VALUE_SEPARATOR.split(pair, 2);
                try {
                    if (nameAndValue.length != 2) {
                        final StringBuilder sb = new StringBuilder("Failed to parse server response");
                        if (errorDescription != null) {
                            sb.append(".\nDetails: ").append(errorDescription);
                        }
                        throw new AuthorizationException("parsing_error", sb.toString(), null, null);
                    }

                    final String name = URLDecoder.decode(nameAndValue[0], UTF_8);
                    final String value = URLDecoder.decode(nameAndValue[1], UTF_8);
                    if (RESPONSE_CODE.equals(name)) {
                        code = value;
                    } else if (RESPONSE_STATE.equals(name)) {
                        state = value;
                    } else if (AuthorizationException.ERROR_CODE.equals(name)) {
                        error = value;
                    } else if (AuthorizationException.ERROR_DESCRIPTION.equals(name)) {
                        errorDescription = value;
                    } else if (AuthorizationException.ERROR_URI.equals(name)) {
                        errorUriString = value;
                    }
                }
                catch (final UnsupportedEncodingException e) {
                    throw new Error(e);
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
