// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.util.List;

public abstract class Provider {

    public static Provider JAVA_FX = new JavaFxProvider();

    private final String className;

    protected Provider(final String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public abstract List<String> checkRequirements();

    public abstract void augmentProcessParameters(final List<String> command, final List<String> classPath);
}
