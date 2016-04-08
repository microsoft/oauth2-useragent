These notes are for release *0.8.1*.
Other releases and their notes can be found at the [oauth2-useragent GitHub Releases](https://github.com/Microsoft/oauth2-useragent/releases) page.

* Major:
    * Add feature to probe `Provider` implementations to find out if there's one that's compatible. Thanks to @yacaovsnc who reviewed pull request #21.
* Minor:
    * `AuthorizationException#toString()` is now human-readable.  Fixes issue #23 with pull request #24.
