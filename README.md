# Releasator

*Releasator* is a tool for making maven-based releases of a git repository into Nexus.

## Software stack

Currently, *Releasator* is

* written in `bash` so it runs on `Linux` (maybe also `Cygwin` - not tested)
* uses the [Maven Release Plugin](http://maven.apache.org/maven-release/maven-release-plugin/) - aka "MRP"
* releases a `SNAPSHOT` version from a `GIT` repository to a `Nexus` instance.

## Features

* simplifies the release process
* prevents most common mistakes
* uses `buildNumber` property of the project's pom.xml to store a codename

This script originated as a tool that helped me with the most common use-cases.
It can happen that your specific use-case will be different, and require adjustment.

If you find these features not interesting enough for you, it's quite safe to just use "MRP"; it's just a bit more manual work

## Installation

1. Download the releasator.tgz and unpack it somewhere on your system. Good location might be `/opt/releasator`
2. On the target system, go to a dir that is on `PATH` and create a symlink:
```
cd /usr/local/bin
ln -s /opt/releasator/bin/releasator.sh releasator
```
3. That's it.

## Preparing project for releasing

## Commands

The expected usage is covered by following commands:

* `prepare` - prepares all the release stuff locally, that is, without affecting any remote systems (Nexus, remote GIT repo). Network connection may still be needed for downloading dependencies.
* `upload` - pushes tags and release commits to the remote GIT repository; uploads artifacts to target Nexus
* `cancel` - removes the release from local GIT so that another attempt can be performed

See more details in following sections.

### Command `prepare`

> TODO

### Command `upload`

> TODO

### Command `cancel`

> TODO
