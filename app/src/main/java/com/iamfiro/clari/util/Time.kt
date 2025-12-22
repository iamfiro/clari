package com.iamfiro.clari.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun unixTimeToKoreanDate(
    unixTime: Long,
    isMillis: Boolean = false,
    pattern: String = "yyyy년 M월 d일 (E) a h:mm",
): String {
    val instant = if (isMillis) {
        Instant.ofEpochMilli(unixTime)
    } else {
        Instant.ofEpochSecond(unixTime)
    }

    val kst: ZonedDateTime = instant.atZone(ZoneId.of("Asia/Seoul"))

    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.KOREAN)
    return kst.format(formatter)
}

fun millisToMinutes(millis: Long): Long {
    return millis / 1000 / 60
}