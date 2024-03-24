package dev.enginecrafter77.githubrelease;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nullable;
import java.util.Optional;

@Getter
@Setter
public class GithubReleaseExtension {
	private static final String REAL_ENDPOINT = "https://api.github.com";
	private static final String MOCK_ENDPOINT = "http://localhost:5000"; // A simple flask mock

	public String repository;
	public String token;

	@Nullable
	public BuildArtifactContainer artifacts;

	@Nullable
	public Object releaseData;

	public void release(@DelegatesTo(value = GithubReleaseData.class, strategy = Closure.DELEGATE_FIRST) Closure<? super GithubReleaseData> closure)
	{
		this.releaseData = ConfigureUtil.configureUsing(closure);
	}

	public void artifacts(@DelegatesTo(value = BuildArtifactContainer.class, strategy = Closure.DELEGATE_FIRST) Closure<? super BuildArtifactContainer> closure)
	{
		this.artifacts = new BuildArtifactContainer();
		ConfigureUtil.configure(closure, this.artifacts);
	}

	private BuildArtifactContainer evaluateArtifacts(Project project)
	{
		if(this.artifacts != null)
			return this.artifacts;

		BuildArtifactContainer container = new BuildArtifactContainer();
		Optional.ofNullable(project.getTasks().findByName("jar")).map(Jar.class::cast).ifPresent(container::fromJar);
		Optional.ofNullable(project.getTasks().findByName("sourcesJar")).map(Jar.class::cast).ifPresent(container::fromJar);
		return container;
	}

	public void configureTask(GithubPublishReleaseTask task)
	{
		task.setGroup("github-release");

		String endpoint = REAL_ENDPOINT;
		@Nullable String mockServerProp = System.getProperty("dev.enginecrafter77.githubrelease.mockServer");
		if(Boolean.parseBoolean(mockServerProp))
			endpoint = MOCK_ENDPOINT;

		Project project = task.getProject();
		BuildArtifactContainer artifacts = this.evaluateArtifacts(project);

		task.setEndpointUrl(endpoint);
		task.setRepositoryUrl(this.getRepository());
		task.setToken(this.getToken());
		task.setArtifacts(artifacts);

		if(this.releaseData != null)
			task.setReleaseData(this.releaseData);
		else
			task.release(GithubReleaseData::useLatestTag);
	}
}
