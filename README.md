# CLI project example

This CLI project is a sample using [Picocli](https://link) and [Spring Boot](https://link)

It is a sample to start a CLI Java program

# Usage

## Launch
Examples :
```bat
# help
picocli-springboot-cli -h

# version
picocli-springboot-cli -V

# Use input.csv in same folder
picocli-springboot-cli

# Force use already done lines
picocli-springboot-cli --force

# Use another input csv
picocli-springboot-cli -f path/to/big.csv

# Wait 5s between each CSV line
picocli-springboot-cli -w 5000

# Test CSV lines
picocli-springboot-cli -i 12345,666666,test
picocli-springboot-cli -i 12346,666666,test2 12347,555445,test3
```

## Java runtime
> :warning: **only if** **java-runtime** folder **not present** in zip

Without embedded java runtime, program needs JDK 8

Before launching `picocli-springboot-cli` you must tell which java to use if none configured on the server :

```bat
set JAVACMD=[java home]\bin\java
```

## Help

```bat
bin/picocli-springboot-cli -h
Usage: picocli-springboot-cli [-dhV] [--force] [-f=<inputCsv>] [-w=<wait>] [-i
                              [=<input>...]]...
  -d, --dry-run              To simulate
  -f, --file=<inputCsv>      input csv file path
      --force                to force all inputs, even already done
  -h, --help                 Show this help message and exit.
  -i, --input[=<input>...]   CSV lines with ';' and  separated by space
  -V, --version              Print version information and exit.
  -w, --wait=<wait>          milliseconds to wait between 2 inputs
```

> **bin/input.csv** is used by default (use **-f** if needed)

> **-i** is for testing one or many csv lines from command line
> Example : picocli-springboot-cli -i 004xxx,12345,666666,MICCoPs 004xxx,12345,555445,MIQQiRA

## Folder structure
```

├───bin
|   |   application.properties          # config file
|   |   input.csv                       # CSV 
|   |   picocli-springboot-cli                         # MAIN SCRIPT - unix version
|   |   picocli-springboot-cli.bat                     # MAIN SCRIPT - windows version
|   |   setup                           # not configured
|   |   setup.bat                       # use java runtime if present
│   ├───java-runtime                    # (optionnal) java 11 runtime generated with jlink
│   └───work                            # working folder
|           picocli-springboot-cli.log                 # logs
|           inputs.done                 # treated lines
|           output15-04-2020 060632.csv # output
└───repo                                # all needed jars
```

------------------------------------------------

# Development

## Frameworks

This CLI uses [Picocli](https://picocli.info/) and [SpringBoot](https://spring.io/projects/spring-boot)

## Create artifact

### Without java runtime

We use [appassembler-maven-plugin](https://www.mojohaus.org/appassembler/appassembler-maven-plugin) to generate a **script** that can launch the program with **all needed dependencies**

Then, we use [maven-assembly-plugin](https://maven.apache.org/plugins/maven-assembly-plugin/) to generate **ZIP file**

```shell
mvn clean package
```

Expanded **artifact** will be generated in :file_folder: **target\appassembler**

 Zipped **artifact** will be :package: **target\followup-picocli-springboot-cli-cmd-[version].zip**

### :star: With java runtime 

```bat
@rem Use Oracle JDK 11
set JAVA_HOME=C:\jdk\oracle-jdk.11.0.1-x64

@rem rebuild with JDK 11 
mvn clean package

@rem jre folder must be in appassembler output directory
set RUNTIME_FOLDER=target\appassembler\bin\java-runtime

@rem use jlink with needed jdk modules
%JAVA_HOME%\bin\jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.logging,java.xml,java.naming,java.sql,java.desktop --output %RUNTIME_FOLDER% --module-path %JAVA_HOME%\jmods

@rem Repackage with java runtime
mvn assembly:single
```

## Testing

CLI test can be done by using generated script

```shell
cd target\appassembler\bin
picocli-springboot-cli.bat
```
