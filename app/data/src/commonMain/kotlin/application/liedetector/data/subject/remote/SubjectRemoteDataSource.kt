package application.liedetector.data.subject.remote

import application.liedetector.models.SubjectDto

interface SubjectRemoteDataSource {
    suspend fun createSubject(subject: SubjectDto): SubjectDto
    suspend fun getSubject(id: String): SubjectDto
    suspend fun getSubjects(): List<SubjectDto>
    suspend fun deleteSubjects(ids: List<String>): Boolean
}
