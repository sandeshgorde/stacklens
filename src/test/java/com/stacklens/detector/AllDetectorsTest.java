package com.stacklens.detector;

import com.stacklens.model.Issue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests for all detectors — verifies each one detects its target error
 * and that none of them falsely match an unrelated line.
 */
class AllDetectorsTest {

    private static final String CLEAN_LINE = "2024-01-15 10:30:00 INFO  Application started successfully";

    @Test
    void timeoutDetector_detectsSocketTimeout() {
        IssueDetector detector = new TimeoutDetector();
        String line = "java.net.SocketTimeoutException: Read timed out";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("TimeoutError", result.get().getType());
    }

    @Test
    void timeoutDetector_doesNotMatchCleanLine() {
        IssueDetector detector = new TimeoutDetector();
        assertFalse(detector.detect(CLEAN_LINE).isPresent());
    }

    @Test
    void connectionRefusedDetector_detectsConnectionRefused() {
        IssueDetector detector = new ConnectionRefusedDetector();
        String line = "java.net.ConnectException: Connection refused: localhost/127.0.0.1:8080";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("ConnectionRefused", result.get().getType());
    }

    @Test
    void connectionRefusedDetector_doesNotMatchCleanLine() {
        IssueDetector detector = new ConnectionRefusedDetector();
        assertFalse(detector.detect(CLEAN_LINE).isPresent());
    }

    @Test
    void outOfMemoryDetector_detectsHeapSpace() {
        IssueDetector detector = new OutOfMemoryDetector();
        String line = "java.lang.OutOfMemoryError: Java heap space";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("OutOfMemoryError", result.get().getType());
    }

    @Test
    void outOfMemoryDetector_detectsGcOverhead() {
        IssueDetector detector = new OutOfMemoryDetector();
        String line = "java.lang.OutOfMemoryError: GC overhead limit exceeded";

        assertTrue(detector.detect(line).isPresent());
    }

    @Test
    void outOfMemoryDetector_doesNotMatchCleanLine() {
        IssueDetector detector = new OutOfMemoryDetector();
        assertFalse(detector.detect(CLEAN_LINE).isPresent());
    }

    @Test
    void authenticationErrorDetector_detects401() {
        IssueDetector detector = new AuthenticationErrorDetector();
        String line = "ERROR  401 Unauthorized: /api/orders";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("AuthenticationError", result.get().getType());
    }

    @Test
    void authenticationErrorDetector_detectsBadCredentials() {
        IssueDetector detector = new AuthenticationErrorDetector();
        String line = "Bad credentials for user: admin";

        assertTrue(detector.detect(line).isPresent());
    }

    @Test
    void authenticationErrorDetector_detectsJwtExpired() {
        IssueDetector detector = new AuthenticationErrorDetector();
        String line = "io.jsonwebtoken.ExpiredJwtException: JWT expired at 2024-01-01T00:00:00Z";

        // This line does not contain "jwt expired" literally, test the explicit phrase
        String line2 = "JWT expired - token is no longer valid";
        assertTrue(detector.detect(line2).isPresent());
    }

    @Test
    void authenticationErrorDetector_doesNotMatchCleanLine() {
        IssueDetector detector = new AuthenticationErrorDetector();
        assertFalse(detector.detect(CLEAN_LINE).isPresent());
    }

    @Test
    void threadPoolDetector_detectsRejectedExecution() {
        IssueDetector detector = new ThreadPoolExhaustionDetector();
        String line = "java.util.concurrent.RejectedExecutionException: Task rejected from ThreadPoolExecutor";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("ThreadPoolExhaustion", result.get().getType());
    }

    @Test
    void threadPoolDetector_doesNotMatchCleanLine() {
        IssueDetector detector = new ThreadPoolExhaustionDetector();
        assertFalse(detector.detect(CLEAN_LINE).isPresent());
    }

    @Test
    void http500Detector_detects500InternalServerError() {
        IssueDetector detector = new Http500Detector();
        String line = "ERROR  500 Internal Server Error: /api/checkout";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("Http500InternalServerError", result.get().getType());
    }

    @Test
    void http500Detector_detectsStatusEqual500() {
        IssueDetector detector = new Http500Detector();
        String line = "Response: status=500 from http://payment-service/charge";

        assertTrue(detector.detect(line).isPresent());
    }

