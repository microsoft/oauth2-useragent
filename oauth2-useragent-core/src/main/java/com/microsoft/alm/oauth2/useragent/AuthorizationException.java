// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

public class AuthorizationException extends Exception {

    static final String ERROR_CODE = "error";
    static final String ERROR_DESCRIPTION = "error_description";
    static final String ERROR_URI = "error_uri";

    private static final String UTF_8 = "UTF-8";

    private final String code;
    private final String description;
    private final URI uri;

    public AuthorizationException(final String code) {
        this(code, null, null, null);
    }

    public AuthorizationException(final String code, final String description, final URI uri, final Throwable cause) {
        super(code, cause);
        if (code == null) {
            throw new IllegalArgumentException("The 'code' argument cannot be null.");
        }
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

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName()).append(": ");
        sb.append("Code: ").append(code);
        if (uri != null) {
            sb.append(" Uri: ").append(uri.toString());
        }
        if (description != null) {
            sb.append(" Description: ").append(description);
        }
        return sb.toString();
    }

    public static String toString(final String code, final Throwable throwable, final URI uri) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(baos);
        throwable.printStackTrace(printStream);
        final String description = baos.toString();
        return toString(code, description, uri);
    }

    public static String toString(final String code, final String description, final URI uri) {
        try {
            final StringBuilder sb = new StringBuilder();
            sb.append(ERROR_CODE).append('=').append(URLEncoder.encode(code, UTF_8));
            if (description != null) {
                sb.append('&');
                sb.append(ERROR_DESCRIPTION).append('=').append(URLEncoder.encode(description, UTF_8));
            }
            if (uri != null) {
                sb.append('&');
                sb.append(ERROR_URI).append('=').append(URLEncoder.encode(uri.toString(), UTF_8));
            }
            return sb.toString();
        }
        catch (final UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }
}
