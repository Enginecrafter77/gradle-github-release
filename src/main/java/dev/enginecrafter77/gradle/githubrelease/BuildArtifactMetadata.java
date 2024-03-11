package dev.enginecrafter77.gradle.githubrelease;

import lombok.Data;

import javax.annotation.Nullable;

@Data
public class BuildArtifactMetadata {
	@Nullable
	public String name;

	@Nullable
	public String label;
}
