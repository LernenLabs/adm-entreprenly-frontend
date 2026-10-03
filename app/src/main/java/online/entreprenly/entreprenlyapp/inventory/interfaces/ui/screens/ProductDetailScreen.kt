package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.InventoryRules
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryAlertBanner
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryBannerKind
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryError
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryLoading
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.LotStatusPill
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.ProductAvatar
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.SheetHeader
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.formatPrice
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.formatStock
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.lotPill
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.measureLabel
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.productLotRowDetail
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.remainingText
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.viewmodels.InventoryState
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.viewmodels.InventoryViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.UiText
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppCard
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.SecondaryPillButton

/**
 * Product detail (US-07, US-11): price, derived stock, alerts and the
 * associated lots ordered by urgency. Out-of-stock products offer + Create lot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: InventoryViewModel,
    type: ProductType,
    id: Long,
    onBack: () -> Unit,
    onLotClick: (String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var showEdit by rememberSaveable { mutableStateOf(false) }
    var showCreateLot by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val noticeText = (state as? InventoryState.Ready)?.notice?.asString()
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
        when (val s = state) {
            InventoryState.Loading -> InventoryLoading(Modifier.padding(padding))
            is InventoryState.Error -> InventoryError(
                message = s.message,
                onRetry = viewModel::refresh,
                retryText = stringResource(R.string.inventory_retry),
                modifier = Modifier.padding(padding)
            )
            is InventoryState.Ready -> {
                val product = s.product(type, id)
                if (product == null) {
                    InventoryError(
                        message = UiText.Res(R.string.inventory_not_available),
                        onRetry = viewModel::refresh,
                        retryText = stringResource(R.string.inventory_retry),
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    ProductDetailContent(
                        product = product,
                        state = s,
                        viewModel = viewModel,
                        onBack = onBack,
                        onLotClick = onLotClick,
                        onEdit = { showEdit = true },
                        onCreateLot = { showCreateLot = true },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }

    val ready = state as? InventoryState.Ready
    val current = ready?.product(type, id)
    if (showEdit && current != null && ready != null) {
        ModalBottomSheet(
            onDismissRequest = { showEdit = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            SheetHeader(
                title = stringResource(R.string.inventory_form_edit_title),
                onClose = { showEdit = false },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            ProductFormSheet(
                viewModel = viewModel,
                initial = current,
                currentStockText = formatStock(current, ready.stockOf(current)),
                onClose = { showEdit = false }
            )
        }
    }
    if (showCreateLot && current != null && ready != null) {
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
                products = ready.products,
                preselected = current,
                locked = true,
                onClose = { showCreateLot = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailContent(
    product: Product,
    state: InventoryState.Ready,
    viewModel: InventoryViewModel,
    onBack: () -> Unit,
    onLotClick: (String, Long) -> Unit,
    onEdit: () -> Unit,
    onCreateLot: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stock = state.stockOf(product)
    val outOfStock = stock <= 0.0
    val lowStock = !outOfStock && InventoryRules.isLowStock(state.alerts, product)
    val lots = InventoryRules.sortLots(state.lotsOf(product))

    Column(modifier.fillMaxSize()) {
        DetailHeader(name = product.name, subtitle = measureLabel(product.type), onBack = onBack)
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AppCard {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProductAvatar(product.name, size = 56.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow(
                                label = stringResource(R.string.inventory_detail_price),
                                value = formatPrice(product)
                            )
                            DetailRow(
                                label = stringResource(R.string.inventory_detail_stock),
                                value = formatStock(product, stock)
                            )
                        }
                    }
                }
                when {
                    outOfStock -> InventoryAlertBanner(
                        title = stringResource(R.string.inventory_detail_out_of_stock),
                        message = stringResource(
                            if (product.type == ProductType.UNIT) {
                                R.string.inventory_detail_out_of_stock_units
                            } else {
                                R.string.inventory_detail_out_of_stock_kg
                            }
                        ),
                        kind = InventoryBannerKind.ERROR
                    )
                    lowStock -> InventoryAlertBanner(
                        title = stringResource(R.string.inventory_detail_low_stock),
                        message = remainingText(product, stock),
                        kind = InventoryBannerKind.ERROR
                    )
                }
                Text(
                    stringResource(R.string.inventory_detail_lots),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (lots.isEmpty()) {
                    Text(
                        stringResource(R.string.inventory_detail_no_lots),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    lots.forEach { lot ->
                        val pill = lotPill(lot)
                        Surface(
                            onClick = { onLotClick(lot.type.value, lot.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        lot.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        productLotRowDetail(lot),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                LotStatusPill(pill.text, pill.kind)
                            }
                        }
                    }
                }
                PrimaryPillButton(
                    text = stringResource(R.string.inventory_detail_edit),
                    onClick = onEdit
                )
                if (outOfStock) {
                    SecondaryPillButton(
                        text = stringResource(R.string.inventory_detail_create_lot),
                        onClick = onCreateLot
                    )
                }
                // Extension point: delete product (out of scope).
            }
        }
    }
}

@Composable
private fun DetailHeader(name: String, subtitle: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.nav_back),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
