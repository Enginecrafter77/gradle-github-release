package dev.enginecrafter77.githubrelease;

import org.apache.commons.lang3.StringEscapeUtils;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.kohsuke.github.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class GithubPublishReleaseTask extends DefaultTask {
	private static final Pattern REPO_URL_REGEX = Pattern.compile("^https?://(?:www.)?github.com/([^/]+/[^/.]+)(?:.git)?$");

	public GithubPublishReleaseTask()
	{
		this.getEndpointUrl().convention(GithubReleaseGradlePlugin.GITHUB_API_ENDPOINT);
	}

	@Input
	public abstract Property<String> getEndpointUrl();

	@Input
	public abstract Property<String> getRepositoryUrl();

	@Input
	public abstract Property<String> getToken();

	@Nested
	public abstract GithubReleaseData getRelease();

	@Nested
	public abstract BuildArtifactContainer getArtifacts();

	public void release(Action<? super GithubReleaseData> action)
	{
		action.execute(this.getRelease());
	}

	public void artifacts(Action<? super BuildArtifactContainer> action)
	{
		action.execute(this.getArtifacts());
	}

	@Internal
	public Provider<String> getRepository()
	{
		return this.getRepositoryUrl().map((String url) -> {
			Matcher matcher = REPO_URL_REGEX.matcher(url);
			if(!matcher.matches())
				throw new IllegalArgumentException("Repository URL does not match the regex!");
			return matcher.group(1);
		});
	}

	@TaskAction
	public void run() throws Exception
	{
		Collection<BuildArtifact> artifacts = this.getArtifacts().getArtifacts().get();
		GithubReleaseData releaseData = this.getRelease();

		GitHub github = (new GitHubBuilder()).withEndpoint(this.getEndpointUrl().get()).withOAuthToken(this.getToken().get()).build();
		GHRepository repository = github.getRepository(this.getRepository().get());

		//Github API requires the message to use CRLF line endings.
		//noinspection deprecation
		GHRelease release = repository.createRelease(releaseData.getTag().get())
				.name(StringEscapeUtils.escapeJson(releaseData.getName().get()))
				.body(StringEscapeUtils.escapeJson(releaseData.getMessage().get().replace("\n", "\r\n")))
				.prerelease(releaseData.getPreRelease().get())
				.draft(releaseData.getDraft().get())
				.create();

		for(BuildArtifact artifact : artifacts)
		{
			File file = artifact.getFile().getAsFile().get();
			String name = artifact.getName().get();
			@Nullable String label = artifact.getLabel().getOrNull();

			try(InputStream input = Files.newInputStream(file.toPath()))
			{
				@Nullable String mimeType = artifact.getContentType().getOrNull();
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
