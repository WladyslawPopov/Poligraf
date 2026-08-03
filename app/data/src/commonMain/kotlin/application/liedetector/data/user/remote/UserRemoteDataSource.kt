package application.liedetector.data.user.remote

import application.liedetector.models.AnalysisRequest
import application.liedetector.models.SubjectDto
import application.liedetector.models.UserDto

interface UserRemoteDataSource {
    suspend fun startAnalysis(request: AnalysisRequest): Map<String, String>
    suspend fun createSubject(subject: SubjectDto): SubjectDto
    suspend fun getSubject(id: String): SubjectDto
    suspend fun getSubjects(): List<SubjectDto>
    suspend fun syncUser(user: UserDto): String
}
