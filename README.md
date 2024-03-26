# Gradle Github Release plugin

## Overview

Gradle Github Release plugin does just what its
name suggests. When invoked from gradle, it creates
a release on github and uploads the built binaries
to the release as assets.

## Getting Started

First, add the plugin to gradle:

settings.gradle

```groovy
pluginManagement {
	repositories {
		maven {
			name "Enginecrafter77-Maven"
			url "https://maven.enginecrafter77.dev/general"
		}
	}
	plugins {
		id "dev.enginecrafter77.githubrelease" version "<version>"
	}
}
```

build.gradle

```groovy
plugins {
	id "dev.enginecrafter77.githubrelease"
}

github {
	repository = "https://github.com/user/repo.git"
	token = "ghb_xxxxxxxxxxxxxxxxxxx"
	artifacts {
		from jar
		from sourcesJar
	}
	release {
		useLatestTag()
	}
}
```

This configuration will create a release from
the latest annotated tag in the git repository.

To create the release, run:

```bash
./gradlew githubRelease
```

## Customizing release

In the release closure, you can specify the exact
release information to use.

```groovy
release {
	tag = "vX.Y.Z" // The tag to create the release on
	name = "Release XYZ" // The release title, optional, default: same as tag
	message = "Release message\n\nA new line example" // The release body
	draft = false // optional, default: false
	preRelease = false // optional, default: false
}
```

On the other hand, you can use `useLatestTag()` method.
This method looks into the git repository located in the root project directory
to locate the latest annotated git tag. The tag property is set to
the annotated tag's name, and the tag's message is copied
into the message property.

```groovy
release {
	useLatestTag()
}
```

Additionally, if your git repository is not located
in the project directory, you can specify it as an
argument to the useLatestTag method.

```groovy
release {
	useLatestTag(new File("some-git-dir"))
}
```

## Artifact types

One may wish to publish artifacts other than the main JAR.
In such cases, artifacts offers a few more options.

The easiest way is using the `from` method. This method takes 1
task argument. The task should specify exactly 1 output file, which
will be uploaded as the artifact.

```groovy
artifacts {
	from jar
}
```

The artifact can be further customized as such:

```groovy
artifacts {
	from jar with {
		name = "artifact-name.jar"
		label = "some label" // optional
	}
}
```

Or, using the shorthand form:

```groovy
artifacts {
	from jar named "artifact-name.jar" labeled "some label"
}
```

By default, the artifacts use the filename of
the file as the name of the uploaded asset. While
this is desirable in most cases, you can customize
it for each artifact.

If you want to manually specify an artifact, use the following:

```groovy
artifacts {
	artifact {
		file = (Provider<RegularFile>) provider
		contentType = "application/octet-stream" // optional
		name = "file-name.txt" // optional, same as filename if left unset
		label = "some label" // optional
	}
}
```

Please note that this approach is intented for advanced users who know what
`Provider<RegularFile>` is. The behavior of this approach is generally unknown.

## Lazy Evaluation

All the closures are evaluated during configure time, and as such
certain options are not yet known. An example of such case would be
a project using a plugin which sets `project.version` according to
the local git repository info in `afterEvaluate` method. Thus, a configuration
like this probably won't work as expected.

```groovy
artifacts {
	from jar named "artifact-name-${project.version}.jar"
}
```

To achieve the desired result, use the gradle `Property` system.

```groovy
def githubArtifactNameProperty = objects.property(String)

afterEvaluate {
	// project.version is now set
	githubArtifactNameProperty.set("artifact-name-${project.version}.jar")
}

github {
	artifacts {
		from jar named githubArtifactNameProperty
	}
}
```

## Testing mode

For testing the plugin, a very minimal github api mock
server was created using Python and Flask. The server
is implemented in `github-mock-server.py` file.

To run the server, first you need to create a virtual environment.

```bash
python3 -m venv flask-venv
source flask-venv/bin/activate
pip install flask
```

To exit the virtual environment, run `deactivate` and `source flask-venv/bin/activate` to enter it again.

To actually run the server, run:

```bash
./github-mock-server.py
```

The server will be running on `http://localhost:5000`, which is what the plugin expects.

To force the plugin to use the mock server, runi t as such.

```bash
./gradlew -Ddev.enginecrafter77.githubrelease.endpoint=http://localhost:5000 githubRelease
```

It is recommended to set token to a random string
such as "00000" to avoid accidentally using the real github api.

## Custom tasks

If desired, one can also register a custom GithubReleaseExtension task.

```groovy
tasks.register("task-name", dev.enginecrafter77.githubrelease.GithubPublishReleaseTask.class) {
	endpointUrl = "https://api.github.com" // required
	repositoryUrl = "https://github.com/user/repo" // required
	token = "xxxxxxxx" // required
	release {
		tag = "vX.Y.Z" // The tag to create the release on
		name = "Release XYZ" // The release title
		message = "Release message\n\nA new line example" // The release body
		draft = false
		preRelease = false
	}
	artifacts {
		from jar with {
			name = "artifact-name.jar"
			label = "some label" // optional
		}
		from sourcesJar
	}
}
```
