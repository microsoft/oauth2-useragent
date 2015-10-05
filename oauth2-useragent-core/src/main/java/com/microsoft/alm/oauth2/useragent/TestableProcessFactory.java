// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.io.IOException;

interface TestableProcessFactory {
    TestableProcess create(final String... args) throws IOException;
}
