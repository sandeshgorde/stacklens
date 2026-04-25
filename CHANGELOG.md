# Changelog

All notable changes to StackLens will be documented in this file.

## [1.2.0] - 2026-04-25

### Added

- Feat: add TransactionErrorDetector and corresponding tests

### CI

- Ci: auto-extract release notes from CHANGELOG

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
- Ci: automate CHANGELOG generation with git-cliff

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
- Ci: update git-cliff release action

### Documentation

- Docs: fix 1.0.0 release date to match actual initial commit

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

## [1.1.0] - 2026-04-17

### Added

- Feat: v1.1.0 — severity levels, occurrence counts, stack location, 5 new detectors, stdin, summary mode, demo GIF

- Add Severity enum (CRITICAL / ERROR / WARNING) to every detector
- Enrich Issue model with occurrenceCount, stackContext, and location
- Overhaul IssueClassifier: multi-line stack trace extraction, occurrence
  counting, app-frame location detection (skips JDK/Spring/Hibernate internals)
- Add 5 new detectors (8 → 13 total):
    SpringBeanDetector, LazyInitializationDetector, ClassCastDetector,
    StackOverflowDetector, ConcurrentModificationDetector
- Add --summary / -s flag: compact one-line-per-issue table
- Add stdin support: stacklens analyze - (pipe from kubectl/docker logs)
- Update HumanReadableFormatter: severity badge, count, location, stack context
- Update JsonFormatter: severity, occurrences, location, matchedLine, stackContext
- Add IssueClassifierTest and NewDetectorsTest (73 tests total, all passing)
- Add Playwright demo script: scripts/generate-demo.mjs → docs/demo.gif
- Update README: animated demo GIF, before/after table, revised feature list

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>

## [1.0.0] - 2026-04-13

### Added

- Feat: add domain model classes (Issue, AnalysisResult)

Issue holds the type, explanation, suggestions, and matched log line
for a single detected problem. AnalysisResult wraps a list of Issues
with the source descriptor (file path or inline text).

https://claude.ai/code/session_018keNUAUtdxDJb3GFGEBLo4
- Feat: add IssueDetector interface and 8 built-in detectors

Adds the IssueDetector interface and implementations for:
- NullPointerDetector
- DatabaseConnectionDetector (HikariCP, JDBC, driver not found)
- TimeoutDetector (SocketTimeoutException, gateway timeout)
- ConnectionRefusedDetector
- OutOfMemoryDetector (heap space, GC overhead, Metaspace)
- AuthenticationErrorDetector (401, 403, bad credentials, JWT expired)
- ThreadPoolExhaustionDetector (RejectedExecutionException)
- Http500Detector

Each detector matches one line at a time and returns an Optional<Issue>
with a clear explanation and actionable suggestions.

https://claude.ai/code/session_018keNUAUtdxDJb3GFGEBLo4
- Feat: add IssueClassifier and LogAnalyzer

IssueClassifier applies all registered detectors to each log line
and deduplicates results by issue type — so the same error appearing
100 times in a log is only reported once.

LogAnalyzer is the entry point: reads files via NIO or splits inline
text into lines and delegates to IssueClassifier.

https://claude.ai/code/session_018keNUAUtdxDJb3GFGEBLo4
- Feat: add output formatters and CLI commands

Output layer:
- HumanReadableFormatter: ANSI-colored terminal output with word-wrapped
  explanations and numbered suggestions
- JsonFormatter: pretty-printed JSON using Jackson records

CLI layer (picocli):
- Main: root command entry point, shows help when no subcommand given
- AnalyzeCommand: 'analyze' subcommand supporting file input and --text
  inline mode, --output json, --no-color; exit codes 0/1/2 for scripting

https://claude.ai/code/session_018keNUAUtdxDJb3GFGEBLo4
- Feat: add sample log files for manual testing

Five realistic sample logs covering all detector types:
- sample-npe.log: NullPointerException in OrderService
- sample-db-failure.log: HikariCP/JDBC connection failure
- sample-oom.log: OutOfMemoryError in batch processing job
- sample-mixed-errors.log: auth failure, timeout, thread pool exhaustion, HTTP 500
- sample-healthy.log: clean log (no errors)

Also updated .gitignore to allow samples/*.log files.

https://claude.ai/code/session_018keNUAUtdxDJb3GFGEBLo4

### CI

- Ci: add GitHub Actions workflow and issue/PR templates

CI workflow (ci.yml):
- Triggers on push to main/claude/** and PRs to main
- Builds and tests on Java 17 and Java 21 (matrix)
- Uploads test results and the fat JAR as build artifacts

GitHub templates:
- bug_report.md: structured bug report with reproduction steps
- feature_request.md: feature/detector request with example log lines
- pull_request_template.md: checklist-driven PR description

https://claude.ai/code/session_018keNUAUtdxDJb3GFGEBLo4

### Documentation

- Docs: add README, CONTRIBUTING, CHANGELOG, LICENSE, and architecture docs

README.md:
- Project overview, why it exists, feature table
- Installation from source and shell alias setup
- Full usage examples with human-readable and JSON output samples
- Architecture diagram, project structure, exit codes, roadmap

CONTRIBUTING.md:
- Step-by-step guide to adding a new error detector
- Testing instructions, code style guidelines, PR process

CHANGELOG.md: documents 1.0.0 release with all features listed

LICENSE: MIT License

docs/architecture.md: component diagram, data flow, extension points

https://claude.ai/code/session_018keNUAUtdxDJb3GFGEBLo4

### Testing

- Test: add JUnit 5 unit tests for detectors, analyzer, and CLI

Tests cover:
- NullPointerDetectorTest: match, no-match, null input, trimming
- DatabaseConnectionDetectorTest: JDBC, CommunicationsLinkFailure, PSQLException
- AllDetectorsTest: smoke tests for all 8 detectors (positive + negative cases)
- LogAnalyzerTest: file and text mode, deduplication, Windows line endings
- AnalyzeCommandTest: CLI exit codes, JSON output, missing file, no args

https://claude.ai/code/session_018keNUAUtdxDJb3GFGEBLo4


