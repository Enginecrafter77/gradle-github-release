package dev.enginecrafter77.githubrelease;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.internal.Actions;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;

public abstract class BuildArtifactContainer {
	private final ObjectFactory objectFactory;
	private final ProviderFactory providerFactory;

	@Inject
	public BuildArtifactContainer(ObjectFactory objectFactory, ProviderFactory providerFactory)
	{
		this.objectFactory = objectFactory;
		this.providerFactory = providerFactory;
	}

	@Nested
	public abstract ListProperty<BuildArtifact> getArtifacts();

	public void fromJar(Jar task, Action<? super BuildArtifact> configureAction)
	{
		this.from((BuildArtifact artifact) -> {
			artifact.getFile().set(task.getArchiveFile());
			artifact.getContentType().set("application/java-archive");
			configureAction.execute(artifact);
		});
	}

	@Deprecated
	public void fromJar(Jar task, @DelegatesTo(BuildArtifact.class) Closure<? super BuildArtifact> closure)
	{
		this.fromJar(task, ConfigureUtil.configureUsing(closure));
	}

	public void fromJar(Jar task)
	{
		this.fromJar(task, Actions.doNothing());
	}

	public void fromZip(Zip task, Action<? super BuildArtifact> configureAction)
	{
		this.from((BuildArtifact artifact) -> {
			artifact.getFile().set(task.getArchiveFile());
			artifact.getContentType().set("application/zip");
			configureAction.execute(artifact);
		});
	}

	@Deprecated
	public void fromZip(Zip task, @DelegatesTo(BuildArtifact.class) Closure<? super BuildArtifact> closure)
	{
		this.fromZip(task, ConfigureUtil.configureUsing(closure));
	}

	public void fromZip(Zip task)
	{
		this.fromZip(task, Actions.doNothing());
	}

	public void fromFile(Provider<RegularFile> file, Action<? super BuildArtifact> configureAction)
	{
		this.from((BuildArtifact artifact) -> {
			artifact.getFile().set(file);
			configureAction.execute(artifact);
		});
	}

	@Deprecated
	public void fromFile(Provider<RegularFile> file, @DelegatesTo(BuildArtifact.class) Closure<? super BuildArtifact> closure)
	{
		this.fromFile(file, ConfigureUtil.configureUsing(closure));
	}

	public void fromFile(Provider<RegularFile> file)
	{
		this.fromFile(file, Actions.doNothing());
	}

	public void from(Action<? super BuildArtifact> action)
	{
		this.getArtifacts().add(this.providerFactory.provider(() -> {
			BuildArtifact artifact = this.objectFactory.newInstance(BuildArtifact.class);
			artifact.getName().convention(artifact.getFile().map((RegularFile file) -> file.getAsFile().getName()));
			action.execute(artifact);
			return artifact;
		}));
	}

	public BuildArtifactContainer from(BuildArtifactContainer other)
	{
		this.getArtifacts().set(other.getArtifacts());
		return this;
	}
}
