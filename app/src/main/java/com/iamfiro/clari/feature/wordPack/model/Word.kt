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
    ),

    WordPack(
        name = "백엔드 · 서버 아키텍처",
        description = "API 설계와 서버 구조 회의에서 자주 쓰이는 용어",
        publisherId = "community.backend",
        publisherName = "Backend Devs",
        thumbnail = "https://example.com/thumb/backend.png",
        tempWordForView = listOf(
            Word("REST", "자원을 URI로 표현하는 아키텍처 스타일"),
            Word("Load Balancer", "트래픽을 여러 서버로 분산하는 장치"),
            Word("Stateless", "서버가 클라이언트 상태를 저장하지 않는 구조")
        ),
        wordCount = 184,
        isDownloaded = false,
        downloadCount = 3472
    ),

    WordPack(
        name = "Android · 모바일 개발",
        description = "안드로이드 앱 개발 회의 필수 용어",
        publisherId = "official.mobile",
        publisherName = "Clari",
        thumbnail = "https://example.com/thumb/android.png",
        tempWordForView = listOf(
            Word("Jetpack Compose", "선언형 UI를 위한 Android 툴킷"),
            Word("ViewModel", "UI 상태를 관리하는 아키텍처 컴포넌트"),
            Word("Recomposition", "상태 변경 시 UI를 다시 그리는 과정")
        ),
        wordCount = 312,
        isDownloaded = true,
        downloadCount = 10291
    ),

    WordPack(
        name = "디자인 · UX 회의 용어",
        description = "디자이너와 개발자 협업 시 자주 등장하는 개념",
        publisherId = "community.design",
        publisherName = "UX Collective",
        thumbnail = "https://example.com/thumb/design.png",
        tempWordForView = listOf(
            Word("Wireframe", "화면 구조를 단순화한 설계도"),
            Word("Affinity Map", "아이디어를 군집화하는 기법"),
            Word("Usability Test", "사용성 검증을 위한 테스트")
        ),
        wordCount = 97,
        isDownloaded = false,
        downloadCount = 2210
    ),

    WordPack(
        name = "스타트업 · 비즈니스 미팅",
        description = "기획·투자·전략 회의에서 나오는 비즈니스 용어",
        publisherId = "community.business",
        publisherName = "Startup Notes",
        thumbnail = "https://example.com/thumb/business.png",
        tempWordForView = listOf(
            Word("PMF", "제품이 시장 요구에 맞는 상태"),
            Word("CAC", "고객 1명을 획득하는 데 드는 비용"),
            Word("Runway", "자금이 버틸 수 있는 기간")
        ),
        wordCount = 143,
        isDownloaded = true,
        downloadCount = 6789
    ),

    WordPack(
        name = "보안 · 인프라 기본",
        description = "보안 점검 및 인프라 회의 핵심 용어",
        publisherId = "official.security",
        publisherName = "Clari 공식",
        thumbnail = "https://example.com/thumb/security.png",
        tempWordForView = listOf(
            Word("Zero Trust", "아무도 기본적으로 신뢰하지 않는 보안 모델"),
            Word("Firewall", "네트워크 접근을 제어하는 보안 장치"),
            Word("Encryption", "데이터를 암호화해 보호하는 기술")
        ),
        wordCount = 268,
        isDownloaded = false,
        downloadCount = 8124
    )
)