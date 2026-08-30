package application.poligraf.domain.model

/**
 * Determines how the unified analyzer session behaves.
 * LIVE — recording/paused/live processing; REVIEW — read-only history with summary.
 */
enum class AnalyzerMode { LIVE, REVIEW }
