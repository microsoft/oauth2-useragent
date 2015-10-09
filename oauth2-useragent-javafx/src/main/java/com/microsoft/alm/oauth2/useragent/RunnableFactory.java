// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

public interface RunnableFactory {
    Runnable create(final JavaFx javaFx);
}
