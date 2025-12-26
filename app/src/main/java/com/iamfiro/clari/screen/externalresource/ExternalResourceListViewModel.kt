package com.iamfiro.clari.screen.externalresource

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamfiro.clari.core.repository.ExternalResourceRepository
import com.iamfiro.clari.feature.externalresource.model.ExternalResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ExternalResourceListVM"

class ExternalResourceListViewModel(
    private val repository: ExternalResourceRepository
) : ViewModel() {

    private val _resources = MutableStateFlow<List<ExternalResource>>(emptyList())
    val resources: StateFlow<List<ExternalResource>> = _resources.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAddingResource = MutableStateFlow(false)
    val isAddingResource: StateFlow<Boolean> = _isAddingResource.asStateFlow()

    init {
        loadResources()
    }

    private fun loadResources() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getAllResources()
                .onSuccess { list ->
                    _resources.value = list
                    Log.d(TAG, "리소스 ${list.size}개 로드 완료")
                }
                .onFailure { e ->
                    Log.e(TAG, "리소스 로드 실패", e)
                    _error.value = "리소스를 불러오는데 실패했습니다: ${e.message}"
                }
            
            _isLoading.value = false
        }
    }

    fun refresh() {
        loadResources()
    }

    fun addResource(url: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isAddingResource.value = true
            _error.value = null
            
            repository.createResource(url)
                .onSuccess { resource ->
                    Log.d(TAG, "리소스 추가 완료: ${resource.title}")
                    _resources.value = listOf(resource) + _resources.value
                    onSuccess()
                }
                .onFailure { e ->
                    Log.e(TAG, "리소스 추가 실패", e)
                    _error.value = "리소스 추가에 실패했습니다: ${e.message}"
                }
            
            _isAddingResource.value = false
        }
    }

    fun deleteResource(resourceId: String) {
        viewModelScope.launch {
            repository.deleteResource(resourceId)
                .onSuccess {
                    Log.d(TAG, "리소스 삭제 완료: $resourceId")
                    _resources.value = _resources.value.filter { it.id != resourceId }
                }
                .onFailure { e ->
                    Log.e(TAG, "리소스 삭제 실패", e)
                    _error.value = "리소스 삭제에 실패했습니다"
                }
        }
    }
}


