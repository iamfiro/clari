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

    val tempWordForView: List<Word>, // 단어팩 상세 페이지에서 몇개 보여주는 정도

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