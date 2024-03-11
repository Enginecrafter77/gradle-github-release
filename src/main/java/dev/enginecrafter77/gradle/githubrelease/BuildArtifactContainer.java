package dev.enginecrafter77.gradle.githubrelease;

import groovy.lang.Closure;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.internal.Actions;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class BuildArtifactContainer {
	private final List<BuildArtifact> artifacts;

	public BuildArtifactContainer()
	{
		this.artifacts = new ArrayList<BuildArtifact>();
	}

	private BuildArtifactMetadata configureMetadata(Action<? super BuildArtifactMetadata> configureAction)
	{
		BuildArtifactMetadata metadata = new BuildArtifactMetadata();
		configureAction.execute(metadata);
		return metadata;
	}

	public void addArtifact(File file, @Nullable String type, @Nullable Object buildDependency, Action<? super BuildArtifactMetadata> configureAction)
	{
		BuildArtifactMetadata metadata = this.configureMetadata(configureAction);
		BuildArtifact artifact = new BuildArtifact(metadata, file, type, buildDependency);
		this.artifacts.add(artifact);
	}

	@SuppressWarnings("deprecation")
	public void fromJar(Jar task, Action<? super BuildArtifactMetadata> configureAction)
	{
		this.addArtifact(task.getArchivePath(), "application/java-archive", task, configureAction);
	}

	public void fromJar(Jar task, Closure<? super BuildArtifactMetadata> closure)
	{
		this.fromJar(task, ConfigureUtil.configureUsing(closure));
	}

	public void fromJar(Jar task)
	{
		this.fromJar(task, Actions.doNothing());
	}

	@SuppressWarnings("deprecation")
	public void fromZip(Zip task, Action<? super BuildArtifactMetadata> configureAction)
	{
		this.addArtifact(task.getArchivePath(), "application/zip", task, configureAction);
	}

	public void fromZip(Zip task, Closure<? super BuildArtifactMetadata> closure)
	{
		this.fromZip(task, ConfigureUtil.configureUsing(closure));
	}

	public void fromZip(Zip task)
	{
		this.fromZip(task, Actions.doNothing());
	}

	public void fromTask(Object task, File file, Action<? super BuildArtifactMetadata> configureAction)
	{
		this.addArtifact(file, null, task, configureAction);
	}

	public void fromTask(Object task, File file, Closure<? super BuildArtifactMetadata> closure)
	{
		this.fromTask(task, file, ConfigureUtil.configureUsing(closure));
	}

	public void fromTask(Object task, File file)
	{
		this.fromTask(task, file, Actions.doNothing());
	}

	public void fromFile(File file, Action<? super BuildArtifactMetadata> configureAction)
	{
		this.addArtifact(file, null, null, configureAction);
	}

	public void fromFile(File file, Closure<? super BuildArtifactMetadata> closure)
	{
		this.fromFile(file, ConfigureUtil.configureUsing(closure));
	}

	public void fromFile(File file)
	{
		this.fromFile(file, Actions.doNothing());
	}

	public void fromFile(FileSystemLocation file, Action<? super BuildArtifactMetadata> configureAction)
	{
		this.fromFile(file.getAsFile(), configureAction);
	}

	public void fromFile(FileSystemLocation file, Closure<? super BuildArtifactMetadata> closure)
	{
		this.fromFile(file.getAsFile(), closure);
	}

	public void fromFile(FileSystemLocation file)
	{
		this.fromFile(file, Actions.doNothing());
	}

	public void fromFile(String file, Action<? super BuildArtifactMetadata> action)
	{
		this.fromFile(new File(file), action);
	}

	public void fromFile(String file, Closure<? super BuildArtifactMetadata> closure)
	{
		this.fromFile(new File(file), closure);
	}

	public void fromFile(String file)
	{
		this.fromFile(file, Actions.doNothing());
	}
}
