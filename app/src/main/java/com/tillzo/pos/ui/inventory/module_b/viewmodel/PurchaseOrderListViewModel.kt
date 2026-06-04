package com.tillzo.pos.ui.inventory.module_b.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.domain.usecase.po.GetPurchaseOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseOrderListViewModel @Inject constructor(
    private val getPOsUseCase: GetPurchaseOrdersUseCase
) : ViewModel() {

    private val _poList = MutableStateFlow<List<PurchaseOrderEntity>>(emptyList())
    val poList: StateFlow<List<PurchaseOrderEntity>> = _poList.asStateFlow()

    init {
        viewModelScope.launch {
            getPOsUseCase().collectLatest {
                _poList.value = it
            }
        }
    }
}
