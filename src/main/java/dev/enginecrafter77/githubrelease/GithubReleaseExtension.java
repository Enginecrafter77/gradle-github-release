package dev.enginecrafter77.githubrelease;

import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

@Getter
public class GithubReleaseExtension {
	private final ProviderFactory providerFactory;
	private final ObjectFactory objectFactory;

	@Nonnull
	private final Property<BuildArtifactContainer> artifacts;

	@Nonnull
	private final Property<GithubReleaseData> release;

	public final Property<String> repository;

	public final Property<String> token;

	@Inject
	public GithubReleaseExtension(ProviderFactory providerFactory, ObjectFactory objectFactory, ProjectLayout projectLayout)
	{
		this.providerFactory = providerFactory;
		this.objectFactory = objectFactory;
		this.repository = objectFactory.property(String.class);
		this.token = objectFactory.property(String.class);
		this.artifacts = objectFactory.property(BuildArtifactContainer.class);
		this.release = objectFactory.property(GithubReleaseData.class);
	}

	public void release(Action<? super GithubReleaseData> action)
	{
		this.release.set(ConfigureObject.with(this.objectFactory, this.providerFactory).configureProvider(GithubReleaseData.class, action));
	}

	public void setToken(String token)
	{
		this.token.set(token);
	}

	public void setRepository(String repository)
	{
		this.repository.set(repository);
	}

	public void artifacts(Action<? super BuildArtifactContainer> action)
	{
		this.artifacts.set(ConfigureObject.with(this.objectFactory, this.providerFactory).configureProvider(BuildArtifactContainer.class, action));
	}

	private Provider<String> getEndpointUrl()
	{
		return this.providerFactory.systemProperty("dev.enginecrafter77.githubrelease.endpoint").orElse(GithubReleaseGradlePlugin.GITHUB_API_ENDPOINT);
	}

	public void configureTask(GithubPublishReleaseTask task)
	{
		task.setGroup("github-release");
		task.getEndpointUrl().set(this.getEndpointUrl());
		task.getRepositoryUrl().set(this.getRepository());
		task.getToken().set(this.getToken());
		task.getArtifacts().from(this.artifacts.get());
		task.getRelease().from(this.release.get());
	}
}
