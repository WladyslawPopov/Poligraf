package application.liedetector.ai

import com.google.cloud.vertexai.api.Content
import com.google.cloud.vertexai.api.GenerateContentRequest
import com.google.cloud.vertexai.api.Part
import com.google.cloud.vertexai.api.PredictionServiceClient
import com.google.cloud.vertexai.api.PredictionServiceSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {
    private val projectId = System.getenv("GCP_PROJECT_ID") ?: throw IllegalStateException("GCP_PROJECT_ID missing!")

    // 1. Указываем мульти-регион Европы
    private val location = "eu"

    // 2. Новая модель!
    private val modelName = "gemini-3.5-flash"

    private val endpoint = "projects/$projectId/locations/$location/publishers/google/models/$modelName"

    // 3. ВОТ ОНО! Вручную указываем правильный URL для мульти-регионов (с .rep.)
    private val settings: PredictionServiceSettings = PredictionServiceSettings.newBuilder()
        .setEndpoint("aiplatform.eu.rep.googleapis.com:443")
        .build()

    suspend fun testAi(): String = withContext(Dispatchers.IO) {
        try {
            // 4. Снова используем официальный, мощный SDK!
            PredictionServiceClient.create(settings).use { client ->
                val content = Content.newBuilder()
                    .setRole("user")
                    .addParts(Part.newBuilder().setText("Hello! Response with: 'AI is Online'").build())
                    .build()

                val request = GenerateContentRequest.newBuilder()
                    .setModel(endpoint)
                    .addContents(content)
                    .build()

                val response = client.generateContent(request)

                if (response.candidatesCount > 0) {
                    val result = response.getCandidates(0).content.getParts(0).text
                    if (result.isNullOrBlank()) "AI reached, but response text is empty" else result
                } else {
                    "AI Reached, but no candidates returned"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "AI Connection Error: ${e.message}"
        }
    }
}
