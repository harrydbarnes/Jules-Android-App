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
