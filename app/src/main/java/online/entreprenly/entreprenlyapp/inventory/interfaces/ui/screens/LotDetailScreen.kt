package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.InventoryRules
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.LotStatus
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryAlertBanner
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryBannerKind
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryError
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryLoading
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.LotStatusPill
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.lotPill
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.viewmodels.InventoryState
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.viewmodels.InventoryViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.UiText as SharedUiText
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppCard

/**
 * Lot detail (US-06): status banner when it applies, plus status, product,
 * dates and current quantity. No initial quantity, no edit/delete actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotDetailScreen(
    viewModel: InventoryViewModel,
    type: ProductType,
    id: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background) { padding ->
        when (val s = state) {
            InventoryState.Loading -> InventoryLoading(Modifier.padding(padding))
            is InventoryState.Error -> InventoryError(
                message = s.message,
                onRetry = viewModel::refresh,
                retryText = stringResource(R.string.inventory_retry),
                modifier = Modifier.padding(padding)
            )
            is InventoryState.Ready -> {
                val lot = s.lot(type, id)
                if (lot == null) {
                    InventoryError(
                        message = SharedUiText.Res(R.string.inventory_not_available),
                        onRetry = viewModel::refresh,
                        retryText = stringResource(R.string.inventory_retry),
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    val product = s.products.firstOrNull { it.type == lot.type && it.id == lot.productId }
                    LotDetailContent(
                        lot = lot,
                        productName = product?.name ?: lot.label,
                        viewModel = viewModel,
                        refreshing = s.refreshing,
                        onBack = onBack,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LotDetailContent(
    lot: Lot,
    productName: String,
    viewModel: InventoryViewModel,
    refreshing: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = InventoryRules.todayUtc()
    val status = InventoryRules.lotStatus(lot, today)
    val pill = lotPill(lot, today)

    Column(modifier.fillMaxSize()) {
        LotDetailHeader(title = lot.label, subtitle = productName, onBack = onBack)
        PullToRefreshBox(
            isRefreshing = refreshing,
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
                when (status) {
                    LotStatus.EXPIRING_SOON -> {
                        val left = InventoryRules.daysLeft(lot.expiryDate!!, today).toInt()
                        InventoryAlertBanner(
                            title = stringResource(R.string.inventory_lot_expiring_title),
                            message = pluralStringResource(
                                R.plurals.inventory_lot_expiring_message,
                                left,
                                left
                            ),
                            kind = InventoryBannerKind.WARNING
                        )
                    }
                    LotStatus.EXPIRED -> InventoryAlertBanner(
                        title = stringResource(R.string.inventory_lot_expired_title),
                        message = stringResource(R.string.inventory_lot_expired_message),
                        kind = InventoryBannerKind.ERROR
                    )
                    LotStatus.ACTIVE, LotStatus.OUT_OF_STOCK -> Unit
                }
                AppCard {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LotDetailRow(
                            label = stringResource(R.string.inventory_lot_status),
                            value = { LotStatusPill(pill.text, pill.kind) }
                        )
                        LotDetailRow(
                            label = stringResource(R.string.inventory_lot_product_label),
                            value = {
                                Text(
                                    productName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                        LotDetailRow(
                            label = stringResource(R.string.inventory_lot_entry),
                            value = {
                                Text(
                                    InventoryRules.formatLotDate(lot.entryDate),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        )
                        if (lot.type == ProductType.UNIT && lot.expiryDate != null) {
                            LotDetailRow(
                                label = stringResource(R.string.inventory_lot_expiry_label),
                                value = {
                                    Text(
                                        InventoryRules.formatLotDate(lot.expiryDate),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            )
                        }
                        val amount = if (lot.type == ProductType.UNIT) {
                            pluralStringResource(
                                R.plurals.inventory_quantity_units,
                                lot.quantityUnits,
                                lot.quantityUnits
                            )
                        } else {
                            stringResource(
                                R.string.inventory_quantity_kg,
                                InventoryRules.formatKg(lot.quantityKg)
                            )
                        }
                        LotDetailRow(
                            label = stringResource(R.string.inventory_lot_quantity_label),
                            value = {
                                Text(
                                    amount,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    }
                }
                // Extension point: edit lot (out of scope).
                // Extension point: delete lot (out of scope).
            }
        }
    }
}

@Composable
private fun LotDetailRow(label: String, value: @Composable () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        value()
    }
}

@Composable
private fun LotDetailHeader(title: String, subtitle: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
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
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1
                )
            }
        }
    }
}
