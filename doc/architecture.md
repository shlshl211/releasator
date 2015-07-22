# Releasator

## Usecases

### UC1 - completely local release

> Story: Joe prepares and publishes

```
cd .../myproject
releasator prepare 0.0.1
releasator publish
```

### UC2 - publish a release archive

> Story: Joe publishes a ZIP received independently of code

```
releasator publish-archive <dir|zipfile>
```

### UC3 - responsibility split
*NOTE: this use-case needs more thinking*
> Story: Jane prepares, Joe publishes

1. Jane runs "prepare release" job on CI
2. CI builds the release ZIP and stores its *base url* in the TAG object (or commit message)
   ```
   releasator prepare 0.0.1 --author jane@example.com --push
   ```
3. Optionally, Joe downloads and tests the release
   ```
   git pull
   releasator install
   ```
4. Joe publishes the release
   ```
   releasator publish
   ```
5. Joe merges the release branch into base branch


## Common sequence

```
cd .../my-project
releasator get <scmurl>
releasator prepare <releaseVersion> <params...>
releasator upload <repourl>
```

### Subcommand `prepare` steps

1. download (`mvn dependency:go-offline`)
2. switch to release version + commit
3. build
4. tag
5. switch to devel version (revert step #2) + commit

## Validations

* **No snapshot dependencies**
* Release version must be derived from snapshot version
* Version must not exist in Nexus
* Version tag must not exist yet

## Project build specification

* Java version
* Maven version
* other tools version, like nodejs
* build command - defaults to mvn deploy
* (optional) release repo url

## Tool Configuration

* jdk version -> location
* maven version -> location
* tools version -> location
* vcs -> scmurl translation
* vcs -> weburl translation
* default release repo url
* server credentials - repo, git, ...

## Features

* vcs/scm mapping configuration + scm tag removal
* attach build log
* maintain changelog
* isolated build
* modify output before publishing

## Implementation

### Files

```
./    (project dir)
./build-spec.properties
./.releasator/
	repository/
	output/
	log/
	settings.xml
	build-spec.properties
	...
~/.m2/   (user config)
	releasator.cfg
```

## Planning

### Phase 1
* subcommands `prepare` and `upload`
* no configuration
* no buildspec
* no features
* no validations
