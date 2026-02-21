package com.jules.loader.data.model

data class ListSessionsResponse(
    val sessions: List<Session>?,
    val nextPageToken: String?
)

data class Session(
    val name: String,
    val id: String,
    val title: String?,
    val prompt: String?,
    val status: String?,
    val sourceContext: SourceContext?
)

data class SourceContext(
    val source: String,
    val githubRepoContext: GithubRepoContext?
)

data class GithubRepoContext(
    val startingBranch: String?
)

// Request body for creating a new session/task
data class CreateSessionRequest(
    val prompt: String,
    val sourceContext: SourceContext? = null
)

// Response models for live logs (activities)
data class ListActivitiesResponse(
    val activities: List<ActivityLog>?
)

data class ActivityLog(
    val id: String,
    val description: String,
    val timestamp: String,
    val type: String // e.g., "PLANNING", "EXECUTING", "MESSAGE"
)

// Response models for user sources (repositories)
data class ListSourcesResponse(
    val sources: List<SourceContext>?
)
