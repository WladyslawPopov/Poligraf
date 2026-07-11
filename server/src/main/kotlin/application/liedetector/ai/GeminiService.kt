package application.liedetector.ai

import com.google.cloud.aiplatform.v1.Content
import com.google.cloud.aiplatform.v1.GenerateContentRequest
import com.google.cloud.aiplatform.v1.Part
import com.google.cloud.aiplatform.v1.PredictionServiceClient
import com.google.cloud.aiplatform.v1.PredictionServiceSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {
    private val projectId = System.getenv("GCP_PROJECT_ID") ?: throw IllegalStateException("GCP_PROJECT_ID missing!")
    
    // us-central1 - самый стабильный регион, где Gemini 1.5 Flash появилась первой
    private val location = "us-central1"
    private val modelName = "gemini-1.5-flash"
    
    // Формат пути: "projects/{project}/locations/{location}/publishers/google/models/{model}"
    private val endpoint = "projects/$projectId/locations/$location/publishers/google/models/$modelName"

    private val settings: PredictionServiceSettings = PredictionServiceSettings.newBuilder()
        .setEndpoint("us-central1-aiplatform.googleapis.com:443")
        .build()

    suspend fun testAi(): String = withContext(Dispatchers.IO) {
        try {
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
                    "AI Reached, but no candidates returned (check safety filters)"
                }
            }
        } catch (e: Exception) {
            // Печатаем полную ошибку в консоль для диагностики
            e.printStackTrace()
            "AI Connection Error: ${e.message}"
        }
    }
}
