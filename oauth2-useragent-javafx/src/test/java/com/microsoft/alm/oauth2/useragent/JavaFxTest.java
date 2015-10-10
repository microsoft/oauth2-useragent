// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Platform;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.atomic.AtomicReference;

public class JavaFxTest {

    private final AtomicReference<Exception> expectedExceptionRef = new AtomicReference<>();
    private final AtomicReference<Exception> actualExceptionRef = new AtomicReference<>();

    @Before public void before() {
        JavaFx.RUNNABLE_FACTORY_OVERRIDE = null;
        expectedExceptionRef.set(null);
        actualExceptionRef.set(null);
    }

    @After public void after() {
        JavaFx.RUNNABLE_FACTORY_OVERRIDE = null;
        final Exception actualException = this.actualExceptionRef.get();
        //noinspection ThrowableResultOfMethodCallIgnored
        final Exception expectedException = this.expectedExceptionRef.get();
        if (expectedException == null) {
            if (actualException != null) {
                if (actualException instanceof AuthorizationException) {
                    final AuthorizationException authorizationException = (AuthorizationException) actualException;
                    Assert.fail(authorizationException.getMessage() + UserAgentImpl.NEW_LINE + authorizationException.getDescription());
                }
                throw new Error(actualException);
            }
        }
        else {
            if (actualException == null) {
                final String template = "Expected exception '%1$s', got nothing.";
                final String message = String.format(template, expectedException.getClass().toString());
                Assert.fail(message);
            }
            else {
                Assert.assertEquals(expectedException.getClass(), actualException.getClass());
                Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
                if (actualException instanceof AuthorizationException) {
                    final AuthorizationException expectedAuthz = (AuthorizationException) expectedException;
                    final AuthorizationException actualAuthz = (AuthorizationException) actualException;
                    final String prefix = expectedAuthz.getDescription();
                    final String firstPart = actualAuthz.getDescription().substring(0, prefix.length());
                    Assert.assertEquals(actualAuthz.getDescription(), prefix, firstPart);
                }
            }
        }
    }

    @Category(IntegrationTests.class)
    @Test public void nullPointerException() {
        expectedExceptionRef.set(new NullPointerException());
        final String[] args = {UserAgentImpl.REQUEST_AUTHORIZATION_CODE};
        JavaFx.RUNNABLE_FACTORY_OVERRIDE = new RunnableFactory() {
            public Runnable create(final JavaFx javaFx) {
                return new Runnable() {
                    public void run() {
                        try {
                            javaFx.requestAuthorizationCode(null, null);
                        }
                        catch (final Exception e) {
                            actualExceptionRef.set(e);
                        }
                        finally {
                            Platform.exit();
                        }
                    }
                };
            }
        };

        LauncherImpl.launchApplication(JavaFx.class, args);
    }
}
