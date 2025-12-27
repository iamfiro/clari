package com.iamfiro.clari.core.mapper

import com.iamfiro.clari.core.network.dto.ProjectWordDTO
import com.iamfiro.clari.core.network.dto.ProjectDto
import com.iamfiro.clari.feature.project.model.Project
import com.iamfiro.clari.feature.project.model.Word

object KeywordPackMapper {
    
    /**
     * KeywordPackDto -> Project (기존 UI 호환용)
     */
    fun fromDto(dto: ProjectDto): Project {
        return Project(
            id = dto.id,
            name = dto.name,
            description = "", // API에서 description 미제공
            publisherId = "user",
            publisherName = "사용자",
            thumbnail = dto.previewImageUrl ?: "",
            word = dto.words.map { keyword ->
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
     * ProjectWordDTO -> Word
     */
    fun keywordToWord(dto: ProjectWordDTO): Word {
        return Word(
            name = dto.name,
            meaning = dto.description
        )
    }
    
    /**
     * Word -> ProjectWordDTO
     */
    fun wordToKeyword(word: Word): ProjectWordDTO {
        return ProjectWordDTO(
            name = word.name,
            description = word.meaning
        )
    }
    
    /**
     * List<Word> -> List<ProjectWordDTO>
     */
    fun wordsToKeywords(words: List<Word>): List<ProjectWordDTO> {
        return words.map { wordToKeyword(it) }
    }
}



