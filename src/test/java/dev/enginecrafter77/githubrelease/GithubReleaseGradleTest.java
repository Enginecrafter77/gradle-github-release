package dev.enginecrafter77.githubrelease;

import org.eclipse.jgit.api.Git;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class GithubReleaseGradleTest {
	@TempDir
	File projectDir;

	private File getBuildFile() {
		return new File(projectDir, "build.gradle");
	}

	@Test
	public void testBasic() throws Exception
	{
		copyResourceToFile("build.gradle", getBuildFile());

		try(Git git = Git.init().setDirectory(this.projectDir).call())
		{
			git.add().addFilepattern(".").call();
			git.commit().setMessage("Initial commit").call();
			git.tag().setAnnotated(true).setName("v0.0.1").setMessage("R-0.0.1").call();
		}

		// Run the build
		Assertions.assertDoesNotThrow(() -> {
			GradleRunner runner = GradleRunner.create();
			runner.withEnvironment(System.getenv());
			runner.forwardOutput();
			runner.withPluginClasspath();
			runner.withProjectDir(projectDir);
			runner.withArguments("--stacktrace", "-Ddev.enginecrafter77.githubrelease.mockServer=true", "githubRelease");
			runner.build();
		});
	}

	private void copyResourceToFile(String resource, File dest) throws IOException
	{
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(resource);
		if(input == null)
			throw new FileNotFoundException("Resource not found");

		File dir = dest.getParentFile();
		if(dir != null && !dir.exists())
			dir.mkdirs();

		Files.copy(input, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		input.close();
	}
}
