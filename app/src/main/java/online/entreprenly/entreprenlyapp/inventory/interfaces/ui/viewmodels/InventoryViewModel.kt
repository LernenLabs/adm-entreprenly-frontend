package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.inventory.application.commandservices.ProductCommandService
import online.entreprenly.entreprenlyapp.inventory.application.commandservices.LotCommandService
import online.entreprenly.entreprenlyapp.inventory.application.queryservices.LotQueryService
import online.entreprenly.entreprenlyapp.inventory.application.queryservices.ProductQueryService
import online.entreprenly.entreprenlyapp.inventory.application.queryservices.StockAlertQueryService
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateLotCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.CreateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.commands.UpdateProductCommand
import online.entreprenly.entreprenlyapp.inventory.domain.model.entities.StockAlert
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetAllLotsQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetAllProductsQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.queries.GetStockAlertsQuery
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.InventoryRules
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.FormState
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.UiText

/** Screen state shared by the inventory tabs and detail screens. */
sealed interface InventoryState {
    data object Loading : InventoryState
    data class Error(val message: UiText) : InventoryState
    data class Ready(
        val products: List<Product>,
        val lots: List<Lot>,
        val alerts: List<StockAlert>,
        val refreshing: Boolean = false,
        val recentlyAddedProduct: String? = null,
        val recentlyAddedLot: String? = null,
        val notice: UiText? = null
    ) : InventoryState {
        fun product(type: ProductType, id: Long): Product? =
            products.firstOrNull { it.type == type && it.id == id }

        fun lot(type: ProductType, id: Long): Lot? =
            lots.firstOrNull { it.type == type && it.id == id }

        fun lotsOf(product: Product): List<Lot> = InventoryRules.lotsOfProduct(product, lots)

        fun stockOf(product: Product): Double = InventoryRules.productStock(product, lots)
    }
}

/**
 * Loads unit + weight products and lots in parallel. Alerts degrade to an
 * empty list when they fail; products or lots failures surface an error.
 */
class InventoryViewModel(
    private val productQueryService: ProductQueryService,
    private val lotQueryService: LotQueryService,
    private val stockAlertQueryService: StockAlertQueryService,
    private val productCommandService: ProductCommandService,
    private val lotCommandService: LotCommandService
) : ViewModel() {

    private val _state = MutableStateFlow<InventoryState>(InventoryState.Loading)
    val state: StateFlow<InventoryState> = _state.asStateFlow()

    private val _productForm = MutableStateFlow(FormState())
    val productForm: StateFlow<FormState> = _productForm.asStateFlow()

    private val _lotForm = MutableStateFlow(FormState())
    val lotForm: StateFlow<FormState> = _lotForm.asStateFlow()

    init {
        refresh()
    }

    /** Keeps the current content visible while reloading. */
    fun refresh() {
        val current = _state.value
        _state.value = if (current is InventoryState.Ready) {
            current.copy(refreshing = true, recentlyAddedProduct = null, recentlyAddedLot = null)
        } else {
            InventoryState.Loading
        }
        viewModelScope.launch { _state.value = load(null, null) }
    }

    fun consumeNotice() {
        val current = _state.value as? InventoryState.Ready ?: return
        if (current.notice != null) _state.value = current.copy(notice = null)
    }

    fun clearProductForm() {
        _productForm.value = FormState()
    }

    fun clearLotForm() {
        _lotForm.value = FormState()
    }

    fun createProduct(command: CreateProductCommand) {
        if (_productForm.value.loading) return
        _productForm.value = FormState(loading = true)
        viewModelScope.launch {
            when (val r = productCommandService.handle(command)) {
                is Result.Success -> {
                    _productForm.value = FormState(success = UiText.Res(R.string.inventory_product_created))
                    val notice = if (r.value.initialStockFailed) {
                        UiText.Res(R.string.inventory_product_partial_stock)
                    } else {
                        UiText.Res(R.string.inventory_product_created)
                    }
                    _state.value = load(
                        highlightProduct = r.value.product.key,
                        highlightLot = null,
                        notice = notice,
                        keepContent = true
                    )
                }
                is Result.Failure -> _productForm.value = FormState(error = UiText.Raw(r.error.message))
            }
        }
    }

    fun updateProduct(command: UpdateProductCommand) {
        if (_productForm.value.loading) return
        _productForm.value = FormState(loading = true)
        viewModelScope.launch {
            when (val r = productCommandService.handle(command)) {
                is Result.Success -> {
                    _productForm.value = FormState(success = UiText.Res(R.string.inventory_product_updated))
                    _state.value = load(
                        highlightProduct = r.value.key,
                        highlightLot = null,
                        notice = UiText.Res(R.string.inventory_product_updated),
                        keepContent = true
                    )
                }
                is Result.Failure -> _productForm.value = FormState(error = UiText.Raw(r.error.message))
            }
        }
    }

    fun createLot(command: CreateLotCommand) {
        if (_lotForm.value.loading) return
        _lotForm.value = FormState(loading = true)
        viewModelScope.launch {
            when (val r = lotCommandService.handle(command)) {
                is Result.Success -> {
                    _lotForm.value = FormState(success = UiText.Res(R.string.inventory_lot_created))
                    _state.value = load(
                        highlightProduct = null,
                        highlightLot = r.value.key,
                        notice = UiText.Res(R.string.inventory_lot_created),
                        keepContent = true
                    )
                }
                is Result.Failure -> _lotForm.value = FormState(error = UiText.Raw(r.error.message))
            }
        }
    }

    private suspend fun load(
        highlightProduct: String?,
        highlightLot: String?,
        notice: UiText? = null,
        keepContent: Boolean = false
    ): InventoryState = coroutineScope {
        if (keepContent) {
            val current = _state.value as? InventoryState.Ready
            if (current != null) _state.value = current.copy(refreshing = true)
        } else if (_state.value !is InventoryState.Ready) {
            _state.value = InventoryState.Loading
        }
        val products = async { productQueryService.handle(GetAllProductsQuery) }
        val lots = async { lotQueryService.handle(GetAllLotsQuery) }
        val alerts = async { stockAlertQueryService.handle(GetStockAlertsQuery) }
        when (val p = products.await()) {
            is Result.Failure -> InventoryState.Error(UiText.Raw(p.error.message))
            is Result.Success -> when (val l = lots.await()) {
                is Result.Failure -> InventoryState.Error(UiText.Raw(l.error.message))
                // Alerts are informative only: a failure degrades to no low-stock pills.
                is Result.Success -> InventoryState.Ready(
                    products = p.value,
                    lots = l.value,
                    alerts = (alerts.await() as? Result.Success)?.value.orEmpty(),
                    recentlyAddedProduct = highlightProduct,
                    recentlyAddedLot = highlightLot,
                    notice = notice
                )
            }
        }
    }
}
