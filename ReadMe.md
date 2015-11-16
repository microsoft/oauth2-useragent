${project.name} ${project.version}
==================================
${project.description}

License
-------
The MIT license can be found in [License.txt](License.txt)

How to use
----------
Maven is the preferred way of referencing this library.  Add the following to your POM:

```
  <dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>${project.artifactId}</artifactId>
    <version>${project.version}</version>
  </dependency>
```

...and then you can write code like:

```
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

...the resulting program accepts an OAuth 2.0 authorization endpoint URI and a redirect URI to watch for as command-line arguments.  The `requestAuthorizationCode` method will then perform steps (A)-(C) of the "Authorization Code Flow" (see section 4.1 and Figure 3 in RFC 6749), which is to say:
<ol type="A">
  <li>a browser window (also known as "user-agent") will be opened and directed to the authorization endpoint URI</li>
  <li>the user can then authenticate and decide whether to authorize the client's request</li>
  <li>the authorization server will send the browser to the redirect URI with either an authorization code or an error code, which will manifest itself as returning an instance of `AuthorizationResponse` or throwing an `AuthorizationException`, respectively</li>
</ol>
