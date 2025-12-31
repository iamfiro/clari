package com.iamfiro.clari.screen.project.detail

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.repository.ExternalResourceRepository
import com.iamfiro.clari.core.repository.ProjectRepository
import com.iamfiro.clari.feature.project.model.Project
import com.iamfiro.clari.feature.project.model.ProjectConnector
import com.iamfiro.clari.feature.project.model.ProjectConnectorType
import com.iamfiro.clari.feature.project.model.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ProjectDetailViewModel"

class ProjectDetailViewModel(
    private val projectRepository: ProjectRepository,
    private val projectId: String
) : ViewModel() {
    
    private val externalResourceRepository = ExternalResourceRepository.getInstance()

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            Log.d(TAG, "loadProject 시작: projectId=$projectId")
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                project = null
            )
            
            projectRepository.getKeywordPackById(projectId)
                .onSuccess { pack ->
                    Log.d(TAG, "프로젝트 로드 완료: id=${pack.id}, name=${pack.name}, words=${pack.word.size}, isOwned=${pack.isOwned}, isSaved=${pack.isSaved}")
                    _uiState.value = _uiState.value.copy(
                        project = pack,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "프로젝트 로드 실패: projectId=$projectId", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "프로젝트를 불러오는데 실패했습니다: ${e.message}",
                        project = null
                    )
                }
        }
    }

    fun refresh() {
        loadProject()
    }

    fun addWord(name: String, meaning: String) {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            
            val word = Word(name = name, meaning = meaning)
            projectRepository.addProjectWord(projectId, word)
                .onSuccess { pack ->
                    Log.d(TAG, "키워드 추가 완료: $name, words=${pack.word.size}")
                    _uiState.value = _uiState.value.copy(
                        project = pack,
                        error = null
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "키워드 추가 실패", e)
                    _uiState.value = _uiState.value.copy(
                        error = "키워드 추가에 실패했습니다: ${e.message}"
                    )
                }
        }
    }

    fun removeWord(wordName: String) {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            
            projectRepository.removeProjectWord(projectId, wordName)
                .onSuccess { pack ->
                    Log.d(TAG, "키워드 삭제 완료: $wordName, words=${pack.word.size}")
                    _uiState.value = _uiState.value.copy(
                        project = pack,
                        error = null
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "키워드 삭제 실패", e)
                    _uiState.value = _uiState.value.copy(
                        error = "키워드 삭제에 실패했습니다: ${e.message}"
                    )
                }
        }
    }

    fun updateProjectName(newName: String) {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            
            val currentProject = _uiState.value.project ?: return@launch
            projectRepository.updateProject(
                packId = projectId,
                name = newName,
                keywords = currentProject.word,
                isPublic = currentProject.isPublic,
                previewImageUrl = currentProject.thumbnail
            )
                .onSuccess { pack ->
                    Log.d(TAG, "프로젝트 이름 변경 완료: $newName")
                    _uiState.value = _uiState.value.copy(
                        project = pack,
                        error = null
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "프로젝트 이름 변경 실패", e)
                    _uiState.value = _uiState.value.copy(
                        error = "이름 변경에 실패했습니다: ${e.message}"
                    )
                }
        }
    }

    fun updateBannerImage(uri: Uri) {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            
            val currentProject = _uiState.value.project ?: return@launch

            projectRepository.updateProject(
                packId = projectId,
                name = currentProject.name,
                keywords = currentProject.word,
                isPublic = currentProject.isPublic,
                previewImageUrl = uri.toString()
            )
                .onSuccess { pack ->
                    Log.d(TAG, "배너 이미지 업데이트 완료")
                    _uiState.value = _uiState.value.copy(
                        project = pack,
                        error = null
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "배너 이미지 업데이트 실패", e)
                    _uiState.value = _uiState.value.copy(
                        error = "배너 이미지 업로드에 실패했습니다: ${e.message}"
                    )
                }
        }
    }

    fun togglePublic() {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTogglingPublic = true, error = null)
            
            val currentProject = _uiState.value.project ?: return@launch
            val newIsPublic = !currentProject.isPublic
            
            projectRepository.updateProject(
                packId = projectId,
                name = currentProject.name,
                keywords = currentProject.word,
                isPublic = newIsPublic,
                previewImageUrl = currentProject.thumbnail
            )
                .onSuccess { pack ->
                    Log.d(TAG, "공개 설정 변경 완료: isPublic=$newIsPublic")
                    _uiState.value = _uiState.value.copy(
                        project = pack,
                        isTogglingPublic = false,
                        error = null
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "공개 설정 변경 실패", e)
                    _uiState.value = _uiState.value.copy(
                        isTogglingPublic = false,
                        error = "공개 설정 변경에 실패했습니다: ${e.message}"
                    )
                }
        }
    }

    fun cloudSave() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            
            projectRepository.cloudSave(projectId)
                .onSuccess { message ->
                    Log.d(TAG, "Cloud Save 완료: $message")
                    loadProject()
                    _uiState.value = _uiState.value.copy(isSaving = false)
                }
                .onFailure { e ->
                    Log.e(TAG, "Cloud Save 실패", e)
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "저장에 실패했습니다: ${e.message}"
                    )
                }
        }
    }

    fun cloudUnsave(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUnsaving = true, error = null)
            
            projectRepository.cloudUnsave(projectId)
                .onSuccess { message ->
                    Log.d(TAG, "Cloud Unsave 완료: $message")
                    _uiState.value = _uiState.value.copy(isUnsaving = false)
                    onSuccess()
                }
                .onFailure { e ->
                    Log.e(TAG, "Cloud Unsave 실패", e)
                    _uiState.value = _uiState.value.copy(
                        isUnsaving = false,
                        error = "저장 해제에 실패했습니다: ${e.message}"
                    )
                }
        }
    }

    fun deleteProject(onSuccess: () -> Unit) {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            
            projectRepository.deleteProject(projectId)
                .onSuccess {
                    Log.d(TAG, "프로젝트 삭제 완료")
                    onSuccess()
                }
                .onFailure { e ->
                    Log.e(TAG, "프로젝트 삭제 실패", e)
                    _uiState.value = _uiState.value.copy(
                        error = "프로젝트 삭제에 실패했습니다: ${e.message}"
                    )
                }
        }
    }

    fun addConnector(type: ProjectConnectorType, name: String, url: String) {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null, isAddingConnector = true)

            externalResourceRepository.createResource(url)
                .onSuccess { resource ->
                    Log.d(TAG, "외부 리소스 생성 완료: id=${resource.id}, title=${resource.title}")

                    val currentProject = _uiState.value.project ?: return@onSuccess
                    val connector = ProjectConnector(
                        type = type, 
                        name = name,
                        url = url
                    )
                    val updatedConnectors = (currentProject.connector ?: emptyList()) + connector

                    _uiState.value = _uiState.value.copy(
                        project = currentProject.copy(connector = updatedConnectors),
                        isAddingConnector = false
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "외부 리소스 생성 실패", e)
                    _uiState.value = _uiState.value.copy(
                        error = "외부 연결 추가에 실패했습니다: ${e.message}",
                        isAddingConnector = false
                    )
                }
        }
    }

    fun updateConnector(oldConnector: ProjectConnector, newConnector: ProjectConnector) {
        if (!_uiState.value.canEdit) return
        val currentProject = _uiState.value.project ?: return
        val connectors = currentProject.connector?.toMutableList() ?: return
        val index = connectors.indexOfFirst { 
            it.type == oldConnector.type && it.url == oldConnector.url 
        }
        if (index != -1) {
            connectors[index] = newConnector
            _uiState.value = _uiState.value.copy(
                project = currentProject.copy(connector = connectors)
            )
        }
    }

    fun removeConnector(connector: ProjectConnector) {
        if (!_uiState.value.canEdit) return
        val currentProject = _uiState.value.project ?: return
        val connectors = currentProject.connector?.filter { 
            !(it.type == connector.type && it.url == connector.url)
        }
        _uiState.value = _uiState.value.copy(
            project = currentProject.copy(connector = connectors)
        )
    }

    suspend fun getShareLink(): String {
        return "https://clari-deeplink.thnos.app/$projectId"
    }

    fun getAiSuggestions(keywordName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAiLoading = true,
                aiSuggestions = emptyList()
            )
            
            projectRepository.generateWordDescription(keywordName)
                .onSuccess { suggestions ->
                    Log.d(TAG, "AI 제안 ${suggestions.size}개 수신")
                    _uiState.value = _uiState.value.copy(
                        aiSuggestions = suggestions,
                        isAiLoading = false
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "AI 제안 실패", e)
                    _uiState.value = _uiState.value.copy(
                        error = "AI 제안을 가져오는데 실패했습니다",
                        isAiLoading = false
                    )
                }
        }
    }

    fun clearAiSuggestions() {
        _uiState.value = _uiState.value.copy(aiSuggestions = emptyList())
    }

    fun aiAutofill(query: String, count: Int = 50, onComplete: (List<Word>) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAiLoading = true)
            
            projectRepository.generateAutofill(query, count)
                .onSuccess { words ->
                    Log.d(TAG, "AI 자동채우기 ${words.size}개 키워드 생성")
                    _uiState.value = _uiState.value.copy(isAiLoading = false)
                    onComplete(words)
                }
                .onFailure { e ->
                    Log.e(TAG, "AI 자동채우기 실패", e)
                    _uiState.value = _uiState.value.copy(
                        error = "AI 자동채우기에 실패했습니다",
                        isAiLoading = false
                    )
                    onComplete(emptyList())
                }
        }
    }

    fun addWordsFromAi(words: List<Word>) {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            
            val currentProject = _uiState.value.project ?: return@launch
            val updatedWords = currentProject.word + words
            
            projectRepository.updateProject(
                packId = projectId,
                name = currentProject.name,
                keywords = updatedWords,
                isPublic = currentProject.isPublic,
                previewImageUrl = currentProject.thumbnail
            )
                .onSuccess { pack ->
                    Log.d(TAG, "${words.size}개 키워드 일괄 추가 완료, words=${pack.word.size}")
                    _uiState.value = _uiState.value.copy(
                        project = pack,
                        error = null
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "키워드 일괄 추가 실패", e)
                    _uiState.value = _uiState.value.copy(
                        error = "키워드 추가에 실패했습니다"
                    )
                }
        }
    }

    fun generateAiWords(topic: String, count: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAiLoading = true,
                aiGeneratedWords = emptyList()
            )
            
            projectRepository.generateAutofill(topic, count)
                .onSuccess { words ->
                    Log.d(TAG, "AI 단어 생성 완료: ${words.size}개")
                    _uiState.value = _uiState.value.copy(
                        aiGeneratedWords = words,
                        isAiLoading = false,
                        error = null
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "AI 단어 생성 실패", e)
                    _uiState.value = _uiState.value.copy(
                        error = "AI 단어 생성에 실패했습니다",
                        isAiLoading = false
                    )
                }
        }
    }

    fun removeAiGeneratedWord(word: Word) {
        val currentWords = _uiState.value.aiGeneratedWords
        _uiState.value = _uiState.value.copy(
            aiGeneratedWords = currentWords.filter { it != word }
        )
    }

    fun addAiGeneratedWordsToProject(onComplete: () -> Unit) {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            val wordsToAdd = _uiState.value.aiGeneratedWords
            if (wordsToAdd.isEmpty()) {
                onComplete()
                return@launch
            }

            val currentProject = _uiState.value.project ?: return@launch
            val updatedWords = currentProject.word + wordsToAdd
            
            projectRepository.updateProject(
                packId = projectId,
                name = currentProject.name,
                keywords = updatedWords,
                isPublic = currentProject.isPublic,
                previewImageUrl = currentProject.thumbnail
            )
                .onSuccess { pack ->
                    Log.d(TAG, "${wordsToAdd.size}개 단어 추가 완료")
                    _uiState.value = _uiState.value.copy(
                        project = pack,
                        aiGeneratedWords = emptyList(),
                        error = null
                    )
                    onComplete()
                }
                .onFailure { e ->
                    Log.e(TAG, "단어 추가 실패", e)
                    _uiState.value = _uiState.value.copy(
                        error = "단어 추가에 실패했습니다"
                    )
                }
        }
    }

    fun clearAiGeneratedWords() {
        _uiState.value = _uiState.value.copy(aiGeneratedWords = emptyList())
    }
}
