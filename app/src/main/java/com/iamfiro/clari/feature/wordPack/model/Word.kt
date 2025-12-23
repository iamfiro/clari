package com.iamfiro.clari.feature.wordPack.model

data class Word(
    val name: String,
    val meaning: String,
)

data class WordPack(
    val name: String,
    val description: String,

    val publisherId: String,
    val publisherName: String,

    val thumbnail: String,

    val tempWordForView: List<Word>, // 단어팩 상세 페이지에서 몇개 보여주는 정도
    val wordCount: Int,

    val isDownloaded: Boolean,

    val downloadCount: Int,
)

val dummy_words = listOf<Word>(
    Word("손도현", "김도현은 손을 좋아한다"),
    Word("클라이언트", "컴퓨터 네트워크나 웹 서비스에서 정보나 서비스를 요청하고 제공받는 주체"),
    Word("안니", "컴퓨터 웹 서비스에서 정보나 서비스를 요청하고 제공받는 주체"),
    Word("손도현", "김도현은 손을 좋아한다"),
    Word("클라이언트", "컴퓨터 네트워크나 웹 서비스에서 정보나 서비스를 요청하고 제공받는 주체"),
    Word("안니", "컴퓨터 웹 서비스에서 정보나 서비스를 요청하고 제공받는 주체")
)

val dummyWordPacks = listOf(
    WordPack(
        name = "AWS · 클라우드 기초",
        description = "클라우드 입문자를 위한 AWS 핵심 용어 모음",
        publisherId = "official",
        publisherName = "Clari",
        thumbnail = "https://example.com/thumb/aws.png",
        tempWordForView = listOf(
            Word("EC2", "AWS에서 제공하는 가상 서버 서비스"),
            Word("S3", "객체 스토리지 서비스"),
            Word("IAM", "AWS 리소스 접근 권한을 관리하는 서비스")
        ),
        wordCount = 4292,
        isDownloaded = true,
        downloadCount = 12432
    ),

    WordPack(
        name = "프론트엔드 개발 용어",
        description = "React, 웹 개발 회의에서 자주 나오는 용어 정리",
        publisherId = "community.frontend",
        publisherName = "Frontend Korea",
        thumbnail = "https://example.com/thumb/frontend.png",
        tempWordForView = listOf(
            Word("CSR", "브라우저에서 렌더링을 수행하는 방식"),
            Word("SSR", "서버에서 HTML을 생성해 전달하는 방식"),
            Word("Hydration", "SSR 이후 JS 이벤트를 연결하는 과정")
        ),
        wordCount = 12,
        isDownloaded = false,
        downloadCount = 5821
    ),

    WordPack(
        name = "AI · 머신러닝 회의 용어",
        description = "AI 프로젝트 회의 중 등장하는 필수 개념",
        publisherId = "official.ai",
        publisherName = "Clari 공식",
        thumbnail = "https://example.com/thumb/ai.png",
        tempWordForView = listOf(
            Word("Overfitting", "학습 데이터에만 과도하게 맞춰진 상태"),
            Word("Inference", "학습된 모델로 예측을 수행하는 단계"),
            Word("Epoch", "전체 데이터를 한 번 학습하는 단위")
        ),
        wordCount = 231,
        isDownloaded = false,
        downloadCount = 9034
    )
)