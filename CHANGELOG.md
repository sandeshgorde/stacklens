# Changelog

All notable changes to StackLens will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Planned
- AI-powered explanations using LLM APIs
- Spring Boot structured log format support
- Watch mode to tail log files in real time
- Plugin system for custom detectors
- IntelliJ / VS Code plugin

---

## [1.1.0] - 2026-04-17

### Added

- **Severity levels** — every issue is now classified as `CRITICAL`, `ERROR`, or `WARNING`
- **Occurrence counting** — repeated matches are counted and shown (e.g. `NullPointerException ×47`)
- **Stack trace extraction** — frames following the matched line are collected and shown as context
- **Location detection** — the first application-owned stack frame is extracted and shown as a `Location:` field (skips JDK, Spring, Hibernate internal frames)
- **`--summary` / `-s` flag** — compact one-line-per-issue table with severity, count, and location; ideal for quick triage or pasting into Slack
- **Stdin support** — pass `-` as the file argument to read from stdin (`kubectl logs pod | stacklens analyze -`)
- **5 new detectors** (8 → 13 total):
  - `SpringBeanDetector` — `NoSuchBeanDefinitionException`, `BeanCreationException`, circular dependencies (CRITICAL)
  - `LazyInitializationDetector` — Hibernate `LazyInitializationException`, `could not initialize proxy` (ERROR)
  - `ClassCastDetector` — `ClassCastException`, `cannot be cast to` (ERROR)
  - `StackOverflowDetector` — `StackOverflowError` (CRITICAL)
  - `ConcurrentModificationDetector` — `ConcurrentModificationException` (ERROR)
- **`IssueClassifierTest`** — new test class covering counting, context, and location extraction
- **`NewDetectorsTest`** — smoke tests for all five new detectors

### Changed

- `Issue` model now carries `severity`, `occurrenceCount`, `stackContext`, and `location`
- `IssueDetector` interface gains `getSeverity()` method
- `IssueClassifier` is now multi-line aware — scans up to 15 stack frames after each match
- Detectors now sorted by severity in the classifier (CRITICAL reported first)
- JSON output now includes `severity`, `occurrences`, `location`, `matchedLine`, and `stackContext` fields
- JSON output omits `null` fields (e.g. `location` when no app frame is found)
- `AnalyzeCommand` now accepts a file path string (supports `-` for stdin) instead of `java.nio.file.Path`
- Human-readable output shows severity badge, occurrence count, location, and stack context per issue

---

## [1.0.0] - 2026-03-11

### Added

- Initial release of StackLens
- `analyze` CLI command supporting file input and inline text (`--text`)
- Human-readable terminal output with ANSI color support
- JSON output mode (`--output json`) for scripting and integrations
- **8 built-in error detectors:**
  - `NullPointerDetector` — detects `NullPointerException`
  - `DatabaseConnectionDetector` — detects JDBC and HikariCP connection failures
  - `TimeoutDetector` — detects `SocketTimeoutException` and request timeouts
  - `ConnectionRefusedDetector` — detects `Connection refused` errors
  - `OutOfMemoryDetector` — detects `OutOfMemoryError` (heap space, GC overhead)
  - `AuthenticationErrorDetector` — detects 401/403, bad credentials, expired JWT
  - `ThreadPoolExhaustionDetector` — detects `RejectedExecutionException`
  - `Http500Detector` — detects HTTP 500 Internal Server Error
- Deduplication of repeated errors (each issue type reported once)
- Exit code signaling: `0` = clean, `1` = error, `2` = issues detected
- JUnit 5 unit tests for all detectors, analyzer, and CLI
- Sample log files for manual testing
- GitHub Actions CI workflow (Java 17 and 21)
- MIT License
