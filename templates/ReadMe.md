${project.name} ${project.version}
==================================
${project.description}


License
-------
The MIT license can be found in [License.txt](License.txt)


Build status
------------
This project has continuous integration hosted by Travis CI:
[![Build Status](https://travis-ci.org/Microsoft/oauth2-useragent.svg?branch=master)](https://travis-ci.org/Microsoft/oauth2-useragent)


What this library provides
--------------------------
There is a `UserAgentImpl` class (which is mockable via the `UserAgent` interface it implements).

The `requestAuthorizationCode` method will perform steps (A)-(C) of the "Authorization Code Flow" (see Figure 3 in [section 4.1 of RFC 6749](http://tools.ietf.org/html/rfc6749#section-4.1)), which is to say:
<ol type="A">
  <li>a browser window (also known as "user-agent") will be opened and directed to the authorization endpoint URI</li>
  <li>the user (also known as "resource owner") can then authenticate and decide whether to authorize the client's request</li>
  <li>the authorization server will send the browser to the redirect URI with either an authorization code or an error code, which will manifest itself as returning an instance of <tt>AuthorizationResponse</tt> or throwing an <tt>AuthorizationException</tt>, respectively</li>
</ol>

### Available user agents and their requirements
The following table summarizes the current and planned support for user agents.

| Provider | Minimum Java Version | Requires desktop? | Requires 3rd-party dependency | Notes |
|--------------------------|------------------------|-------------------|-------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| JavaFX | Oracle Java 7 Update 6 (:warning:) | Yes | No | Uses WebView and WebEngine.  JavaFx ships with Oracle's Java since version 7 Update 6.  OpenJDK 8 users can [build & install OpenJFX](https://wiki.openjdk.java.net/display/OpenJFX/Building+OpenJFX). <br />:warning: Mac OS X 10.11 (El Capitan) and greater only work with Oracle Java 8, because it looks like [Java FX is broken on Mac OS X 10.11](http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8143907). |
| SWT (planned) | 1.6 | Yes | Yes | This will use the Standard Widget Toolkit from the Eclipse project, which will require the client either ship with the SWT JAR(s) or download them on-demand. |


Why would I want to use this library?
-------------------------------------
If you are writing an interactive desktop Java application (also known as "client") that needs to connect to a remote resource protected with OAuth 2.0, then this library is for you.  A pop-up window hosting a user agent (web browser) will be presented to the resource owner (user) when they need to authenticate to the remote resource and authorize access to the client.  The library monitors the web browser to detect when it tries to visit the redirect URL and intercepts the request to complete the process and close the window.

The web browser monitoring feature avoids
1. having to register a redirect URI that points to the local machine (which is sometimes impossible) and
2. hosting a web server on the local machine that would listen for a connection from the web browser.

If you are writing a web-based Java application or an Android app that needs to connect to a remote resource protected with OAuth 2.0, this library won't help you.  Consider using something like the [Google OAuth Client Library for Java](https://github.com/google/google-oauth-java-client).


Why not use `java.awt.Desktop.browse(URI)`?
-------------------------------------------
The tradeoffs associated with launching the default [external] browser with a URI entail not having to worry about detecting, shipping and/or hosting a web browser but with the difficulty that external browsers have no built-in, direct way to communicate with other processes.  The trick that is often used in OAuth 2.0 scenarios is to get the authorization server to redirect to an address that points to the local computer, which brings about its own set of challenges.


How to use
----------
Maven is the preferred way of referencing this library.  Add the following to your POM:

```xml
  <dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>${project.artifactId}</artifactId>
    <version>${project.version}</version>
  </dependency>
```

...and then you can write code like:

```java
public class App {
  public static void main(final String[] args) throws AuthorizationException, URISyntaxException {

    final URI authorizationEndpoint = new URI(args[0]);
    final URI redirectUri = new URI(args[1]);

    final UserAgent userAgent = new UserAgentImpl();

    final AuthorizationResponse authorizationResponse = userAgent.requestAuthorizationCode(authorizationEndpoint, redirectUri);
    final String code = authorizationResponse.getCode();

    System.out.print("Authorization Code: ");
    System.out.println(code);
  }
}
```

...the resulting program accepts an OAuth 2.0 authorization endpoint URI and a redirect URI to watch for as command-line arguments, then launches a web browser to perform the "Authorization Code Flow" described above.


How to build
------------
If you would like to recompile this library, it's relatively easy.

### System requirements
You will need the following software in your PATH:

1. Oracle JDK 8.
    * Version 8 of the Oracle Java Development Kit is required because of the dependency on JavaFX.  You *might* be able to build using OpenJDK with OpenJFX.
2. Maven 3.2+

### Run a quick build with Maven

1. Open a command prompt or terminal window.
2. Run `mvn clean verify`
    * This will download any Maven plugins you might be missing, compile the code, run some unit tests, and package up the JARs.

### Run a more comprehensive build with Maven
Like the "quick build", plus some integration tests will be run, whereby a browser will pop up momentarily a few times.

1. Open a command prompt or terminal window.
2. Run `mvn clean verify -Dintegration_tests=true`


How can I contribute?
---------------------
Please refer to [Contributing.md](Contributing.md).


Code of Conduct
---------------
This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
