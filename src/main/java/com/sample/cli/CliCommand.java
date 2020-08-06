package com.sample.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.google.common.base.Stopwatch;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import de.vandermeer.asciitable.AsciiTable;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Unmatched;

/**
 * 
 * Application main command using picocli
 * 
 * Implements {@link Callable}&lt;Integer&gt; so that picocli will use returned
 * Integer as exit code
 * 
 */
@Component
@Slf4j
@Command(name = "${command.name}", mixinStandardHelpOptions = true, helpCommand = true, version = "${info.build.version}")
public class CliCommand implements Callable<Integer> {

	@Autowired
	private CSVFormat inputFormat;

	@Autowired
	private Path doneFile;

	@Autowired
	private Set<String> doneInputs;

	@Autowired
	ResourceLoader resourceLoader;

	@Autowired
	private CsvConfig csvConfig;

	int inputsCount;

	int inputsProcessed;

	List<CSVRecord> inputs;

	@Option(names = { "-i", "--input" }, arity = "0..*", description = "CSV lines with ';' and  separated by space")
	private List<String> input = new ArrayList<>();

	@Option(names = { "-f", "--file" }, description = "input csv file path")
	private File inputCsv;

	@Option(names = { "-d", "--dry-run" }, description = "To simulate")
	boolean dryRun;

	@Option(names = { "--force" }, description = "to force all inputs, even already done")
	boolean force;

	@Option(names = { "-w", "--wait" }, description = "milliseconds to wait between 2 inputs")
	long wait = 0;

	/**
	 * All other CLI parameters unmatched to prevent picocli to say option not
	 * recognized and to allow using springboot CLI parameters with --
	 */
	@Unmatched
	private List<String> allOtherParameters;

	/**
	 * Main program
	 */
	@Override
	public Integer call() throws Exception {

		displayLineSeparator();

		checkInputs();

		displayLineSeparator();
		displayMemoryUsed();
		displayLineSeparator();

		long totalTime = 0;

		for (CSVRecord record : inputs) {

			Stopwatch watch = Stopwatch.createStarted();

			doJob(record);

			done(record.get(0));

			// wait to not overload an external resource
			if (wait > 0) {
				log.info("Waiting {}ms", wait);
				Thread.sleep(wait);
			}

			watch.stop();
			long elapsed = watch.elapsed(TimeUnit.MILLISECONDS);
			totalTime += elapsed;
			log.info("Took {}ms", elapsed);
			String estimated = DurationFormatUtils
					.formatDurationHMS((totalTime / inputsProcessed) * (inputsCount - inputsProcessed));
			log.info("Estimated remaining time : {}", estimated);

			displayMemoryUsed();

			displayLineSeparator();

		}

		return 0;
	}

	private void doJob(CSVRecord record) {
		log.info("Input : {}", record);

		AsciiTable at = new AsciiTable();
		at.addRule();
		at.addRow((Object[]) csvConfig.getInputHeaders());
		at.addRule();
		at.addRow(Stream.of(csvConfig.getInputHeaders()).map(record::get).toArray());
		at.addRule();

		log.info("\n{}", at.render());
	}

	private void displayMemoryUsed() {
		log.info("Memory used : {} [MB]", Runtime.getRuntime().totalMemory() / (1024 * 1024));
	}

	/**
	 * Prepare input data
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void checkInputs() throws IOException, FileNotFoundException {
		if (CollectionUtils.isEmpty(input)) {

			if (inputCsv == null) {
				inputCsv = resourceLoader.getResource("input.csv").getFile();
			}

			log.info("Using input csv file '{}'", inputCsv.toString());

			try (Reader in = new FileReader(inputCsv)) {
				inputs = inputFormat.parse(in).getRecords();
			}

		} else {

			log.info("Using inputs supplied with -i : '{}'", input);

			inputs = inputFormat.parse(new StringReader(StringUtils.join(input, System.lineSeparator()))).getRecords();

		}

		log.info("{} inputs already done", doneInputs.size());

		// Remove header if line contains one of configured header name
		inputs.removeIf(record -> StringUtils.equalsAnyIgnoreCase(record.get(0), csvConfig.getInputHeaders()));

		if (force) {
			log.info("--force option activated ! Inputs will be treated again !");
		} else {
			// Remove treated/done inputs
			inputs.removeIf(record -> doneInputs.contains(record.get(0)));
		}

		inputsCount = inputs.size();
		log.info("{} inputs to manage", inputsCount);
	}
	
	/**
	 * Save treated input so that command can be launched again
	 * 
	 * Same input will not be treated next time
	 * 
	 * @param input
	 * @throws IOException
	 */
	private void done(String input) throws IOException {
		if (!dryRun) {
			Files.write(doneFile, Arrays.asList(input), StandardOpenOption.APPEND);
		}
		inputsProcessed++;
		displayLineSeparator();
		log.info("INPUTS RESTANTS : {}", inputsCount - inputsProcessed);
		displayLineSeparator();
	}

	private void displayLineSeparator(Object... objects) {
		log.info("-".repeat(100));
	}

}
