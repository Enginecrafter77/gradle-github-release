package dev.enginecrafter77.githubrelease;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;

public abstract class BuildArtifact {
	@InputFile
	public abstract RegularFileProperty getFile();

	@Input
	@Optional
	public abstract Property<String> getContentType();

	@Input
	@Optional
	public abstract Property<String> getName();

	@Input
	@Optional
	public abstract Property<String> getLabel();
}
