// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.io.IOException;

class DefaultProcessFactory implements TestableProcessFactory {
    @Override
    public TestableProcess create(final String... args) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder(args);
        final DefaultProcess testableProcess = new DefaultProcess(processBuilder.start());
        return testableProcess;
    }
}
