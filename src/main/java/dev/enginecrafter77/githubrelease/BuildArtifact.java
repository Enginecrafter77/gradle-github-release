package dev.enginecrafter77.githubrelease;

import org.gradle.api.Action;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;

public abstract class BuildArtifact {
	private final ProviderFactory providerFactory;

	@Inject
	public BuildArtifact(ProviderFactory providerFactory)
	{
		this.providerFactory = providerFactory;
		this.getName().convention(this.getFile().map((RegularFile file) -> file.getAsFile().getName()));
		this.getContentType().convention(this.getFile().flatMap(this::contentDeterminationProvider));
	}

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

	public BuildArtifact named(String name)
	{
		this.getName().set(name);
		return this;
	}

	public BuildArtifact named(Provider<String> name)
	{
		this.getName().set(name);
		return this;
	}

	public BuildArtifact labeled(String label)
	{
		this.getLabel().set(label);
		return this;
	}

	public BuildArtifact labeled(Provider<String> name)
	{
		this.getLabel().set(name);
		return this;
	}

	public BuildArtifact with(Action<? super BuildArtifact> configureAction)
	{
		configureAction.execute(this);
		return this;
	}

	private Provider<String> contentDeterminationProvider(RegularFile file)
	{
		return this.providerFactory.provider(() -> this.determineContentType(file));
	}

	@Nonnull
	private String determineContentType(RegularFile file) throws IOException
	{
		String mimeType = Files.probeContentType(file.getAsFile().toPath());
		if(mimeType == null)
			mimeType = "application/octet-stream";
		return mimeType;
	}
}
