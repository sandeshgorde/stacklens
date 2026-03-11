package com.stacklens.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.stacklens.model.AnalysisResult;
import com.stacklens.model.Issue;

import java.util.List;

/**
 * Formats analysis results as JSON.
 *
 * Useful for piping StackLens output into other tools or scripts.
 *
 * Example output:
 * {
 *   "source": "app.log",
 *   "issueCount": 1,
 *   "issues": [
 *     {
 *       "issue": "NullPointerException",
 *       "explanation": "...",
 *       "suggestions": ["..."]
 *     }
 *   ]
 * }
 */
public class JsonFormatter {

    private final ObjectMapper objectMapper;

    public JsonFormatter() {
        objectMapper = new ObjectMapper();
        // Pretty-print the JSON output for readability
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Formats an analysis result as a JSON string.
     *
     * @param result the analysis result to format
     * @return pretty-printed JSON string
     * @throws RuntimeException if JSON serialization fails (should not happen in practice)
     */
    public String format(AnalysisResult result) {
        try {
            JsonOutput output = buildJsonOutput(result);
            return objectMapper.writeValueAsString(output);
        } catch (Exception e) {
            // This is an internal error — the models we use are always serializable
            throw new RuntimeException("Failed to serialize result to JSON", e);
        }
    }

    /** Maps an AnalysisResult to our JSON output structure. */
    private JsonOutput buildJsonOutput(AnalysisResult result) {
        List<JsonIssue> jsonIssues = result.getIssues().stream()
            .map(this::toJsonIssue)
            .toList();

        return new JsonOutput(result.getSource(), jsonIssues.size(), jsonIssues);
    }

    /** Maps a single Issue to its JSON representation. */
    private JsonIssue toJsonIssue(Issue issue) {
        return new JsonIssue(issue.getType(), issue.getExplanation(), issue.getSuggestions());
    }

    // ── Internal record classes for JSON serialization ──────────────────────

    /** Top-level JSON output structure. */
    record JsonOutput(
        @JsonProperty("source") String source,
        @JsonProperty("issueCount") int issueCount,
        @JsonProperty("issues") List<JsonIssue> issues
    ) {}

    /** JSON structure for a single detected issue. */
    record JsonIssue(
        @JsonProperty("issue") String issue,
        @JsonProperty("explanation") String explanation,
        @JsonProperty("suggestions") List<String> suggestions
    ) {}
}
