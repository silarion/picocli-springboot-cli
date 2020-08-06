package com.sample.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

/**
 * Spring {@link Component}, called at startup because it implements
 * {@link CommandLineRunner}
 * 
 * Implements ExitCodeGenerator to return exit code used by {@link Application#main(String[])}
 */
@Component
public class CommandRunner implements CommandLineRunner, ExitCodeGenerator {

	@Autowired
	private CliCommand command;

	@Autowired
	private IFactory factory; // auto-configured to inject PicocliSpringFactory

	private int exitCode;

	@Override
	public void run(String... args) throws Exception {
		exitCode = new CommandLine(command, factory).execute(args);
	}

	@Override
	public int getExitCode() {
		return exitCode;
	}

}
