package dev.enginecrafter77.gradle.githubrelease;

import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.util.ConfigureUtil;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Setter
public class GithubPublishReleaseTask extends DefaultTask {
	private static final Pattern REPO_URL_REGEX = Pattern.compile("^https?://(?:www.)?github.com/([^/]+/[^/.]+)(?:.git)?$");

	@Input
	public String endpointUrl;

	@Input
	public String repositoryUrl;

	@Input
	public String username;

	@Input
	public String token;

	@Input
	public Object releaseData;

	@Input
	private Collection<Object> artifacts;

	@Nonnull
	private String getRepositoryName()
	{
		Matcher matcher = REPO_URL_REGEX.matcher(this.repositoryUrl);
		if(!matcher.matches())
			throw new IllegalArgumentException("Repository URL does not match the regex!");
		return matcher.group(1);
	}

	public void setArtifacts(Collection<Object> artifacts)
	{
		this.setDependsOn(artifacts.stream().map(this::resolveArtifact).map(BuildArtifact::getBuildTask).filter(Objects::nonNull).collect(Collectors.toList()));
		this.artifacts = artifacts;
	}

	public void release(Closure<? super GithubReleaseData> closure)
	{
		this.release(ConfigureUtil.configureUsing(closure));
	}

	public void release(Action<? super GithubReleaseData> action)
	{
		this.releaseData = action;
	}

	private GithubReleaseData evaluateReleaseData()
	{
		if(this.releaseData instanceof GithubReleaseData)
		{
			return (GithubReleaseData)this.releaseData;
		}

		if(this.releaseData instanceof Action)
		{
			@SuppressWarnings("unchecked") Action<? super GithubReleaseData> action = (Action<? super GithubReleaseData>)this.releaseData;
			GithubReleaseData data = new GithubReleaseData(this.getProject());
			action.execute(data);
			return data;
		}

		throw new UnsupportedOperationException("releaseData is neither GithubReleaseData instance nor Closure!");
	}

	private BuildArtifact resolveArtifact(Object artifact)
	{
		if(artifact instanceof Jar)
		{
			Jar task = (Jar)artifact;
			return new BuildArtifact(task.getArchiveFile().get().getAsFile(), "application/java-archive", task);
		}
		if(artifact instanceof Zip)
		{
			Zip task = (Zip)artifact;
			return new BuildArtifact(task.getArchiveFile().get().getAsFile(), "application/zip", task);
		}
		else if(artifact instanceof AbstractArchiveTask)
		{
			AbstractArchiveTask task = (AbstractArchiveTask)artifact;
			return new BuildArtifact(task.getArchiveFile().get().getAsFile(), null, task);
		}
		else if(artifact instanceof File)
		{
			return new BuildArtifact((File)artifact, null, null);
		}
		else if(artifact instanceof FileSystemLocation)
		{
			return new BuildArtifact(((FileSystemLocation)artifact).getAsFile(), null, null);
		}
		else if(artifact instanceof String)
		{
			return new BuildArtifact(new File((String)artifact), null, null);
		}
		else
		{
			throw new IllegalArgumentException("artifact must be either a Task reference, String, File or FileSystemLocation instance! Provided: " + artifact.getClass().getName());
		}
	}

	@TaskAction
	public void run() throws Exception
	{
		Collection<BuildArtifact> artifacts = this.artifacts.stream().map(this::resolveArtifact).collect(Collectors.toList());
		GithubReleaseData releaseData = this.evaluateReleaseData();

		GitHub github = (new GitHubBuilder()).withEndpoint(this.endpointUrl).withOAuthToken(this.username, this.token).build();
		GHRepository repository = github.getRepository(this.getRepositoryName());
		GHRelease release = repository.createRelease(releaseData.tag)
				.name(releaseData.name)
				.body(releaseData.message)
				.prerelease(releaseData.preRelease)
				.draft(releaseData.draft)
				.create();

		for(BuildArtifact artifact : artifacts)
		{
			File file = artifact.getOutput();
			String name = file.getName();
			try(InputStream input = Files.newInputStream(file.toPath()))
			{
				@Nullable String mimeType = artifact.getContentType();
				if(mimeType == null)
					mimeType = Files.probeContentType(file.toPath());
				if(mimeType == null)
					mimeType = "application/octet-stream";
				release.uploadAsset(name, input, mimeType);
			}
		}
	}
}
