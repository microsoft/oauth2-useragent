// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcess;
import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcessFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class UserAgentImplTest {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String NEW_LINE = System.getProperty("line.separator");

    @Test public void extractResponseFromRedirectUri_Typical() {
        final String redirectedUri = "https://msopentech.com/" +
                "?code=AAABAAAAiL9Kn2Z27UubvWFPbm0gLSXKVsoCQ5SqteFtDHVxXA8fd44gIaK71" +
                "juLqGyAA&session_state=10f521b6-41a9-41ba-8faa-8645e74d5123";

        final String actual = UserAgentImpl.extractResponseFromRedirectUri(redirectedUri);

        final String expected =
                "code=AAABAAAAiL9Kn2Z27UubvWFPbm0gLSXKVsoCQ5SqteFtDHVxXA8fd44gIaK71" +
                "juLqGyAA&session_state=10f521b6-41a9-41ba-8faa-8645e74d5123";
        Assert.assertEquals(expected, actual);
    }

    @Test public void findCompatibleProvider_atLeastOneCompatible() throws Exception {
        final CompatibleProvider compatibleProvider = new CompatibleProvider();
        final IncompatibleProvider incompatibleProvider = new IncompatibleProvider();
        final List<Provider> providers = new ArrayList<Provider>();
        providers.add(incompatibleProvider);
        providers.add(compatibleProvider);
        final UserAgentImpl cut = new UserAgentImpl(null, null, providers);

        final Provider actual = cut.findCompatibleProvider(null);

        Assert.assertEquals(compatibleProvider, actual);
        final Map<Provider, List<String>> actualMap = cut.getUnmetProviderRequirements();
        Assert.assertEquals(1, actualMap.size());
        final List<String> unmetRequirements = actualMap.get(incompatibleProvider);
        Assert.assertEquals(3, unmetRequirements.size());
        Assert.assertEquals(1, compatibleProvider.getCheckCount());
        Assert.assertEquals(1, incompatibleProvider.getCheckCount());
    }

    @Test public void findCompatibleProvider_atLeastOneCompatible_checkOnlyOnce() throws Exception {
        final CompatibleProvider compatibleProvider = new CompatibleProvider();
        final IncompatibleProvider incompatibleProvider = new IncompatibleProvider();
        final List<Provider> providers = new ArrayList<Provider>();
        providers.add(incompatibleProvider);
        providers.add(compatibleProvider);
        final UserAgentImpl cut = new UserAgentImpl(null, null, providers);

        final Provider actual = cut.findCompatibleProvider(null);

        Assert.assertEquals(compatibleProvider, actual);
        final Map<Provider, List<String>> actualMap = cut.getUnmetProviderRequirements();
        Assert.assertEquals(1, actualMap.size());
        final List<String> unmetRequirements = actualMap.get(incompatibleProvider);
        Assert.assertEquals(3, unmetRequirements.size());
        Assert.assertEquals(1, compatibleProvider.getCheckCount());
        Assert.assertEquals(1, incompatibleProvider.getCheckCount());

        final Provider actual2 = cut.findCompatibleProvider(null);

        Assert.assertEquals(compatibleProvider, actual2);
        Assert.assertEquals(1, compatibleProvider.getCheckCount());
        Assert.assertEquals(1, incompatibleProvider.getCheckCount());
    }

    @Test public void findCompatibleProvider_onlyIncompatible() throws Exception {
        final IncompatibleProvider incompatibleProvider = new IncompatibleProvider();
        final List<Provider> providers = new ArrayList<Provider>();
        providers.add(incompatibleProvider);
        final UserAgentImpl cut = new UserAgentImpl(null, null, providers);

        final Provider actual = cut.findCompatibleProvider(null);

        Assert.assertEquals(null, actual);
        final Map<Provider, List<String>> actualMap = cut.getUnmetProviderRequirements();
        Assert.assertEquals(1, actualMap.size());
        final List<String> unmetRequirements = actualMap.get(incompatibleProvider);
        Assert.assertEquals(3, unmetRequirements.size());
        Assert.assertEquals(1, incompatibleProvider.getCheckCount());
    }

    @Test public void decode_requestAuthorizationCode() throws AuthorizationException, UnsupportedEncodingException {
        final String authorizationEndpoint = "https://login.microsoftonline.com/common/oauth2/authorize?resource=foo&client_id=bar&response_type=code&redirect_uri=https%3A//redirect.example.com";
        final String redirectUri = "https://redirect.example.com";
        final UserAgent mockUserAgent = Mockito.mock(UserAgent.class);
        Mockito.when(mockUserAgent.requestAuthorizationCode(URI.create(authorizationEndpoint), URI.create(redirectUri))).thenReturn(new AuthorizationResponse("red", null));
        final StringBuilder sb = new StringBuilder();
        sb.append(authorizationEndpoint).append(NEW_LINE);
        sb.append(redirectUri).append(NEW_LINE);
        final String stdout = sb.toString();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(stdout.getBytes(UTF_8));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        UserAgentImpl.decode(mockUserAgent, new String[]{UserAgentImpl.REQUEST_AUTHORIZATION_CODE}, inputStream, outputStream);

        Mockito.verify(mockUserAgent).requestAuthorizationCode(Matchers.isA(URI.class), Matchers.isA(URI.class));
        final String actual = outputStream.toString(UTF_8.name());
        Assert.assertEquals("code=red", actual.trim());
    }

    @Test public void encode_requestAuthorizationCode() throws AuthorizationException, IOException {
        final String authorizationEndpoint = "https://login.microsoftonline.com/common/oauth2/authorize?resource=foo&client_id=bar&response_type=code&redirect_uri=https%3A//redirect.example.com";
        final String redirectUri = "https://redirect.example.com";
        final TestProcess process = new TestProcess("code=red");
        final TestableProcessFactory processFactory = new TestableProcessFactory() {
            @Override public TestableProcess create(final String... command) throws IOException {
                return process;
            }
        };
        final UserAgentImpl cut = new UserAgentImpl(processFactory, TestProvider.INSTANCE, null);

        final AuthorizationResponse actual = cut.encode(UserAgentImpl.REQUEST_AUTHORIZATION_CODE, authorizationEndpoint, redirectUri);

        Assert.assertEquals("red", actual.getCode());
        final String actualStdout = process.getOutput();
        Assert.assertEquals(authorizationEndpoint + NEW_LINE + redirectUri + NEW_LINE, actualStdout);

    }

    @Test public void encode_programmingError() throws AuthorizationException, IOException {
        final String authorizationEndpoint = "https://login.microsoftonline.com/common/oauth2/authorize?resource=foo&client_id=bar&response_type=code&redirect_uri=https%3A//redirect.example.com";
        final String redirectUri = "https://redirect.example.com";
        final String stackTrace = "Exception in Application start method";
        final TestProcess process = new TestProcess("", stackTrace);
        final TestableProcessFactory processFactory = new TestableProcessFactory() {
            @Override public TestableProcess create(final String... command) throws IOException {
                return process;
            }
        };
        final UserAgentImpl cut = new UserAgentImpl(processFactory, TestProvider.INSTANCE, null);

        try {
            cut.encode(UserAgentImpl.REQUEST_AUTHORIZATION_CODE, authorizationEndpoint, redirectUri);
        }
        catch (final AuthorizationException e) {
            Assert.assertEquals(stackTrace, e.getDescription().trim());
            Assert.assertEquals("unknown_error", e.getCode());
            return;
        }
        Assert.fail("An AuthorizationException should have been thrown.");
    }

    @Test public void appendProperties_Typical() throws Exception {
        final StringBuilder sb = new StringBuilder();
        final Properties properties = new Properties();
        properties.put("name", "v\\a|u/e");
        properties.put("ping", "pöng");
        properties.put("bing", "(b-o_n.g)");

        UserAgentImpl.appendProperties(properties, sb);

        assertLinesEqual(sb.toString(),
                "# --- BEGIN SYSTEM PROPERTIES ---",
                "",
                "bing=(b-o_n.g)",
                "name=v\\a|u/e",
                "ping=p%C3%B6ng",
                "",
                "# ---- END SYSTEM PROPERTIES ----"
        );
    }

    @Test public void appendVariables_Typical() throws Exception {
        final StringBuilder sb = new StringBuilder();
        final LinkedHashMap<String, String> variables = new LinkedHashMap<String, String>();
        variables.put("TMPDIR", "/var/folders/2f9992f171054fccabbdb978d49a2511");
        variables.put("PATH", "C:/Windows/System32;C:/Windows");
        variables.put("HOME", "/hömë/éxàmplè");

        UserAgentImpl.appendVariables(variables, sb);

        assertLinesEqual(sb.toString(),
                "# --- BEGIN ENVIRONMENT VARIABLES ---",
                "",
                "HOME=/h%C3%B6m%C3%AB/%C3%A9x%C3%A0mpl%C3%A8",
                "PATH=C:/Windows/System32;C:/Windows",
                "TMPDIR=/var/folders/2f9992f171054fccabbdb978d49a2511",
                "",
                "# ---- END ENVIRONMENT VARIABLES ----"
        );
    }

    @Test public void appendPairs_nullValue() throws Exception {
        final StringBuilder sb = new StringBuilder();
        final LinkedHashMap<String, String> variables = new LinkedHashMap<String, String>();
        variables.put("HOME", null);

        UserAgentImpl.appendPairs(variables.keySet(), variables, sb, "BEGIN", "END");

        assertLinesEqual(sb.toString(),
                "BEGIN",
                "",
                "HOME=",
                "",
                "END"
        );
    }

    private static void assertLinesEqual(final String actual, final String... expectedLines) {
        final StringReader sr = new StringReader(actual);
        try {
            final BufferedReader br = new BufferedReader(sr);
            for (final String eLine : expectedLines) {
                final String aLine = br.readLine();
                Assert.assertEquals(eLine, aLine);
            }
        }
        catch (final IOException e) {
            throw new Error(e);
        }
        finally {
            sr.close();
        }
    }

    private static void assertLinesMatch(final String actual, final String... expectedPatterns) {
        final StringReader sr = new StringReader(actual);
        try {
            final BufferedReader br = new BufferedReader(sr);
            for (final String ePattern : expectedPatterns) {
                final String aLine = br.readLine();
                final boolean matches = Pattern.matches(ePattern, aLine);
                if (!matches) {
                    Assert.fail("Line '" + aLine + "' did not match pattern '" + ePattern + "'.");
                }
            }
        }
        catch (final IOException e) {
            throw new Error(e);
        }
        finally {
            sr.close();
        }
    }

    @Test public void relayProperties_typical() throws Exception {
        final HashSet<String> propertyNames = new HashSet<String>(Arrays.asList("http.proxyHost", "http.proxyPort"));
        final Properties properties = new Properties();
        properties.setProperty("http.proxyHost", "192.0.2.42");
        final List<String> commands = new ArrayList<String>();

        UserAgentImpl.relayProperties(properties, propertyNames, commands);

        Assert.assertEquals(1, commands.size());
        Assert.assertEquals("-Dhttp.proxyHost=192.0.2.42", commands.get(0));
    }

    @Test public void relayProperties_noneSet() throws Exception {
        final HashSet<String> propertyNames = new HashSet<String>(Arrays.asList("http.proxyHost", "http.proxyPort"));
        final Properties properties = new Properties();
        final List<String> commands = new ArrayList<String>();

        UserAgentImpl.relayProperties(properties, propertyNames, commands);

        Assert.assertEquals(0, commands.size());
    }

    @Test public void scanProviders_Compatible() {
        final Provider compatibleProvider = new CompatibleProvider();
        //noinspection ArraysAsListWithZeroOrOneArgument
        final List<Provider> providers = Arrays.asList(compatibleProvider);
        final LinkedHashMap<Provider, List<String>> unmetReqs = new LinkedHashMap<Provider, List<String>>();

        final Provider actual = UserAgentImpl.scanProviders(null, providers, unmetReqs);

        Assert.assertEquals(compatibleProvider, actual);
    }

    private static abstract class TestHelperProvider extends Provider {
        private int checkCount = 0;

        TestHelperProvider(final String className) {
            super(className);
        }

        @Override public List<String> checkRequirements() {
            checkCount++;
            return null;
        }

        public int getCheckCount() {
            return checkCount;
        }

        @Override public void augmentProcessParameters(List<String> command, List<String> classPath) {
        }
    }

    private static class CompatibleProvider extends TestHelperProvider {
        public CompatibleProvider() {
            super("Compatible");
        }

        @Override public List<String> checkRequirements() {
            super.checkRequirements();
            return Collections.emptyList();
        }
    }

    @Test public void determineProvider_Incompatible() throws IOException {
        final Provider incompatibleProvider = new IncompatibleProvider();
        //noinspection ArraysAsListWithZeroOrOneArgument
        final List<Provider> providers = Arrays.asList(incompatibleProvider);

        try {
            UserAgentImpl.determineProvider(null, providers);
        }
        catch (final IllegalStateException e) {
            final String actual = e.getMessage();
            final StringReader sr = new StringReader(actual);
            try {
                final BufferedReader br = new BufferedReader(sr);
                Assert.assertEquals("I don't support your platform yet.", br.readLine());
                Assert.assertEquals("Unmet requirements for the 'Incompatible' provider:", br.readLine());
                Assert.assertEquals(" - You must construct additional Pylons.", br.readLine());
                Assert.assertEquals(" - You have not enough minerals.", br.readLine());
                Assert.assertEquals(" - Insufficient Vespene gas.", br.readLine());
                Assert.assertEquals("", br.readLine());
                Assert.assertEquals("Please send details about your operating system version, Java version, 32- vs. 64-bit, etc.", br.readLine());
                Assert.assertEquals("The following System Properties and Environment Variables would be very useful.", br.readLine());
                Assert.assertEquals("# --- BEGIN SYSTEM PROPERTIES ---", br.readLine());
                Assert.assertEquals("", br.readLine());
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.equals("# ---- END SYSTEM PROPERTIES ----")) {
                        break;
                    }
                }
                Assert.assertEquals("", br.readLine());
                Assert.assertEquals("# --- BEGIN ENVIRONMENT VARIABLES ---", br.readLine());
                Assert.assertEquals("", br.readLine());
                while ((line = br.readLine()) != null) {
                    if (line.equals("# ---- END ENVIRONMENT VARIABLES ----")) {
                        break;
                    }
                }
                Assert.assertEquals(null, br.readLine());
            }
            finally {
                sr.close();
            }
            return;
        }
        Assert.fail("An IllegalStateException should have been thrown");
    }

    private static class IncompatibleProvider extends TestHelperProvider {
        private static final List<String> MISSING_PREREQUISITES = Arrays.asList(
                "You must construct additional Pylons.",
                "You have not enough minerals.",
                "Insufficient Vespene gas."
        );
        public IncompatibleProvider() {
            super("Incompatible");
        }

        @Override public List<String> checkRequirements() {
            super.checkRequirements();
            return MISSING_PREREQUISITES;
        }
    }
}
