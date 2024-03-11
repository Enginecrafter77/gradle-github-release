package dev.enginecrafter77.gradle.githubrelease;

import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;
import org.gradle.util.ConfigureUtil;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import javax.annotation.Nullable;
import java.io.IOException;

@Getter
@Setter
public class GithubReleaseExtension {
	public String repository;
	public String username;
	public String token;

	@Nullable
	public Object releaseData;

	public void release(Closure<? super GithubReleaseData> closure)
	{
		this.releaseData = ConfigureUtil.configureUsing(closure);
	}

	public GitHub createClient() throws IOException
	{
		return new GitHubBuilder().withOAuthToken(this.token, this.username).build();
	}
}
