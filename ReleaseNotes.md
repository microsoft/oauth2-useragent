These notes are for release *0.6.0*.  Other releases and their notes can be found at the [oauth2-useragent GitHub Releases]() page.

* Major:
    * Improved the determination of the sub-process classpath.  Thanks to Yang Cao (@yacaovsnc) who helped fixed issue #9 with pull request #14.
* Minor:
    * Content in the sub-process `stderr` stream will no longer cause an authentication failure.  Thanks to Yang Cao (@yacaovsnc) who fixed issue #10, made possible with pull requests #11, #12 and #15.
