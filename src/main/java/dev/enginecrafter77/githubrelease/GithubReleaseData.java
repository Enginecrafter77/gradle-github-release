package dev.enginecrafter77.githubrelease;

import lombok.Data;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import javax.inject.Inject;
import java.util.List;

public abstract class GithubReleaseData {
	private final ProviderFactory providerFactory;
	private final ProjectLayout projectLayout;

	@Inject
	public GithubReleaseData(ProviderFactory providerFactory, ProjectLayout projectLayout)
	{
		this.providerFactory = providerFactory;
		this.projectLayout = projectLayout;

		this.getName().convention(this.getTag());
		this.getPreRelease().convention(false);
		this.getDraft().convention(false);
	}

	@Input
	public abstract Property<String> getTag();

	@Input
	@Optional
	public abstract Property<String> getName();

	@Input
	public abstract Property<String> getMessage();

	@Input
	@Optional
	public abstract Property<Boolean> getDraft();

	@Input
	@Optional
	public abstract Property<Boolean> getPreRelease();

	public void useLatestTag()
	{
		this.useLatestTag(this.projectLayout.getProjectDirectory());
	}

	public void useLatestTag(Directory directory)
	{
		Provider<GitTagInfo> tagInfoProvider = this.providerFactory.provider(() -> this.queryTag(directory));
		this.getTag().set(tagInfoProvider.map(GitTagInfo::getName));
		this.getMessage().set(tagInfoProvider.map(GitTagInfo::getFullMessage));
	}

	private GitTagInfo queryTag(Directory directory)
	{
		GitTagInfo info = new GitTagInfo();
		try(Git git = Git.open(directory.getAsFile()))
		{
			List<Ref> tags = git.tagList().call();
			Ref latestTag = tags.get(tags.size() - 1);

			RevWalk walk = new RevWalk(git.getRepository());
			RevTag tag = walk.parseTag(latestTag.getObjectId());

			info.setName(tag.getTagName());
			info.setFullMessage(tag.getFullMessage());
		}
		catch(Exception exc)
		{
			throw new RuntimeException("Resolving tag from git failed", exc);
		}
		return info;
	}

	public GithubReleaseData set(GithubReleaseData other)
	{
		this.getTag().set(other.getTag());
		this.getName().set(other.getName());
		this.getMessage().set(other.getMessage());
		this.getDraft().set(other.getDraft());
		this.getPreRelease().set(other.getPreRelease());
		return this;
	}

	@Data
	private static class GitTagInfo
	{
		String name;
		String fullMessage;
	}
}
