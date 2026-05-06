package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

/**
 * Detects common Kafka producer and consumer errors in log lines.
 *
 * Matches patterns like ProducerFencedException, CommitFailedException,
 * WakeupException, and common errors from org.apache.kafka.common.errors.
 * These errors are frequent in microservice stacks with Kafka integration.
 */
public class KafkaErrorDetector implements IssueDetector {

    /**
     * Returns the issue type identifier for Kafka errors.
     *
     * @return the string "KafkaError"
     */
    @Override
    public String getIssueType() { return "KafkaError"; }

    /**
     * Returns the severity level for Kafka errors.
     *
     * @return Severity.ERROR
     */
    @Override
    public Severity getSeverity() { return Severity.ERROR; }

    /**
     * Analyzes a log line for Kafka producer/consumer error patterns.
     *
     * Checks for:
     * - ProducerFencedException (producer fenced by broker)
     * - CommitFailedException (consumer commit failed)
     * - WakeupException (consumer/producer wakeup)
     * - org.apache.kafka.common.errors (any Kafka common error)
     *
     * @param line a single line from the log or stack trace
     * @return an Optional containing the detected Issue, or empty if no match
     */
    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        if (line.contains("ProducerFencedException") ||
            line.contains("CommitFailedException") ||
            line.contains("WakeupException") ||
            line.contains("org.apache.kafka.common.errors")) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "A Kafka producer or consumer error occurred. This is common in microservice stacks " +
                "when there are issues with broker connectivity, consumer group coordination, or " +
                "producer/consumer lifecycle management.",
                List.of(
                    "Check that Kafka broker is running and accessible from the application",
                    "Verify consumer group ID configuration and avoid duplicate consumers joining the group",
                    "Ensure producer/consumer are properly closed during application shutdown",
                    "Check for network issues or incorrect broker addresses in configuration",
                    "Verify that the topic exists and the application has permission to produce/consume"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}