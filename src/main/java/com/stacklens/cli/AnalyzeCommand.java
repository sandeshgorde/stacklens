package com.stacklens.cli;

import com.stacklens.analyzer.LogAnalyzer;
import com.stacklens.model.AnalysisResult;
import com.stacklens.output.HumanReadableFormatter;
import com.stacklens.output.JsonFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * The "analyze" subcommand — the main command users interact with.
 *
 * Usage examples:
 *   stacklens analyze app.log
 *   stacklens analyze app.log --output json
 *   stacklens analyze --text "java.lang.NullPointerException at OrderService"
 */
@Command(
    name = "analyze",
    description = "Analyze a log file or stack trace and explain detected errors.",
    mixinStandardHelpOptions = true
)
public class AnalyzeCommand implements Callable<Integer> {

    /** Path to the log file to analyze. Optional if --text is provided instead. */
    @Parameters(
        index = "0",
        description = "Path to the log file to analyze.",
        arity = "0..1"
    )
    private Path logFile;

    /**
     * Inline text mode: pass a stack trace or log snippet directly.
     * Use instead of a file path.
     */
    @Option(
        names = {"--text", "-t"},
        description = "Analyze inline text (stack trace or log snippet) instead of a file."
    )
    private String inlineText;

    /**
     * Output format. Defaults to "human" (readable terminal output).
     * Use "json" for machine-readable output.
     */
    @Option(
        names = {"--output", "-o"},
        description = "Output format: human (default) or json.",
        defaultValue = "human"
    )
    private String outputFormat;

    /**
     * Whether to use ANSI color codes in terminal output.
     * Defaults to true. Set to false for plain text (useful in CI or log files).
     */
    @Option(
        names = {"--no-color"},
        description = "Disable ANSI color codes in output.",
        defaultValue = "false"
    )
    private boolean noColor;

    @Override
    public Integer call() {
        // Validate: user must provide either a file OR inline text
        if (logFile == null && inlineText == null) {
            System.err.println("Error: Provide a log file path or use --text to pass inline text.");
            System.err.println("Example: stacklens analyze app.log");
            System.err.println("Example: stacklens analyze --text \"NullPointerException at OrderService\"");
            return 1;
        }

        if (logFile != null && inlineText != null) {
            System.err.println("Error: Provide either a file path or --text, not both.");
            return 1;
        }

        try {
            LogAnalyzer analyzer = new LogAnalyzer();
            AnalysisResult result;

            if (logFile != null) {
                result = analyzer.analyzeFile(logFile);
            } else {
                result = analyzer.analyzeText(inlineText);
            }

            // Print the result in the requested format
            String output = formatResult(result);
            System.out.println(output);

            // Exit code: 0 = no issues, 2 = issues found (useful for scripting)
            return result.hasIssues() ? 2 : 0;

        } catch (java.nio.file.NoSuchFileException e) {
            System.err.println("Error: File not found: " + e.getMessage());
            return 1;
        } catch (java.io.IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return 1;
        }
    }

    /** Formats the result according to the --output flag. */
    private String formatResult(AnalysisResult result) {
        if ("json".equalsIgnoreCase(outputFormat)) {
            return new JsonFormatter().format(result);
        }
        // Default: human-readable output with optional colors
        boolean useColors = !noColor;
        return new HumanReadableFormatter(useColors).format(result);
    }
}
