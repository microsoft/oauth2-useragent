// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.util.List;

class JavaFxProvider extends Provider {

    protected JavaFxProvider() {
        super("JavaFx");
    }

    @Override public List<String> checkRequirements() {
        // TODO: implement
        return null;
    }

    @Override public void augmentProcessParameters(final List<String> command, final List<String> classPath) {
        // TODO: implement
    }
}
