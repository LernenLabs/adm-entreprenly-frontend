package online.entreprenly.entreprenlyapp.sales.interfaces.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import online.entreprenly.entreprenlyapp.sales.domain.model.aggregates.Sale
import online.entreprenly.entreprenlyapp.sales.domain.model.entities.SaleItem
import online.entreprenly.entreprenlyapp.sales.domain.model.entities.SaleProduct
import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.PaymentMethod

import online.entreprenly.entreprenlyapp.sales.application.commandservices.SalesCommandService
import online.entreprenly.entreprenlyapp.sales.application.queryservices.SalesQueryService
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import online.entreprenly.entreprenlyapp.shared.application.result.Result

data class SalesUiState(
    val cashTotalToday: Double = 0.0,
    val digitalTotalToday: Double = 0.0,
    val allProducts: List<SaleProduct> = emptyList(),
    val filteredProducts: List<SaleProduct> = emptyList(),
    val searchQuery: String = "",
    val currentTicket: List<SaleItem> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val showSuccessDialog: Boolean = false,
    val lastSale: Sale? = null,
    val salesHistory: List<Sale> = emptyList(),
    val isHistoryOpen: Boolean = false
) {
    val ticketTotal: Double
        get() = currentTicket.sumOf { it.subtotal }
}

class SalesViewModel(
    private val salesQueryService: SalesQueryService,
    private val salesCommandService: SalesCommandService
) : ViewModel() {
    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    init {
        // Cargar datos mock de inventario
        val mocks = listOf(
            SaleProduct("1", "Arroz Costeño", false, 4.5, 50.0),
            SaleProduct("2", "Gaseosa Inca Kola 1.5L", true, 8.0, 24.0),
            SaleProduct("3", "Azúcar Rubia Ledesma", false, 3.8, 40.0),
            SaleProduct("4", "Aceite Primor 1L", true, 9.5, 12.0),
            SaleProduct("5", "Fideos Don Vittorio 500g", true, 2.5, 30.0)
        )
        _uiState.update { it.copy(allProducts = mocks, filteredProducts = mocks) }
        
        loadCashRegisterSummary()
    }

    private fun loadCashRegisterSummary() {
        viewModelScope.launch {
            when (val result = salesQueryService.getCashRegisterSummary()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            cashTotalToday = result.value.cashTotal,
                            digitalTotalToday = result.value.digitalTotal
                        )
                    }
                }
                is Result.Failure -> {
                    // Manejar error si es necesario (el repositorio impl usa fallback, así que rara vez llegará aquí)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.allProducts
            } else {
                state.allProducts.filter { it.name.contains(query, ignoreCase = true) }
            }
            state.copy(searchQuery = query, filteredProducts = filtered)
        }
    }

    fun onAddItemToTicket(product: SaleProduct, quantity: Double) {
        if (quantity <= 0.0 || quantity > product.stockAvailable) return

        _uiState.update { state ->
            val existingItem = state.currentTicket.find { it.productId == product.id }
            val newTicket = if (existingItem != null) {
                state.currentTicket.map {
                    if (it.productId == product.id) {
                        it.copy(quantity = it.quantity + quantity)
                    } else it
                }
            } else {
                state.currentTicket + SaleItem(
                    productId = product.id,
                    productName = product.name,
                    isUnit = product.isUnit,
                    quantity = quantity,
                    unitPrice = product.price
                )
            }
            state.copy(currentTicket = newTicket)
        }
    }

    fun onRemoveItem(index: Int) {
        _uiState.update { state ->
            val newList = state.currentTicket.toMutableList()
            if (index in newList.indices) {
                newList.removeAt(index)
            }
            state.copy(currentTicket = newList)
        }
    }

    fun onSelectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun onFinishSale() {
        val state = _uiState.value
        val pm = state.selectedPaymentMethod ?: return
        if (state.currentTicket.isEmpty()) return

        val sale = Sale(
            items = state.currentTicket,
            paymentMethod = pm,
            total = state.ticketTotal
        )

        viewModelScope.launch {
            when (val result = salesCommandService.createSale(sale)) {
                is Result.Success -> {
                    val finalSale = result.value
                    _uiState.update { currentState ->
                        val newCash = currentState.cashTotalToday + if (pm == PaymentMethod.CASH) finalSale.total else 0.0
                        val newDigital = currentState.digitalTotalToday + if (pm == PaymentMethod.DIGITAL) finalSale.total else 0.0

                        currentState.copy(
                            cashTotalToday = newCash,
                            digitalTotalToday = newDigital,
                            currentTicket = emptyList(),
                            selectedPaymentMethod = null,
                            searchQuery = "",
                            filteredProducts = currentState.allProducts,
                            showSuccessDialog = true,
                            lastSale = finalSale,
                            salesHistory = currentState.salesHistory + finalSale
                        )
                    }
                }
                is Result.Failure -> {
                    // Manejo de error si la API y el fallback fallaran simultáneamente.
                }
            }
        }
    }

    fun onDismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false, lastSale = null) }
    }

    fun onToggleHistory(isOpen: Boolean) {
        _uiState.update { it.copy(isHistoryOpen = isOpen) }
    }
}
