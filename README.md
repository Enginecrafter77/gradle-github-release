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
		fromJar jar
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
	name = "Release XYZ" // The release title
	message = "Release message\n\nA new line example" // The release body
	draft = false
	preRelease = false
}
```

On the other hand, you can use `useLatestTag()` method.
This method looks into the git repository located in the root project directory
to locate the latest annotated git tag. The tag and name
properties are set to the annotated tag's name, and the
tag's message is copied into the message property.
The draft and preRelease properties are set to false.
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

In this example, we can include any JAR built by Jar type tasks.
```groovy
artifacts {
	fromJar <jar-task>
}
```

Similarly, we can include a zip file built by a Zip type task.
```groovy
artifacts {
	fromZip <jar-task>
}
```

This notation allows you to include any file specified
by the file argument built by task specified by the task argument.
```groovy
artifacts {
	fromTask <task>, <file>
}
```

This option allows you to include any file which is not built by a task.
The use cases for this method are limited, but it was included nonetheless.
```groovy
artifacts {
	fromFile <file>
}
```

### Artifact options
By default, the artifacts use the filename of
the file as the name of the uploaded asset. While
this is desirable in most cases, you can customize
it for each artifact. Additionally, you can specify
the optional label option for the artifact.

```groovy
artifacts {
	fromJar(jar) {
		name = "some-jar-${version}.jar"
		label = "Optional label"
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
./gradlew -Ddev.enginecrafter77.githubrelease.mockServer=true githubRelease
```

It is recommended to set token to a random string
such as "00000" to avoid accidentally using the real github api.
