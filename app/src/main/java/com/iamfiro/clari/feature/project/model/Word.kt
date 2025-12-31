package com.iamfiro.clari.feature.project.model

data class Word(
    val name: String,
    val meaning: String,
    val koreanPronunciation: String? = null,
    val synonyms: List<String>? = null
)

val dummy_words = listOf<Word>(
    Word("손도현", "김도현은 손을 좋아한다"),
    Word("클라이언트", "컴퓨터 네트워크나 웹 서비스에서 정보나 서비스를 요청하고 제공받는 주체"),
    Word("안니", "컴퓨터 웹 서비스에서 정보나 서비스를 요청하고 제공받는 주체"),
)
