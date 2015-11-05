// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent.subprocess;

import java.io.IOException;

/**
 * Represents an abstraction around sub-processes, to make it easier to unit test
 * programs that launch other programs by simulating said launch with test doubles.
 */
public interface TestableProcessFactory {
    /**
     * Creates a child process represented by the provided command and starts it.
     *
     * @param command the program to launch and its arguments
     * @return an instance of {@link TestableProcess} representing a process,
     *          whether real or simulated.
     * @throws IOException if an I/O error occurs
     */
    TestableProcess create(final String... command) throws IOException;
}
