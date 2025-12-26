package com.iamfiro.clari.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun getTodayDateTime(): LocalDateTime {
    val timeZoneId = ZoneId.systemDefault().toString()

    return LocalDateTime.now(ZoneId.of(timeZoneId))
}

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

val dateKoreanFormatter = DateTimeFormatter.ofPattern(
    "yyyy년 M월 d일(E) hh:mm",
    Locale.KOREA
)

fun toRelativeDateLabel(dateTime: LocalDateTime, now: LocalDateTime): String {
    val date = dateTime.toLocalDate()
    val today = now.toLocalDate()

    return when {
        date == today -> "오늘"
        date == today.minusDays(1) -> "어제"
        date.year == today.year ->
            date.format(DateTimeFormatter.ofPattern("M월 d일"))
        else ->
            date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"))
    }
}

fun formatMmSs(totalSec: Int): String {
    val m = totalSec / 60
    val s = totalSec % 60
    return "%02d:%02d".format(m, s)
}
