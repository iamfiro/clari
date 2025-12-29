package com.iamfiro.clari.core.mapper

import com.iamfiro.clari.core.network.dto.ProjectWordDTO
import com.iamfiro.clari.core.network.dto.ProjectDto
import com.iamfiro.clari.feature.project.model.Project
import com.iamfiro.clari.feature.project.model.Word

object KeywordPackMapper {
    fun fromDto(dto: ProjectDto): Project {
        android.util.Log.d("KeywordPackMapper", "fromDto 호출: name=${dto.name}, words.size=${dto.words.size}")
        val words = dto.words.map { keyword ->
            Word(
                name = keyword.name,
                meaning = keyword.description
            )
        }
        android.util.Log.d("KeywordPackMapper", "매핑 완료: words.size=${words.size}")
        return Project(
            id = dto.id,
            name = dto.name,
            description = "", // API에서 description 미제공
            publisherId = "user",
            publisherName = "사용자",
            thumbnail = dto.previewImageUrl ?: "",
            word = words,
            isDownloaded = true,
            downloadCount = 0,
            connector = null
        )
    }
    fun keywordToWord(dto: ProjectWordDTO): Word {
        return Word(
            name = dto.name,
            meaning = dto.description
        )
    }
    fun wordToKeyword(word: Word): ProjectWordDTO {
        return ProjectWordDTO(
            name = word.name,
            description = word.meaning
        )
    }

    fun wordsToKeywords(words: List<Word>): List<ProjectWordDTO> {
        return words.map { wordToKeyword(it) }
    }
}



