// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URI;
import java.util.List;

public class JavaFx extends Application implements UserAgent, Runnable {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final InterceptingBrowser interceptingBrowser;

    public static void main(final String[] args) {
        launch(args);
    }

    public JavaFx() {
        interceptingBrowser = new InterceptingBrowser();
    }

    @Override
    public AuthorizationResponse requestAuthorizationCode(final URI authorizationEndpoint, final URI redirectUri)
            throws AuthorizationException {
        interceptingBrowser.sendRequest(authorizationEndpoint, redirectUri);

        return interceptingBrowser.waitForResponse();
    }

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     * NOTE: This method is called on the JavaFX Application Thread.
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        // TODO: it would be nice if we could prepend the title with a user-supplied string
        primaryStage.setTitle("OAuth2 Authorization Request");
        final Scene scene = new Scene(interceptingBrowser);
        primaryStage.setScene(scene);
        primaryStage.show();

        final Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void stop() throws Exception {
        interceptingBrowser.cancel();
    }

    @Override
    public void run() {
        final Parameters parameters = getParameters();
        final List<String> parameterList = parameters.getRaw();
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        final String[] args = parameterList.toArray(EMPTY_STRING_ARRAY);
        UserAgentImpl.decode(this, args, System.in, System.out);
        System.exit(0);
    }
}
