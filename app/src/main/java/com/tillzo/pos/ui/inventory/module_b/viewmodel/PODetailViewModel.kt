package com.tillzo.pos.ui.inventory.module_b.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tillzo.pos.data.local.dao.PurchaseOrderDao
import com.tillzo.pos.data.local.entity.GrnHeaderEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderEntity
import com.tillzo.pos.data.local.entity.PurchaseOrderItemEntity
import com.tillzo.pos.domain.repository.GrnRepository
import com.tillzo.pos.domain.usecase.po.SharePurchaseOrderUseCase
import com.tillzo.pos.domain.usecase.po.UpdatePOStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PODetailViewModel @Inject constructor(
    private val getPoDao: PurchaseOrderDao,
    private val updateStatusUseCase: UpdatePOStatusUseCase,
    private val shareUseCase: SharePurchaseOrderUseCase,
    private val grnRepository: GrnRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val poId: String = checkNotNull(savedStateHandle["poId"])

    private val _po = MutableStateFlow<PurchaseOrderEntity?>(null)
    val po: StateFlow<PurchaseOrderEntity?> = _po.asStateFlow()

    private val _items = MutableStateFlow<List<PurchaseOrderItemEntity>>(emptyList())
    val items: StateFlow<List<PurchaseOrderItemEntity>> = _items.asStateFlow()

    private val _linkedGrns = MutableStateFlow<List<GrnHeaderEntity>>(emptyList())
    val linkedGrns: StateFlow<List<GrnHeaderEntity>> = _linkedGrns.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val loadedPo = getPoDao.getPOById(poId)
            _po.value = loadedPo
            val loadedItems = getPoDao.getPOItems(poId)
            _items.value = loadedItems
        }
        viewModelScope.launch {
            grnRepository.getGrnsForPO(poId).collectLatest {
                _linkedGrns.value = it
            }
        }
    }

    fun updateStatus(status: String) {
        viewModelScope.launch {
            updateStatusUseCase(poId, status)
            loadData() // refresh
        }
    }
}
