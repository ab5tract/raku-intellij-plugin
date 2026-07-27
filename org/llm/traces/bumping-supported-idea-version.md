# Bumping the supported IDEA version

Until RIP is available directly on the IntelliJ marketplace, it needs to be released "manually" for each new version of the IDE.

There are a number of `gradle` tasks available for assisting with this process. However, theyh are not necessary for building a local plugin. 

1. Edit the `.versions/idea-version` file and edit it to include the version number you want to support.
2. Edit the `.versions/raku-beta-version` file to whatever you wish, so long as it ends in a number. The default pattern is `$ideaVersion-beta.1`
3. `./gradlew buildPlugin`