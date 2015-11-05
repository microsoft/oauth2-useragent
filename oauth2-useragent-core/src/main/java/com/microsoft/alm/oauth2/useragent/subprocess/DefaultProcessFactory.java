// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent.subprocess;

import java.io.IOException;

public class DefaultProcessFactory implements TestableProcessFactory {
    @Override
    public TestableProcess create(final String... command) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        final DefaultProcess testableProcess = new DefaultProcess(processBuilder.start());
        return testableProcess;
    }
}
