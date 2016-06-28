// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import java.awt.Dimension;
import java.net.URI;

public class StandardWidgetToolkit implements UserAgent, RunnableFactory<StandardWidgetToolkit>, Runnable {

    static RunnableFactory<StandardWidgetToolkit> RUNNABLE_FACTORY_OVERRIDE = null;

    private RunnableFactory<StandardWidgetToolkit> swtRunnableFactory;
    private String[] commandLineArgs;

    private Shell shell;
    private Display display;

    private SwtInterceptingBrowser swtInterceptingBrowser;

    public StandardWidgetToolkit(String... commandLineArgs) {
        this.swtRunnableFactory = RUNNABLE_FACTORY_OVERRIDE != null ? RUNNABLE_FACTORY_OVERRIDE : this;
        this.commandLineArgs = commandLineArgs;

        display = new Display();
        shell = new Shell(display);
        shell.setText("OAuth 2.0 Authorization Request");

        shell.setLayout(new FillLayout());
        Monitor monitor = display.getPrimaryMonitor();
        Rectangle bounds = monitor.getBounds();
        Dimension size = new Dimension((int) (bounds.width * 0.25), (int) (bounds.height * 0.55));
        shell.setSize(size.width, size.height);
        shell.setLocation((bounds.width - size.width) / 2, (bounds.height - size.height) / 2);

        Browser browser = new org.eclipse.swt.browser.Browser(shell, SWT.ON_TOP);

        swtInterceptingBrowser = new SwtInterceptingBrowser(browser, display, shell);
    }

    public static void main(final String[] args) {
        final StandardWidgetToolkit swt = new StandardWidgetToolkit(args);

        final Runnable runnable = swt.swtRunnableFactory.create(swt);
        final Thread thread = new Thread(runnable);
        thread.start();

        swt.showSwtWindow();
    }

    @Override
    public AuthorizationResponse requestAuthorizationCode(final URI authorizationEndpoint, final URI redirectUri)
            throws AuthorizationException {
        swtInterceptingBrowser.sendRequest(authorizationEndpoint, redirectUri);

        return swtInterceptingBrowser.waitForResponse();
    }

    @Override
    public Runnable create(final StandardWidgetToolkit standardWidgetToolkit) {
        return this;
    }

    @Override
    public void run() {
        UserAgentImpl.decode(this, this.commandLineArgs, System.in, System.out);
        System.exit(0);
    }

    private void showSwtWindow() {
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    void dispose() {
        this.display.asyncExec(new Runnable() {
            @Override
            public void run() {
                shell.dispose();
            }
        });
    }
}
