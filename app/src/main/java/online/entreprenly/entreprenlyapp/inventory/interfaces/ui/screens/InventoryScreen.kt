package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryError
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryLoading
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.SegmentedControl
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.SheetHeader
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.viewmodels.InventoryState
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.viewmodels.InventoryViewModel

/** Inventory root: orange header, Productos|Lotes control, tab content and FAB. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onProductClick: (Product) -> Unit,
    onLotClick: (String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showAddProduct by rememberSaveable { mutableStateOf(false) }
    var showCreateLot by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val noProductsMessage = stringResource(R.string.inventory_lots_no_products)

    val notice = (state as? InventoryState.Ready)?.notice
    val noticeText = notice?.asString()
    LaunchedEffect(noticeText) {
        if (noticeText != null) {
            snackbar.showSnackbar(noticeText)
            viewModel.consumeNotice()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            InventoryHeader()
            val tabOptions = listOf(
                stringResource(R.string.inventory_products),
                stringResource(R.string.inventory_lots)
            )
            SegmentedControl(
                options = tabOptions,
                selected = tab,
                onSelect = { tab = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (val s = state) {
                    InventoryState.Loading -> InventoryLoading()
                    is InventoryState.Error -> InventoryError(
                        message = s.message,
                        onRetry = viewModel::refresh,
                        retryText = stringResource(R.string.inventory_retry)
                    )
                    is InventoryState.Ready -> {
                        if (tab == 0) {
                            ProductsTab(
                                products = s.products,
                                lots = s.lots,
                                alerts = s.alerts,
                                recentlyAdded = s.recentlyAddedProduct,
                                refreshing = s.refreshing,
                                onRefresh = viewModel::refresh,
                                onProductClick = onProductClick,
                                onAddClick = { showAddProduct = true }
                            )
                        } else {
                            LotsTab(
                                lots = s.lots,
                                products = s.products,
                                recentlyAdded = s.recentlyAddedLot,
                                refreshing = s.refreshing,
                                onRefresh = viewModel::refresh,
                                onLotClick = { type, id -> onLotClick(type, id) },
                                onAddClick = {
                                    if (s.products.isEmpty()) {
                                        noProductsMessage?.let { message ->
                                            scope.launch { snackbar.showSnackbar(message) }
                                        }
                                    } else {
                                        showCreateLot = true
                                    }
                                }
                            )
                        }
                    }
                }
                FloatingActionButton(
                    onClick = {
                        if (tab == 0) {
                            showAddProduct = true
                        } else {
                            val hasProducts = (state as? InventoryState.Ready)?.products?.isNotEmpty() == true
                            if (!hasProducts) {
                                scope.launch { snackbar.showSnackbar(noProductsMessage) }
                            } else {
                                showCreateLot = true
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
        }
    }

    if (showAddProduct) {
        ModalBottomSheet(
            onDismissRequest = { showAddProduct = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            SheetHeader(
                title = stringResource(R.string.inventory_form_add_title),
                onClose = { showAddProduct = false },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            ProductFormSheet(
                viewModel = viewModel,
                initial = null,
                onClose = { showAddProduct = false }
            )
        }
    }
    if (showCreateLot) {
        ModalBottomSheet(
            onDismissRequest = { showCreateLot = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            SheetHeader(
                title = stringResource(R.string.inventory_lot_create_title),
                onClose = { showCreateLot = false },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LotFormSheet(
                viewModel = viewModel,
                products = (state as? InventoryState.Ready)?.products.orEmpty(),
                preselected = null,
                locked = false,
                onClose = { showCreateLot = false }
            )
        }
    }
}

@Composable
private fun InventoryHeader(modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.inventory_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
