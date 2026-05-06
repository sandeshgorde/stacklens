package com.stacklens.classifier;

import com.stacklens.detector.*;
import com.stacklens.model.Issue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Applies all registered detectors to a log and collects enriched issues.
 *
 * Improvements over naive line-by-line scanning:
 *  - Multi-line awareness: when a match is found, the following stack trace
 *    frames are collected and stored on the Issue as context.
 *  - Occurrence counting: each additional match of the same issue type
 *    increments a counter rather than being silently dropped.
 *  - Location extraction: the first non-framework stack frame is identified
 *    and reported as the probable source location in application code.
 *
 * To add support for a new error type:
 *   1. Create a class implementing IssueDetector
 *   2. Add an instance of it to the detectors list in the constructor
 */
public class IssueClassifier {

    /** Packages considered "internal" — skipped when looking for the app frame. */
    private static final List<String> INTERNAL_PREFIXES = List.of(
        "java.", "javax.", "jakarta.", "sun.", "com.sun.", "jdk.",
        "org.springframework.", "org.hibernate.", "org.apache.",
        "com.fasterxml.", "picocli.", "org.junit.", "org.mockito.",
        "com.zaxxer.", "io.netty.", "reactor."
    );

    /** Maximum number of stack frames to collect after a matching line. */
    private static final int MAX_CONTEXT_FRAMES = 15;

    private final List<IssueDetector> detectors;

    public IssueClassifier() {
        detectors = new ArrayList<>();

        // CRITICAL severity first so they surface at the top of results
        detectors.add(new OutOfMemoryDetector());
        detectors.add(new ThreadPoolExhaustionDetector());
        detectors.add(new StackOverflowDetector());
        detectors.add(new SpringBeanDetector());

        // ERROR severity
        detectors.add(new NullPointerDetector());
        detectors.add(new DatabaseConnectionDetector());
        detectors.add(new ConnectionRefusedDetector());
        detectors.add(new LazyInitializationDetector());
        detectors.add(new ClassCastDetector());
        detectors.add(new ConcurrentModificationDetector());
        detectors.add(new Http500Detector());
        detectors.add(new FileSystemErrorDetector());
        detectors.add(new TransactionErrorDetector());
        detectors.add(new KafkaErrorDetector());

        // WARNING severity
        detectors.add(new TimeoutDetector());
        detectors.add(new AuthenticationErrorDetector());
    }

    /**
     * Analyzes all log lines and returns enriched, unique issues.
     *
     * For each issue type, stores:
     *  - The matched line from the first occurrence
     *  - The stack trace context (frames following the match)
     *  - The first application-owned frame as a location string
     *  - The total number of occurrences across the whole log
     *
     * @param lines the log lines to analyze
     * @return list of issues ordered by first occurrence (CRITICAL before ERROR before WARNING)
     */
    public List<Issue> classify(List<String> lines) {
        // LinkedHashMap preserves insertion order (= first-occurrence order)
        Map<String, IssueAccumulator> accumulators = new LinkedHashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            for (IssueDetector detector : detectors) {
                Optional<Issue> result = detector.detect(line);

                if (result.isPresent()) {
                    String type = detector.getIssueType();

                    if (!accumulators.containsKey(type)) {
                        // First occurrence: collect context and location
                        List<String> context = collectStackContext(lines, i + 1);
                        String location = extractAppLocation(context);
                        accumulators.put(type, new IssueAccumulator(result.get(), context, location));
                    } else {
                        accumulators.get(type).incrementCount();
                    }

                    // A line matches at most one detector — avoids double-counting
                    break;
                }
            }
        }

        return accumulators.values().stream()
            .map(IssueAccumulator::toEnrichedIssue)
            .toList();
    }

    /**
     * Collects stack trace lines starting at {@code startIndex}.
     * Stops when a non-stack-trace line is encountered or the frame limit is reached.
     */
    private List<String> collectStackContext(List<String> lines, int startIndex) {
        List<String> frames = new ArrayList<>();

        for (int i = startIndex; i < lines.size() && frames.size() < MAX_CONTEXT_FRAMES; i++) {
            String trimmed = lines.get(i).trim();

            if (trimmed.startsWith("at ")
                    || trimmed.startsWith("Caused by:")
                    || trimmed.startsWith("... ")
                    || trimmed.startsWith("Suppressed:")) {
                frames.add(trimmed);
            } else if (trimmed.isEmpty()) {
                // Allow a single blank line inside a stack trace block
                if (!frames.isEmpty()) break;
            } else {
                break;
            }
        }

        return frames;
    }

    /**
     * Finds the first stack frame that belongs to application code (not a framework
     * or JDK package) and returns it as a short location string.
     *
     * Example: "OrderService.processOrder(OrderService.java:142)"
     */
    private String extractAppLocation(List<String> frames) {
        for (String frame : frames) {
            if (!frame.startsWith("at ")) continue;

            String frameContent = frame.substring(3).trim(); // strip "at "

            if (!isInternalFrame(frameContent)) {
                // frameContent = "com.example.OrderService.processOrder(OrderService.java:142)"
                // Return everything after the last '.' before the '(' — i.e. Class.method(...)
                int parenIdx = frameContent.indexOf('(');
                if (parenIdx < 0) return frameContent;

                String classAndMethod = frameContent.substring(0, parenIdx);
                String fileAndLine = frameContent.substring(parenIdx); // "(OrderService.java:142)"

                int lastDot = classAndMethod.lastIndexOf('.');
                int secondLastDot = lastDot > 0 ? classAndMethod.lastIndexOf('.', lastDot - 1) : -1;

                // Include ClassName.methodName
                String shortName = secondLastDot >= 0
                    ? classAndMethod.substring(secondLastDot + 1)
                    : classAndMethod;

                return shortName + fileAndLine;
            }
        }
        return null;
    }

    private boolean isInternalFrame(String frame) {
        for (String prefix : INTERNAL_PREFIXES) {
            if (frame.startsWith(prefix)) return true;
        }
        return false;
    }

    public List<IssueDetector> getDetectors() {
        return List.copyOf(detectors);
    }

    // ── Internal accumulator ──────────────────────────────────────────────────

    private static class IssueAccumulator {
        private final Issue firstIssue;
        private final List<String> stackContext;
        private final String location;
        private int count = 1;

        IssueAccumulator(Issue firstIssue, List<String> stackContext, String location) {
            this.firstIssue = firstIssue;
            this.stackContext = stackContext;
            this.location = location;
        }

        void incrementCount() { count++; }

        Issue toEnrichedIssue() {
            return new Issue(
                firstIssue.getType(),
                firstIssue.getSeverity(),
                firstIssue.getExplanation(),
                firstIssue.getSuggestions(),
                firstIssue.getMatchedLine(),
                count,
                stackContext,
                location
            );
        }
    }
}
