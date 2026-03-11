package com.stacklens.analyzer;

import com.stacklens.classifier.IssueClassifier;
import com.stacklens.model.AnalysisResult;
import com.stacklens.model.Issue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Reads log content (from file or text) and coordinates analysis.
 *
 * LogAnalyzer is the entry point for analysis. It handles I/O
 * (reading files or splitting pasted text) and delegates detection
 * to IssueClassifier.
 */
public class LogAnalyzer {

    private final IssueClassifier classifier;

    public LogAnalyzer() {
        this.classifier = new IssueClassifier();
    }

    // Package-private constructor for testing with a custom classifier
    LogAnalyzer(IssueClassifier classifier) {
        this.classifier = classifier;
    }

    /**
     * Reads a log file from disk and analyzes its contents.
     *
     * @param filePath path to the log file
     * @return analysis result containing all detected issues
     * @throws IOException if the file cannot be read
     */
    public AnalysisResult analyzeFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<Issue> issues = classifier.classify(lines);
        return new AnalysisResult(filePath.toString(), issues);
    }

    /**
     * Analyzes a stack trace or log text pasted directly as a string.
     *
     * The text is split into individual lines for analysis.
     *
     * @param text the raw log or stack trace text
     * @return analysis result containing all detected issues
     */
    public AnalysisResult analyzeText(String text) {
        // Split on newlines; handle both Unix (\n) and Windows (\r\n) line endings
        List<String> lines = Arrays.asList(text.split("\\r?\\n"));
        List<Issue> issues = classifier.classify(lines);
        return new AnalysisResult("inline text", issues);
    }
}
