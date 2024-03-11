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
package dev.enginecrafter77.gradle.githubrelease;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.jvm.tasks.Jar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GithubReleaseGradlePlugin implements Plugin<Project> {
	private static final String REAL_ENDPOINT = "https://api.github.com";
	private static final String MOCK_ENDPOINT = "http://localhost:5000"; // A simple flask mock

	@Override
	public void apply(@Nonnull Project project)
	{
		project.getExtensions().add("github", new GithubReleaseExtension());
		project.getTasks().register("githubRelease", DefaultTask.class, (DefaultTask task) -> {
			task.setGroup("github-release");
			task.dependsOn("defaultGithubRelease");
		});
		project.getTasks().register("defaultGithubRelease", GithubPublishReleaseTask.class, this::configureDefaultReleaseTask);
	}

	protected void configureDefaultReleaseTask(GithubPublishReleaseTask task)
	{
		task.setGroup("github-release");

		String endpoint = REAL_ENDPOINT;
		@Nullable String mockServerProp = System.getProperty("dev.enginecrafter77.gradle.githubrelease.mockServer");
		if(Boolean.parseBoolean(mockServerProp))
			endpoint = MOCK_ENDPOINT;

		Project project = task.getProject();
		GithubReleaseExtension extension = project.getExtensions().getByType(GithubReleaseExtension.class);

		Jar jarTask = (Jar)project.getTasks().findByName("jar");
		Jar srcJarTask = (Jar)project.getTasks().findByName("sourcesJar");

		task.setEndpointUrl(endpoint);
		task.setRepositoryUrl(extension.getRepository());
		task.setUsername(extension.getUsername());
		task.setToken(extension.getToken());
		task.setArtifacts(Stream.of(jarTask, srcJarTask).filter(Objects::nonNull).collect(Collectors.toList()));

		if(extension.releaseData != null)
			task.setReleaseData(extension.releaseData);
		else
			task.release(GithubReleaseData::useLatestTag);
	}
}
