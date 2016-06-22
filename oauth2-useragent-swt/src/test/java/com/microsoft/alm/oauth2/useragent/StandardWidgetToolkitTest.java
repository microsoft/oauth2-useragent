// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTests.class)
public class StandardWidgetToolkitTest {

    private StandardWidgetToolkit underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new StandardWidgetToolkit();
    }

    @Test
    public void testLoadSwt() {
        assertNotNull(underTest);
    }
}
