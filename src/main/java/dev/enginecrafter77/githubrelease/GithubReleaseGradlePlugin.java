/*
 * This file is part of the dropdroid project
 * Copyright (c) 2023 Enginecrafter77 <hutiramichal@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package dev.enginecrafter77.githubrelease;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;

import javax.annotation.Nonnull;
import java.util.Optional;

public class GithubReleaseGradlePlugin implements Plugin<Project> {
	public static final String GITHUB_API_ENDPOINT = "https://api.github.com";

	@Override
	public void apply(@Nonnull Project project)
	{
		project.getExtensions().add("github", GithubReleaseExtension.class);
		project.getTasks().register("githubRelease", DefaultTask.class, (DefaultTask task) -> {
			task.setGroup("github-release");
			task.dependsOn("defaultGithubRelease");
		});
		project.getTasks().register("defaultGithubRelease", GithubPublishReleaseTask.class, this::configureDefaultReleaseTask);
		project.beforeEvaluate(this::extensionConventions);
	}

	protected void extensionConventions(Project project)
	{
		GithubReleaseExtension extension = project.getExtensions().getByType(GithubReleaseExtension.class);

		extension.getArtifacts().convention(project.provider(() -> {
			BuildArtifactContainer container = project.getObjects().newInstance(BuildArtifactContainer.class);
			Optional.ofNullable(project.getTasks().findByName("jar")).map(Jar.class::cast).ifPresent(container::from);
			Optional.ofNullable(project.getTasks().findByName("sourcesJar")).map(Jar.class::cast).ifPresent(container::from);
			return container;
		}));

		extension.getRelease().convention(project.provider(() -> {
			GithubReleaseData data = project.getObjects().newInstance(GithubReleaseData.class);
			data.useLatestTag();
			return data;
		}));
	}

	protected void configureDefaultReleaseTask(GithubPublishReleaseTask task)
	{
		Project project = task.getProject();
		GithubReleaseExtension extension = project.getExtensions().getByType(GithubReleaseExtension.class);
		extension.configureTask(task);
	}
}
