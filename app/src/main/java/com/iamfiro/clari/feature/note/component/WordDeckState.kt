package com.iamfiro.clari.feature.note.component

import com.iamfiro.clari.core.service.KeywordHit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WordDeckState {

    private val _terms = MutableStateFlow<List<DetectedTerm>>(emptyList())
    val terms: StateFlow<List<DetectedTerm>> = _terms.asStateFlow()

    private val _shouldTriggerHaptic = MutableStateFlow(false)
    val shouldTriggerHaptic: StateFlow<Boolean> = _shouldTriggerHaptic.asStateFlow()

    private val seenTermIds = mutableSetOf<String>()

    fun onTermDetected(keyword: KeywordHit) {
        val termId = generateTermId(keyword)
        val isNewTerm = !seenTermIds.contains(termId)

        _terms.update { currentTerms ->
            val filteredTerms = currentTerms.filter { it.id != termId }

            val newTerm = DetectedTerm(
                id = termId,
                keyword = keyword,
                detectedAt = System.currentTimeMillis()
            )

            listOf(newTerm) + filteredTerms
        }

        if (isNewTerm) {
            seenTermIds.add(termId)
            _shouldTriggerHaptic.value = true
        }
    }

    fun onTermsDetected(keywords: List<KeywordHit>) {
        if (keywords.isEmpty()) return

        var hasNewTerm = false

        _terms.update { currentTerms ->
            val newTermIds = keywords.map { generateTermId(it) }.toSet()

            keywords.forEach { keyword ->
                val termId = generateTermId(keyword)
                if (!seenTermIds.contains(termId)) {
                    seenTermIds.add(termId)
                    hasNewTerm = true
                }
            }

            val filteredTerms = currentTerms.filter { it.id !in newTermIds }

            val newTerms = keywords.map { keyword ->
                DetectedTerm(
                    id = generateTermId(keyword),
                    keyword = keyword,
                    detectedAt = System.currentTimeMillis()
                )
            }

            newTerms + filteredTerms
        }

        if (hasNewTerm) {
            _shouldTriggerHaptic.value = true
        }
    }

    fun onHapticTriggered() {
        _shouldTriggerHaptic.value = false
    }

    fun removeTerm(termId: String) {
        _terms.update { currentTerms ->
            currentTerms.filter { it.id != termId }
        }
    }

    fun clear() {
        _terms.value = emptyList()
        _shouldTriggerHaptic.value = false
        seenTermIds.clear()
    }

    val termCount: Int
        get() = _terms.value.size

    private fun generateTermId(keyword: KeywordHit): String {
        return keyword.name.lowercase().trim()
    }
}

fun List<KeywordHit>.toDetectedTerms(): List<DetectedTerm> {
    return this.map { keyword ->
        DetectedTerm(
            id = keyword.name.lowercase().trim(),
            keyword = keyword,
            detectedAt = System.currentTimeMillis()
        )
    }
}
