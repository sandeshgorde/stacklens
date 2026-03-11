package com.stacklens.output;

import com.stacklens.model.AnalysisResult;
import com.stacklens.model.Issue;

/**
 * Formats analysis results as human-readable terminal output.
 *
 * Produces clearly structured output with section headers, explanations,
 * and a numbered list of suggested fixes.
 */
public class HumanReadableFormatter {

    // Simple ANSI color codes for terminals that support them
    private static final String BOLD   = "\033[1m";
    private static final String RED    = "\033[31m";
    private static final String YELLOW = "\033[33m";
    private static final String GREEN  = "\033[32m";
    private static final String CYAN   = "\033[36m";
    private static final String RESET  = "\033[0m";

    private final boolean useColors;

    /**
     * @param useColors set to true to enable ANSI color codes in output
     */
    public HumanReadableFormatter(boolean useColors) {
        this.useColors = useColors;
    }

    /**
     * Formats a full analysis result as a printable string.
     *
     * @param result the analysis result to format
     * @return formatted string ready to print to stdout
     */
    public String format(AnalysisResult result) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append(bold("StackLens Analysis Report"));
        sb.append("\n");
        sb.append(bold("Source: ")).append(result.getSource());
        sb.append("\n");
        sb.append("─".repeat(60)).append("\n");

        if (!result.hasIssues()) {
            sb.append(green("\n✓ No known issues detected in the provided log.\n"));
            sb.append("  The log looks clean, but always review warnings manually.\n");
            return sb.toString();
        }

        sb.append("\n");
        sb.append(red("✗ " + result.getIssues().size() + " issue(s) detected:\n"));
        sb.append("\n");

        int issueNumber = 1;
        for (Issue issue : result.getIssues()) {
            formatIssue(sb, issue, issueNumber++);
        }

        return sb.toString();
    }

    /** Formats a single Issue block. */
    private void formatIssue(StringBuilder sb, Issue issue, int number) {
        sb.append("─".repeat(60)).append("\n");
        sb.append(bold(yellow("Issue #" + number + ": " + issue.getType()))).append("\n\n");

        sb.append(bold("Detected in:")).append("\n");
        sb.append("  ").append(cyan(truncate(issue.getMatchedLine(), 120))).append("\n\n");

        sb.append(bold("Explanation:")).append("\n");
        // Word-wrap the explanation at ~70 characters for readability
        for (String wrappedLine : wordWrap(issue.getExplanation(), 70)) {
            sb.append("  ").append(wrappedLine).append("\n");
        }
        sb.append("\n");

        sb.append(bold("Suggested fixes:")).append("\n");
        int i = 1;
        for (String suggestion : issue.getSuggestions()) {
            sb.append("  ").append(i++).append(". ").append(suggestion).append("\n");
        }
        sb.append("\n");
    }

    /** Truncates a string to maxLength and appends "..." if truncated. */
    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Wraps text at word boundaries to fit within maxWidth characters.
     * Returns a list of wrapped lines.
     */
    private java.util.List<String> wordWrap(String text, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() + word.length() + 1 > maxWidth) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                if (current.length() > 0) current.append(" ");
                current.append(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    // Color helper methods — return colored text if colors are enabled, plain text otherwise

    private String bold(String text) {
        return useColors ? BOLD + text + RESET : text;
    }

    private String red(String text) {
        return useColors ? RED + text + RESET : text;
    }

    private String yellow(String text) {
        return useColors ? YELLOW + text + RESET : text;
    }

    private String green(String text) {
        return useColors ? GREEN + text + RESET : text;
    }

    private String cyan(String text) {
        return useColors ? CYAN + text + RESET : text;
    }
}
