package dev.enginecrafter77.gradle.githubrelease;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
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

	private File getSettingsFile() {
		return new File(projectDir, "settings.gradle");
	}

	@Test
	public void testBasic() throws IOException
	{
		copyResourceToFile("build.gradle", getBuildFile());
		copyResourceToFile("settings.gradle", getSettingsFile());

		// Run the build
		GradleRunner runner = GradleRunner.create();
		runner.withEnvironment(System.getenv());
		runner.forwardOutput();
		runner.withPluginClasspath();
		runner.withProjectDir(projectDir);
		runner.withArguments("githubRelease");
		BuildResult result = runner.build();

		//Assertions.assertTrue(result.getOutput().contains("4.2.0/420"));
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
