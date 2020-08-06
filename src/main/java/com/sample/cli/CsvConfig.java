package com.sample.cli;

import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties
@Data
@Slf4j
public class CsvConfig {

	private String[] inputHeaders = new String[] {"header1", "header2", "header3"};
	
    private char inputSeparator = ',';
    
    @Bean
	public CSVFormat inputFormat() {
		log.info("Input CSV headers : '{}'", Arrays.deepToString(inputHeaders));
		return CSVFormat.EXCEL
				.withDelimiter(inputSeparator)
				.withCommentMarker('#')
				.withHeader(inputHeaders)
				.withIgnoreEmptyLines()
				.withIgnoreHeaderCase()
				.withAllowMissingColumnNames()
				.withAllowDuplicateHeaderNames();
	}
    
}