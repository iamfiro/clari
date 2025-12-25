package com.iamfiro.clari.feature.project.model

enum class ProjectConnectorType {
    NOTION,
    GDRIVE
}

data class ProjectConnector (
    val type: ProjectConnectorType,
    val url: String,
    val name: String,
)

data class Project(
    val id: String,
    val name: String,
    val description: String,

    val publisherId: String,
    val publisherName: String,

    val thumbnail: String,

    val word: List<Word>,

    val isDownloaded: Boolean,

    val downloadCount: Int,

    val connector: List<ProjectConnector>? = null,
)

val dummy_project = listOf(
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
