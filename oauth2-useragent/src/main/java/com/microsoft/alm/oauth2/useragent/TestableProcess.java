// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.io.InputStream;
import java.io.OutputStream;

interface TestableProcess {
    InputStream getInputStream();
    OutputStream getOutputStream();
    int waitFor() throws InterruptedException;
}
