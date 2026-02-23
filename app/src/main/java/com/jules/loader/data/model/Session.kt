package com.jules.loader.data.model

import com.google.gson.annotations.SerializedName

data class ListSessionsResponse(
    val sessions: List<Session>?,
    val nextPageToken: String?
)

data class Session(
    val name: String,
    val id: String,
    val title: String?,
    val prompt: String?,
    @SerializedName("state") val status: String?,
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
    val name: String?,
    val id: String?,
    val description: String?,
    @SerializedName("createTime") val timestamp: String?,
    val type: String?,

    // Union fields containing the replies/comments
    val userMessage: MessageLog?,
    val agentMessage: MessageLog?,
    val planGenerated: PlanLog?
) {
    fun getResolvedType(): String {
        return type ?: when {
            userMessage != null -> "USER MESSAGE"
            agentMessage != null -> "AGENT MESSAGE"
            planGenerated != null -> "PLAN GENERATED"
            else -> "ACTIVITY"
        }
    }

    fun getResolvedDescription(): String {
        // Fallbacks to extract the actual comment details and content
        return userMessage?.message ?: userMessage?.text ?: userMessage?.prompt ?:
               agentMessage?.message ?: agentMessage?.text ?: agentMessage?.prompt ?:
               planGenerated?.plan ?: planGenerated?.message ?:
               description ?: "No details"
    }
}

data class MessageLog(
    val message: String?,
    val text: String?,
    val prompt: String?
)

data class PlanLog(
    val plan: String?,
    val message: String?
)

// Response models for user sources (repositories)
data class ListSourcesResponse(
    val sources: List<SourceContext>?
)
