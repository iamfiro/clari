package com.iamfiro.clari.feature.note.model

data class NoteDetail(
    val id: String,
    val title: String,
    val recordedAtText: String,
    val aiSummary: AiSummary,
    val transcripts: List<TranscriptLine>,
)

data class AiSummary(
    val title: String,
    val content: String,
)

data class Speaker(
    val id: Int,
    val label: String = "참석자 $id",
)

data class TranscriptLine(
    val speaker: Speaker,
    val timeSec: Int,
    val text: String,
)

val dummy_note_detail = NoteDetail(
    id = "note_001",
    title = "발표 주제 '킥'과 관련된 아이디어 논의",
    recordedAtText = "2024년 4월 12일 12:11",
    aiSummary = AiSummary(
        title = "AI 요약",
        content = "이번 회의에서는 AWS 기반 서비스 아키텍처 설계 방향과 Terraform을 활용한 IaC 도입 필요성에 대해 논의했으며, 초기에는 단순한 구조로 시작하되 확장성과 관리 효율을 고려한 설계를 목표로 하기로 했다."
    ),
    transcripts = listOf(
        TranscriptLine(Speaker(1), 0, "오늘 발표 주제는 킥인데, 일단 전체적인 방향부터 얘기해볼게요."),
        TranscriptLine(Speaker(2), 4, "네, 킥을 기술적으로 풀어내는 게 핵심인 거죠?"),
        TranscriptLine(Speaker(1), 8, "맞아요. 단순 아이디어 말고 실제 구현 가능성까지 봐야 해요."),
        TranscriptLine(Speaker(1), 14, "그래서 저는 AWS 기반으로 구조를 잡는 게 좋다고 생각했어요."),
        TranscriptLine(Speaker(2), 19, "EC2랑 RDS 정도만 쓰는 건가요?"),
        TranscriptLine(Speaker(1), 23, "초기에는 그 정도로 단순하게 가도 될 것 같아요."),
        TranscriptLine(Speaker(1), 28, "대신 나중에 확장할 수 있도록 구조는 미리 열어두는 게 중요해요."),
        TranscriptLine(Speaker(3), 34, "그럼 인프라는 수동으로 관리하나요?"),
        TranscriptLine(Speaker(1), 37, "아니요, 그건 Terraform으로 IaC를 도입하는 게 좋아 보여요."),
        TranscriptLine(Speaker(2), 42, "Terraform 쓰면 환경 복제도 쉬워지겠네요."),
        TranscriptLine(Speaker(1), 46, "맞아요. 개발, 테스트, 운영 환경을 거의 동일하게 가져갈 수 있어요."),
        TranscriptLine(Speaker(3), 52, "근데 초반에 Terraform 세팅하는 비용이 좀 들지 않나요?"),
        TranscriptLine(Speaker(1), 56, "들긴 하는데, 장기적으로 보면 관리 비용이 훨씬 줄어요."),
        TranscriptLine(Speaker(2), 61, "특히 팀 프로젝트면 더 의미 있겠네요."),
        TranscriptLine(Speaker(1), 65, "그래서 이걸 킥 포인트로 가져가면 좋겠다고 생각했어요."),
        TranscriptLine(Speaker(1), 70, "단순 서비스 아이디어가 아니라 운영까지 고려했다는 느낌을 줄 수 있어서요."),
        TranscriptLine(Speaker(3), 76, "심사위원 입장에서도 좋아할 만한 포인트네요."),
        TranscriptLine(Speaker(2), 80, "그럼 발표 흐름은 문제 제시, 구조, Terraform 이런 식인가요?"),
        TranscriptLine(Speaker(1), 85, "네, 그리고 마지막에 확장 시나리오를 간단히 보여주면 좋을 것 같아요."),
        TranscriptLine(Speaker(1), 91, "예를 들면 트래픽 늘어났을 때 오토스케일링이라든지."),
        TranscriptLine(Speaker(3), 96, "시간 남으면 다이어그램 하나 넣어도 좋겠네요."),
        TranscriptLine(Speaker(1), 100, "좋아요. 그럼 제가 아키텍처 그림이랑 Terraform 예시 준비해볼게요."),
        TranscriptLine(Speaker(2), 106, "저는 발표 스크립트 정리해볼게요."),
        TranscriptLine(Speaker(1), 110, "좋습니다. 오늘 회의는 여기까지 할게요.")
    )
)