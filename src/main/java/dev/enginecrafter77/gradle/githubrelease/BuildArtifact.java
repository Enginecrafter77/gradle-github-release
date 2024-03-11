package dev.enginecrafter77.gradle.githubrelease;

import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Optional;

public class BuildArtifact {
	private final BuildArtifactMetadata metadata;

	@Getter
	private final File artifactFile;

	@Getter
	@Nullable
	private final Object buildDependency;

	@Getter
	@Nullable
	private final String contentType;

	public BuildArtifact(BuildArtifactMetadata metadata, File artifactFile, @Nullable String contentType, @Nullable Object buildDependency)
	{
		this.artifactFile = artifactFile;
		this.contentType = contentType;
		this.buildDependency = buildDependency;
		this.metadata = metadata;
	}

	@Nonnull
	public String getName()
	{
		return Optional.ofNullable(this.metadata.getName()).orElseGet(this.artifactFile::getName);
	}

	@Nullable
	public String getLabel()
	{
		return this.metadata.getLabel();
	}
}
