package com.tillzo.pos.ui.inventory.module_c.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.domain.usecase.grn.GetGrnsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GrnListViewModel @Inject constructor(
    private val getGrnsUseCase: GetGrnsUseCase
) : ViewModel() {

    private val _grnList = MutableStateFlow<List<GrnHeaderEntity>>(emptyList())
    val grnList: StateFlow<List<GrnHeaderEntity>> = _grnList.asStateFlow()

    init {
        viewModelScope.launch {
            getGrnsUseCase().collectLatest {
                _grnList.value = it
            }
        }
    }
}
