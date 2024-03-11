package dev.enginecrafter77.gradle.githubrelease;

import lombok.Value;
import org.gradle.api.Task;

import javax.annotation.Nullable;
import java.io.File;

@Value
public class BuildArtifact {
	File output;

	@Nullable
	String contentType;

	@Nullable
	Task buildTask;
}
