package com.iamfiro.clari.feature.recording

enum class Language(val displayName: String, val code: String) {
    KOREAN("한국어", "ko-KR"),
    ENGLISH("English", "en-US");

    fun getCountryCode(): String {
        return code
    }
}

