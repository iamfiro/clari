package com.iamfiro.clari.feature.recording

enum class Language(val displayName: String, val code: String) {
    KOREAN("한국어", "kor"),
    ENGLISH("English", "eng");

    fun getCountryCode(): String {
        return code
    }
}

