// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent.subprocess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class StreamConsumer implements Runnable {

    static final String NEW_LINE = System.getProperty("line.separator");

    private final InputStream source;
    private final StringBuilder contents;

    public StreamConsumer(final InputStream source) {
        if (source == null)
            throw new IllegalArgumentException("The 'source' argument is null.");

        this.source = source;
        this.contents = new StringBuilder();
    }

    @Override
    public String toString() {
        return contents.toString();
    }

    @Override
    public void run() {
        try {
            final InputStreamReader inputStreamReader = new InputStreamReader(source);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                contents.append(line).append(NEW_LINE);
            }
        }
        catch (final IOException ignored) {
        }
        finally {
            try {
                source.close();
            }
            catch (final IOException ignored) {
            }
        }
    }
}
