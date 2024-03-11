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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.component.SoftwareComponent;

import javax.annotation.Nonnull;

public class GithubReleaseGradlePlugin implements Plugin<Project> {
	@Override
	public void apply(@Nonnull Project project)
	{
		project.getTasks().register("githubRelease", (Task task) -> {
			task.doLast(this::afterEvaluate);
		});
	}

	protected void afterEvaluate(Task task)
	{
		SoftwareComponent component = task.getProject().getComponents().getAt("java");
		System.out.println(component);
	}
}
