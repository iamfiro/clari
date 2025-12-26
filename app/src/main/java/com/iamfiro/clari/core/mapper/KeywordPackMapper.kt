package com.iamfiro.clari.core.mapper

import com.iamfiro.clari.core.network.dto.KeywordDto
import com.iamfiro.clari.core.network.dto.KeywordPackDto
import com.iamfiro.clari.feature.project.model.Project
import com.iamfiro.clari.feature.project.model.Word

object KeywordPackMapper {
    
    /**
     * KeywordPackDto -> Project (기존 UI 호환용)
     */
    fun fromDto(dto: KeywordPackDto): Project {
        return Project(
            id = dto.id,
            name = dto.name,
            description = "", // API에서 description 미제공
            publisherId = "user",
            publisherName = "사용자",
            thumbnail = dto.previewImageUrl ?: "",
            word = dto.keywords.map { keyword ->
                Word(
                    name = keyword.name,
                    meaning = keyword.description
                )
            },
            isDownloaded = true,
            downloadCount = 0,
            connector = null
        )
    }
    
    /**
     * KeywordDto -> Word
     */
    fun keywordToWord(dto: KeywordDto): Word {
        return Word(
            name = dto.name,
            meaning = dto.description
        )
    }
    
    /**
     * Word -> KeywordDto
     */
    fun wordToKeyword(word: Word): KeywordDto {
        return KeywordDto(
            name = word.name,
            description = word.meaning
        )
    }
    
    /**
     * List<Word> -> List<KeywordDto>
     */
    fun wordsToKeywords(words: List<Word>): List<KeywordDto> {
        return words.map { wordToKeyword(it) }
    }
}

