package dev.enginecrafter77.githubrelease;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class BuildArtifactContainer {
	private final ObjectFactory objectFactory;

	@Inject
	public BuildArtifactContainer(ObjectFactory objectFactory)
	{
		this.objectFactory = objectFactory;
	}

	@Nested
	public abstract ListProperty<BuildArtifact> getArtifacts();

	public BuildArtifact artifact(Action<? super BuildArtifact> configureAction)
	{
		BuildArtifact artifact = this.objectFactory.newInstance(BuildArtifact.class);
		this.getArtifacts().add(artifact);
		return artifact.with(configureAction);
	}

	public BuildArtifact from(Provider<RegularFile> file)
	{
		return this.artifact((BuildArtifact artifact) -> artifact.getFile().set(file));
	}

	public BuildArtifact from(TaskProvider<?> task)
	{
		return this.from(task.flatMap(this::findTaskOutput)).with((BuildArtifact artifact) -> {
			artifact.getContentType().set(task.map(this::getContentTypeFromTask));
		});
	}

	public BuildArtifact from(Task task)
	{
		return this.from(this.findTaskOutput(task)).with((BuildArtifact artifact) -> {
			artifact.getContentType().set(this.getContentTypeFromTask(task));
		});
	}

	@Deprecated
	public void fromJar(Jar task, @DelegatesTo(BuildArtifact.class) Closure<? super BuildArtifact> closure)
	{
		this.fromJar(task, ConfigureUtil.configureUsing(closure));
	}

	@Deprecated
	public void fromJar(Jar task, Action<? super BuildArtifact> configureAction)
	{
		this.from(task).with(configureAction);
	}

	@Deprecated
	public void fromZip(Zip task, @DelegatesTo(BuildArtifact.class) Closure<? super BuildArtifact> closure)
	{
		this.from(task).with(ConfigureUtil.configureUsing(closure));
	}

	@Deprecated
	public void fromZip(Zip task, Action<? super BuildArtifact> configureAction)
	{
		this.from(task).with(configureAction);
	}

	@Deprecated
	public void fromFile(Provider<RegularFile> file, @DelegatesTo(BuildArtifact.class) Closure<? super BuildArtifact> closure)
	{
		this.from(file).with(ConfigureUtil.configureUsing(closure));
	}

	@Deprecated
	public void fromFile(Provider<RegularFile> file, Action<? super BuildArtifact> configureAction)
	{
		this.from(file).with(configureAction);
	}

	public void empty()
	{
		this.getArtifacts().empty();
	}

	public void set(BuildArtifactContainer other)
	{
		this.getArtifacts().set(other.getArtifacts());
	}

	@Nonnull
	private Provider<RegularFile> findTaskOutput(Task task)
	{
		try
		{
			Predicate<Method> returnsRegularFileProperty = (Method method) -> Provider.class.isAssignableFrom(method.getReturnType());
			Predicate<Method> hasOutputFileAnnotation = (Method method) -> method.getAnnotation(OutputFile.class) != null;

			Class<?> cls = task.getClass();
			if(cls.getName().endsWith("_Decorated"))
				cls = cls.getSuperclass();
			List<Method> methods = Arrays.stream(cls.getMethods()).filter(returnsRegularFileProperty).filter(hasOutputFileAnnotation).collect(Collectors.toList());
			if(methods.size() != 1)
				throw new RuntimeException("Task " + task + " has to declare exactly 1 OutputFile property!");
			return (RegularFileProperty)methods.get(0).invoke(task);
		}
		catch(ReflectiveOperationException exc)
		{
			throw new RuntimeException("Unable to locate the output file of task " + task, exc);
		}
	}

	@Nullable
	private String getContentTypeFromTask(Task task)
	{
		if(task instanceof Jar)
			return "application/java-archive";
		else if(task instanceof Zip)
			return "application/zip";
		else
			return null;
	}
}
