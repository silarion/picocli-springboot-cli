package com.sample.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@ConfigurationProperties
@Slf4j
public class Application {

	@Setter
	private String workFolder = "work";

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Application.class);

		// Check eclipse environment to auto activate develoment mode
		// MUST be tested in Windows and Mac environment if changed
		String variableName = "java.library.path"; // for Windows
		if (SystemUtils.IS_OS_MAC) {
			variableName = "java.class.path";
		}
		if (StringUtils.contains(System.getProperty(variableName), "eclipse")) {
			app.setAdditionalProfiles("development");
		}

		System.exit(SpringApplication.exit(app.run(args)));
	}

	@Bean
	public Path workFolder() throws IOException {
		return Files.createDirectories(Paths.get(workFolder));
	}

	@Bean
	public Path doneFile() throws IOException {
		Path file = Files.createDirectories(workFolder()).resolve("input.done");
		return Files.exists(file) ? file : Files.createFile(file);
	}

	@Bean
	public Set<String> doneInputs() throws IOException {
		return Files.readAllLines(doneFile(), StandardCharsets.UTF_8)
			.stream()
			.filter(StringUtils::isNotBlank)
			.distinct()
			.collect(ImmutableSet.toImmutableSet());
	}

}
