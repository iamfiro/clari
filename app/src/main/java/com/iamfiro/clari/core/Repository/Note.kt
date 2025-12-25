package com.iamfiro.clari.core.Repository

import com.iamfiro.clari.feature.note.model.Note
import com.iamfiro.clari.feature.note.model.NoteType
import com.iamfiro.clari.feature.note.model.AiSummary
import com.iamfiro.clari.feature.note.model.Speaker
import com.iamfiro.clari.feature.note.model.TranscriptLine
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.UUID

class NoteRepository {
    // TODO: 추 후에 실제 API로 변경
    private val mockNotes = mutableListOf(
        Note(
            id = "note_001",
            type = NoteType.READY,
            name = "발표 주제 '킥'과 관련된 아이디어 논의",
            duration = 827_842,
            createdAt = LocalDateTime.parse("2025-12-23T14:30"),
            recordedAtText = "2024년 4월 12일 12:11",
            aiSummary = AiSummary(
                title = "AI 요약",
                content = "이번 회의에서는 AWS 기반 서비스 아키텍처 설계 방향과 Terraform을 활용한 IaC 도입 필요성에 대해 논의했으며, 초기에는 단순한 구조로 시작하되 확장성과 관리 효율을 고려한 설계를 목표로 하기로 했다."
            ),
            transcripts = listOf(
                TranscriptLine(Speaker(1), 0, "오늘 발표 주제는 킥인데, 일단 전체적인 방향부터 얘기해볼게요."),
                TranscriptLine(Speaker(2), 4, "네, 킥을 기술적으로 풀어내는 게 핵심인 거죠?"),
                TranscriptLine(Speaker(1), 8, "맞아요. 단순 아이디어 말고 실제 구현 가능성까지 봐야 해요.")
            )
        ),
        Note(
            id = "note_002",
            type = NoteType.READY,
            name = "AWS 아키텍처 설계 리뷰",
            duration = 1_532_120,
            createdAt = LocalDateTime.parse("2024-12-01T10:05"),
            recordedAtText = "2024년 12월 1일 10:05",
            aiSummary = AiSummary(
                title = "AI 요약",
                content = "AWS 아키텍처 설계에 대한 리뷰 회의에서 주요 인프라 구성 요소와 비용 최적화 방안을 논의했습니다."
            ),
            transcripts = listOf(
                TranscriptLine(Speaker(1), 0, "오케이, 오늘은 AWS 아키텍처 설계 리뷰. 지금 다이어그램 열어놨어?"),
                TranscriptLine(Speaker(2), 3, "네. 저장은 S3, 메타데이터는 RDS로 되어 있고, 처리 서버는 ECS로 되어 있어요."),
                TranscriptLine(Speaker(1), 9, "좋아. 근데 일단 질문. RDS를 왜 골랐지?"),
                TranscriptLine(Speaker(2), 14, "관계형이 편해서요. 노트랑 트랜스크립트랑 관계가 있어서…"),
                TranscriptLine(Speaker(1), 19, "그건 맞는데, 트랜스크립트는 길어지면 RDS 비용/성능이 좀 애매해질 수 있어."),
                TranscriptLine(Speaker(2), 25, "그럼 DynamoDB로 가는 게 낫나요?"),
                TranscriptLine(Speaker(1), 29, "케이스 따라. 조회 패턴이 단순하면 DynamoDB 좋고, 복잡한 조인 필요하면 RDS 유지."),
                TranscriptLine(Speaker(2), 35, "우리 조회는 '노트 상세 열면 해당 노트의 라인들을 시간순으로' 이게 대부분이죠."),
                TranscriptLine(Speaker(1), 41, "그럼 DynamoDB도 충분. 파티션키 noteId, 소트키 timestamp."),
                TranscriptLine(Speaker(2), 48, "아 그러면 페이징도 쉬워지겠네요."),
                TranscriptLine(Speaker(1), 52, "맞아. 다음. 처리 서버 ECS로 둔 이유는?"),
                TranscriptLine(Speaker(2), 57, "음… 장시간 작업 때문에 Lambda 제한이 걸릴까봐요."),
                TranscriptLine(Speaker(1), 63, "좋은 판단. 다만 항상 켜두면 비용이 나가. 스팟이나 오토스케일 고려했어?"),
                TranscriptLine(Speaker(2), 70, "오토스케일은 넣었는데, 스팟은 아직…"),
                TranscriptLine(Speaker(1), 75, "스팟은 중단될 수 있어서 배치 처리에 어울려. 작업 큐 기반으로 하면 안정적."),
                TranscriptLine(Speaker(2), 82, "큐… SQS를 넣는다는 거죠?"),
                TranscriptLine(Speaker(1), 86, "응. 업로드 이벤트 → SQS → 워커가 처리 → 결과 저장."),
                TranscriptLine(Speaker(2), 93, "그럼 업로드 이벤트는 S3 이벤트로 트리거할까요?"),
                TranscriptLine(Speaker(1), 98, "S3 이벤트가 간단한데, 대량 이벤트 때 재시도/중복 처리 고려해야 해."),
                TranscriptLine(Speaker(2), 104, "중복 처리면 idempotent하게 만들면 되겠네요. noteId 기반으로 결과 덮어쓰기."),
                TranscriptLine(Speaker(1), 110, "좋아. 다음은 인증. 지금은 Cognito로 되어 있던데, 이유가 뭐야?"),
                TranscriptLine(Speaker(2), 116, "구글 로그인 붙이기 쉽고, 토큰으로 API 보호하려고요."),
                TranscriptLine(Speaker(1), 122, "좋다. 다만 모바일에서 토큰 갱신 흐름이 헷갈릴 수 있어서 문서 정리해야 해."),
                TranscriptLine(Speaker(2), 129, "Refresh token이랑 access token 수명 정하고…"),
                TranscriptLine(Speaker(1), 134, "응. 그리고 API Gateway 쓸 건지, ALB + ECS로 갈 건지도 정해야 돼."),
                TranscriptLine(Speaker(2), 140, "지금은 ALB로 되어 있어요. 근데 API Gateway가 더 편한가요?"),
                TranscriptLine(Speaker(1), 146, "간단한 REST면 Gateway가 편하고, ECS랑 바로 붙일 수도 있어. 비용은 트래픽에 따라."),
                TranscriptLine(Speaker(2), 153, "아… 비용 얘기 나왔으니 최적화도 정리하죠."),
                TranscriptLine(Speaker(1), 158, "그래. 첫 번째는 스토리지. 오디오 원본은 S3 Standard로 두나?"),
                TranscriptLine(Speaker(2), 164, "일단은… 네."),
                TranscriptLine(Speaker(1), 168, "그러면 Lifecycle로 30일 뒤 IA, 90일 뒤 Glacier 같은 정책 추천."),
                TranscriptLine(Speaker(2), 175, "오 좋다. 사용자가 예전 노트 자주 안 열면 이득이네요."),
                TranscriptLine(Speaker(1), 181, "두 번째는 컴퓨트. STT/요약 처리가 고비용이면 배치로 몰아서 할지, 실시간 일부만 할지."),
                TranscriptLine(Speaker(2), 188, "실시간은 키워드 탐지만 먼저 하고, 전체 요약은 나중에 처리하는 방식이요?"),
                TranscriptLine(Speaker(1), 194, "응. 사용자 체감은 빠르고 비용은 줄일 수 있어."),
                TranscriptLine(Speaker(2), 200, "그럼 '부분 결과' 상태 표시도 필요하겠네요. 아직 처리 중이라든가."),
                TranscriptLine(Speaker(1), 206, "맞아. UI/백엔드 상태 동기화 중요."),
                TranscriptLine(Speaker(2), 211, "Terraform 얘기도 해야겠죠. 지금 모듈 구조는… vpc, ecs, s3, dynamodb로 쪼개놨어요."),
                TranscriptLine(Speaker(1), 218, "좋다. 근데 상태 파일은 어디에 저장해?"),
                TranscriptLine(Speaker(2), 223, "로컬… 입니다."),
                TranscriptLine(Speaker(1), 227, "그럼 협업 힘들어. 원격 상태로 바꾸자. S3 backend + DynamoDB lock."),
                TranscriptLine(Speaker(2), 234, "오케이. 그러면 state locking으로 동시에 apply하는 거 막을 수 있죠."),
                TranscriptLine(Speaker(1), 240, "맞아. 그리고 환경 분리. dev/prod 워크스페이스나 별도 backend key."),
                TranscriptLine(Speaker(2), 247, "dev는 비용 적게, prod는 안정성 위주로."),
                TranscriptLine(Speaker(1), 252, "그리고 태깅. 비용 분석하려면 모든 리소스에 tags 필수."),
                TranscriptLine(Speaker(2), 258, "project=clari, env=dev 이런 식으로요."),
                TranscriptLine(Speaker(1), 263, "좋다. 마지막으로 보안. S3 퍼블릭 차단, 최소 권한 IAM, KMS 암호화는?"),
                TranscriptLine(Speaker(2), 270, "S3는 퍼블릭 차단 넣었고, KMS는 아직 미적용이에요."),
                TranscriptLine(Speaker(1), 275, "오디오 파일은 민감할 수 있으니 SSE-KMS 추천. 접근 로그도 남기고."),
                TranscriptLine(Speaker(2), 282, "CloudTrail이랑 S3 access logs 켜면 되겠네요."),
                TranscriptLine(Speaker(1), 287, "응. 그리고 presigned URL로 업로드/다운로드 흐름 정리하면 서버 부하도 줄어."),
                TranscriptLine(Speaker(2), 295, "아 그럼 앱이 직접 S3에 올리고, 백엔드는 권한만 발급하는 구조."),
                TranscriptLine(Speaker(1), 301, "정확해. 오늘 결론은 이거: DB는 패턴 보고 DynamoDB 후보, 처리 파이프라인은 SQS 기반, Terraform 원격 상태로 전환."),
                TranscriptLine(Speaker(2), 310, "네. 제가 todo 정리해서 이슈로 올려둘게요."),
                TranscriptLine(Speaker(1), 315, "좋아. 다음 회의 때는 비용 추정치까지 간단히 숫자로 가져오자.")
            )
        ),
        Note(
            id = "note_003",
            type = NoteType.NOT_READY,
            name = "클라우드 스터디 1주차",
            duration = 3_215_000,
            createdAt = LocalDateTime.parse("2024-12-03T19:40")
        ),
        Note(
            id = "note_004",
            type = NoteType.NOT_READY,
            name = "Terraform 상태 파일 설명",
            duration = 642_300,
            createdAt = LocalDateTime.parse("2024-12-05T16:10")
        ),
    )

