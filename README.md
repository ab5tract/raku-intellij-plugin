# Raku IntelliJ Plugin
## RIP - a Raku plugin for IntelliJ IDEs

This is a continuation of the work done by the fine folks at Edument[^discontinuation] to build and support Comma,
an IDE for Raku. They were kind enough to release the source code, which is how we have RIP now.

Unfortunately the standalone mode is off the table for the moment, as JetBrains do not provide any guidance or documentation
on how to currently build a standalone version. Howver running as a plugin is fully supported
inside of `IntelliJ IDEA` or any of the IntelliJ IDEs.

It is now built with `Gradle IntelliJ Platform Plugin 2.0` and requires IntelliJ IDEA version `2024.2` or later, 
available for download [here](https://www.jetbrains.com/idea/download/) (scroll down for the free Community edition).

### Notice to existing Comma users

The project structures have changed between Comma and RIP. Pains have been taken such that the plugin will automtically
resolve this issue, however _it is a good idea for any Comma users to clear their IDE's cache_ for good measure.

### Install

1) Download `raku-plugin.zip` from the latest [release](https://github.com/ab5tract/raku-intellij-plugin/releases)
2) Open `Settings > Plugins` in IntelliJ IDEA and find the gear icon
   - select `Install plugin from disk` from the subsequent menu.
   - navigate to where `raku-plugin.zip` was downloaded (no need to extract or copy the file anywhere)
   - select `raku-plugin.zip`.
4) Restart the IDE
5) Enjoy!

### Build

To build, simply:

1) Clone this repo, open it in IntelliJ IDEA
2) Select `build > build`[^build] (or `intellij platform > runIde` to run in a sandbox) from the 
Gradle target options (look for the elephant on the left side of the IDEA window).
3) Enjoy! And please report any issues you have to the issue tracker here.

[^discontinuation]: Read the [discontinuation](https://commaide.com/discontinued) announcement.
[^build]: The plugin will be produced as `build/distributions/comma-${version}.zip`.
