package com.iamfiro.clari.core.Repository

import com.iamfiro.clari.feature.project.model.Project
import com.iamfiro.clari.feature.project.model.ProjectConnector
import com.iamfiro.clari.feature.project.model.ProjectConnectorType
import com.iamfiro.clari.feature.project.model.Word
import kotlinx.coroutines.delay

class ProjectRepository {
    // TODO: 추 후에 실제 API로 변경
    private val mockProjects = mutableListOf(
        Project(
            id = "1",
            name = "AWS · 클라우드 기초",
            description = "클라우드 입문자를 위한 AWS 핵심 용어 모음",
            publisherId = "official",
            publisherName = "Clari",
            thumbnail = "https://example.com/thumb/aws.png",
            word = listOf(
                Word("EC2", "AWS에서 제공하는 가상 서버 서비스"),
                Word("S3", "객체 스토리지 서비스"),
                Word("IAM", "AWS 리소스 접근 권한을 관리하는 서비스")
            ),
            isDownloaded = true,
            downloadCount = 12432,
            connector = listOf(
                ProjectConnector(type = ProjectConnectorType.NOTION, name = "열말보고서 2분기", url = "https://github.com")
            )
        ),
        Project(
            id = "2",
            name = "프론트엔드 개발 용어",
            description = "React, 웹 개발 회의에서 자주 나오는 용어 정리",
            publisherId = "community.frontend",
            publisherName = "Frontend Korea",
            thumbnail = "https://example.com/thumb/frontend.png",
            word = listOf(
                Word("CSR", "브라우저에서 렌더링을 수행하는 방식"),
                Word("SSR", "서버에서 HTML을 생성해 전달하는 방식"),
                Word("Hydration", "SSR 이후 JS 이벤트를 연결하는 과정")
            ),
            isDownloaded = false,
            downloadCount = 5821
        ),
    )

    suspend fun getAllProjects(): List<Project> {
        delay(1000)
        return mockProjects.toList()
    }

    suspend fun getProjectById(projectId: String): Project? {
        return mockProjects.find { it.id == projectId }
    }


    suspend fun deleteProject(projectId: String): Boolean {
        val project = mockProjects.find { it.id == projectId }
        return if (project != null) {
            mockProjects.remove(project)
            true
        } else {
            false
        }
    }

    suspend fun addWordToProject(projectId: String, word: Word): Project? {
        val projectIndex = mockProjects.indexOfFirst { it.id == projectId }
        return if (projectIndex != -1) {
            val project = mockProjects[projectIndex]
            val updatedProject = project.copy(
                word = project.word + word
            )
            mockProjects[projectIndex] = updatedProject
            updatedProject
        } else {
            null
        }
    }

    suspend fun addConnectorToProject(projectId: String, connector: ProjectConnector): Project? {
        val projectIndex = mockProjects.indexOfFirst { it.id == projectId }
        return if (projectIndex != -1) {
            val project = mockProjects[projectIndex]
            val updatedConnectors = (project.connector ?: emptyList()) + connector
            val updatedProject = project.copy(
                connector = updatedConnectors
            )
            mockProjects[projectIndex] = updatedProject
            updatedProject
        } else {
            null
        }
    }

    suspend fun getWordMeaning(projectId: String, wordName: String): String? {
        val project = mockProjects.find { it.id == projectId }
        return project?.word?.find { it.name == wordName }?.meaning
    }

    suspend fun updateProject(projectId: String, name: String?): Project? {
        val projectIndex = mockProjects.indexOfFirst { it.id == projectId }
        return if (projectIndex != -1) {
            val project = mockProjects[projectIndex]
            val updatedProject = project.copy(
                name = name ?: project.name
            )
            mockProjects[projectIndex] = updatedProject
            updatedProject
        } else {
            null
        }
    }
}