    private val noteIdMap = mutableMapOf<Note, String>()

    init {
        mockNotes.forEachIndexed { index, note ->
            noteIdMap[note] = "note_${index + 1}"
        }
    }

    suspend fun getAllNotes(): List<Note> {
        delay(1000)
        return mockNotes.toList()
    }

    suspend fun getNoteById(noteId: String): Note? {
        val note = mockNotes.find { it.id == noteId }
        return note?.copy(
            recordedAtText = note.recordedAtText ?: note.createdAt.toString()
        )
    }

    suspend fun createNote(note: Note): Note {
        val newNote = note.copy()
        mockNotes.add(newNote)
        val newId = UUID.randomUUID().toString()
        noteIdMap[newNote] = newId
        return newNote
    }

    suspend fun updateNote(noteId: String, name: String?): Note? {
        val noteEntry = noteIdMap.entries.find { it.value == noteId }
        if (noteEntry == null) return null

        val oldNote = noteEntry.key
        val noteIndex = mockNotes.indexOf(oldNote)
        if (noteIndex == -1) return null

        val updatedNote = oldNote.copy(
            name = name ?: oldNote.name
        )

        mockNotes[noteIndex] = updatedNote
        noteIdMap.remove(oldNote)
        noteIdMap[updatedNote] = noteId

        return updatedNote
    }
}
