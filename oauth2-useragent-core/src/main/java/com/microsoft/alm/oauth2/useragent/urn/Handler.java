// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent.urn;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(final URL u) throws IOException {
        return new URLConnection(u) {
            @Override
            public void connect() throws IOException {
                // void, there is nothing to connect to
            }
        };
    }
}
