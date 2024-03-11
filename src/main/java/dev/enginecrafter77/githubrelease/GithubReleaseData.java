package dev.enginecrafter77.githubrelease;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.gradle.api.Project;

import java.io.File;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
public class GithubReleaseData {
	private final Project project;

	@Setter
	public String tag;

	@Setter
	public String name;

	@Setter
	public String message;

	@Setter
	public boolean draft;

	@Setter
	public boolean preRelease;

	public GithubReleaseData(Project project)
	{
		this.project = project;
	}

	public void useLatestTag()
	{
		this.useLatestTag(this.project.getRootProject().getProjectDir());
	}

	public void useLatestTag(File gitRepositoryDirectory)
	{
		try(Git git = Git.open(gitRepositoryDirectory))
		{
			List<Ref> tags = git.tagList().call();
			Ref latestTag = tags.get(tags.size() - 1);

			RevWalk walk = new RevWalk(git.getRepository());
			RevTag tag = walk.parseTag(latestTag.getObjectId());

			this.tag = tag.getTagName();
			this.message = tag.getFullMessage();
			this.name = tag.getTagName();
			this.draft = false;
			this.preRelease = false;
		}
		catch(Exception exc)
		{
			throw new RuntimeException("Resolving tag from git failed", exc);
		}
	}
}
