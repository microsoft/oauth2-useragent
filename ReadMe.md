${project.name} ${project.version}
==================================
${project.description}


License
-------
The MIT license can be found in [License.txt](License.txt)


What this library provides
--------------------------
There is a `UserAgentImpl` class (which is mockable via the `UserAgent` interface it implements).

The `requestAuthorizationCode` method will perform steps (A)-(C) of the "Authorization Code Flow" (see Figure 3 in [section 4.1 of RFC 6749](http://tools.ietf.org/html/rfc6749#section-4.1)), which is to say:
<ol type="A">
  <li>a browser window (also known as "user-agent") will be opened and directed to the authorization endpoint URI</li>
  <li>the user (also known as "resource owner") can then authenticate and decide whether to authorize the client's request</li>
  <li>the authorization server will send the browser to the redirect URI with either an authorization code or an error code, which will manifest itself as returning an instance of <tt>AuthorizationResponse</tt> or throwing an <tt>AuthorizationException</tt>, respectively</li>
</ol>


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
