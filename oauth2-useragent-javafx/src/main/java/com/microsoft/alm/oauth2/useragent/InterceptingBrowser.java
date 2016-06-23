// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URI;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class InterceptingBrowser extends Region implements ChangeListener<String> {

    private final Lock lock = new ReentrantLock();
    private final Condition responseReceived = lock.newCondition();
    private final WebView webView = new WebView();
    private final WebEngine webEngine = webView.getEngine();

    private String destinationUriString;
    private String redirectUriString;
    private String response;

    public InterceptingBrowser() {
        webEngine.locationProperty().addListener(this);
        final Worker<Void> loadWorker = webEngine.getLoadWorker();
        loadWorker.stateProperty().addListener(new ChangeListener<Worker.State>() {
            public void changed(final ObservableValue<? extends Worker.State> observable, final Worker.State oldValue, final Worker.State newValue) {
                lock.lock();
                try {
                    if (Worker.State.FAILED.equals(newValue)) {
                        final Throwable exception = loadWorker.getException();
                        if (exception != null) {
                            response = AuthorizationException.toString("load_error", exception, null);
                        }
                        else {
                            response = AuthorizationException.toString("load_error", "Exception details were not available", null);
                        }
                        responseReceived.signal();
                    }
                }
                finally {
                    lock.unlock();
                }
            }
        });

        getChildren().add(webView);
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }

    @Override
    public String toString() {
        return "destinationUri: " + destinationUriString + " redirectUri: " + redirectUriString;
    }

    @Override
    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
        lock.lock();
        try {
            if (matchesRedirection(redirectUriString, newValue)) {
                response = UserAgentImpl.extractResponseFromRedirectUri(newValue);
                responseReceived.signal();
            }
        }
        finally {
            lock.unlock();
        }
    }

    static boolean matchesRedirection(final String expectedRedirectUriString, final String actualUriString) {
        return (actualUriString != null && actualUriString.startsWith(expectedRedirectUriString));
    }

    public void sendRequest(final URI destinationUri, final URI redirectUri) {
        destinationUriString = destinationUri.toString();
        redirectUriString = redirectUri.toString();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                webEngine.load(destinationUriString);
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

    public void cancel() {
        lock.lock();
        try {
            response = "error=cancelled&error_description=The browser window was closed by the user.";
            responseReceived.signal();
        }
        finally {
            lock.unlock();
        }
    }
}
