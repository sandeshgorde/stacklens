package com.stacklens.classifier;

import com.stacklens.detector.*;
import com.stacklens.model.Issue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Applies all registered detectors to each line of a log and collects detected issues.
 *
 * The classifier is the orchestrator — it holds the list of all active detectors
 * and runs them against each log line. Duplicate issue types are suppressed so
 * that the same error appearing 100 times in a log is only reported once.
 *
 * To add support for a new error type:
 *   1. Create a class implementing IssueDetector
 *   2. Add an instance of it to the detectors list below
 */
public class IssueClassifier {

    /** All registered detectors, evaluated in order for each log line. */
    private final List<IssueDetector> detectors;

    public IssueClassifier() {
        detectors = new ArrayList<>();

        // Register all built-in detectors
        detectors.add(new NullPointerDetector());
        detectors.add(new DatabaseConnectionDetector());
        detectors.add(new TimeoutDetector());
        detectors.add(new ConnectionRefusedDetector());
        detectors.add(new OutOfMemoryDetector());
        detectors.add(new AuthenticationErrorDetector());
        detectors.add(new ThreadPoolExhaustionDetector());
        detectors.add(new Http500Detector());
    }

    /**
     * Analyzes all log lines and returns unique detected issues.
     *
     * Each line is tested against every detector. If a match is found,
     * the issue is recorded. Duplicate issue types are ignored — we only
     * report each type of problem once, even if it appears many times in the log.
     *
     * @param lines the log lines to analyze
     * @return a list of unique issues detected across all lines
     */
    public List<Issue> classify(List<String> lines) {
        // Use a set to track issue types we have already reported
        Set<String> seenIssueTypes = new LinkedHashSet<>();
        List<Issue> detectedIssues = new ArrayList<>();

        for (String line : lines) {
            for (IssueDetector detector : detectors) {
                Optional<Issue> result = detector.detect(line);

                if (result.isPresent()) {
                    Issue issue = result.get();

                    // Only add this issue type if we haven't seen it before
                    if (seenIssueTypes.add(issue.getType())) {
                        detectedIssues.add(issue);
                    }
                }
            }
        }

        return detectedIssues;
    }

    /**
     * Returns the list of all registered detectors.
     * Useful for testing and introspection.
     */
    public List<IssueDetector> getDetectors() {
        return List.copyOf(detectors);
    }
}
