package application.poligraf.data.subject.remote

import application.poligraf.models.SubjectDto

interface SubjectRemoteDataSource {
    suspend fun createSubject(subject: SubjectDto): SubjectDto
    suspend fun getSubject(id: String): SubjectDto
    suspend fun getSubjects(): List<SubjectDto>
    suspend fun deleteSubjects(ids: List<String>): Boolean
}
