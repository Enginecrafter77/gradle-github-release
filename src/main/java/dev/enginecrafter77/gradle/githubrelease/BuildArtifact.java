package dev.enginecrafter77.gradle.githubrelease;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gradle.api.Action;

import javax.annotation.Nullable;
import java.io.File;

@AllArgsConstructor
public class BuildArtifact {
	private final Action<? super BuildArtifactMetadata> metadataConfig;

	@Getter
	private final File artifactFile;

	@Getter
	@Nullable
	private final String contentType;

	@Getter
	@Nullable
	private final Object buildDependency;

	public void configureMetadata(BuildArtifactMetadata metadata)
	{
		this.metadataConfig.execute(metadata);
	}
}
