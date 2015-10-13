// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;

import java.net.URI;
import java.util.List;

public class JavaFx extends Application implements UserAgent, Runnable, RunnableFactory {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    static RunnableFactory RUNNABLE_FACTORY_OVERRIDE = null;

    private final RunnableFactory runnableFactory;
    private InterceptingBrowser interceptingBrowser = null;

    public static void main(final String[] args) {
        launch(args);
    }

    public JavaFx() {
        this.runnableFactory = RUNNABLE_FACTORY_OVERRIDE != null ? RUNNABLE_FACTORY_OVERRIDE : this;
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
        primaryStage.setTitle("OAuth 2.0 Authorization Request");

        // TODO: consider adding a way to inspect the connection security/certificate
        final TextField addressBar = new TextField();
        addressBar.setEditable(false);

        interceptingBrowser = new InterceptingBrowser();
        final WebEngine webEngine = interceptingBrowser.getWebEngine();
        webEngine.locationProperty().addListener(new ChangeListener<String>() {
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                addressBar.setText(newValue);
            }
        });

        final VBox vBox = new VBox(5);
        vBox.getChildren().setAll(addressBar, interceptingBrowser);
        VBox.setVgrow(interceptingBrowser, Priority.ALWAYS);

        final Scene scene = new Scene(vBox);
        primaryStage.setScene(scene);
        primaryStage.show();

        final Runnable runnable = runnableFactory.create(this);
        final Thread thread = new Thread(runnable);
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

    @Override
    public Runnable create(final JavaFx javaFx) {
        return this;
    }
}
