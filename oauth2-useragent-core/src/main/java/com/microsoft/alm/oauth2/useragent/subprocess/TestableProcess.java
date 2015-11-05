// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent.subprocess;

import java.io.InputStream;
import java.io.OutputStream;

public interface TestableProcess {
    InputStream getErrorStream();
    InputStream getInputStream();
    OutputStream getOutputStream();
    int waitFor() throws InterruptedException;
}
