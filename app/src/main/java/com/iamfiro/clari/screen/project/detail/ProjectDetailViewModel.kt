package com.iamfiro.clari.screen.project.detail

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // AI 자동완성 제안
    private val _aiSuggestions = MutableStateFlow<List<String>>(emptyList())
    val aiSuggestions: StateFlow<List<String>> = _aiSuggestions.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            projectRepository.getKeywordPackById(projectId)
                .onSuccess { pack ->
                    _project.value = pack
                    Log.d(TAG, "프로젝트 로드 완료: ${pack.name}")
                }
                .onFailure { e ->
                    Log.e(TAG, "프로젝트 로드 실패", e)
                    _error.value = "프로젝트를 불러오는데 실패했습니다: ${e.message}"
                }
            
            _isLoading.value = false
        }
    }

    fun refresh() {
        loadProject()
    }

    fun addWord(name: String, meaning: String) {
        viewModelScope.launch {
            _error.value = null
            
            val word = Word(name = name, meaning = meaning)
            projectRepository.addProjectWord(projectId, word)
                .onSuccess { pack ->
                    _project.value = pack
                    Log.d(TAG, "키워드 추가 완료: $name")
                }
                .onFailure { e ->
                    Log.e(TAG, "키워드 추가 실패", e)
                    _error.value = "키워드 추가에 실패했습니다: ${e.message}"
                }
        }
    }

    fun removeWord(wordName: String) {
        viewModelScope.launch {
            _error.value = null
            
            projectRepository.removeProjectWord(projectId, wordName)
                .onSuccess { pack ->
                    _project.value = pack
                    Log.d(TAG, "키워드 삭제 완료: $wordName")
                }
                .onFailure { e ->
                    Log.e(TAG, "키워드 삭제 실패", e)
                    _error.value = "키워드 삭제에 실패했습니다: ${e.message}"
                }
        }
    }

    fun updateProjectName(newName: String) {
        viewModelScope.launch {
            _error.value = null
            
            projectRepository.updateProject(projectId, name = newName)
                .onSuccess { pack ->
                    _project.value = pack
                    Log.d(TAG, "프로젝트 이름 변경 완료: $newName")
                }
                .onFailure { e ->
                    Log.e(TAG, "프로젝트 이름 변경 실패", e)
                    _error.value = "이름 변경에 실패했습니다: ${e.message}"
                }
        }
    }

    fun updateBannerImage(uri: Uri) {
        viewModelScope.launch {
            _error.value = null
            
            // 이미지 URL을 previewImageUrl로 업데이트
            projectRepository.updateProject(
                projectId, 
                previewImageUrl = uri.toString()
            )
                .onSuccess { pack ->
                    _project.value = pack
                    Log.d(TAG, "배너 이미지 업데이트 완료")
                }
                .onFailure { e ->
                    Log.e(TAG, "배너 이미지 업데이트 실패", e)
                    _error.value = "배너 이미지 업로드에 실패했습니다: ${e.message}"
                }
        }
    }

    fun deleteProject(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _error.value = null
            
            projectRepository.deleteProject(projectId)
                .onSuccess {
                    Log.d(TAG, "프로젝트 삭제 완료")
                    onSuccess()
                }
                .onFailure { e ->
                    Log.e(TAG, "프로젝트 삭제 실패", e)
                    _error.value = "프로젝트 삭제에 실패했습니다: ${e.message}"
                }
        }
    }

    // Connector 관련 (로컬 상태만 관리 - API에서는 지원하지 않음)
    fun addConnector(type: ProjectConnectorType, name: String, url: String) {
        val currentProject = _project.value ?: return
        val connector = ProjectConnector(type = type, name = name, url = url)
        val updatedConnectors = (currentProject.connector ?: emptyList()) + connector
        _project.value = currentProject.copy(connector = updatedConnectors)
    }

    fun updateConnector(oldConnector: ProjectConnector, newConnector: ProjectConnector) {
        val currentProject = _project.value ?: return
        val connectors = currentProject.connector?.toMutableList() ?: return
        val index = connectors.indexOfFirst { 
            it.type == oldConnector.type && it.url == oldConnector.url 
        }
        if (index != -1) {
            connectors[index] = newConnector
            _project.value = currentProject.copy(connector = connectors)
        }
    }

    fun removeConnector(connector: ProjectConnector) {
        val currentProject = _project.value ?: return
        val connectors = currentProject.connector?.filter { 
            !(it.type == connector.type && it.url == connector.url)
        }
        _project.value = currentProject.copy(connector = connectors)
    }

    suspend fun getShareLink(): String {
        // 공유 링크 생성 (실제 API 없으므로 임시 URL 반환)
        return "https://clari.app/share/pack/$projectId"
    }

    // AI 기능

    /**
     * 키워드 이름으로 설명 자동완성 제안 받기
     */
    fun getAiSuggestions(keywordName: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiSuggestions.value = emptyList()
            
            projectRepository.generateWordDescription(keywordName)
                .onSuccess { suggestions ->
                    _aiSuggestions.value = suggestions
                    Log.d(TAG, "AI 제안 ${suggestions.size}개 수신")
                }
                .onFailure { e ->
                    Log.e(TAG, "AI 제안 실패", e)
                    _error.value = "AI 제안을 가져오는데 실패했습니다"
                }
            
            _isAiLoading.value = false
        }
    }

    fun clearAiSuggestions() {
        _aiSuggestions.value = emptyList()
    }

    /**
     * AI 자동채우기 - 쿼리로 키워드 목록 생성
     */
    fun aiAutofill(query: String, count: Int = 50, onComplete: (List<Word>) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            
            projectRepository.generateAutofill(query, count)
                .onSuccess { words ->
                    Log.d(TAG, "AI 자동채우기 ${words.size}개 키워드 생성")
                    onComplete(words)
                }
                .onFailure { e ->
                    Log.e(TAG, "AI 자동채우기 실패", e)
                    _error.value = "AI 자동채우기에 실패했습니다"
                    onComplete(emptyList())
                }
            
            _isAiLoading.value = false
        }
    }

    /**
     * AI 생성 키워드들을 프로젝트에 추가
     */
    fun addWordsFromAi(words: List<Word>) {
        viewModelScope.launch {
            _error.value = null
            
            val currentProject = _project.value ?: return@launch
            val updatedWords = currentProject.word + words
            
            projectRepository.updateProject(projectId, keywords = updatedWords)
                .onSuccess { pack ->
                    _project.value = pack
                    Log.d(TAG, "${words.size}개 키워드 일괄 추가 완료")
                }
                .onFailure { e ->
                    Log.e(TAG, "키워드 일괄 추가 실패", e)
                    _error.value = "키워드 추가에 실패했습니다"
                }
        }
    }
}
