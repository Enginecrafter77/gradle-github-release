package dev.enginecrafter77.githubrelease;

import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;
import org.kohsuke.github.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
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
	private BuildArtifactContainer artifacts;

	@Nonnull
	private String getRepositoryName()
	{
		Matcher matcher = REPO_URL_REGEX.matcher(this.repositoryUrl);
		if(!matcher.matches())
			throw new IllegalArgumentException("Repository URL does not match the regex!");
		return matcher.group(1);
	}

	public void setArtifacts(BuildArtifactContainer artifacts)
	{
		this.artifacts = artifacts;
		this.reloadDependencies();
	}

	public void artifacts(Closure<? super BuildArtifactContainer> closure)
	{
		this.artifacts = new BuildArtifactContainer();
		closure.setResolveStrategy(Closure.DELEGATE_FIRST);
		closure.setDelegate(this.artifacts);
		closure.call();
		this.reloadDependencies();
	}

	private void reloadDependencies()
	{
		this.setDependsOn(this.artifacts.getArtifacts().stream().map(BuildArtifact::getBuildDependency).filter(Objects::nonNull).collect(Collectors.toList()));
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

	@TaskAction
	public void run() throws Exception
	{
		Collection<BuildArtifact> artifacts = this.artifacts.getArtifacts();
		GithubReleaseData releaseData = this.evaluateReleaseData();

		GitHub github = (new GitHubBuilder()).withEndpoint(this.endpointUrl).withOAuthToken(this.username, this.token).build();
		GHRepository repository = github.getRepository(this.getRepositoryName());

		//noinspection deprecation
		GHRelease release = repository.createRelease(releaseData.tag)
				.name(StringEscapeUtils.escapeJson(releaseData.name))
				.body(StringEscapeUtils.escapeJson(releaseData.message))
				.prerelease(releaseData.preRelease)
				.draft(releaseData.draft)
				.create();

		BuildArtifactMetadata metadata = new BuildArtifactMetadata();
		for(BuildArtifact artifact : artifacts)
		{
			artifact.configureMetadata(metadata);

			File file = artifact.getArtifactFileProvider().get();
			String name = Optional.ofNullable(metadata.getName()).orElseGet(file::getName);
			@Nullable String label = metadata.getLabel();

			try(InputStream input = Files.newInputStream(file.toPath()))
			{
				@Nullable String mimeType = artifact.getContentType();
				if(mimeType == null)
					mimeType = Files.probeContentType(file.toPath());
				if(mimeType == null)
					mimeType = "application/octet-stream";
				GHAsset asset = release.uploadAsset(name, input, mimeType);
				if(label != null)
					asset.setLabel(label);
			}
		}
	}
}
