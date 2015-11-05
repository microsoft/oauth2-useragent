// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent.subprocess;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Supports the abstraction around sub-processes, to make it easier to unit test
 * programs that launch other programs by simulating said launch with test doubles.
 *
 * @see TestableProcessFactory
 */
public interface TestableProcess {

    /**
     * Gets the error stream of the subprocess.
     * The stream obtains data piped from the error output stream of the
     * process represented by this <code>TestableProcess</code> object.
     * <p>
     * Implementation note: It is a good idea for the input stream to be
     * buffered.
     *
     * @return  the input stream connected to the error stream of the
     *          subprocess.
     */
    InputStream getErrorStream();

    /**
     * Gets the input stream of the subprocess.
     * The stream obtains data piped from the standard output stream
     * of the process represented by this <code>TestableProcess</code> object.
     * <p>
     * Implementation note: It is a good idea for the input stream to
     * be buffered.
     *
     * @return  the input stream connected to the normal output of the
     *          subprocess.
     */
    InputStream getInputStream();

    /**
     * Gets the output stream of the subprocess.
     * Output to the stream is piped into the standard input stream of
     * the process represented by this <code>TestableProcess</code> object.
     * <p>
     * Implementation note: It is a good idea for the output stream to
     * be buffered.
     *
     * @return  the output stream connected to the normal input of the
     *          subprocess.
     */
    OutputStream getOutputStream();

    /**
     * causes the current thread to wait, if necessary, until the
     * process represented by this <code>TestableProcess</code> object has
     * terminated. This method returns
     * immediately if the subprocess has already terminated. If the
     * subprocess has not yet terminated, the calling thread will be
     * blocked until the subprocess exits.
     *
     * @return     the exit value of the process. By convention,
     *             <code>0</code> indicates normal termination.
     * @exception  InterruptedException  if the current thread is
     *             {@linkplain Thread#interrupt() interrupted} by another
     *             thread while it is waiting, then the wait is ended and
     *             an {@link InterruptedException} is thrown.
     */
    int waitFor() throws InterruptedException;
}
