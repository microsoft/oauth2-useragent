// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TestProvider extends Provider {

    private static final List<String> EMPTY_REQUIREMENTS = Collections.unmodifiableList(new ArrayList<String>());
    public static final Provider INSTANCE = new TestProvider();

    private TestProvider() {
        super("TestProvider");
    }

    @Override public List<String> checkRequirements() {
        return EMPTY_REQUIREMENTS;
    }

    @Override
    public void augmentProcessParameters(final List<String> command, final List<String> classPath) {
        // do nothing on purpose
    }
}
