// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;

import static org.mockito.Mockito.mock;

public class SwtInterceptingBrowserTest {

    private SwtInterceptingBrowser underTest;

    private Display displayMock;
    private Shell shellMock;
    private Browser browserMock;

    @Before
    public void setUp() throws Exception {
        displayMock = mock(Display.class);
        shellMock = mock(Shell.class);
        browserMock = mock(Browser.class);

        underTest = new SwtInterceptingBrowser(browserMock, displayMock, shellMock);
    }
}
