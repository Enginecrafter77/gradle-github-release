package dev.enginecrafter77.gradle.githubrelease;

import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;
import org.gradle.jvm.tasks.Jar;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class GithubReleaseExtension {
	private static final String REAL_ENDPOINT = "https://api.github.com";
	private static final String MOCK_ENDPOINT = "http://localhost:5000"; // A simple flask mock

	public String repository;
	public String username;
	public String token;

	@Nullable
	public Collection<Object> artifacts;

	@Nullable
	public Object releaseData;

	public void release(Closure<? super GithubReleaseData> closure)
	{
		this.releaseData = ConfigureUtil.configureUsing(closure);
	}

	private Collection<Object> evaluateArtifacts(Project project)
	{
		if(this.artifacts != null)
			return this.artifacts;

		Jar jarTask = (Jar)project.getTasks().findByName("jar");
		Jar srcJarTask = (Jar)project.getTasks().findByName("sourcesJar");
		return Stream.of(jarTask, srcJarTask).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public void configureTask(GithubPublishReleaseTask task)
	{
		task.setGroup("github-release");

		String endpoint = REAL_ENDPOINT;
		@Nullable String mockServerProp = System.getProperty("dev.enginecrafter77.gradle.githubrelease.mockServer");
		if(Boolean.parseBoolean(mockServerProp))
			endpoint = MOCK_ENDPOINT;

		Project project = task.getProject();
		Collection<Object> artifacts = this.evaluateArtifacts(project);

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
