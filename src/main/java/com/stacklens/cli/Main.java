package com.stacklens.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Entry point for the StackLens CLI application.
 *
 * StackLens is a developer tool that reads Java and Spring Boot logs
 * or stack traces and explains the root cause of errors with suggested fixes.
 *
 * Usage:
 *   stacklens --help
 *   stacklens analyze app.log
 *   stacklens analyze --text "java.lang.NullPointerException at OrderService"
 *   stacklens analyze app.log --output json
 */
@Command(
    name = "stacklens",
    description = "Analyze Java and Spring Boot logs to explain errors and suggest fixes.",
    mixinStandardHelpOptions = true,
    version = "StackLens 1.0.0",
    subcommands = {
        AnalyzeCommand.class
    }
)
public class Main implements Runnable {

    /**
     * Called when "stacklens" is invoked with no subcommand.
     * Prints usage help to guide the user.
     */
    @Override
    public void run() {
        // When no subcommand is given, show help
        CommandLine.usage(this, System.out);
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
