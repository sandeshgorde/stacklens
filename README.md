# StackLens

> Point it at a log. Know what's broken, where, and how to fix it — in seconds.

[![CI](https://github.com/AbaSheger/stacklens/actions/workflows/ci.yml/badge.svg)](https://github.com/AbaSheger/stacklens/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://adoptium.net/)
[![Version](https://img.shields.io/badge/version-1.2.0-blue.svg)](https://github.com/AbaSheger/stacklens/releases)

![StackLens demo](docs/demo.gif)

---

```
$ java -jar stacklens.jar analyze app.log

StackLens Analysis Report — Source: app.log
────────────────────────────────────────────────────────────
✗ 3 issue type(s) detected  (73 total occurrences)
  1 CRITICAL   1 ERROR   1 WARNING

[CRITICAL]  Issue #1: SpringBeanFailure

  Location:  UserServiceConfig.configure(UserServiceConfig.java:34)

  Explanation:
    Spring failed to start. A bean could not be created because a
    required dependency is missing or misconfigured.

  Suggested fixes:
    1. Check the full startup log — the root cause is usually a few lines below
    2. Ensure all @Component classes are in a package scanned by @SpringBootApplication
    ...

[ERROR]  Issue #2: NullPointerException  ×47

  Location:  OrderService.createOrder(OrderService.java:42)
  ...

[WARNING]  Issue #3: TimeoutError  ×25

  Location:  OrderRepository.findAll(OrderRepository.java:88)
  ...
```

---

## Why StackLens?

You open a production log. There are 800 lines. Somewhere in there is the reason your service went down at 3am.

StackLens reads the log and tells you:

| Without StackLens | With StackLens |
|---|---|
| Scroll through 800 lines | Instant summary |
| Grep for exception names | Exact class + line number |
| Google the error | Explanation + fixes inline |
| No idea how often it happened | `NullPointerException ×47` |
| Works per-request | Pipe `kubectl logs` straight in |

It's offline, dependency-free, and works with any Java or Spring Boot application.

---

## Features

- Analyze log **files**, **paste stack traces**, or **pipe from stdin** (`kubectl logs`, `docker logs`)
- Detects **13 common backend failure types** out of the box
- **Severity levels**: CRITICAL / ERROR / WARNING so you know what to fix first
- **Occurrence counting**: "NullPointerException ×47" tells you how bad it really is
- **Stack trace context**: shows the first app-owned frame so you know exactly where to look
- **`--summary` mode**: one-line-per-issue table for quick triage
- Outputs **human-readable** or **JSON** format
- **Exit codes** for scripting: `0` = clean, `2` = issues found
- Extensible: add new detectors by implementing one interface

### Detected Error Types

| Severity | Error Type | Example Pattern |
|---|---|---|
| CRITICAL | `OutOfMemoryError` | `OutOfMemoryError: Java heap space` |
| CRITICAL | `ThreadPoolExhaustion` | `RejectedExecutionException: Task rejected` |
| CRITICAL | `StackOverflowError` | `java.lang.StackOverflowError` |
| CRITICAL | `SpringBeanFailure` | `NoSuchBeanDefinitionException`, `BeanCreationException` |
| ERROR | `NullPointerException` | `java.lang.NullPointerException at ...` |
| ERROR | `DatabaseConnectionFailure` | `Unable to acquire JDBC Connection` |
| ERROR | `ConnectionRefused` | `Connection refused: localhost:8080` |
| ERROR | `LazyInitializationException` | `could not initialize proxy - no Session` |
| ERROR | `ClassCastException` | `cannot be cast to` |
| ERROR | `ConcurrentModificationException` | `java.util.ConcurrentModificationException` |
| ERROR | `Http500InternalServerError` | `500 Internal Server Error` |
| WARNING | `TimeoutError` | `SocketTimeoutException: Read timed out` |
| WARNING | `AuthenticationError` | `401 Unauthorized`, `Bad credentials`, `JWT expired` |

---

## Installation

### Prerequisites

- Java 17 or higher

### Download (Recommended)

Download the latest pre-built JAR from the [Releases page](https://github.com/AbaSheger/stacklens/releases/latest).

**Linux/macOS:**
```bash
curl -L https://github.com/AbaSheger/stacklens/releases/latest/download/stacklens.jar -o stacklens.jar
java -jar stacklens.jar analyze app.log
```

**Windows PowerShell:**
```powershell
Invoke-WebRequest https://github.com/AbaSheger/stacklens/releases/latest/download/stacklens.jar -OutFile stacklens.jar
java -jar stacklens.jar analyze app.log
```

### Build from Source

```bash
git clone https://github.com/AbaSheger/stacklens.git
cd stacklens
mvn clean package -DskipTests
```

This produces `target/stacklens.jar`.

### Create a Shell Alias (Optional)

For convenience, add this to your `~/.bashrc` or `~/.zshrc`:

```bash
alias stacklens='java -jar /path/to/stacklens.jar'
```

Then reload your shell:

```bash
source ~/.bashrc
```

---

## Usage

### Analyze a log file

```bash
java -jar stacklens.jar analyze app.log
```

### Analyze an inline stack trace

```bash
java -jar stacklens.jar analyze --text "java.lang.NullPointerException at OrderService.java:42"
```

### Pipe from stdin (kubectl, docker, etc.)

```bash
kubectl logs my-pod | java -jar stacklens.jar analyze -
docker logs my-container | java -jar stacklens.jar analyze -
```

### Quick summary table

```bash
java -jar stacklens.jar analyze app.log --summary
```

### Output as JSON

```bash
java -jar stacklens.jar analyze app.log --output json
```

### Disable ANSI colors (for CI or log files)

```bash
java -jar stacklens.jar analyze app.log --no-color
```

### Get help

```bash
java -jar stacklens.jar --help
java -jar stacklens.jar analyze --help
```

---

## Example Output

### Human-Readable

```
StackLens Analysis Report
Source: samples/sample-npe.log
────────────────────────────────────────────────────────────

✗ 1 issue type(s) detected (1 total occurrence(s))
  1 ERROR

────────────────────────────────────────────────────────────
[ERROR]  Issue #1: NullPointerException

Location:
  OrderService.createOrder(OrderService.java:42)

Detected in:
  java.lang.NullPointerException: Cannot invoke "com.example.User.getEmail()" because "user" is null
  at com.example.OrderService.createOrder(OrderService.java:42)
  at com.example.OrderController.createOrder(OrderController.java:28)
  at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
  ... 2 more frame(s)

Explanation:
  A null object reference was accessed. This happens when code calls a
  method or accesses a field on an object that has not been initialized
  (is null).

Suggested fixes:
  1. Ensure all objects are properly initialized before use
  2. Add null checks before accessing object fields or methods
  3. Use Optional<T> to express that a value may be absent
  4. Validate method parameters at the start of each method
  5. Check if a dependency injection (e.g. @Autowired) is missing or failed
```

### Summary Mode (`--summary`)

```
StackLens Summary — Source: app.log
──────────────────────────────────────────────────────────────────────

4 issue type(s)  •  73 total occurrence(s)

[CRITICAL]     SpringBeanFailure                   ×1    —
[CRITICAL]     ThreadPoolExhaustion                ×3    RequestDispatcher.dispatch(RequestDispatcher.java:88)
[ERROR]        NullPointerException                ×47   OrderService.createOrder(OrderService.java:42)
[WARNING]      TimeoutError                        ×22   OrderRepository.findAll(OrderRepository.java:33)
```

### JSON Output

```json
{
  "source": "samples/sample-npe.log",
  "issueCount": 1,
  "totalOccurrences": 1,
  "issues": [ {
    "issue": "NullPointerException",
    "severity": "ERROR",
    "occurrences": 1,
    "location": "OrderService.createOrder(OrderService.java:42)",
    "matchedLine": "java.lang.NullPointerException: ...",
    "stackContext": [
      "at com.example.OrderService.createOrder(OrderService.java:42)",
      "at com.example.OrderController.createOrder(OrderController.java:28)"
    ],
    "explanation": "A null object reference was accessed...",
    "suggestions": [
      "Ensure all objects are properly initialized before use",
      "Add null checks before accessing object fields or methods"
    ]
  } ]
}
```

### Clean Log

```
StackLens Analysis Report
Source: samples/sample-healthy.log
────────────────────────────────────────────────────────────

✓ No known issues detected in the provided log.
  The log looks clean, but always review warnings manually.
```

---

## Exit Codes

StackLens uses exit codes to support shell scripting:

| Code | Meaning |
|---|---|
| `0` | No issues detected |
| `1` | Error (file not found, invalid arguments) |
| `2` | One or more issues detected |

**Example: fail a CI build if issues are found:**

```bash
java -jar stacklens.jar analyze app.log || exit 1
```

---

## Sample Log Files

The `samples/` directory contains log files you can use to try StackLens:

| File | Contains |
|---|---|
| `sample-npe.log` | NullPointerException |
| `sample-db-failure.log` | Database connection failure |
| `sample-oom.log` | OutOfMemoryError |
| `sample-mixed-errors.log` | Auth failure, timeout, thread pool exhaustion, HTTP 500 |
| `sample-healthy.log` | Clean log (no errors) |

```bash
java -jar target/stacklens.jar analyze samples/sample-mixed-errors.log
```

---

## Architecture

StackLens follows a clean, layered architecture that makes it easy to extend:

```
CLI (picocli)
    └── AnalyzeCommand
            └── LogAnalyzer          (reads files / text, drives analysis)
                    └── IssueClassifier   (applies all detectors to each line)
                            └── IssueDetector (interface)
                                    ├── NullPointerDetector
                                    ├── DatabaseConnectionDetector
                                    ├── TimeoutDetector
                                    ├── ConnectionRefusedDetector
                                    ├── OutOfMemoryDetector
                                    ├── AuthenticationErrorDetector
                                    ├── ThreadPoolExhaustionDetector
                                    └── Http500Detector
            └── HumanReadableFormatter / JsonFormatter
```

### Adding a New Detector

1. Create a class implementing `IssueDetector`
2. Register it in `IssueClassifier`

That's it. No other changes required. See [CONTRIBUTING.md](CONTRIBUTING.md) for a step-by-step guide.

---

## Project Structure

```
stacklens/
├── src/
│   ├── main/java/com/stacklens/
│   │   ├── cli/           # CLI commands
│   │   ├── analyzer/      # LogAnalyzer — entry point for analysis
│   │   ├── classifier/    # IssueClassifier — orchestrates detectors
│   │   ├── detector/      # One class per error type
│   │   ├── model/         # Issue, AnalysisResult
│   │   └── output/        # HumanReadableFormatter, JsonFormatter
│   └── test/java/com/stacklens/
│       ├── analyzer/      # LogAnalyzerTest
│       ├── detector/      # Per-detector unit tests
│       └── cli/           # CLI integration tests
├── samples/               # Sample log files for manual testing
├── docs/                  # Additional documentation
├── .github/
│   ├── workflows/ci.yml   # GitHub Actions CI
│   └── ISSUE_TEMPLATE/    # Bug and feature request templates
├── pom.xml
├── README.md
├── CONTRIBUTING.md
├── CHANGELOG.md
└── LICENSE
```

---

## Running Tests

```bash
mvn test
```

---

## Roadmap

- [ ] **AI explanations** — use an LLM API to generate context-aware, code-specific explanations
- [ ] **Spring Boot structured logs** — parse JSON log format from Logback
- [ ] **Watch mode** — tail a log file and detect issues in real time
- [ ] **Plugin system** — load custom detectors from external JARs
- [ ] **HTML report** — generate a standalone HTML report file
- [ ] **IntelliJ / VS Code plugin** — surface StackLens output inside the IDE

---

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) to get started.

The most impactful contributions are **new error detectors**. If you've seen a common Java or Spring Boot error that StackLens doesn't detect yet, please add it!

---

## License

StackLens is released under the [MIT License](LICENSE).
