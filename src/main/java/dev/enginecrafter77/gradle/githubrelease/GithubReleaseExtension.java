package dev.enginecrafter77.gradle.githubrelease;

import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;
import org.gradle.jvm.tasks.Jar;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nullable;

@Getter
@Setter
public class GithubReleaseExtension {
	private static final String REAL_ENDPOINT = "https://api.github.com";
	private static final String MOCK_ENDPOINT = "http://localhost:5000"; // A simple flask mock

	public String repository;
	public String username;
	public String token;

	@Nullable
	public BuildArtifactContainer artifacts;

	@Nullable
	public Object releaseData;

	public void release(Closure<? super GithubReleaseData> closure)
	{
		this.releaseData = ConfigureUtil.configureUsing(closure);
	}

	public void artifacts(Closure<? super BuildArtifactContainer> closure)
	{
		this.artifacts = new BuildArtifactContainer();
		ConfigureUtil.configure(closure, this.artifacts);
	}

	@SuppressWarnings("deprecation")
	private BuildArtifactContainer evaluateArtifacts(Project project)
	{
		if(this.artifacts != null)
			return this.artifacts;

		BuildArtifactContainer container = new BuildArtifactContainer();
		Jar jarTask = (Jar)project.getTasks().findByName("jar");
		Jar srcJarTask = (Jar)project.getTasks().findByName("sourcesJar");
		if(jarTask != null)
			container.addArtifact(jarTask.getArchivePath(), "application/java-archive", jarTask, metadata -> {});
		if(srcJarTask != null)
			container.addArtifact(srcJarTask.getArchivePath(), "application/java-archive", jarTask, metadata -> {});
		return container;
	}

	public void configureTask(GithubPublishReleaseTask task)
	{
		task.setGroup("github-release");

		String endpoint = REAL_ENDPOINT;
		@Nullable String mockServerProp = System.getProperty("dev.enginecrafter77.gradle.githubrelease.mockServer");
		if(Boolean.parseBoolean(mockServerProp))
			endpoint = MOCK_ENDPOINT;

		Project project = task.getProject();
		BuildArtifactContainer artifacts = this.evaluateArtifacts(project);

		task.setEndpointUrl(endpoint);
		task.setRepositoryUrl(this.getRepository());
		task.setUsername(this.getUsername());
		task.setToken(this.getToken());
		task.setArtifacts(artifacts);

		if(this.releaseData != null)
			task.setReleaseData(this.releaseData);
		else
			task.release(GithubReleaseData::useLatestTag);
	}
}
