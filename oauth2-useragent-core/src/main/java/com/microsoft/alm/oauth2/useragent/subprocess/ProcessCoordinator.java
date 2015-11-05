// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent.subprocess;

import java.io.PrintStream;

/**
 * Provides a façade for working with a subprocess where a request is sent via
 * command-line parameters and/or a string sent to the child process's
 * standard input (stdin) stream and a response is read from the child process's
 * standard output (stdout) and standard error (stderr) streams.
 */
public class ProcessCoordinator {

    private final TestableProcess process;
    private final PrintStream stdIn;
    private final StreamConsumer stdOut;
    private final Thread stdOutThread;
    private final StreamConsumer stdErr;
    private final Thread stdErrThread;

    /**
     * Initializes the ProcessCoordinator with an instance of {@link TestableProcess}.
     *
     * @param process a process which has already been started
     */
    public ProcessCoordinator(final TestableProcess process) {
        this.process = process;
        this.stdIn = new PrintStream(process.getOutputStream());
        this.stdOut = new StreamConsumer(process.getInputStream());
        this.stdOutThread = new Thread(stdOut);
        stdOutThread.start();
        this.stdErr = new StreamConsumer(process.getErrorStream());
        this.stdErrThread = new Thread(stdErr);
        stdErrThread.start();
    }

    /**
     * Writes the specified string to the child process's stdin stream.
     *
     * @param s The string to write.
     */
    public void print(final String s) {
        stdIn.print(s);
    }

    /**
     * Writes the specified string to the child process's stdin stream,
     * followed by a new line.
     *
     * @param s The string to write.
     */
    public void println(final String s) {
        stdIn.println(s);
    }

    /**
     * Causes the current thread to wait, if necessary, until the child process
     * has terminated and both its stdout and stderr streams have been consumed.
     *
     * @return the exit value of the process.
     *          By convention, 0 indicates normal termination.
     * @throws InterruptedException if the current thread is interrupted by another
     *                              thread while it is waiting, then the wait is ended
     *                              and an InterruptedException is thrown.
     */
    public int waitFor() throws InterruptedException {
        stdIn.flush();
        stdIn.close();

        stdOutThread.join();
        stdErrThread.join();

        return process.waitFor();
    }

    /**
     * Gets the contents of the child process's standard output stream.
     * @return a string representing stdout
     */
    public String getStdOut() {
        return stdOut.toString();
    }

    /**
     * Gets the contents of the child process's standard error stream.
     * @return a string representing stderr
     */
    public String getStdErr() {
        return stdErr.toString();
    }
}
