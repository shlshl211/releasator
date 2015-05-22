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

## Preparing project for *Releasator*

**pom.xml**
* properly fill `<scm>` structure
* decide about your `buildNumber` property - defining it will make the *codename* required
* add following fragment to your `/project/build/pluginManagement/plugins`:
```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-release-plugin</artifactId>
    <version>2.1</version>
    <configuration>
        <mavenExecutorId>forked-path</mavenExecutorId>
        <useReleaseProfile>false</useReleaseProfile>
        <arguments>-Prelease-profile</arguments>
    </configuration>
</plugin>
```
(this is because only version 2.1 is working; some newer were failing, and the latest ones were not tested yet)

**settings.xml**
If this file is placed in your project root, it will be used for the `prepare` command.
It should contain enough settings for successful build; there should be no nexus credentials.

**upload-settings.xml**
This file is required in your project root during the `upload` command.

> TODO: is this all?

```
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>PragueNexus</id>
      <!-- setting universal mirror makes maven to only ask one single target for artifacts -->
      <mirrorOf>*</mirrorOf>
      <name>internal mirror for all stable repositories</name>
      <url>http://example.com:8081/nexus/content/groups/public</url>
    </mirror>
  </mirrors>

  <servers>
    <server>
      <!-- for hp-corporate-pom based projects, defining distributionManagement with id="releasator.repo" to use property `releasator.repo.url` -->
      <id>releasator.repo</id>
      <username>admin</username>
      <password>{CpOteL+dISUHVmARpxFeaLq5xuXAaZnDzUbevO7ZbZU=}</password>
    </server>
  </servers>

  <profiles>
    <profile>
      <!-- this is activated by project's pom, in configuration of maven-release-plugin -->
      <id>release-profile</id>
      <properties>
        <releasator.repo.url>http://example.com:8081/nexus/content/repositories/releases</releasator.repo.url>
        <releasator.siterepo.url>file:///tmp/releasator/siterepo</releasator.siterepo.url>
      </properties>
    </profile>
  </profiles>
</settings>

```

## Commands

The expected usage is covered by following commands:

* `prepare` - prepares all the release stuff locally, that is, without affecting any remote systems (Nexus, remote GIT repo). Network connection may still be needed for downloading dependencies.
* `upload` - pushes tags and release commits to the remote GIT repository; uploads artifacts to target Nexus
* `cancel` - removes the release from local GIT so that another attempt can be performed

See more details in following sections.

### Command `prepare`

> TODO

Prepares the release (TODO: locally!).

Parameters:
1. *RELEASE_VERSION* - (required) - the desired version of the release
2. *CODENAME* - for projects that define property `buildNumber` in the topmost `pom.xml`, the codename is required, and used to fill this value

Hints:
* use version format of three numbers, each of them without trailing zero - like `1.2.34`
* avoid trying to align your release versions with *official* product versions - it always leads to dirty compromises
* try to stick with [SemVer](http://semver.org); it is simple, reasonable, and rich enough


**Known issue** - also uploads artifacts to Nexus as defined in `settings.xml`

### Command `upload`

> TODO

### Command `cancel`

> TODO
