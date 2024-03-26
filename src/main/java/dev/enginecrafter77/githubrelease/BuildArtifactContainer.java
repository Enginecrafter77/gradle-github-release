package dev.enginecrafter77.githubrelease;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nullable;
import javax.inject.Inject;

public abstract class BuildArtifactContainer {
	private final ObjectFactory objectFactory;

	@Inject
	public BuildArtifactContainer(ObjectFactory objectFactory)
	{
		this.objectFactory = objectFactory;
	}

	@Nested
	public abstract ListProperty<BuildArtifact> getArtifacts();

	public BuildArtifact artifact(Action<? super BuildArtifact> configureAction)
	{
		BuildArtifact artifact = this.objectFactory.newInstance(BuildArtifact.class);
		this.getArtifacts().add(artifact);
		return artifact.with(configureAction);
	}

	public BuildArtifact from(AbstractArchiveTask task)
	{
		return this.artifact((BuildArtifact artifact) -> {
			artifact.getFile().set(task.getArchiveFile());

			@Nullable String contentType = this.getContentTypeFromTask(task);
			if(contentType != null)
				artifact.getContentType().set(contentType);
		});
	}

	@Nullable
	private String getContentTypeFromTask(AbstractArchiveTask task)
	{
		if(task instanceof Jar)
			return "application/java-archive";
		else if(task instanceof Zip)
			return "application/zip";
		else
			return null;
	}

	public BuildArtifact from(Provider<RegularFile> file)
	{
		return this.artifact((BuildArtifact artifact) -> artifact.getFile().set(file));
	}

	@Deprecated
	public void fromJar(Jar task, @DelegatesTo(BuildArtifact.class) Closure<? super BuildArtifact> closure)
	{
		this.fromJar(task, ConfigureUtil.configureUsing(closure));
	}

	@Deprecated
	public void fromJar(Jar task, Action<? super BuildArtifact> configureAction)
	{
		this.from(task).with(configureAction);
	}

	@Deprecated
	public void fromZip(Zip task, @DelegatesTo(BuildArtifact.class) Closure<? super BuildArtifact> closure)
	{
		this.from(task).with(ConfigureUtil.configureUsing(closure));
	}

	@Deprecated
	public void fromZip(Zip task, Action<? super BuildArtifact> configureAction)
	{
		this.from(task).with(configureAction);
	}

	@Deprecated
	public void fromFile(Provider<RegularFile> file, @DelegatesTo(BuildArtifact.class) Closure<? super BuildArtifact> closure)
	{
		this.from(file).with(ConfigureUtil.configureUsing(closure));
	}

	@Deprecated
	public void fromFile(Provider<RegularFile> file, Action<? super BuildArtifact> configureAction)
	{
		this.from(file).with(configureAction);
	}

	public void set(BuildArtifactContainer other)
	{
		this.getArtifacts().set(other.getArtifacts());
	}
}
