package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

public class FileSystemErrorDetector implements IssueDetector {

    @Override
    public String getIssueType() { return "FileSystemException"; }

    @Override
    public Severity getSeverity() { return Severity.ERROR; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        if (line.contains("FileNotFoundException") ||
            line.contains("AccessDeniedException") ||
            line.contains("No such file or directory") ||
            (line.toLowerCase().contains("permission denied") &&
             (line.contains("java.io") || line.contains("/") || line.contains("\\")))) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "A file system operation failed due to missing files or incorrect permissions. " +
                "This is common in misconfigured deployments and containerized environments " +
                "where volumes may not be mounted or have wrong permissions.",
                List.of(
                    "Verify the file or directory path exists in the container",
                    "Check that Docker volume mounts are correctly configured",
                    "Ensure the application has read/write permissions to the mounted path",
                    "Use chown or chmod to fix file ownership and permissions",
                    "Check if the file is mounted as read-only when write access is needed"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}