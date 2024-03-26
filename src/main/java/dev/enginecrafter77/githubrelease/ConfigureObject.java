package dev.enginecrafter77.githubrelease;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

public class ConfigureObject {
	private final ObjectFactory objectFactory;
	private final ProviderFactory providerFactory;

	public ConfigureObject(ObjectFactory objectFactory, ProviderFactory providerFactory)
	{
		this.objectFactory = objectFactory;
		this.providerFactory = providerFactory;
	}

	public <T> T configureInstance(Class<T> type, Action<? super T> configureAction)
	{
		T value = this.objectFactory.newInstance(type);
		configureAction.execute(value);
		return value;
	}

	public <T> Provider<T> configureProvider(Class<T> type, Action<? super T> configureAction)
	{
		return this.providerFactory.provider(() -> this.configureInstance(type, configureAction));
	}

	public static ConfigureObject with(ObjectFactory objectFactory, ProviderFactory providerFactory)
	{
		return new ConfigureObject(objectFactory, providerFactory);
	}

	public static ConfigureObject on(Project project)
	{
		return new ConfigureObject(project.getObjects(), project.getProviders());
	}
}
