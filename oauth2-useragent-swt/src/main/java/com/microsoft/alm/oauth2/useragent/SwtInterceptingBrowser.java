// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import java.net.URI;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SwtInterceptingBrowser {

    private Display display;
    private Browser browser;

    private final Lock lock = new ReentrantLock();
    private final Condition responseReceived = lock.newCondition();

    private String redirectUriString;
    private String destinationUriString;
    private String response;

    public SwtInterceptingBrowser(final Browser browser, final Display display, final Shell shell) {
        this.browser = browser;
        this.display = display;

        this.browser.addLocationListener(new LocationAdapter() {
            @Override
            public void changing(LocationEvent locationEvent) {
                super.changing(locationEvent);
                final String newValue = locationEvent.location;

                lock.lock();
                try {
                    if (redirectUriString != null && newValue != null && newValue.startsWith(redirectUriString)) {
                        // Do not load this new location, as we are only interested in the authorization code
                        locationEvent.doit = false;

                        response = UserAgentImpl.extractResponseFromRedirectUri(newValue);
                        responseReceived.signal();
                    }
                }
                finally {
                    lock.unlock();
                }
            }
        });

        shell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(Event event) {
                lock.lock();
                try {
                    response = "error=cancelled&error_description=The browser window was closed by the user.";
                    responseReceived.signal();
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    public void sendRequest(final URI destinationUri, final URI redirectUri) {
        destinationUriString = destinationUri.toString();
        redirectUriString = redirectUri.toString();

        this.display.asyncExec(new Runnable() {
            @Override
            public void run() {
                browser.setUrl(destinationUriString);
            }
        });
    }

    public AuthorizationResponse waitForResponse() throws AuthorizationException {
        lock.lock();
        try {
            responseReceived.awaitUninterruptibly();

            return AuthorizationResponse.fromString(response);
        } finally {
            lock.unlock();
        }
    }
}
