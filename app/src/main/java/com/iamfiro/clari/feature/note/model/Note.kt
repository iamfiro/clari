package com.iamfiro.clwari.feature.note.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date

data class Note(
    val name: String,

    val duration: Long,

    val createdAt: LocalDateTime,
)

val dummy_notes = listOf(
    Note(
        "발표 주제 '킥'과 관련된 아이디어 논의",
        duration = 827_842,
        createdAt = LocalDateTime.parse("2025-12-23T14:30")
    ),
    Note(
        "AWS 아키텍처 설계 리뷰",
        duration = 1_532_120,
        createdAt = LocalDateTime.parse("2024-12-01T10:05")
    ),
    Note(
        "클라우드 스터디 1주차",
        duration = 3_215_000,
        createdAt = LocalDateTime.parse("2024-12-03T19:40")
    ),
    Note(
        "Terraform 상태 파일 설명",
        duration = 642_300,
        createdAt = LocalDateTime.parse("2024-12-05T16:10")
    ),
    Note(
        "기능경기대회 아이디어 브레인스토밍",
        duration = 1_124_900,
        createdAt = LocalDateTime.parse("2024-12-07T21:15")
    ),
    Note(
        "멘토링 회의 - 프로젝트 방향성",
        duration = 2_045_600,
        createdAt = LocalDateTime.parse("2024-12-10T18:00")
    ),
    Note(
        "STT 정확도 개선 회의",
        duration = 955_400,
        createdAt = LocalDateTime.parse("2024-12-12T14:45")
    ),
    Note(
        "디자인 시스템 구조 정리",
        duration = 1_305_800,
        createdAt = LocalDateTime.parse("2024-12-15T22:20")
    ),
    Note(
        "Clari MVP 기능 우선순위 논의",
        duration = 1_789_000,
        createdAt = LocalDateTime.parse("2024-12-18T13:30")
    ),
    Note(
        "도메인 단어 팩 기획 회의",
        duration = 1_012_500,
        createdAt = LocalDateTime.parse("2024-12-20T17:10")
    ),
    Note(
        "실시간 자막 UX 피드백 정리",
        duration = 734_200,
        createdAt = LocalDateTime.parse("2024-12-22T20:55")
    ),
    Note(
        "신규 기능 제안 미팅",
        duration = 568_900,
        createdAt = LocalDateTime.parse("2024-12-23T09:10")
    ),
    Note(
        "2025 서비스 로드맵 논의",
        duration = 2_340_000,
        createdAt = LocalDateTime.parse("2025-01-03T15:00")
    ),
    Note(
        "API 명세 리뷰 회의",
        duration = 1_480_600,
        createdAt = LocalDateTime.parse("2025-01-07T11:20")
    ),
    Note(
        "배포 전 최종 점검",
        duration = 905_000,
        createdAt = LocalDateTime.parse("2025-01-10T11:20")
    )
)