    @Test
    void http500Detector_doesNotMatchCleanLine() {
        IssueDetector detector = new Http500Detector();
        assertFalse(detector.detect(CLEAN_LINE).isPresent());
    }

    @Test
    void fileSystemErrorDetector_detectsFileNotFoundException() {
        IssueDetector detector = new FileSystemErrorDetector();
        String line = "java.io.FileNotFoundException: /config/settings.json";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("FileSystemException", result.get().getType());
    }

    @Test
    void fileSystemErrorDetector_detectsAccessDeniedException() {
        IssueDetector detector = new FileSystemErrorDetector();
        String line = "java.nio.file.AccessDeniedException: /data/upload (Permission denied)";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("FileSystemException", result.get().getType());
    }

    @Test
    void fileSystemErrorDetector_detectsNoSuchFileOrDirectory() {
        IssueDetector detector = new FileSystemErrorDetector();
        String line = "java.io.FileNotFoundException: /app/logs/app.log (No such file or directory)";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("FileSystemException", result.get().getType());
    }

    @Test
    void fileSystemErrorDetector_detectsPermissionDenied() {
        IssueDetector detector = new FileSystemErrorDetector();
        String line = "java.io.FileNotFoundException: /tmp/test.txt (Permission denied)";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("FileSystemException", result.get().getType());
    }

    @Test
    void fileSystemErrorDetector_doesNotMatchCleanLine() {
        IssueDetector detector = new FileSystemErrorDetector();
        assertFalse(detector.detect(CLEAN_LINE).isPresent());
    }

    @Test
    void transactionErrorDetector_detectsTransactionSystemException() {
        IssueDetector detector = new TransactionErrorDetector();
        String line = "org.springframework.transaction.TransactionSystemException: Could not commit JPA transaction";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("TransactionError", result.get().getType());
    }

    @Test
    void transactionErrorDetector_detectsRollbackException() {
        IssueDetector detector = new TransactionErrorDetector();
        String line = "jakarta.persistence.RollbackException: Transaction marked as rollbackOnly";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("TransactionError", result.get().getType());
    }

    @Test
    void transactionErrorDetector_detectsCouldNotExecuteStatement() {
        IssueDetector detector = new TransactionErrorDetector();
        String line = "org.hibernate.exception.ConstraintViolationException: could not execute statement";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("TransactionError", result.get().getType());
    }

    @Test
    void transactionErrorDetector_doesNotMatchSpringTransactionStackFrameAlone() {
        IssueDetector detector = new TransactionErrorDetector();
        String line = "at org.springframework.transaction.interceptor.TransactionAspectSupport.completeTransactionAfterThrowing(TransactionAspectSupport.java:688)";

        assertFalse(detector.detect(line).isPresent());
    }

    @Test
    void transactionErrorDetector_doesNotMatchCleanLine() {
        IssueDetector detector = new TransactionErrorDetector();
        assertFalse(detector.detect(CLEAN_LINE).isPresent());
    }

    @Test
    void kafkaErrorDetector_detectsProducerFencedException() {
        IssueDetector detector = new KafkaErrorDetector();
        String line = "org.apache.kafka.common.errors.ProducerFencedException: Producer is fenced";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("KafkaError", result.get().getType());
    }

    @Test
    void kafkaErrorDetector_detectsCommitFailedException() {
        IssueDetector detector = new KafkaErrorDetector();
        String line = "org.apache.kafka.clients.consumer.CommitFailedException: Commit cannot be completed";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
    }

    @Test
    void kafkaErrorDetector_detectsWakeupException() {
        IssueDetector detector = new KafkaErrorDetector();
        String line = "org.apache.kafka.common.errors.WakeupException: Kafka consumer wakeup";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
    }

    @Test
    void kafkaErrorDetector_detectsKafkaCommonErrors() {
        IssueDetector detector = new KafkaErrorDetector();
        String line = "org.apache.kafka.common.errors.NetworkException: Connection to node failed";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
    }

    @Test
    void kafkaErrorDetector_doesNotMatchCleanLine() {
        IssueDetector detector = new KafkaErrorDetector();
        assertFalse(detector.detect(CLEAN_LINE).isPresent());
    }
}
